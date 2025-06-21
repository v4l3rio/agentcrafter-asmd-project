package agentcrafter.common

import agentcrafter.common.QLearner.*
import agentcrafter.common.Constants

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random



/**
 * Configuration parameters for Q-learning algorithms.
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
 */
case class LearningParameters(
    alpha: Double = Constants.DEFAULT_LEARNING_RATE,
    gamma: Double = Constants.DEFAULT_DISCOUNT_FACTOR,
    eps0: Double = Constants.DEFAULT_INITIAL_EXPLORATION_RATE,
    epsMin: Double = Constants.DEFAULT_MINIMUM_EXPLORATION_RATE,
    warm: Int = Constants.DEFAULT_WARMUP_EPISODES,
    optimistic: Double = Constants.DEFAULT_OPTIMISTIC_INITIALIZATION
):
  /**
   * Calculates the current epsilon value based on the episode number.
   *
   * @param ep
   *   Current episode number
   * @return
   *   The epsilon value for the current episode
   */
  def calculateEpsilon(ep: Int): Double =
    if ep < warm then eps0 else math.max(epsMin, eps0 - (eps0 - epsMin) * (ep - warm) / warm)

/**
 * Type alias for representing an agent's trajectory during an episode.
 * Contains state, action, exploration flag, and Q-values for each step.
 */
private type Trajectory = List[(State, Action, Boolean, Array[Double])]

/**
 * Type alias for reward values in the learning system.
 */
type Reward = Double

/**
 * Type alias for agent identifiers.
 */
type ID = String

/**
 * Type alias for episode outcomes containing success status, step count, and trajectory.
 * 
 * @return A tuple of (success: Boolean, steps: Int, trajectory: Trajectory)
 */
type EpisodeOutcome = (Boolean, Int, Trajectory)

/**
 * Type alias for environment update functions that handle state transitions.
 * 
 * @param state The current state
 * @param action The action to take
 * @return StepResult containing the next state and reward
 */
type UpdateFunction = (State, Action) => StepResult

/**
 * Type alias for environment reset functions that return the initial state.
 * 
 * @return The initial state for a new episode
 */
type ResetFunction = () => State

extension [T](actions: Array[T]) private def draw(using rng: Random) = actions(rng.nextInt(actions.length))

/**
 * Factory object for creating QLearner instances.
 */
object QLearner:
  /**
   * Creates a new QLearner instance with the specified parameters.
   *
   * @param goalState
   *   The target state that the agent should reach
   * @param goalReward
   *   The reward given when the goal state is reached
   * @param updateFunction
   *   Function that handles state transitions in the environment
   * @param resetFunction
   *   Function that resets the environment to an initial state
   * @param learningParameters
   *   Configuration parameters for the Q-learning algorithm
   * @return
   *   A new QLearner instance
   */
  def apply(
    goalState: State,
    goalReward: Reward,
    updateFunction: UpdateFunction,
    resetFunction: ResetFunction,
    learningParameters: LearningParameters = LearningParameters()
  ): QLearner =
    new QLearner(learningParameters, goalState, goalReward, updateFunction, resetFunction)

/**
 * Q-Learning implementation for reinforcement learning.
 *
 * This class implements the Q-learning algorithm with epsilon-greedy exploration. It maintains a Q-table that maps
 * state-action pairs to expected future rewards.
 *
 * @param learningParameters
 *   Configuration parameters for the learning algorithm
 * @param goalState
 *   The target state that terminates episodes successfully
 * @param goalReward
 *   The reward given when reaching the goal state
 * @param updateFunction
 *   Function that handles environment state transitions
 * @param resetFunction
 *   Function that resets the environment to initial conditions
 */
