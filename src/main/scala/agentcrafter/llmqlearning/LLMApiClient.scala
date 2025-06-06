package agentcrafter.llmqlearning

import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.client4.StreamBackend
import scala.concurrent.duration.Duration
import play.api.libs.json._
import scala.util.{Failure, Try, Using}
import scala.io.Source
import java.io.File
import sttp.client4.{Response, quickRequest}
import sttp.model.Uri
import io.github.cdimascio.dotenv.Dotenv

val dotenv: Dotenv =
  Dotenv.configure()
    .ignoreIfMissing()
    .load()

/**
 * Pure‑Scala LLM API client based on **sttp‑client v4** (quick layer).
 *
 * * No external JSON lib – minimal `escapeJson` helper only.
 * * Safer URI construction that always preserves host/port and endpoint path.
 * * Clearer error messages when the connection fails (e.g. server down).
 */
class LLMApiClient(
                    baseUrl: String = "https://api.openai.com",
                    apiKey: String = dotenv.get("OPENAI_API_KEY", "API")
                  ):
  /**
   * Trigger an LLM generation.
   *
   * @param prompt   Extra text to prepend
   * @param model    Model name (default: "gpt-4o")
   * @param stream   Whether to request streaming output
   * @param endpoint API path (default: "/v1/chat/completions")
   */
  def callLLM(
               prompt: String = "",
               model: String = "gpt-4o",
               stream: Boolean = false,
               endpoint: String = "/v1/chat/completions"
             ): Try[String] =
    for
      simContent <- readSimulationAppFile()
      response   <- postRequest(prompt, model, stream, endpoint, simContent)
    yield response


  /**
   * Constructs a full URI by combining the base URL with the endpoint path.
   *
   * @param endpoint The API endpoint path
   * @return Complete URI for the API call
   * @throws IllegalArgumentException if the resulting URI is invalid
   */
  private def fullUri(endpoint: String): Uri =
    val cleanBase   = baseUrl.stripSuffix("/")
    val cleanPath   = endpoint.stripPrefix("/")
    Uri.parse(s"$cleanBase/$cleanPath").getOrElse(throw new IllegalArgumentException(s"Invalid URI: $cleanBase/$cleanPath"))

  /**
   * Performs the actual HTTP POST request to the LLM API.
   *
   * @param prompt The user prompt
   * @param model The LLM model to use
   * @param stream Whether to enable streaming
   * @param endpoint The API endpoint
   * @param simContent The simulation DSL content to include
   * @return Try containing the LLM response or an error
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
         |Simulation DSL content:
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
      val resp: Response[String] = quickRequest
        .post(uri)
        .contentType("application/json")
        .header("Authorization", s"Bearer $apiKey")
        .body(body)
        .readTimeout(Duration.Inf) // wait indefinitely for response
        .send(HttpClientSyncBackend())

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
   * Escapes control characters so the string can be safely embedded in JSON.
   *
   * @param s The string to escape
   * @return JSON-safe escaped string
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
   * Wraps a string in JSON quotes with proper escaping.
   *
   * @param s The string to wrap
   * @return JSON-formatted string with quotes
   */
  private def jsonString(s: String): String = "\"" + escapeJson(s) + "\""

  /**
   * Extracts the `simulation:` block from SimulationApp.scala if present.
   *
   * This method reads the SimulationApp.scala file and extracts any simulation
   * DSL content to include in the LLM prompt for context.
   *
   * @return Try containing the simulation content or an error message
   */
  private def readSimulationAppFile(): Try[String] =
    val simPath = "src/main/scala/agentcrafter/llmqlearning/SimulationApp.scala"
    val file     = File(simPath)

    if file.exists then
      Using(Source.fromFile(file)) { src =>
        val content = src.mkString
        content.indexOf("simulation:") match
          case -1  => "No simulation block found in file"
          case idx => content.substring(idx)
      }
    else Try("SimulationApp.scala file not found")
