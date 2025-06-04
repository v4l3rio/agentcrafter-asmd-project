package agentcrafter.llmqlearning

/**
 * DSL properties for LLM configuration
 */
enum LLMProperty[T]:
  case Enabled extends LLMProperty[Boolean]
  case Model extends LLMProperty[String]

  infix def >>(value: T)(using config: LLMConfig): Unit = this match
    case LLMProperty.Enabled => config.enabled = value.asInstanceOf[Boolean]
    case LLMProperty.Model => config.model = value.asInstanceOf[String]

/**
 * Implicit conversions for boolean values in DSL
 */
given Conversion[Boolean, Unit] = _ => ()