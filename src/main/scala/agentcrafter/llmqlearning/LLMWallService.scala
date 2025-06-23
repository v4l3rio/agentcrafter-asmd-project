package agentcrafter.llmqlearning

import agentcrafter.marl.builders.SimulationBuilder
import scala.util.matching.Regex
import scala.util.{Failure, Success}

/**
 * Service responsible for LLM wall generation and loading into simulation.
 */
object LLMWallService extends LLMService[String]:

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
   * Extracts ASCII map from LLM response.
   */
  protected def extractContentFromResponse(response: String): Option[String] =
    // Look for ASCII content between ```ascii and ``` tags
    val asciiPattern = "```ascii\\s*\\n([\\s\\S]*?)\\n```".r
    asciiPattern.findFirstMatchIn(response) match
      case Some(m) => Some(m.group(1).trim)
      case None =>
        // Fallback: look for any content between ``` tags
        val genericPattern = "```\\s*\\n([\\s\\S]*?)\\n```".r
        genericPattern.findFirstMatchIn(response) match
          case Some(m) => Some(m.group(1).trim)
          case None =>
            // Last resort: try to find lines that look like ASCII art
            val lines = response.split("\\n")
            val asciiLines = lines.filter(line =>
              line.trim.nonEmpty &&
              line.forall(c => c == '#' || c == '.' || c == ' ')
            )
            if asciiLines.nonEmpty then Some(asciiLines.mkString("\\n"))
            else None

  /**
   * Loads the ASCII walls into the simulation builder.
   *
   * @param builder
   *   The simulation builder
   * @param asciiWalls
   *   The ASCII representation of walls
   */
  def loadIntoBuilder(builder: SimulationBuilder, asciiWalls: String): Unit =
    try
      builder.wallsFromAscii(asciiWalls)
      println("Successfully loaded LLM-generated walls into simulation")
    catch
      case ex: Exception =>
        println(s"Error loading walls into simulation: ${ex.getMessage}")

  /**
   * Alias for backward compatibility.
   */
  def loadWallsIntoBuilder(builder: SimulationBuilder, asciiWalls: String): Unit =
    loadIntoBuilder(builder, asciiWalls)
