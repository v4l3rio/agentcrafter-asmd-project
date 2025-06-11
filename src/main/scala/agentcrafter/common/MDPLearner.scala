package agentcrafter.common

import scala.util.Random
import scala.collection.mutable
import scala.annotation.tailrec

/**
 * Markov Decision Process (MDP) based Q-learning implementation.
 * 
 * This class implements Q-learning using an MDP-based approach that separates
 * the learning algorithm from the action selection policy. It provides an
 * alternative to the traditional QLearner with potentially different convergence
 * characteristics and exploration strategies.
 * 
 * @param learningParameters Configuration parameters for the learning algorithm
 * @param goalState The target state that terminates episodes successfully
 * @param goalReward The reward given when reaching the goal state
 * @param gridWorld The grid world environment for the agent
 * @param initialState The starting state for episodes
 */
class MDPLearner(
  learningParameters: LearningParameters,
  goalState: State,
  goalReward: Double,
  gridWorld: GridWorld,
  initialState: State
) extends Learner:

  private given rng: Random = Random()
  private var ep = 0
  private val qTable = new QTable(learningParameters.optimistic)
  private val policy = new EpsilonGreedyPolicy(qTable)
  
  /**
   * Chooses an action for the given state using epsilon-greedy policy.
   * 
   * @param state The current state
   * @return The selected action and whether it was exploratory
   */
  def choose(state: State): (Action, Boolean) =
    val epsilon = learningParameters.calculateEpsilon(ep)
    policy.selectAction(state, epsilon)
  
  /**
   * Updates the Q-table based on the observed transition and reward.
   * 
   * @param state The previous state
   * @param action The action that was taken
   * @param reward The reward received for the transition
   * @param nextState The resulting state after taking the action
   */
  def update(state: State, action: Action, reward: Reward, nextState: State): Unit =
    val isGoal = nextState == goalState
    val finalReward = if isGoal then goalReward else reward
    qTable.update(state, action, finalReward, nextState, learningParameters)
  
  /**
   * Updates the Q-table with goal-specific logic.
   * 
   * @param state The previous state
   * @param action The action that was taken
   * @param envReward The environment reward received
   * @param nextState The resulting state after taking the action
   */
  def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit =
    update(state, action, envReward, nextState)
  
  /**
   * Runs a complete episode using the MDP-based approach.
   * 
   * @param maxSteps Maximum number of steps allowed in the episode
   * @return The outcome of the episode
   */
  def episode(maxSteps: Int = 200): EpisodeOutcome =
    ep += 1
    val episodeRunner = new EpisodeRunner(gridWorld, qTable, policy, goalState, goalReward)
    episodeRunner.run(initialState, maxSteps, learningParameters.calculateEpsilon(ep), learningParameters)
  
  /**
   * Gets the current epsilon value for exploration.
   * 
   * @return The current epsilon value
   */
  def eps: Double = learningParameters.calculateEpsilon(ep)
  
  /**
   * Increments the episode counter.
   */
  def incEp(): Unit = ep += 1
  
  /**
   * Creates a complete snapshot of the current Q-table.
   * 
   * @return An immutable map containing all state-action Q-values
   */
  def QTableSnapshot: Map[(State, Action), Double] = qTable.snapshot()
  
  /**
   * Gets the Q-value for a specific state-action pair.
   * 
   * @param state The state to query
   * @param action The action to query
   * @return The Q-value for the given state-action pair
   */
  def getQValue(state: State, action: Action): Double = qTable.getValue(state, action)

/**
 * Separated Q-Table management
 */
private class QTable(optimisticValue: Double):
  private val table: mutable.Map[(State, Action), Double] = mutable.Map()
  
  def getValue(state: State, action: Action): Double =
    table.getOrElse((state, action), optimisticValue)
  
  def update(state: State, action: Action, reward: Double, nextState: State, params: LearningParameters): Unit =
    val currentQ = getValue(state, action)
    val maxNextQ = Action.values.map(getValue(nextState, _)).maxOption.getOrElse(0.0)
    val newQ = currentQ + params.alpha * (reward + params.gamma * maxNextQ - currentQ)
    table((state, action)) = newQ
  
  def getBestAction(state: State): Action =
    Action.values.maxBy(getValue(state, _))
  
  def snapshot(): Map[(State, Action), Double] = table.toMap

/**
 * Separated policy management
 */
private class EpsilonGreedyPolicy(qTable: QTable)(using rng: Random):
  def selectAction(state: State, epsilon: Double): (Action, Boolean) =
    val isExploring = rng.nextDouble() < epsilon
    val action = if isExploring then
      Action.values.toVector(rng.nextInt(Action.values.length))
    else
      qTable.getBestAction(state)
    (action, isExploring)

/**
 * Separated episode execution
 */
private class EpisodeRunner(
  gridWorld: GridWorld,
  qTable: QTable,
  policy: EpsilonGreedyPolicy,
  goalState: State,
  goalReward: Double
):
  def run(startState: State, maxSteps: Int, epsilon: Double, learningParams: LearningParameters): EpisodeOutcome =
    @tailrec
    def loop(state: State, steps: Int, trajectory: List[(State, Action, Boolean, Array[Double])]): EpisodeOutcome =
      if steps >= maxSteps then (false, steps, trajectory.reverse)
      else
        val (action, isExploring) = policy.selectAction(state, epsilon)
        val stepResult = gridWorld.step(state, action)
        val isGoal = stepResult.state == goalState
        val reward = if isGoal then goalReward else stepResult.reward
        
        // Update Q-table
        qTable.update(state, action, reward, stepResult.state, learningParams)
        
        val qValues = Action.values.map(qTable.getValue(state, _))
        val newTrajectory = (state, action, isExploring, qValues) :: trajectory
        
        if isGoal then (true, steps + 1, newTrajectory.reverse)
        else loop(stepResult.state, steps + 1, newTrajectory)
    
    loop(startState, 0, Nil)

object MDPLearner:
  def apply(
    alpha: Double,
    gamma: Double,
    eps0: Double,
    epsMin: Double,
    warm: Int,
    optimistic: Double,
    simulationBuilder: agentcrafter.MARL.builders.SimulationBuilder,
    goalState: State,
    goalReward: Double,
    initialState: State = State(0, 0)
  ): MDPLearner =
    val learningParams = LearningParameters(alpha, gamma, eps0, epsMin, warm, optimistic)
    val gridWorld = GridWorld(
      rows = simulationBuilder.getRows,
      cols = simulationBuilder.getCols,
      walls = simulationBuilder.getWalls
    )
    new MDPLearner(learningParams, goalState, goalReward, gridWorld, initialState)