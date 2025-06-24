package agentcrafter.marl.dsl

import agentcrafter.common.Constants

import scala.annotation.targetName

/**
 * Configuration parameters for Q-learning algorithms in the DSL context.
 *
 * This class provides mutable configuration parameters that can be modified through the DSL syntax during agent
 * configuration.
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
case class LearnerConfig(
  var alpha: Double = Constants.DEFAULT_LEARNING_RATE,
  var gamma: Double = Constants.DEFAULT_DISCOUNT_FACTOR,
  var eps0: Double = Constants.DEFAULT_INITIAL_EXPLORATION_RATE,
  var epsMin: Double = Constants.DEFAULT_MINIMUM_EXPLORATION_RATE,
  var warm: Int = Constants.DEFAULT_WARMUP_EPISODES,
  var optimistic: Double = Constants.DEFAULT_OPTIMISTIC_INITIALIZATION
)

/**
 * DSL properties for configuring Q-learning algorithm parameters.
 *
 * This enumeration provides type-safe property setters for learner configuration through the DSL syntax. Each case
 * corresponds to a specific learning parameter and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum LearnerProperty[T]:
  /** Learning rate (0.0 to 1.0) - controls how much new information overrides old information */
  case Alpha extends LearnerProperty[Double]

  /** Discount factor (0.0 to 1.0) - determines the importance of future rewards */
  case Gamma extends LearnerProperty[Double]

  /** Initial exploration rate for epsilon-greedy policy */
  case Eps0 extends LearnerProperty[Double]

  /** Minimum exploration rate after warm-up period */
  case EpsMin extends LearnerProperty[Double]

  /** Number of episodes for the warm-up period before epsilon decay begins */
  case Warm extends LearnerProperty[Int]

  /** Initial optimistic value for unvisited state-action pairs */
  case Optimistic extends LearnerProperty[Double]

  @targetName("to")
  infix def >>(obj: T)(using config: LearnerConfig): Unit = this match
    case LearnerProperty.Alpha => config.alpha = obj.asInstanceOf[Double]
    case LearnerProperty.Gamma => config.gamma = obj.asInstanceOf[Double]
    case LearnerProperty.Eps0 => config.eps0 = obj.asInstanceOf[Double]
    case LearnerProperty.EpsMin => config.epsMin = obj.asInstanceOf[Double]
    case LearnerProperty.Warm => config.warm = obj.asInstanceOf[Int]
    case LearnerProperty.Optimistic => config.optimistic = obj.asInstanceOf[Double]
