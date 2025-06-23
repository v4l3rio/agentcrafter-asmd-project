package agentcrafter.llmqlearning

import agentcrafter.marl.builders.SimulationBuilder
import scala.util.{Failure, Success}

/**
 * Service responsible for LLM Q-table generation and loading into simulation agents.
 */
object LLMQTableService extends LLMService[String]:

  /**
   * Generates a Q-table from LLM using the specified model.
   *
   * @param builder
   *   The simulation builder
   * @param model
   *   The LLM model to use
   * @param prompt
   *   The prompt for Q-table generation (defaults to standard Q-table prompt)
   * @param simulationContent
   *   The simulation configuration as a string
   * @return
   *   Some(qTableJson) if successful, None otherwise
   */
  def generateFromLLM(
    builder: SimulationBuilder,
    model: String,
    prompt: String,
    simulationContent: String
  ): Option[String] =
    val fullPrompt = buildPrompt(builder, prompt)
    callLLMAndProcess(model, fullPrompt, simulationContent, "Q-table")

  /**
   * Convenience method that uses the default Q-table prompt.
   */
  def loadQTableFromLLM(builder: SimulationBuilder, model: String, simulationContent: String): Option[String] =
    generateFromLLM(builder, model, Prompts.qTable, simulationContent)

  /**
   * Loads the Q-table JSON into all agents in the simulation builder.
   *
   * @param builder
   *   The simulation builder containing agents
   * @param qTableJson
   *   The Q-table JSON string to load
   */
  def loadIntoBuilder(builder: SimulationBuilder, qTableJson: String): Unit =
    val agents = builder.getAgents

    agents.values.foreach { agentSpec =>
      QTableLoader.loadQTableFromJson(qTableJson, learner = agentSpec.learner) match
        case Success(_) => println(s"Loaded LLM Q‑table for agent: ${agentSpec.id}")
        case Failure(ex) => println(s"Failed to load Q‑table for agent ${agentSpec.id}: ${ex.getMessage}")
    }

  /**
   * Alias for backward compatibility.
   */
  def loadQTableIntoAgents(builder: SimulationBuilder, qTableJson: String): Unit =
    loadIntoBuilder(builder, qTableJson)

  /**
   * Extracts Q-table JSON from LLM response.
   * For Q-tables, we return the response as-is since it should be JSON.
   */
  protected def extractContentFromResponse(response: String): Option[String] =
    Some(response)
