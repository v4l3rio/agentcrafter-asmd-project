package agentcrafter.llmqlearning

import scala.annotation.targetName

/**
 * Configuration class for Large Language Model (LLM) integration.
 *
 * This class encapsulates all configuration parameters needed for LLM-enhanced
 * Q-learning, including model selection, feature toggles, and specialized
 * configurations for different LLM-powered features like dynamic wall generation.
 *
 * The class is designed to work with the AgentCrafter DSL, providing a fluent
 * interface for configuring LLM behavior in reinforcement learning experiments.
 *
 * @param enabled      Whether LLM integration is enabled for Q-learning
 * @param model        The LLM model to use for Q-learning assistance (default: "gpt-4o")
 * @param wallsEnabled Whether LLM-powered dynamic wall generation is enabled
 * @param wallsModel   The LLM model to use for wall generation (default: "gpt-4o")
 * @param wallsPrompt  Custom prompt template for wall generation requests
 */
case class LLMConfig(
                      var enabled: Boolean = false,
                      var model: String = "gpt-4o",
                      var wallsEnabled: Boolean = false,
                      var wallsModel: String = "gpt-4o",
                      var wallsPrompt: String = ""
                    )

// ── DSL property objects ──────────────────────────────────────────────

/**
 * DSL object for configuring LLM enablement status.
 *
 * Provides a fluent interface for enabling or disabling LLM integration
 * in the AgentCrafter simulation configuration.
 */
object Enabled:
  /**
   * Sets the LLM enabled status using DSL syntax.
   *
   * @param value  Whether to enable LLM integration
   * @param config The implicit LLM configuration context
   */
  @targetName("to")
  def >>(value: Boolean)(using config: LLMConfig): Unit =
    config.enabled = value

/**
 * DSL object for configuring the LLM model selection.
 *
 * Provides a fluent interface for specifying which LLM model to use
 * for Q-learning assistance and decision-making.
 */
object Model:
  /**
   * Sets the LLM model using DSL syntax.
   *
   * @param value  The model identifier (e.g., "gpt-4o", "claude-3-sonnet")
   * @param config The implicit LLM configuration context
   */
  @targetName("to")
  def >>(value: String)(using config: LLMConfig): Unit =
    config.model = value