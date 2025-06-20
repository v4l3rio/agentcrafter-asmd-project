package agentcrafter.common

/**
 * Interface for reinforcement learning implementations.
 *
 * This trait defines the core contract for all learning algorithms in the AgentCrafter framework. Implementations
 * should provide Q-learning or similar value-based learning capabilities.
 */
trait Learner:
  /**
   * Chooses an action for the given state using the current policy.
   *
   * @param state
   *   The current state of the agent
   * @return
   *   A tuple containing the chosen action and a boolean indicating if the action was exploratory
   */
  def choose(state: State): (Action, Boolean)

  /**
   * Updates the learning algorithm with the result of taking an action.
   *
   * @param state
   *   The state from which the action was taken
   * @param action
   *   The action that was taken
   * @param reward
   *   The reward received for taking the action
   * @param nextState
   *   The resulting state after taking the action
   */
  def update(state: State, action: Action, reward: Reward, nextState: State): Unit

  /**
   * Updates the learning algorithm when a goal state is reached.
   *
   * @param state
   *   The state from which the action was taken
   * @param action
   *   The action that was taken
   * @param envReward
   *   The environment reward received
   * @param nextState
   *   The resulting state (typically the goal state)
   */
  def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit

  /**
   * Runs a complete episode of learning.
   *
   * @param maxSteps
   *   Maximum number of steps allowed in the episode
   * @return
   *   An EpisodeOutcome containing success status, step count, and trajectory
   */
  def episode(maxSteps: Int = QLearnerConstants.DEFAULT_MAX_STEPS): EpisodeOutcome

  /**
   * Gets the current exploration rate (epsilon) for epsilon-greedy policies.
   *
   * @return
   *   The current epsilon value
   */
  def eps: Double

  /**
   * Increments the episode counter, typically affecting exploration rate.
   */
  def incEp(): Unit

  /**
   * Provides a snapshot of the current Q-table for inspection or serialization.
   *
   * @return
   *   A map from (State, Action) pairs to their Q-values
   */
  def QTableSnapshot: Map[(State, Action), Double]

  /**
   * Gets the Q-value for a specific state-action pair.
   *
   * @param state
   *   The state to query
   * @param action
   *   The action to query
   * @return
   *   The Q-value for the given state-action pair
   */
  def getQValue(state: State, action: Action): Double
