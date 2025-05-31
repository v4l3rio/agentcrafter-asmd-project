package llmqlearning

import sttp.client4.httpclient.HttpClientSyncBackend
import scala.concurrent.duration.Duration
import play.api.libs.json._
import scala.util.{Failure, Success, Try, Using}
import scala.io.Source
import java.io.File
import sttp.client4.{Response, UriContext, quickRequest}
import sttp.model.Uri
import io.github.cdimascio.dotenv.Dotenv

val dotenv = Dotenv.load()

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
   * @param model    Model name (default: "gpt-4")
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

  // ---------------------------------------------------------------------------
  // internals
  // ---------------------------------------------------------------------------

  private def fullUri(endpoint: String): Uri =
    val cleanBase   = baseUrl.stripSuffix("/")
    val cleanPath   = endpoint.stripPrefix("/")
    Uri.parse(s"$cleanBase/$cleanPath").getOrElse(throw new IllegalArgumentException(s"Invalid URI: $cleanBase/$cleanPath"))

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

  /** Escapes control characters so the string can be safely embedded in JSON. */
  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\b", "\\b")
      .replace("\f", "\\f")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")

  private def jsonString(s: String): String = "\"" + escapeJson(s) + "\""

  /**
   * Extracts the `simulation:` block from SimulationApp.scala if present.
   */
  private def readSimulationAppFile(): Try[String] =
    val simPath = "src/main/scala/llmqlearning/SimulationApp.scala"
    val file     = File(simPath)

    if file.exists then
      Using(Source.fromFile(file)) { src =>
        val content = src.mkString
        content.indexOf("simulation:") match
          case -1  => "No simulation block found in file"
          case idx => content.substring(idx)
      }
    else Try("SimulationApp.scala file not found")
