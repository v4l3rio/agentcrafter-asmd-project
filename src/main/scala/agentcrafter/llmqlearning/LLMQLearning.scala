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

      val simulationFilePath = findSimulationFile()
      LLMQTableService.loadQTableFromLLM(wrapper.builder, llmConfig.model, simulationFilePath) match
        case Some(qTableJson) =>
          println(s"LLM response from ${llmConfig.model} received – loading agents Q‑table…")
          LLMQTableService.loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println(s"Failed to obtain Q‑table from LLM (${llmConfig.model}); proceeding with normal simulation.")

    wrapper.builder.build()

  /**
   * Finds the simulation file by inspecting the stack trace to locate the file that contains the useLLM call.
   */
  private def findSimulationFile(): String =
    val stackTrace = Thread.currentThread().getStackTrace

    val callingFrame = stackTrace.find { frame =>
      !frame.getClassName.contains("LLMQLearning") &&
      !frame.getClassName.startsWith("java.") &&
      !frame.getClassName.startsWith("scala.") &&
      frame.getClassName.contains("agentcrafter")
    }

    callingFrame match
      case Some(frame) =>
        val className = frame.getClassName.stripSuffix("$")
        s"src/main/scala/${className.replace('.', '/')}.scala"
      case None =>
        throw new RuntimeException("Could not determine simulation file from stack trace")
