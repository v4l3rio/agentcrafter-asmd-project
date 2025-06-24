package agentcrafter.llmqlearning.loader

import agentcrafter.marl.builders.SimulationBuilder

import scala.util.{Failure, Success, Try}

/**
 * Loader for ASCII walls from LLM responses.
 *
 * Handles ASCII content extraction, cleaning LLM-generated decorations, and loading walls into simulation builders.
 * Uses the common LLMResponseParser for consistent error handling.
 */
object WallLoader:

  /**
   * Loads ASCII walls from LLM response into the simulation builder.
   *
   * @param builder
   *   The simulation builder
   * @param rawResponse
   *   Raw LLM response containing ASCII walls
   * @return
   *   Try indicating success or failure with error details
   */
  def loadWallsFromResponse(builder: SimulationBuilder, rawResponse: String): Try[Unit] =
    for
      asciiContent <- LLMResponseParser.extractAsciiContent(rawResponse)
      validatedContent <- LLMResponseParser.validateContent(asciiContent, "ASCII walls")
      _ <- loadIntoBuilder(builder, validatedContent)
    yield ()

  /**
   * Alternative method that extracts ASCII content and returns it without loading. Useful for testing or when you want
   * to inspect the content before loading.
   *
   * @param rawResponse
   *   Raw LLM response
   * @return
   *   Try containing the extracted and validated ASCII content
   */
  def extractWallsFromResponse(rawResponse: String): Try[String] =
    for
      asciiContent <- LLMResponseParser.extractAsciiContent(rawResponse)
      validatedContent <- LLMResponseParser.validateContent(asciiContent, "ASCII walls")
    yield validatedContent

  /**
   * Comprehensive wall loading with full validation pipeline.
   *
   * @param builder
   *   The simulation builder
   * @param rawResponse
   *   Raw LLM response
   * @return
   *   Try indicating success or failure with detailed error information
   */
  def loadWallsWithValidation(builder: SimulationBuilder, rawResponse: String): Try[Unit] =
    for
      asciiContent <- LLMResponseParser.extractAsciiContent(rawResponse)
      validatedContent <- LLMResponseParser.validateContent(asciiContent, "ASCII walls")
      structurallyValid <- validateWallStructure(validatedContent)
      _ <- loadIntoBuilder(builder, structurallyValid)
    yield ()

  /**
   * Loads the ASCII walls into the simulation builder with error handling.
   *
   * @param builder
   *   The simulation builder
   * @param asciiWalls
   *   The ASCII representation of walls
   * @return
   *   Try indicating success or failure
   */
  def loadIntoBuilder(builder: SimulationBuilder, asciiWalls: String): Try[Unit] =
    Try {
      builder.wallsFromAscii(asciiWalls)
      () // Return Unit explicitly
    }.recoverWith { case ex: Exception =>
      Failure(new RuntimeException(s"Error loading walls into simulation: ${ex.getMessage}", ex))
    }

  /**
   * Validates that ASCII content represents a valid wall configuration. Checks for basic structural requirements like
   * consistent line lengths, valid characters, etc.
   *
   * @param asciiWalls
   *   ASCII wall content
   * @return
   *   Try indicating validation success or failure
   */
  def validateWallStructure(asciiWalls: String): Try[String] =
    Try {
      val lines = asciiWalls.split("\n").filter(_.trim.nonEmpty)

      if lines.isEmpty then
        throw new RuntimeException("ASCII walls content is empty")

      // Check for consistent line lengths (basic grid validation)
      val lineLengths = lines.map(_.length)
      val maxLength = lineLengths.max
      val minLength = lineLengths.min

      if maxLength - minLength > 1 then // Allow for minor variations
        throw new RuntimeException(s"Inconsistent line lengths in ASCII walls: min=$minLength, max=$maxLength")

      // Check for valid characters (walls '#', empty space '.', space ' ')
      val validChars = Set('#', '.', ' ')
      val invalidChars = asciiWalls.filterNot(c => validChars.contains(c) || c == '\n')

      if invalidChars.nonEmpty then
        throw new RuntimeException(s"Invalid characters found in ASCII walls: ${invalidChars.distinct.mkString(", ")}")

      asciiWalls
    }
