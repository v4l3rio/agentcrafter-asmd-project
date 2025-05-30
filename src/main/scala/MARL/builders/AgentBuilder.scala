package MARL.builders

import common.State
import MARL.{AgentSpec, Trigger, EndEpisode, Reward}
import common.QLearner

class AgentBuilder(parent: SimulationBuilder):
  private var id: String = ""
  private var st: State = State(0, 0)
  private var gl: Option[State] = None
  private var rew = 0.0
  private var learner: QLearner = new QLearner(id)

  def name (n: String): AgentBuilder = {
    id = n; learner.id = n; this
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
    learner = new QLearner(id, alpha, gamma, eps0, epsMin, warm, optimistic)
    this
  }

  def noGoal(): SimulationBuilder = end()
  def end(): SimulationBuilder =
    val spec = AgentSpec(id, st, gl, rew, learner)
    parent.addAgent(id, spec)
    for g <- gl if rew != 0.0 do
      parent.addTrigger(Trigger(id, g, List(EndEpisode, Reward(rew))))
    parent