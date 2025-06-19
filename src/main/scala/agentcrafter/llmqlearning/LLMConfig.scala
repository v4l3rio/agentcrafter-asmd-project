package agentcrafter.llmqlearning

import scala.annotation.targetName

/**
 * Configuration class for Large Language Model (LLM) integration.
 *
 * This class encapsulates all configuration parameters needed for LLM-enhanced Q-learning, including model selection,
 * feature toggles, and specialized configurations for different LLM-powered features like dynamic wall generation.
 *
 * The class is designed to work with the AgentCrafter DSL, providing a fluent interface for configuring LLM behavior in
 * reinforcement learning experiments.
 *
 * @param enabled
 *   Whether LLM integration is enabled for Q-learning
 * @param model
 *   The LLM model to use for Q-learning assistance (default: "gpt-4o")
 * @param wallsEnabled
 *   Whether LLM-powered dynamic wall generation is enabled
 * @param wallsModel
 *   The LLM model to use for wall generation (default: "gpt-4o")
 * @param wallsPrompt
 *   Custom prompt template for wall generation requests
 */
case class LLMConfig(
  var enabled: Boolean = false,
  var model: String = "gpt-4o",
  var wallsEnabled: Boolean = false,
  var wallsModel: String = "gpt-4o",
  var wallsPrompt: String = ""
)
