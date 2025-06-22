package agentcrafter.common

import agentcrafter.common.Constants
import agentcrafter.common.ActionChoice
import scala.annotation.tailrec
import scala.util.Random

/**
 * Type alias to represent an agent's trajectory during an episode.
 * Contains state, action, exploration flag and Q-values for each step.
 */
type Trajectory = List[(State, Action, Boolean, Array[Double])]

/**
 * Type alias for reward values in the learning system.
 */
type Reward = Double

/**
 * Type alias for agent identifiers.
 */
type ID = String

/**
 * Type alias for episode results containing success state, number of steps and trajectory.
 */
type EpisodeOutcome = (Boolean, Int, Trajectory)

/**
 * Type alias for environment update functions that handle state transitions.
 */
type UpdateFunction = (State, Action) => StepResult

/**
 * Type alias for environment reset functions that return the initial state.
 */
type ResetFunction = () => State

/**
 * Factory object for creating QLearner instances.
 */
object QLearner:
  /**
   * Creates a new QLearner instance with the specified parameters.
   *
   * @param goalState The goal state that the agent must reach
   * @param goalReward The reward given when the goal state is reached
   * @param updateFunction Function that handles state transitions in the environment
   * @param resetFunction Function that resets the environment to the initial state
   * @param learningConfig Configuration parameters for the Q-learning algorithm
   * @return A new QLearner instance
   */
  def apply(
    goalState: State,
    goalReward: Reward,
    updateFunction: UpdateFunction,
    resetFunction: ResetFunction,
    learningConfig: LearningConfig = LearningConfig()
  ): QLearner =
    new QLearner(goalState, goalReward, updateFunction, resetFunction, learningConfig)

/**
 * Implementation of the Q-Learning algorithm for reinforcement learning.
 *
 * This class implements the Q-learning algorithm with epsilon-greedy exploration.
 * It manages learning through episodes, updating Q-values based on
 * received rewards and observed state transitions.
 *
 * @param goalState The goal state that terminates episodes successfully
 * @param goalReward The reward given when the goal state is reached
 * @param updateFunction Function that handles environment state transitions
 * @param resetFunction Function that resets the environment to initial conditions
 * @param config Configuration parameters for the learning algorithm
 */
class QLearner private (
  goalState: State,
  goalReward: Reward,
  updateFunction: UpdateFunction,
  resetFunction: ResetFunction,
  config: LearningConfig
) extends Learner:

  // Main Q-learner components
  private val qTable = new QTable(config)
  private val explorationStrategy = new ExplorationStrategy(config)
  
  private given rng: Random = Random()

  /**
   * Executes a complete learning episode.
   *
   * @param maxSteps Maximum number of steps per episode
   * @return Episode result (success, number of steps, trajectory)
   */
  override def episode(maxSteps: Int = Constants.DEFAULT_MAX_STEPS_PER_EPISODE): EpisodeOutcome =
    explorationStrategy.incrementEpisode()
    val initialState = resetFunction()
    
    runEpisodeLoop(initialState, maxSteps)

  /**
   * Main episode loop - handles step-by-step execution logic.
   *
   * This recursive method implements the main episode loop,
   * handling action selection, environment execution,
   * Q-value updates and trajectory recording.
   *
   * @param state The current state of the agent
   * @param maxSteps Maximum number of steps allowed in the episode
   * @param currentStep The current step count in the episode
   * @param trajectory The trajectory of the episode so far
   */
  @tailrec
  private def runEpisodeLoop(
    state: State, 
    maxSteps: Int, 
    currentStep: Int = 0, 
    trajectory: List[(State, Action, Boolean, Array[Double])] = Nil
  ): EpisodeOutcome =
    
    // Check termination conditions
    if state == goalState then
      (true, currentStep, trajectory.reverse)
    else if currentStep >= maxSteps then
      (false, currentStep, trajectory.reverse)
    else
      // Action selection using exploration strategy
      val actionChoice = explorationStrategy.chooseAction(state, qTable)
      val (action, isExploring) = actionChoice match
        case ActionChoice.Exploration(a) => (a, true)
        case ActionChoice.Exploitation(a) => (a, false)
      
      // Action execution in the environment
      val StepResult(nextState, environmentReward) = updateFunction(state, action)
      
      // Final reward calculation
      val finalReward = if nextState == goalState then goalReward else environmentReward
      
      // Q-table update
      qTable.updateValue(state, action, finalReward, nextState)
      
      // Step recording in trajectory
      val stepRecord = (state, action, isExploring, qTable.getStateValues(state))
      val newTrajectory = stepRecord :: trajectory
      
      // Continue with the next step
      runEpisodeLoop(nextState, maxSteps, currentStep + 1, newTrajectory)

  /**
   * Gets an immutable snapshot of the current Q-table.
   * 
   * @return Map containing all current Q-values
   */
  def QTableSnapshot: Map[(State, Action), Double] =
    qTable.createSnapshot()

  /**
   * Gets the Q-value for a specific state-action pair.
   * 
   * @param state The state
   * @param action The action
   * @return The current Q-value
   */
  def getQValue(state: State, action: Action): Double =
    qTable.getValue(state, action)

  /**
   * Chooses an action for the given state using the epsilon-greedy strategy.
   * 
   * @param state The current state
   * @return Tuple containing (action, is_exploration)
   */
  override def choose(state: State): (Action, Boolean) =
    explorationStrategy.chooseAction(state, qTable) match
      case ActionChoice.Exploration(action) => (action, true)
      case ActionChoice.Exploitation(action) => (action, false)

  /**
   * Gets the current exploration rate (epsilon).
   * 
   * @return The epsilon value for the current episode
   */
  override def eps: Double =
    explorationStrategy.getCurrentEpsilon

  /**
   * Updates the Q-value for a state-action pair.
   * 
   * @param state The current state
   * @param action The action taken
   * @param reward The reward received
   * @param nextState The next state
   */
  override def update(state: State, action: Action, reward: Reward, nextState: State): Unit =
    qTable.updateValue(state, action, reward, nextState)

  /**
   * Updates the Q-value using goal reward logic.
   * 
   * This mirrors the update step performed within [[episode]].
   * The provided environment reward is combined with the goal reward
   * if the next state matches the configured goal state.
   */
  override def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit =
    val finalReward = if nextState == goalState then goalReward else envReward
    qTable.updateValue(state, action, finalReward, nextState)

  /**
   * Increments the episode counter.
   */
  override def incEp(): Unit =
    explorationStrategy.incrementEpisode()

  /**
   * Gets the learning configuration used by this QLearner.
   * 
   * @return The LearningConfig instance
   */
  def getLearningConfig: LearningConfig = config

  /**
   * Gets the goal reward value.
   * 
   * @return The goal reward value
   */
  def getGoalReward: Reward = goalReward

  /**
   * Gets debug information about the current configuration.
   * 
   * @return String containing configuration information
   */
  def getDebugInfo: String =
    s"""QLearner Debug Info:
       |  Current Episode: ${explorationStrategy.getCurrentEpisode}
       |  Current Epsilon: ${explorationStrategy.getCurrentEpsilon}
       |  Q-Table Size: ${qTable.size}
       |  Goal State: $goalState
       |  Goal Reward: $goalReward""".stripMargin

  /**
   * Completely resets the learner to the initial state.
   */
  def reset(): Unit =
    qTable.reset()
    explorationStrategy.resetEpisodeCounter()
