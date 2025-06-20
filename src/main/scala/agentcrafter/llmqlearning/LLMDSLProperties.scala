package agentcrafter.llmqlearning

import scala.annotation.targetName

/**
 * Type-safe dsl properties for LLM configuration.
 *
 * This enumeration provides a type-safe way to configure LLM settings using a fluent dsl syntax. Each property case
 * corresponds to a specific configuration option and enforces the correct value type at compile time.
 *
 * The enum supports the `>>` operator for setting values in a readable, declarative style that integrates seamlessly
 * with the AgentCrafter dsl.
 *
 * @tparam T
 *   The type of value this property accepts
 * @example
 *   {{{} // Using the dsl syntax LLMProperty.Enabled >> true LLMProperty.Model >> "gpt-4o" LLMProperty.WallsEnabled >>
 *   false }}}
 */
enum LLMdslProperties[T]:
  /** Enables or disables LLM integration for Q-learning */
  case Enabled extends LLMdslProperties[Boolean]

  /** Specifies the LLM model to use for Q-learning assistance */
  case Model extends LLMdslProperties[String]

  /** Enables or disables LLM-powered dynamic wall generation */
  case WallsEnabled extends LLMdslProperties[Boolean]

  /** Specifies the LLM model to use for wall generation */
  case WallsModel extends LLMdslProperties[String]

  /** Sets the custom prompt template for wall generation requests */
  case WallsPrompt extends LLMdslProperties[String]

  /**
   * Sets the property value using dsl syntax.
   *
   * This method provides a fluent interface for configuring LLM properties with type safety. The `>>` operator creates
   * a readable assignment syntax.
   *
   * @param value
   *   The value to assign to this property
   * @param config
   *   The implicit LLM configuration context to modify
   */
  @targetName("setProperty")
  infix def >>(value: T)(using config: LLMConfig): Unit = this match
    case LLMdslProperties.Enabled => config.enabled = value.asInstanceOf[Boolean]
    case LLMdslProperties.Model => config.model = value.asInstanceOf[String]
    case LLMdslProperties.WallsEnabled => config.wallsEnabled = value.asInstanceOf[Boolean]
    case LLMdslProperties.WallsModel => config.wallsModel = value.asInstanceOf[String]
    case LLMdslProperties.WallsPrompt => config.wallsPrompt = value.asInstanceOf[String]
