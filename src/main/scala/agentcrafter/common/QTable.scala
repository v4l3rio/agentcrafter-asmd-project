package agentcrafter.common

import scala.collection.mutable
import scala.util.Random

/**
 * Implementation of the Q-table for the Q-learning algorithm.
 *
 * This class manages the storage and updating of Q-values for state-action pairs. It uses a mutable map for efficiency
 * during learning.
 *
 * @param config
 *   Configuration of learning parameters
 */
class QTable(config: LearningConfig):

  // Map that stores Q-values for each (state, action) pair
  private val table: mutable.Map[(State, Action), Double] =
    mutable.Map().withDefaultValue(config.optimistic)

  /**
   * Updates a Q-value using the Q-learning update rule.
   *
   * @param state
   *   The current state
   * @param action
   *   The action taken
   * @param reward
   *   The immediate reward received
   * @param nextState
   *   The resulting state after taking the action
   */
  def updateValue(state: State, action: Action, reward: Reward, nextState: State): Unit =
    val currentValue = getValue(state, action)
    val maxNextValue = Action.values.map(a => getValue(nextState, a)).max

    val newValue = (1.0 - config.alpha) * currentValue +
      config.alpha * (reward + config.gamma * maxNextValue)

    table((state, action)) = newValue

  /**
   * Gets all Q-values for all actions from a specific state.
   *
   * @param state
   *   The state to query
   * @return
   *   Array of Q-values for all possible actions
   */
  def getStateValues(state: State): Array[Double] =
    Action.values.map(action => getValue(state, action))

  /**
   * Gets the Q-value for a specific state-action pair.
   *
   * @param state
   *   The state
   * @param action
   *   The action
   * @return
   *   The current Q-value
   */
  def getValue(state: State, action: Action): Double =
    table.getOrElse((state, action), config.optimistic)

  /**
   * Selects the action with the highest Q-value for a given state (greedy policy). In case of a tie, randomly chooses
   * among the best actions.
   *
   * @param state
   *   The state for which to select the action
   * @param rng
   *   Random number generator
   * @return
   *   The action with the highest Q-value
   */
  def getBestAction(state: State)(using rng: Random): Action =
    val actionValues = Action.values.map(action => action -> getValue(state, action))
    val maxValue = actionValues.map(_._2).max
    val bestActions = actionValues.filter(_._2 == maxValue).map(_._1)

    // Random choice in case of tie
    bestActions(rng.nextInt(bestActions.length))

  /**
   * Creates an immutable snapshot of the current Q-table.
   *
   * @return
   *   An immutable map containing all current Q-values
   */
  def createSnapshot(): Map[(State, Action), Double] =
    table.toMap

  /**
   * Gets the number of visited state-action pairs.
   *
   * @return
   *   The number of state-action pairs in the table
   */
  def size: Int = table.size

  /**
   * Checks if a state-action pair has been visited.
   *
   * @param state
   *   The state
   * @param action
   *   The action
   * @return
   *   true if the pair has been visited
   */
  def hasBeenVisited(state: State, action: Action): Boolean =
    table.contains((state, action))

  /**
   * Resets the Q-table by removing all learned values.
   */
  def reset(): Unit =
    table.clear()
