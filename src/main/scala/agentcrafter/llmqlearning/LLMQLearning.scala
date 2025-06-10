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
      // Find the file that contains the useLLM call
      val simulationFilePath = findSimulationFile()
      LLMQTableService.loadQTableFromLLM(wrapper.builder, llmConfig.model, simulationFilePath) match
        case Some(qTableJson) =>
          println(s"LLM response from ${llmConfig.model} received – loading agents Q‑table…")
          LLMQTableService.loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println(s"Failed to obtain Q‑table from LLM (${llmConfig.model}); proceeding with normal simulation.")

    wrapper.builder.build()

  /**
   * Finds the simulation file by inspecting the stack trace to locate the file
   * that contains the useLLM call.
   */
  private def findSimulationFile(): String =
    val stackTrace = Thread.currentThread().getStackTrace
    
    // Find the first stack frame that's not from this trait or system classes
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
