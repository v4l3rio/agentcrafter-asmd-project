package agentcrafter.llmqlearning

import agentcrafter.MARL.builders.SimulationBuilder
import agentcrafter.common.State
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

/** Service responsible for LLM wall generation and loading into simulation. */
object LLMWallService:

  /**
   * Generates walls from LLM using the specified model and prompt.
   * 
   * @param builder The simulation builder
   * @param model The LLM model to use
   * @param prompt The custom prompt for wall generation
   * @param simulationFilePath Optional path to the simulation file for context
   * @return Some(asciiWalls) if successful, None otherwise
   */
  def generateWallsFromLLM(builder: SimulationBuilder, model: String, prompt: String, simulationFilePath: Option[String] = None): Option[String] =
    val client = LLMApiClient()
    val fullPrompt = buildWallPrompt(builder, prompt)

    println(s"Calling LLM API ($model) to generate walls…")
    client.callLLM(fullPrompt, model, simulationFilePath = simulationFilePath) match
      case Success(response) => 
        extractAsciiFromResponse(response) match
          case Some(ascii) => Some(ascii)
          case None => 
            println("Failed to extract valid ASCII map from LLM response")
            None
      case Failure(ex) =>
        println(s"LLM API call failed: ${ex.getMessage}")
        None

  /**
   * Loads the ASCII walls into the simulation builder.
   * 
   * @param builder The simulation builder
   * @param asciiWalls The ASCII representation of walls
   */
  def loadWallsIntoBuilder(builder: SimulationBuilder, asciiWalls: String): Unit =
    try
      builder.wallsFromAscii(asciiWalls)
      println("Successfully loaded LLM-generated walls into simulation")
    catch
      case ex: Exception =>
        println(s"Error loading walls into simulation: ${ex.getMessage}")

  /**
   * Builds a comprehensive prompt for wall generation including simulation context.
   */
  private def buildWallPrompt(builder: SimulationBuilder, userPrompt: String): String =
    val basePrompt = Prompts.walls
    val gridInfo = getGridInfo(builder)
    val agentInfo = getAgentInfo(builder)
    
    s"""
    |$basePrompt
    |
    |CURRENT SIMULATION CONTEXT:
    |$gridInfo
    |
    |$agentInfo
    |
    |SPECIFIC USER REQUEST:
    |$userPrompt
    |
    |Please generate the ASCII map now based on the above requirements and context:
    """.stripMargin

  /**
   * Extracts grid information from the simulation builder.
   */
  private def getGridInfo(builder: SimulationBuilder): String =
    try
      val rowsField = builder.getClass.getDeclaredField("rows")
      val colsField = builder.getClass.getDeclaredField("cols")
      rowsField.setAccessible(true)
      colsField.setAccessible(true)
      val rows = rowsField.get(builder).asInstanceOf[Int]
      val cols = colsField.get(builder).asInstanceOf[Int]
      s"Grid size: $rows rows x $cols columns"
    catch
      case _: Exception => "Grid size: 10 rows x 10 columns (default)"

  /**
   * Extracts agent information from the simulation builder.
   */
  private def getAgentInfo(builder: SimulationBuilder): String =
    try
      val agentsField = builder.getClass.getDeclaredField("agents")
      agentsField.setAccessible(true)
      val agents = agentsField.get(builder).asInstanceOf[scala.collection.mutable.Map[String, _]]
      
      if agents.isEmpty then
        "No agents defined yet"
      else
        val agentDescriptions = agents.map { case (id, spec) =>
          // Extract agent information using reflection if needed
          s"Agent '$id': (positions will be defined in DSL)"
        }.mkString("\n")
        s"Agents:\n$agentDescriptions"
    catch
      case _: Exception => "Agent information not available"

  /**
   * Extracts ASCII map from LLM response.
   */
  private def extractAsciiFromResponse(response: String): Option[String] =
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