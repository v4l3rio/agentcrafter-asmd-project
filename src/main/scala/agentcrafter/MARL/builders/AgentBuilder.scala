package agentcrafter.marl.builders

import agentcrafter.marl.{AgentSpec, Trigger}
import agentcrafter.common.*

import scala.compiletime.uninitialized

/**
 * Builder for configuring individual agents in a Multi-Agent Reinforcement Learning simulation.
 *
 * This class provides a fluent API for setting up agent properties including starting position, goal state, learning
 * parameters, and goal-reaching behaviors. Each agent can be configured with different learning algorithms and
 * parameters.
 *
 * @param parent
 *   The parent SimulationBuilder that this agent belongs to
 */
class AgentBuilder(parent: SimulationBuilder):
  private var id: String = ""
  private var st: State = State(0, 0)
  private var gl: State = State(0, 0)
  private var learner: Learner = QLearner(
    learningParameters = LearningParameters(),
    goalState = gl,
    goalReward = 0.0,
    updateFunction = (s, _) => StepResult(s, 0.0),
    resetFunction = () => st
  )

  /**
   * Sets the name/identifier for this agent.
   *
   * @param n
   *   The unique name for this agent
   * @return
   *   This builder instance for method chaining
   */
  def name(n: String): AgentBuilder =
    id = n
    this

  /**
   * Sets the starting position for this agent.
   *
   * @param x
   *   Row coordinate of the starting position
   * @param y
   *   Column coordinate of the starting position
   * @return
   *   This builder instance for method chaining
   */
  def start(x: Int, y: Int): AgentBuilder =
    st = State(x, y); this

  /**
   * Sets the goal position for this agent.
   *
   * @param x
   *   Row coordinate of the goal position
   * @param y
   *   Column coordinate of the goal position
   * @return
   *   This builder instance for method chaining
   */
  def goal(x: Int, y: Int): AgentBuilder =
    gl = State(x, y); this

  /**
   * Configures the learning algorithm and parameters for this agent.
   *
   * @param alpha
   *   Learning rate (0.0 to 1.0) - controls how much new information overrides old information
   * @param gamma
   *   Discount factor (0.0 to 1.0) - determines the importance of future rewards
   * @param eps0
   *   Initial exploration rate for epsilon-greedy policy
   * @param epsMin
   *   Minimum exploration rate after warm-up period
   * @param warm
   *   Number of episodes for the warm-up period before epsilon decay begins
   * @param optimistic
   *   Initial optimistic value for unvisited state-action pairs
   * @return
   *   This builder instance for method chaining
   */
  def withLearner(
    alpha: Double = 0.1,
    gamma: Double = 0.99,
    eps0: Double = 0.9,
    epsMin: Double = 0.15,
    warm: Int = 10_000,
    optimistic: Double = 0.5
  ): AgentBuilder =
    val gridWorld = GridWorld(
      rows = parent.getRows,
      cols = parent.getCols,
      walls = parent.getWalls
    )
    QLearner(
      learningParameters = LearningParameters(alpha, gamma, eps0, epsMin, warm, optimistic),
      goalState = gl,
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => st
    )
    this

  /**
   * Builds the agent without a specific goal state.
   *
   * @return
   *   The parent SimulationBuilder for continued configuration
   */
  def noGoal(): SimulationBuilder = build()

  /**
   * Builds and adds this agent to the parent simulation.
   *
   * @return
   *   The parent SimulationBuilder for continued configuration
   */
  def build(): SimulationBuilder =
    val spec = AgentSpec(id, st, gl, learner)
    parent.addAgent(id, spec)
    parent

  private[marl] def currentId: String = id

  private[marl] def currentGoal: State = gl
