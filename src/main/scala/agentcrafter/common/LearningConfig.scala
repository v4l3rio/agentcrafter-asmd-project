package agentcrafter.common

import agentcrafter.common.Constants

/**
 * Configuration of parameters for the Q-learning algorithm.
 *
 * This class contains all the necessary parameters to configure the Q-learning algorithm with epsilon-greedy strategy.
 *
 * @param alpha
 *   Learning rate (0.0 to 1.0) - controls how much new information overwrites old information
 * @param gamma
 *   Discount factor (0.0 to 1.0) - determines the importance of future rewards
 * @param eps0
 *   Initial exploration rate for the epsilon-greedy policy
 * @param epsMin
 *   Minimum exploration rate after the warm-up period
 * @param warm
 *   Number of episodes for the warm-up period before epsilon decay begins
 * @param optimistic
 *   Initial optimistic value for unvisited state-action pairs
 */
case class LearningConfig(
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
   * @param episodeNumber
   *   Current episode number
   * @return
   *   The epsilon value for the current episode
   */
  def calculateEpsilon(episodeNumber: Int): Double =
    if episodeNumber < warm then
      eps0
    else
      math.max(epsMin, eps0 - (eps0 - epsMin) * (episodeNumber - warm) / warm)

  /**
   * Verifies if the parameters are valid.
   *
   * @return
   *   true if all parameters are in the correct ranges
   */
  def isValid: Boolean =
    alpha >= 0.0 && alpha <= 1.0 &&
    gamma >= 0.0 && gamma <= 1.0 &&
    eps0 >= 0.0 && eps0 <= 1.0 &&
    epsMin >= 0.0 && epsMin <= 1.0 &&
    warm >= 0 &&
    optimistic >= 0.0
