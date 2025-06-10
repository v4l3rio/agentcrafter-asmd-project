package agentcrafter.llmqlearning

import scala.annotation.targetName

/** LLM configuration wrapper for the DSL. */
case class LLMConfig(var enabled: Boolean = false, var model: String = "gpt-4o")

// ── DSL property objects ──────────────────────────────────────────────
object Enabled:
  @targetName("to")
  def >>(value: Boolean)(using config: LLMConfig): Unit =
    config.enabled = value

object Model:
  @targetName("to")
  def >>(value: String)(using config: LLMConfig): Unit =
    config.model = value