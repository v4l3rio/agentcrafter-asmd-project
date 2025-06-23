package agentcrafter.llmqlearning

import agentcrafter.marl.builders.SimulationBuilder
import scala.util.{Failure, Success}

/**
 * Common interface for LLM-based services that generate content for simulations.
 *
 * This trait defines the common pattern used by LLM services:
 * 1. Generate content from LLM using a prompt and simulation context
 * 2. Process the LLM response to extract the relevant content
 * 3. Load the processed content into the simulation builder
 *
 * @tparam T The type of content this service generates (e.g., String for walls, JSON for Q-tables)
 */
trait LLMService[T]:

  /**
   * Generates content from LLM using the specified parameters.
   *
   * @param builder The simulation builder
   * @param model The LLM model to use
   * @param prompt The prompt for content generation
   * @param simulationContent The simulation configuration as a string
   * @return Some(content) if successful, None otherwise
   */
  def generateFromLLM(
    builder: SimulationBuilder,
    model: String,
    prompt: String,
    simulationContent: String
  ): Option[T]

  /**
   * Loads the generated content into the simulation builder.
   *
   * @param builder The simulation builder
   * @param content The content to load
   */
  def loadIntoBuilder(builder: SimulationBuilder, content: T): Unit

  /**
   * Builds the complete prompt for LLM generation.
   * Default implementation uses the prompt as-is, but can be overridden for custom prompt building.
   *
   * @param builder The simulation builder (for context)
   * @param userPrompt The user-provided prompt
   * @return The complete prompt to send to the LLM
   */
  protected def buildPrompt(builder: SimulationBuilder, userPrompt: String): String = userPrompt

  /**
   * Processes the raw LLM response to extract the relevant content.
   * This method should be implemented by concrete services to handle their specific response formats.
   *
   * @param response The raw LLM response
   * @return Some(content) if extraction successful, None otherwise
   */
  protected def extractContentFromResponse(response: String): Option[T]

  /**
   * Common implementation for calling the LLM and processing the response.
   * This method encapsulates the shared logic between different LLM services.
   */
  protected final def callLLMAndProcess(
    model: String,
    prompt: String,
    simulationContent: String,
    contentType: String
  ): Option[T] =
    val client = LLMHttpClient()

    println(s"Calling LLM API ($model) to generate $contentType…")
    client.callLLMWithContent(prompt, model, simulationContent) match
      case Success(response) =>
        extractContentFromResponse(response) match
          case Some(content) => Some(content)
          case None =>
            println(s"Failed to extract valid $contentType from LLM response")
            None
      case Failure(ex) =>
        println(s"LLM API call failed: ${ex.getMessage}")
        None