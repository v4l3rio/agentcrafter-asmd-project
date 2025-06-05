package agentcrafter.MARL.builders

import agentcrafter.MARL.{AgentSpec, EndEpisode, Reward, Trigger}
import agentcrafter.common.{GridWorld, QLearner, State}

import scala.compiletime.uninitialized

class AgentBuilder(parent: SimulationBuilder):
  private var id: String = ""
  private var st: State = State(0, 0)
  private var gl: Option[State] = None
  private var rew = 0.0
  private var learner: QLearner = uninitialized // TODO: CHECK

  def name (n: String): AgentBuilder = {
    id = n; this
  }

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
    // Create a GridWorld environment for this agent
    // We'll use the agent's start and goal positions to configure the environment
    val gridWorld = GridWorld(
      rows = parent.getRows,
      cols = parent.getCols,
      start = st,
      goal = gl.getOrElse(State(0, 0)),
      walls = parent.getWalls
    )
    learner = QLearner(alpha, gamma, eps0, epsMin, warm, optimistic, gridWorld)
    this
  }

  def noGoal(): SimulationBuilder = end()
  def end(): SimulationBuilder =
    val spec = AgentSpec(id, st, gl, rew, learner)
    parent.addAgent(id, spec)
    for g <- gl if rew != 0.0 do
      parent.addTrigger(Trigger(id, g, List(EndEpisode, Reward(rew))))
    parent