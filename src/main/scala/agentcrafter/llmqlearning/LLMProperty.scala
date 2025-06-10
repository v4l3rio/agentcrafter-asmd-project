package agentcrafter.llmqlearning

import scala.annotation.targetName

/**
 * DSL properties for LLM configuration
 */
enum LLMProperty[T]:
  case Enabled extends LLMProperty[Boolean]
  case Model extends LLMProperty[String]
  case WallsEnabled extends LLMProperty[Boolean]
  case WallsModel extends LLMProperty[String]
  case WallsPrompt extends LLMProperty[String]

  @targetName("setProperty")
  infix def >>(value: T)(using config: LLMConfig): Unit = this match
    case LLMProperty.Enabled => config.enabled = value.asInstanceOf[Boolean]
    case LLMProperty.Model => config.model = value.asInstanceOf[String]
    case LLMProperty.WallsEnabled => config.wallsEnabled = value.asInstanceOf[Boolean]
    case LLMProperty.WallsModel => config.wallsModel = value.asInstanceOf[String]
    case LLMProperty.WallsPrompt => config.wallsPrompt = value.asInstanceOf[String]
