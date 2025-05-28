package DSL

import DSL.model.*
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

  private var gui = false

  def withGUI(flag: Boolean): SimulationBuilder = {
    gui = flag; this
  }

  /* grid size */
  def grid(r: Int, c: Int) = { rows = r; cols = c; this }
  
  /* add walls */
  def wall(r: Int, c: Int) = { walls += State(r, c); this }
  
  def wallsFromAscii(s: String) =
    val lines = s.stripMargin.split("\n").map(_.trim)
    lines.zipWithIndex.foreach { case (line, r) =>
      line.zipWithIndex.foreach { case (ch, c) =>
        if ch == '#' then walls += State(r, c)
      }
    }; this

  /* add agent */
  class AgentBuilder(id: String):
    private var st: State = State(0, 0); private var gl: State = State(0, 0); private var rew = 0.0
    def start(r: Int, c: Int) = { st = State(r, c); this }
    def goal(r: Int, c: Int) = { gl = State(r, c); this }
    def reward(v: Double) = { rew = v; this }
    def end(): SimulationBuilder =
      agents += id -> AgentSpec(id, st, gl, rew); SimulationBuilder.this
  
  def agent(id: String) = new AgentBuilder(id)

  /* add trigger */
  class TriggerBuilder(who: String, r: Int, c: Int):
    private val eff = mutable.Buffer.empty[Effect]
    def openWall(r: Int, c: Int) = { eff += OpenWall(State(r, c)); this }
    def endEpisode() = { eff += EndEpisode; this }
    def give(bonus: Double) = { eff += Reward(bonus); finish() }
    private def finish() =
      triggers += Trigger(who, State(r, c), eff.toList)
      SimulationBuilder.this
  
  def on(who: String, r: Int, c: Int) = new TriggerBuilder(who, r, c)

  /* trainer options */
  def episodes(n: Int) = { nEpisodes = n; this }

  /* build & play --------------------------------------------------- */
  def play(): Unit =
    val spec = WorldSpec(rows, cols, walls.toSet, triggers.toList, agents.values.toList, nEpisodes)
    new Runner(spec, gui).run()