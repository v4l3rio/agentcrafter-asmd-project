package agentcrafter.marl.dsl

import agentcrafter.marl.builders.TriggerBuilder
import scala.annotation.targetName

/**
 * DSL properties for configuring trigger effects.
 *
 * This enumeration provides type-safe property setters for trigger configuration
 * through the DSL syntax. Each case corresponds to a specific trigger effect
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum TriggerProperty[T]:
  /** Opens a wall at the specified coordinates (row, column) */
  case OpenWall extends TriggerProperty[(Int, Int)]
  /** Gives a bonus reward to the triggering agent */
  case Give extends TriggerProperty[Double]
  /** Ends the current episode when triggered */
  case EndEpisode extends TriggerProperty[Boolean]

  @targetName("to")
  infix def >>(obj: T)(using tb: TriggerBuilder): Unit = this match
    case TriggerProperty.OpenWall =>
      val (x, y) = obj.asInstanceOf[(Int, Int)]
      tb.openWall(x, y)
    case TriggerProperty.Give =>
      val bonus = obj.asInstanceOf[Double]
      tb.give(bonus)
    case TriggerProperty.EndEpisode =>
      val shouldEnd = obj.asInstanceOf[Boolean]
      if shouldEnd then tb.endEpisode()

/**
 * Configuration parameters for LLM-generated wall creation.
 *
 * This class provides mutable configuration parameters for generating
 * walls using Large Language Models through the DSL syntax.
 *
 * @param model
 *   The LLM model identifier to use for wall generation
 * @param prompt
 *   The prompt template for requesting wall layouts from the LLM
 */
case class WallLLMConfig(
  var model: String = "",
  var prompt: String = ""
)

/**
 * DSL properties for configuring LLM-generated wall creation.
 *
 * This enumeration provides type-safe property setters for LLM wall generation
 * configuration through the DSL syntax. Each case corresponds to a specific
 * LLM configuration parameter.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum WallLLMProperty[T]:
  /** The LLM model identifier to use for wall generation */
  case Model extends WallLLMProperty[String]
  /** The prompt template for requesting wall layouts from the LLM */
  case Prompt extends WallLLMProperty[String]

  @targetName("to")
  infix def >>(obj: T)(using config: WallLLMConfig): Unit = this match 
    case WallLLMProperty.Model => config.model = obj.asInstanceOf[String]
    case WallLLMProperty.Prompt => config.prompt = obj.asInstanceOf[String]
  
