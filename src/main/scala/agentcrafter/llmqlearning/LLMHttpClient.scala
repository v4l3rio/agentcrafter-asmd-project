package agentcrafter.llmqlearning

import io.github.cdimascio.dotenv.Dotenv
import play.api.libs.json.*
import sttp.client4.Response
import sttp.model.Uri
import agentcrafter.llmqlearning.SimpleHttpClient

import java.io.File
import scala.io.Source
import scala.util.{Failure, Try, Using}

/**
 * Client for interacting with Large Language Model APIs.
 *
 * This class provides a robust interface for making HTTP requests to LLM services, handling authentication, retries,
 * and response parsing. It's designed to work with OpenAI-compatible APIs and includes error handling and timeout
 * management.
 *
 * @param apiKey
 *   API key for authentication with the LLM service
 * @param baseUrl
 *   Base URL for the LLM API (defaults to OpenAI's API)
 */
class LLMHttpClient(
  baseUrl: String = "https://api.openai.com",
  apiKey: String = Dotenv.configure().ignoreIfMissing().load().get("OPENAI_API_KEY", "API"),
  httpClient: SimpleHttpClient = SimpleHttpClient()
):
  /**
   * Trigger an LLM generation.
   *
   * @param prompt
   *   Extra text to prepend
   * @param model
   *   Model name (default: "gpt-4o")
   * @param stream
   *   Whether to request streaming output
   * @param endpoint
   *   API path (default: "/v1/chat/completions")
   * @param simulationFilePath
   *   Optional path to the file containing simulation dsl content
   */
  def callLLM(
    prompt: String = "",
    model: String = "gpt-4o",
    stream: Boolean = false,
    endpoint: String = "/v1/chat/completions",
    simulationFilePath: Option[String] = None
  ): Try[String] =
    for
      simContent <- readSimulationContent(simulationFilePath)
      response <- postRequest(prompt, model, stream, endpoint, simContent)
    yield response

  /**
   * Makes an LLM API call with simulation content provided directly as a string.
   *
   * @param prompt
   *   The user prompt to send to the LLM
   * @param model
   *   The LLM model to use (default: "gpt-4o")
   * @param simulationContent
   *   The simulation configuration content as a string
   * @param stream
   *   Whether to enable streaming (default: false)
   * @param endpoint
   *   The API endpoint (default: "/v1/chat/completions")
   * @return
   *   Try containing the LLM response or an error
   */
  def callLLMWithContent(
    prompt: String = "",
    model: String = "gpt-4o",
    simulationContent: String,
    stream: Boolean = false,
    endpoint: String = "/v1/chat/completions"
  ): Try[String] =
    postRequest(prompt, model, stream, endpoint, simulationContent)

  /**
   * Performs the actual HTTP POST request to the LLM API.
   *
   * @param prompt
   *   The user prompt
   * @param model
   *   The LLM model to use
   * @param stream
   *   Whether to enable streaming
   * @param endpoint
   *   The API endpoint
   * @param simContent
   *   The simulation dsl content to include
   * @return
   *   Try containing the LLM response or an error
   */
  private def postRequest(
    prompt: String,
    model: String,
    stream: Boolean,
    endpoint: String,
    simContent: String
  ): Try[String] =
    val enhancedPrompt =
      s"""$prompt
         |
         |Simulation dsl content:
         |$simContent""".stripMargin

    val body: String =
      s"""{
         |  "model": ${jsonString(model)},
         |  "messages": [
         |    {
         |      "role": "user",
         |      "content": ${jsonString(enhancedPrompt)}
         |    }
         |  ],
         |  "stream": $stream
         |}""".stripMargin

    Try {
      val uri = fullUri(endpoint)
      val resp: Response[String] = httpClient.post(uri, body, apiKey)

      if resp.code.isSuccess then
        // Parse OpenAI response format
        val parsed = Json.parse(resp.body)
        (parsed \ "choices" \ 0 \ "message" \ "content").asOpt[String]
          .getOrElse(throw new RuntimeException(s"Could not parse response content from: ${resp.body}"))
      else throw new RuntimeException(s"API call failed: HTTP ${resp.code} – ${resp.statusText} (URI: $uri)")
    }.recoverWith {
      case ex: Throwable =>
        Failure(new RuntimeException(s"Error calling OpenAI API at ${fullUri(endpoint)} – ${ex.getMessage}", ex))
    }

  /**
   * Constructs a full URI by combining the base URL with the endpoint path.
   *
   * @param endpoint
   *   The API endpoint path
   * @return
   *   Complete URI for the API call
   * @throws IllegalArgumentException
   *   if the resulting URI is invalid
   */
  private def fullUri(endpoint: String): Uri =
    val cleanBase = baseUrl.stripSuffix("/")
    val cleanPath = endpoint.stripPrefix("/")
    Uri.parse(s"$cleanBase/$cleanPath").getOrElse(
      throw new IllegalArgumentException(s"Invalid URI: $cleanBase/$cleanPath")
    )

  /**
   * Wraps a string in JSON quotes with proper escaping.
   *
   * @param s
   *   The string to wrap
   * @return
   *   JSON-formatted string with quotes
   */
  private def jsonString(s: String): String = "\"" + escapeJson(s) + "\""

  /**
   * Escapes control characters so the string can be safely embedded in JSON.
   *
   * @param s
   *   The string to escape
   * @return
   *   JSON-safe escaped string
   */
  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\b", "\\b")
      .replace("\f", "\\f")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")

  /**
   * Reads simulation content from the specified file path.
   *
   * @param simulationFilePath
   *   Optional path to the file containing simulation dsl content
   * @return
   *   Try containing the simulation content or an error message
   */
  private def readSimulationContent(simulationFilePath: Option[String]): Try[String] =
    Try {
      simulationFilePath match
        case Some(filePath) =>
          val file = File(filePath)
          if !file.exists then
            throw new RuntimeException(s"Simulation file not found: $filePath")

          Using(Source.fromFile(file)) { src =>
            val content = src.mkString
            content.indexOf("simulation:") match
              case -1 => throw new RuntimeException(s"File $filePath does not contain a simulation block")
              case idx =>
                val simulationContent = content.substring(idx)
                // Remove wallsFromLLM blocks to prevent prompt duplication
                filterOutWallsFromLLM(simulationContent)
          }.get

        case None =>
          "simulation:\n  // No simulation file specified"
    }.recover {
      case ex => throw new RuntimeException(s"Error reading simulation file: ${ex.getMessage}")
    }

  /**
   * Filters out wallsFromLLM blocks from simulation content to prevent prompt duplication.
   *
   * @param content
   *   The simulation dsl content
   * @return
   *   Content with wallsFromLLM blocks removed
   */
  private def filterOutWallsFromLLM(content: String): String =
    val lines = content.split("\n")
    val result = scala.collection.mutable.ArrayBuffer[String]()
    var insideWallsFromLLM = false
    var indentLevel = 0
    var wallsFromLLMIndent = 0

    for (line <- lines) {
      val trimmed = line.trim
      val currentIndent = line.takeWhile(_.isWhitespace).length

      if (trimmed.startsWith("wallsFromLLM:")) {
        insideWallsFromLLM = true
        wallsFromLLMIndent = currentIndent
        // Add a comment indicating walls are generated by LLM
        result += (" " * currentIndent) + "// Walls generated by LLM"
      } else if (insideWallsFromLLM) {
        // Exit wallsFromLLM block when we reach a line with same or less indentation
        if (trimmed.nonEmpty && currentIndent <= wallsFromLLMIndent) {
          insideWallsFromLLM = false
          result += line
        }
        // Skip lines inside wallsFromLLM block
      } else {
        result += line
      }
    }

    result.mkString("\n")
