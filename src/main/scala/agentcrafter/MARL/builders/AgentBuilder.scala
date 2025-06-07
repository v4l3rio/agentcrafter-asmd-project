package agentcrafter.MARL.builders

import agentcrafter.MARL.{AgentSpec, Trigger}
import agentcrafter.common.{GridWorld, LearningParameters, QLearner, State, StepResult}

import scala.compiletime.uninitialized

class AgentBuilder(parent: SimulationBuilder):
  private var id: String = ""
  private var st: State = State(0, 0)
  private var gl: State = State(0, 0)
  private var learner: QLearner = QLearner(
    learningParameters = LearningParameters(),
    goalState = gl,
    goalReward = 0.0,
    updateFunction = (s, _) => StepResult(s, 0.0),
    resetFunction = () => st
  )

  def name (n: String): AgentBuilder = {
    id = n; this
  }

  def start(r: Int, c: Int): AgentBuilder = { st = State(r, c); this }
  def goal(r: Int, c: Int): AgentBuilder = { gl = State(r, c); this }

  /** Current agent id (for internal DSL usage) */
  private[MARL] def currentId: String = id

  /** Current goal position (for internal DSL usage) */
  private[MARL] def currentGoal: State = gl

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
      walls = parent.getWalls
    )
    learner = QLearner(learningParameters = LearningParameters(alpha, gamma, eps0, epsMin, warm, optimistic),
                            goalState = gl,
                            goalReward = 0.0,
                            updateFunction = gridWorld.step,
                            resetFunction = () => st)
    this
  }

  def noGoal(): SimulationBuilder = build()
  def build(): SimulationBuilder =
    val spec = AgentSpec(id, st, gl, learner)
    parent.addAgent(id, spec)
    parent