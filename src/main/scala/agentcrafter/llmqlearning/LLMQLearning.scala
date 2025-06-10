package agentcrafter.llmqlearning

import agentcrafter.MARL.builders.SimulationBuilder
import agentcrafter.MARL.DSL.{SimulationDSL, SimulationWrapper}

/* ─────────────────────────── main DSL mix‑in ───────────────────────── */

/**
 * Mixin that augments the base `SimulationDSL` with an `useLLM { … }` block
 * allowing users to enable LLM‑generated Q‑tables.
 */
trait LLMQLearning extends SimulationDSL:

  // Mutable context to track LLM settings (scoped to a `simulation` invocation)
  private var llmConfig: LLMConfig = LLMConfig()

  /** DSL keyword to configure/enable LLM support. */
  def useLLM(block: LLMConfig ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given config: LLMConfig = llmConfig
    block
    llmConfig = config

  /** Overrides the standard `simulation` to optionally bootstrap agents with an LLM‑generated Q‑table. */
  override def simulation(block: SimulationWrapper ?=> Unit): Unit =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block

    if llmConfig.enabled then
      LLMQTableService.loadQTableFromLLM(wrapper.builder, llmConfig.model) match
        case Some(qTableJson) =>
          println(s"LLM response from ${llmConfig.model} received – loading agents Q‑table…")
          LLMQTableService.loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println(s"Failed to obtain Q‑table from LLM (${llmConfig.model}); proceeding with normal simulation.")

    wrapper.builder.build()
