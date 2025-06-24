package agentcrafter.marl

import agentcrafter.common.*

/**
 * Manages simulation-wide state and statistics.
 *
 * This class follows the Single Responsibility Principle by focusing solely on:
 *   - Tracking simulation progress (episodes, rewards)
 *   - Managing simulation statistics
 *   - Providing state snapshots for visualization
 */
class SimulationState:

  private var totalReward = Constants.INITIAL_REWARD_VALUE
  private var currentEpisode = Constants.INITIAL_EPISODE_COUNT
  private var episodeReward = 0.0

  /**
   * Gets the current episode number.
   */
  def getCurrentEpisode: Int = currentEpisode

  /**
   * Sets the current episode number.
   */
  def setCurrentEpisode(episode: Int): Unit =
    currentEpisode = episode

  /**
   * Gets the total accumulated reward across all episodes.
   */
  def getTotalReward: Double = totalReward

  /**
   * Gets the current episode reward.
   */
  def getEpisodeReward: Double = episodeReward

  /**
   * Adds reward to the current episode.
   */
  def addEpisodeReward(reward: Double): Unit =
    episodeReward += reward

  /**
   * Completes the current episode by adding its reward to the total.
   */
  def completeEpisode(): Unit =
    totalReward += episodeReward

  /**
   * Resets the episode reward for a new episode.
   */
  def resetEpisodeReward(): Unit =
    episodeReward = 0.0

  /**
   * Creates an EpisodeState for visualization.
   */
  def createEpisodeState(positions: Map[String, State], openedWalls: Set[State], done: Boolean): EpisodeState =
    EpisodeState(positions, openedWalls, done, episodeReward)
