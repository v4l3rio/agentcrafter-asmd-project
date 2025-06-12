package agentcrafter.llmqlearning

import agentcrafter.MARL.builders.SimulationBuilder
import agentcrafter.MARL.AgentSpec

import scala.collection.mutable
import scala.util.{Failure, Success}

/** Service responsible for LLM Q-table generation and loading into agents. */
object LLMQTableService:

  /**
   * Loads a Q-table from LLM using the specified model.
   * 
   * @param builder The simulation builder
   * @param model The LLM model to use
   * @param simulationFilePath Path to the file containing simulation DSL content
   * @return Some(qTableJson) if successful, None otherwise
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
   * @param builder The simulation builder containing agents
   * @param qTableJson The Q-table JSON string to load
   */
  def loadQTableIntoAgents(builder: SimulationBuilder, qTableJson: String): Unit =
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