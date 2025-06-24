package agentcrafter.llmqlearning

import agentcrafter.llmqlearning.loader.QTableLoader
import agentcrafter.marl.builders.SimulationBuilder

import scala.util.{Failure, Success}

/**
 * Service responsible for LLM Q-table generation and loading into simulation agents.
 */
object LLMQTableService extends LLMService[String]:

  /**
   * Generates Q-tables from LLM using the multi-agent prompt. Works for both single and multiple agents.
   *
   * @param builder
   *   The simulation builder
   * @param model
   *   The LLM model to use
   * @param simulationContent
   *   The simulation configuration as a string
   * @return
   *   Some(qTableJson) if successful, None otherwise
   */
  def loadQTableFromLLM(
    builder: SimulationBuilder,
    model: String,
    simulationContent: String
  ): Option[String] =
    generateFromLLM(builder, model, Prompts.multiAgentQTable, simulationContent)

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
   * Loads Q-tables into the simulation builder with robust fallback strategy. Works for both single and multiple
   * agents. If some agent Q-tables are corrupted, only the valid ones are loaded. If all are corrupted, all agents use
   * default (optimistic) initialization.
   *
   * @param builder
   *   The simulation builder containing agents
   * @param qTableJson
   *   The Q-table JSON string to load
   */
  def loadIntoBuilder(builder: SimulationBuilder, qTableJson: String): Unit =
    val agents = builder.getAgents
    val agentLearners = agents.map { case (id, spec) => id -> spec.learner }

    val loadResults = QTableLoader.loadMultiAgentQTablesFromJson(qTableJson, agentLearners)

    // Count successful and failed loads
    val successCount = loadResults.values.count(_.isSuccess)
    val totalCount = loadResults.size

    loadResults.foreach { case (agentId, result) =>
      result match
        case Success(_) =>
          println(s"Loaded LLM Q‑table for agent: $agentId")
        case Failure(ex) =>
          println(s"Failed to load Q‑table for agent $agentId: ${ex.getMessage} - using default values")
    }

    if successCount == 0 then
      println(s"All Q-tables failed to load. All $totalCount agents using default optimistic initialization.")
    else if successCount < totalCount then
      println(s"Partial success: $successCount/$totalCount agents loaded from LLM, others using defaults.")
    else
      println(s"Successfully loaded LLM Q-tables for all $totalCount agents.")

  /**
   * Extracts Q-table JSON from LLM response. For Q-tables, we return the response as-is since it should be JSON.
   */
  protected def extractContentFromResponse(response: String): Option[String] =
    Some(response)
