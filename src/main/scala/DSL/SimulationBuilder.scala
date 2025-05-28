package DSL

import common.State

import scala.collection.mutable

/**
 * Builder for creating simulations using a fluent DSL interface
 */
class SimulationBuilder:
  private var rows = 5
  private var cols = 5
  private val walls = mutable.Set.empty[State]
  private val agents = mutable.Map.empty[String, AgentSpec]
  private val triggers = mutable.Buffer.empty[Trigger]
  private var nEpisodes = 10_000
  private var stepLimit  = 400
  private var stepDelay = 70 // ms between steps in GUI mode
  private var showAfter = 0

  private var gui = false

  def withGUI(flag: Boolean): SimulationBuilder = {
    gui = flag; this
  }

  /* grid size */
  def grid(r: Int, c: Int): SimulationBuilder = { rows = r; cols = c; this }

  /* add walls */
  def wall(r: Int, c: Int): SimulationBuilder = { walls += State(r, c); this }

  def wallsFromAscii(s: String): SimulationBuilder =
    val lines = s.stripMargin.split("\n").map(_.trim)
    lines.zipWithIndex.foreach { case (line, r) =>
      line.zipWithIndex.foreach { case (ch, c) =>
        if ch == '#' then walls += State(r, c)
      }
    }; this

  /* add agent */
  class AgentBuilder(id: String):
    private var st: State = State(0, 0)
    private var gl: Option[State] = None
    private var rew = 0.0
    private var learner: QLearner = new QLearner(id) // default learner for this agent

    def start(r: Int, c: Int): AgentBuilder = { st = State(r, c); this }
    def goal(r: Int, c: Int): AgentBuilder = { gl = Some(State(r, c)); this }
    def reward(v: Double): AgentBuilder = { rew = v; this }

    // Method to customize the Q-learner parameters for this specific agent
    def withLearner(alpha: Double = 0.1,
                    gamma: Double = 0.9,
                    eps0: Double = 0.9,
                    epsMin: Double = 0.15,
                    warm: Int = 10_000,
                    optimistic: Double = 0.0): AgentBuilder = {
      learner = new QLearner(id, alpha, gamma, eps0, epsMin, warm, optimistic)
      this
    }

    def noGoal(): SimulationBuilder = end()
    def end(): SimulationBuilder =
      val spec = AgentSpec(id, st, gl, rew, learner)
      agents += id -> spec
      for g <- gl if rew != 0.0 do
        triggers += Trigger(id, g, List(EndEpisode, Reward(rew)))
      SimulationBuilder.this

  def agent(id: String) = new AgentBuilder(id)

  /* add trigger */
  class TriggerBuilder(who: String, r: Int, c: Int):
    private val eff = mutable.Buffer.empty[Effect]
    def openWall(r: Int, c: Int): TriggerBuilder = { eff += OpenWall(State(r, c)); this }
    def endEpisode(): TriggerBuilder = { eff += EndEpisode; this }
    def give(bonus: Double): SimulationBuilder = { eff += Reward(bonus); finish() }
    private def finish() =
      triggers += Trigger(who, State(r, c), eff.toList)
      SimulationBuilder.this

  def on(who: String, r: Int, c: Int) = new TriggerBuilder(who, r, c)

  def steps(n: Int): SimulationBuilder = { stepLimit = n; this }

  def delay(ms: Int): SimulationBuilder = { stepDelay = ms; this }

  def showAfter(n: Int): SimulationBuilder = { showAfter = n; this }

  /* trainer options */
  def episodes(n: Int): SimulationBuilder = { nEpisodes = n; this }

  def play(): Unit =
    val spec = WorldSpec(rows, cols, walls.toSet, triggers.toList, agents.values.toList, nEpisodes, stepLimit, stepDelay, showAfter)
    new Runner(spec, gui).run()