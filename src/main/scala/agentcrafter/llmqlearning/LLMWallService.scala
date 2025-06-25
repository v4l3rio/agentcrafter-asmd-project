package agentcrafter.llmqlearning

import agentcrafter.llmqlearning.loader.WallLoader
import agentcrafter.marl.builders.SimulationBuilder

import scala.util.matching.Regex
import scala.util.{Failure, Success}

/**
 * Service responsible for LLM wall generation and loading into simulation.
 */
object LLMWallService extends LLMService[String]:

  /**
   * Alias for backward compatibility.
   */
  def generateWallsUsingLLM(
    builder: SimulationBuilder,
    model: String,
    prompt: String,
    simulationContent: String
  ): Option[String] =
    generateFromLLM(builder, model, prompt, simulationContent)

  /**
   * Generates walls from LLM using simulation content directly.
   *
   * @param builder
   *   The simulation builder
   * @param model
   *   The LLM model to use
   * @param prompt
   *   The custom prompt for wall generation
   * @param simulationContent
   *   The simulation configuration as a string
   * @return
   *   Some(asciiWalls) if successful, None otherwise
   */
  def generateFromLLM(
    builder: SimulationBuilder,
    model: String,
    prompt: String,
    simulationContent: String
  ): Option[String] =
    val fullPrompt = buildPrompt(builder, prompt)
    callLLMAndProcess(model, fullPrompt, simulationContent, "walls")

  /**
   * Builds a comprehensive prompt for wall generation including simulation context.
   */
  protected override def buildPrompt(builder: SimulationBuilder, userPrompt: String): String =
    val basePrompt = Prompts.walls

    s"""
       |$basePrompt
       |
       |SPECIFIC USER REQUEST:
       |$userPrompt
       |
       |Please generate the ASCII map now based on the above requirements and context:
    """.stripMargin

  
  /**
   * Loads the ASCII walls into the simulation builder.
   *
   * @param builder
   *   The simulation builder
   * @param asciiWalls
   *   The ASCII representation of walls
   */
  def loadIntoBuilder(builder: SimulationBuilder, asciiWalls: String): Unit =
    WallLoader.loadIntoBuilder(builder, asciiWalls) match
      case Success(_) =>
        println("Successfully loaded LLM-generated walls into simulation")
      case Failure(ex) =>
        println(s"Error loading walls into simulation: ${ex.getMessage}")

  /**
   * Extracts ASCII map from LLM response using the common loader.
   */
  protected def extractContentFromResponse(response: String): Option[String] =
    WallLoader.extractWallsFromResponse(response).toOption
