package agentcrafter.common

import scala.util.Random

/**
 * Represents the type of choice made by the agent during action selection.
 */
enum ActionChoice:
  case Exploration(action: Action)
  case Exploitation(action: Action)

/**
 * Epsilon-greedy exploration strategy for the Q-learning algorithm.
 *
 * This class manages the logic of choosing between exploration (random actions) and exploitation (actions based on
 * learned Q-values).
 *
 * @param config
 *   Configuration of learning parameters
 */
class ExplorationStrategy(config: LearningConfig):

  private var currentEpisode: Int = Constants.INITIAL_EPISODE_COUNT

  /**
   * Chooses an action using the epsilon-greedy strategy.
   *
   * @param state
   *   The current state
   * @param qTable
   *   The Q-table to get action values
   * @return
   *   The type of choice made (exploration or exploitation)
   */
  def chooseAction(state: State, qTable: QTable): ActionChoice =
    val epsilon = config.calculateEpsilon(currentEpisode)

    if rng.nextDouble() < epsilon then
      // Exploration: random choice
      val randomAction = Action.values(rng.nextInt(Action.values.length))
      ActionChoice.Exploration(randomAction)
    else
      // Exploitation: greedy choice based on Q-values
      val bestAction = qTable.getBestAction(state)
      ActionChoice.Exploitation(bestAction)

  private given rng: Random = Random()

  /**
   * Gets the current epsilon value.
   *
   * @return
   *   The epsilon value for the current episode
   */
  def getCurrentEpsilon: Double =
    config.calculateEpsilon(currentEpisode)

  /**
   * Increments the episode counter. Should be called at the beginning of each new episode.
   */
  def incrementEpisode(): Unit =
    currentEpisode += Constants.SINGLE_STEP_INCREMENT

  /**
   * Gets the current episode number.
   *
   * @return
   *   The current episode number
   */
  def getCurrentEpisode: Int = currentEpisode

  /**
   * Manually sets the current episode number. Useful for testing or restoring a specific state.
   *
   * @param episode
   *   The new episode number
   */
  def setCurrentEpisode(episode: Int): Unit =
    currentEpisode = episode

  /**
   * Resets the episode counter.
   */
  def resetEpisodeCounter(): Unit =
    currentEpisode = Constants.INITIAL_EPISODE_COUNT