class QLearner private (
  learningParameters: LearningParameters,
  goalState: State,
  goalReward: Reward,
  updateFunction: UpdateFunction,
  resetFunction: ResetFunction
) extends Learner:

  private val A = Action.values
  private val Q: QTable = new QTable()
  private var ep = Constants.INITIAL_EPISODE_COUNT

  override def episode(maxSteps: Int = Constants.DEFAULT_MAX_STEPS_PER_EPISODE): EpisodeOutcome =
    ep += Constants.SINGLE_STEP_INCREMENT

    @tailrec
    def loop(state: State, steps: Int, acc: List[(State, Action, Boolean, Array[Reward])]): EpisodeOutcome =
      if state == goalState then (true, steps, acc.reverse)
      else if steps >= maxSteps then (false, steps, acc.reverse)
      else
        val (action, isExploring) = chooseInternal(state) match
          case Choice.Exploring(action) => (action, true)
          case Choice.Exploiting(state) => (Q.greedy(state), false)

        val StepResult(nextState, nextStateReward) = updateFunction(state, action)
        val isGoal = nextState == goalState
        val reward = if isGoal then goalReward else nextStateReward

        Q.update(state, action, reward, nextState)

        val newAcc = (state, action, isExploring, Q.qValues(state)) :: acc

        if isGoal then (true, steps + Constants.SINGLE_STEP_INCREMENT, newAcc.reverse)
        else loop(nextState, steps + Constants.SINGLE_STEP_INCREMENT, newAcc)

    val initialState = resetFunction()
    loop(initialState, 0, Nil)

  def QTableSnapshot: Map[(State, Action), Double] =
    Q.tableSnapshot()

  def getQValue(state: State, action: Action): Double =
    Q.tableSnapshot().getOrElse((state, action), learningParameters.optimistic)

  override def choose(state: State): (Action, Boolean) =
    chooseInternal(state) match
      case Choice.Exploring(action) => (action, true)
      case Choice.Exploiting(_) => (Q.greedy(state), false)

  /**
   * Internal method for choosing between exploration and exploitation.
   *
   * @param p
   *   The current state
   * @return
   *   A Choice indicating whether exploration or exploitation was used
   */
  private def chooseInternal(p: State): Choice =
    rng.nextDouble() match
      case d if d < eps => Choice.Exploring(A.draw)
      case _ => Choice.Exploiting(p)

  private given rng: Random = Random()

  /**
   * Gets the current exploration rate (epsilon).
   *
   * @return
   *   The current epsilon value based on the episode number
   */
  override def eps: Double = learningParameters.calculateEpsilon(ep)

  override def update(state: State, action: Action, reward: Reward, nextState: State): Unit =
    Q.update(state, action, reward, nextState)

  /**
   * Update Q-value using the learner's goal reward logic.
   *
   * This mirrors the update step performed inside [[episode]]. The provided environment reward is combined with the
   * goal reward if the next state matches the configured goal state.
   */
  override def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit =
    val isGoal = nextState == goalState
    val reward = if isGoal then goalReward else envReward
    Q.update(state, action, reward, nextState)

  override def incEp(): Unit =
    ep += Constants.SINGLE_STEP_INCREMENT

  /**
   * Represents the type of action selection made by the agent.
   */
  enum Choice:

    case Exploring(action: Action)

    case Exploiting(state: State)

  /**
   * Internal Q-table implementation that stores and manages Q-values.
   */
  private class QTable:

    private val table: mutable.Map[(State, Action), Double] =
      mutable.Map().withDefaultValue(learningParameters.optimistic)

    /**
     * Creates an immutable snapshot of the current Q-table.
     *
     * @return
     *   A map containing all current Q-values
     */
    def tableSnapshot(): Map[(State, Action), Double] =
      table.toMap

    /**
     * Gets Q-values for all actions from a given state.
     *
     * @param state
     *   The state to query
     * @return
     *   Array of Q-values for all possible actions
     */
    def qValues(state: State): Array[Reward] = A.map(a => table(state -> a))

    /**
     * Updates a Q-value using the Q-learning update rule.
     *
     * @param state
     *   The current state
     * @param action
     *   The action taken
     * @param reward
     *   The immediate reward received
     * @param newState
     *   The resulting state after taking the action
     */
    def update(state: State, action: Action, reward: Reward, newState: State): Unit =
      val bestNext = A.map(a2 => table(newState, a2)).max
      val newValue = (Constants.Q_LEARNING_ALPHA_COMPLEMENT - learningParameters.alpha) *
        table(state, action) + learningParameters.alpha *
        (reward + learningParameters.gamma * bestNext)
      table(state -> action) = newValue

    /**
     * Selects the greedy action (highest Q-value) for a given state.
     *
     * @param p
     *   The state to select an action for
     * @return
     *   The action with the highest Q-value (random tie-breaking)
     */
    def greedy(p: State): Action =
      val actionValues = A.map(a => a -> table(p -> a))
      val maxValue = actionValues.map(_._2).max
      actionValues.collect { case (a, v) if v == maxValue => a }.draw
