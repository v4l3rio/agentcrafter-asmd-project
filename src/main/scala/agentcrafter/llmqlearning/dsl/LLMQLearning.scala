package agentcrafter.llmqlearning.dsl

import agentcrafter.llmqlearning.{LLMQTableService, LLMWallService}
import agentcrafter.marl.builders.SimulationBuilder
import agentcrafter.marl.dsl.{SimulationDSL, SimulationWrapper}

/**
 * Mixin that augments the base `SimulationDSL` with an `useLLM { … }` block allowing users to enable LLM‑generated
 * Q‑tables.
 */
trait LLMQLearning extends SimulationDSL:

  private var llmConfig: LLMConfig = LLMConfig()
  private var wallConfig: Option[LLMWallConfig] = None

  def useLLM(block: LLMConfig ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given config: LLMConfig = llmConfig

    block
    llmConfig = config

  def wallsFromLLM(block: LLMWallConfig ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    val config = LLMWallConfig()
    given LLMWallConfig = config

    block

    // Store the config for later execution after the simulation block is complete
    wallConfig = Some(config)

  override def simulation(block: SimulationWrapper ?=> Unit): Unit =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)

    block

    // Generate walls from LLM after the simulation block is complete (so agents are defined)
    wallConfig.foreach { config =>
      if config.model.nonEmpty && config.prompt.nonEmpty then
        // Now the simulation builder's toString includes all agent information
        val simulationContent = wrapper.builder.toString
        LLMWallService.generateWallsUsingLLM(
          wrapper.builder,
          config.model,
          config.prompt,
          simulationContent
        ) match
          case Some(asciiWalls) =>
            println(s"LLM wall generation successful, loading walls...")
            LLMWallService.loadWallsIntoBuilder(wrapper.builder, asciiWalls)
          case None =>
            println("LLM wall generation failed, proceeding without generated walls")
    }

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
