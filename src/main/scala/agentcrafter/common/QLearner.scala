package agentcrafter.common

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

/**
 * Configuration parameters for Q-learning algorithms.
 *
 * @param alpha      Learning rate (0.0 to 1.0) - controls how much new information overrides old information
 * @param gamma      Discount factor (0.0 to 1.0) - determines the importance of future rewards
 * @param eps0       Initial exploration rate for epsilon-greedy policy
 * @param epsMin     Minimum exploration rate after warm-up period
 * @param warm       Number of episodes for the warm-up period before epsilon decay begins
 * @param optimistic Initial optimistic value for unvisited state-action pairs
 */
case class LearningParameters(
                               alpha: Double = 0.1,
                               gamma: Double = 0.99,
                               eps0: Double = 0.9,
                               epsMin: Double = 0.15,
                               warm: Int = 10_000,
                               optimistic: Double = 0.5
                             ):
  /**
   * Calculates the current epsilon value based on the episode number.
   *
   * @param ep Current episode number
   * @return The epsilon value for the current episode
   */
  def calculateEpsilon(ep: Int): Double =
    if ep < warm then eps0 else math.max(epsMin, eps0 - (eps0 - epsMin) * (ep - warm) / warm)


private type Trajectory = List[(State, Action, Boolean, Array[Double])]
type Reward = Double
type ID = String
type EpisodeOutcome = (Boolean, Int, Trajectory)
type UpdateFunction = (State, Action) => StepResult
type ResetFunction = () => State


extension [T](actions: Array[T])
  private def draw(using rng: Random) = actions(rng.nextInt(actions.length))

/**
 * Factory object for creating QLearner instances.
 */
object QLearner:
  /**
   * Creates a new QLearner instance with the specified parameters.
   *
   * @param goalState          The target state that the agent should reach
   * @param goalReward         The reward given when the goal state is reached
   * @param updateFunction     Function that handles state transitions in the environment
   * @param resetFunction      Function that resets the environment to an initial state
   * @param learningParameters Configuration parameters for the Q-learning algorithm
   * @return A new QLearner instance
   */
  def apply(
             goalState: State,
             goalReward: Reward,
             updateFunction: UpdateFunction,
             resetFunction: ResetFunction,
             learningParameters: LearningParameters = LearningParameters(),
           ): QLearner =
    new QLearner(learningParameters, goalState, goalReward, updateFunction, resetFunction)

/**
 * Q-Learning implementation for reinforcement learning.
 *
 * This class implements the Q-learning algorithm with epsilon-greedy exploration.
 * It maintains a Q-table that maps state-action pairs to expected future rewards.
 *
 * @param learningParameters Configuration parameters for the learning algorithm
 * @param goalState          The target state that terminates episodes successfully
 * @param goalReward         The reward given when reaching the goal state
 * @param updateFunction     Function that handles environment state transitions
 * @param resetFunction      Function that resets the environment to initial conditions
 */
class QLearner private(
                        learningParameters: LearningParameters,
                        goalState: State,
                        goalReward: Reward,
                        updateFunction: UpdateFunction,
                        resetFunction: ResetFunction
                      ) extends Learner:
  /** Random number generator for stochastic action selection */
  private given rng: Random = Random()

  /**
   * Internal Q-table implementation that stores and manages Q-values.
   */
  private class QTable:
    /** The underlying table storing Q-values with optimistic initialization */
    private val table: mutable.Map[(State, Action), Double] = mutable.Map().withDefaultValue(learningParameters.optimistic)

    /**
     * Creates an immutable snapshot of the current Q-table.
     *
     * @return A map containing all current Q-values
     */
    def tableSnapshot(): Map[(State, Action), Double] =
      table.toMap

    /**
     * Gets Q-values for all actions from a given state.
     *
     * @param state The state to query
     * @return Array of Q-values for all possible actions
     */
    def qValues(state: State): Array[Reward] = A.map(a => table(state -> a))

    /**
     * Updates a Q-value using the Q-learning update rule.
     *
     * @param state    The current state
     * @param action   The action taken
     * @param reward   The immediate reward received
     * @param newState The resulting state after taking the action
     */
    def update(state: State, action: Action, reward: Reward, newState: State): Unit =
      val bestNext = A.map(a2 => table(newState, a2)).max
      val newValue = (1 - learningParameters.alpha) *
        table(state, action) + learningParameters.alpha *
        (reward + learningParameters.gamma * bestNext)
      table(state -> action) = newValue

    /**
     * Selects the greedy action (highest Q-value) for a given state.
     *
     * @param p The state to select an action for
     * @return The action with the highest Q-value (random tie-breaking)
     */
    def greedy(p: State): Action =
      val actionValues = A.map(a => a -> table(p -> a))
      val maxValue = actionValues.map(_._2).max
      actionValues.collect { case (a, v) if v == maxValue => a }.draw


  /**
   * Represents the type of action selection made by the agent.
   */
  enum Choice:
    /** Action was chosen through exploration (random selection) */
    case Exploring(action: Action)
    /** Action was chosen through exploitation (greedy selection) */
    case Exploiting(state: State)


  /** Array of all possible actions */
  private val A = Action.values

  /** The Q-table storing learned values */
  private val Q: QTable = new QTable()

  /** Current episode number */
  private var ep = 0

  /**
   * Gets the current exploration rate (epsilon).
   *
   * @return The current epsilon value based on the episode number
   */
  def eps: Double = learningParameters.calculateEpsilon(ep)

  /**
   * Internal method for choosing between exploration and exploitation.
   *
   * @param p The current state
   * @return A Choice indicating whether exploration or exploitation was used
   */
  private def chooseInternal(p: State): Choice =
    rng.nextDouble() match
      case d if d < eps => Choice.Exploring(A.draw)
      case _ => Choice.Exploiting(p)

  def episode(maxSteps: Int = 200): EpisodeOutcome =
    ep += 1

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
        val reward = if isGoal then goalReward else nextStateReward // Step penalty

        Q.update(state, action, reward, nextState)

        val newAcc = (state, action, isExploring, Q.qValues(state)) :: acc

        if isGoal then (true, steps + 1, newAcc.reverse)
        else loop(nextState, steps + 1, newAcc)

    val initialState = resetFunction()
    loop(initialState, 0, Nil)


  def QTableSnapshot: Map[(State, Action), Double] =
    Q.tableSnapshot()

  def getQValue(state: State, action: Action): Double =
    Q.tableSnapshot().getOrElse((state, action), learningParameters.optimistic)

  // MARL compatibility methods
  def choose(state: State): (Action, Boolean) =
    chooseInternal(state) match
      case Choice.Exploring(action) => (action, true)
      case Choice.Exploiting(_) => (Q.greedy(state), false)

  def update(state: State, action: Action, reward: Reward, nextState: State): Unit =
    Q.update(state, action, reward, nextState)

  /**
   * Update Q-value using the learner's goal reward logic.
   *
   * This mirrors the update step performed inside [[episode]].
   * The provided environment reward is combined with the goal reward
   * if the next state matches the configured goal state.
   */
  def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit =
    val isGoal = nextState == goalState
    val reward = if isGoal then goalReward else envReward
    Q.update(state, action, reward, nextState)

  def incEp(): Unit =
    ep += 1
