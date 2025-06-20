package agentcrafter.llmqlearning

import agentcrafter.marl.AgentSpec
import agentcrafter.marl.builders.SimulationBuilder

import scala.util.{Failure, Success}

object LLMQTableService:

  /**
   * Loads a Q-table from LLM using the specified model.
   *
   * @param builder
   *   The simulation builder
   * @param model
   *   The LLM model to use
   * @param simulationFilePath
   *   Path to the file containing simulation dsl content
   * @return
   *   Some(qTableJson) if successful, None otherwise
   */
  def loadQTableFromLLM(builder: SimulationBuilder, model: String, simulationFilePath: String): Option[String] =
    val client = LLMHttpClient()
    val prompt = Prompts.qTable

    println(s"Calling LLM API ($model) to generate Q‑table…")
    client.callLLM(prompt, model, simulationFilePath = Some(simulationFilePath)) match
      case Success(response) => Some(response)
      case Failure(ex) =>
        println(s"LLM API call failed: ${ex.getMessage}")
        None

  /**
   * Loads the Q-table JSON into all agents in the simulation builder.
   *
   * @param builder
   *   The simulation builder containing agents
   * @param qTableJson
   *   The Q-table JSON string to load
   */
  def loadQTableIntoAgents(builder: SimulationBuilder, qTableJson: String): Unit =
    val agents = builder.getAgents

    agents.values.foreach { agentSpec =>
      QTableLoader.loadQTableFromJson(qTableJson, learner = agentSpec.learner) match
        case Success(_) => println(s"Loaded LLM Q‑table for agent: ${agentSpec.id}")
        case Failure(ex) => println(s"Failed to load Q‑table for agent ${agentSpec.id}: ${ex.getMessage}")
    }
