package agentcrafter.llmqlearning.dsl

import scala.annotation.targetName

/**
 * Configuration class for LLM-enhanced Q-learning.
 *
 * This class encapsulates configuration parameters needed for LLM-generated Q-tables in reinforcement learning
 * experiments.
 *
 * @param enabled
 *   Whether LLM integration is enabled for Q-learning
 * @param model
 *   The LLM model to use for Q-learning assistance (default: "gpt-4o")
 */
case class LLMConfig(
  var enabled: Boolean = false,
  var model: String = "gpt-4o"
)

/**
 * DSL properties for configuring LLM-enhanced Q-learning.
 *
 * This enumeration provides type-safe property setters for LLM Q-learning configuration through the DSL syntax. Each
 * case corresponds to a specific LLM configuration parameter.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum LLMProperty[T]:
  /** Enables or disables LLM integration for Q-learning */
  case Enabled extends LLMProperty[Boolean]

  /** Specifies the LLM model to use for Q-learning assistance */
  case Model extends LLMProperty[String]

  @targetName("to")
  infix def >>(obj: T)(using config: LLMConfig): Unit = this match
    case LLMProperty.Enabled => config.enabled = obj.asInstanceOf[Boolean]
    case LLMProperty.Model => config.model = obj.asInstanceOf[String]
