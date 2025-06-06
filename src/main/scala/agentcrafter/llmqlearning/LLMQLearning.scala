package agentcrafter.llmqlearning

import agentcrafter.MARL.builders.SimulationBuilder
import agentcrafter.MARL.AgentSpec
import agentcrafter.MARL.DSL.{SimulationDSL, SimulationWrapper}

import scala.annotation.targetName
import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Using}

/** Helper object that loads static LLM prompts from `src/main/resources/`. */
object Prompts:
  /**
   * Q‑Table generation prompt, loaded lazily from
   * `src/main/resources/prompts/qtable_prompt.txt` (bundled in the JAR).
   */
  lazy val qTable: String =
    Using.resource(Source.fromResource("prompts/qtable_prompt.txt"))(_.mkString)


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
      loadQTableFromLLM(wrapper.builder, llmConfig.model) match
        case Some(qTableJson) =>
          println(s"LLM response from ${llmConfig.model} received – loading agents Q‑table…")
          loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println(s"Failed to obtain Q‑table from LLM (${llmConfig.model}); proceeding with normal simulation.")

    wrapper.builder.play()


private def loadQTableFromLLM(builder: SimulationBuilder, model: String): Option[String] =
  val client = LLMApiClient()
  val prompt = Prompts.qTable

  println(s"Calling LLM API ($model) to generate Q‑table…")
  client.callLLM(prompt, model) match
    case Success(response) => Some(response)
    case Failure(ex) =>
      println(s"LLM API call failed: ${ex.getMessage}")
      None

private def loadQTableIntoAgents(builder: SimulationBuilder, qTableJson: String): Unit =
  try
    val agentsField = builder.getClass.getDeclaredField("agents")
    agentsField.setAccessible(true)
    val agents = agentsField.get(builder).asInstanceOf[mutable.Map[String, AgentSpec]]

    agents.values.foreach { agentSpec =>
      QTableLoader.loadQTableFromJson(qTableJson, learner = agentSpec.learner) match
        case Success(_)  => println(s"Loaded LLM Q‑table for agent: ${agentSpec.id}")
        case Failure(ex) => println(s"Failed to load Q‑table for agent ${agentSpec.id}: ${ex.getMessage}")
    }
  catch
    case ex: Exception =>
      println(s"Error accessing agents for Q‑table loading: ${ex.getMessage}")
