package agentcrafter.llmqlearning

import agentcrafter.marl.dsl.{Simulationdsl, SimulationWrapper}
import agentcrafter.marl.builders.SimulationBuilder

/**
 * Mixin that augments the base `Simulationdsl` with an `useLLM { … }` block allowing users to enable LLM‑generated
 * Q‑tables.
 */
trait LLMQLearning extends Simulationdsl:

  private var llmConfig: LLMConfig = LLMConfig()

  def useLLM(block: LLMConfig ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given config: LLMConfig = llmConfig

    block
    llmConfig = config

  override def simulation(block: SimulationWrapper ?=> Unit): Unit =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)

    block

    if llmConfig.enabled then
      // Use the simulation builder's toString instead of file path inspection
      val simulationContent = wrapper.builder.toString
      LLMQTableService.loadQTableFromLLM(wrapper.builder, llmConfig.model, simulationContent) match
        case Some(qTableJson) =>
          println(s"LLM response from ${llmConfig.model} received – loading agents Q‑table…")
          LLMQTableService.loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println(s"Failed to obtain Q‑table from LLM (${llmConfig.model}); proceeding with normal simulation.")

    wrapper.builder.build()
