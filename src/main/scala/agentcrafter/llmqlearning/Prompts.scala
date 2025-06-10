package agentcrafter.llmqlearning

import scala.io.Source
import scala.util.Using

/** Helper object that loads static LLM prompts from `src/main/resources/`. */
object Prompts:
  /**
   * Q‑Table generation prompt, loaded lazily from
   * `src/main/resources/prompts/qtable_prompt.txt` (bundled in the JAR).
   */
  lazy val qTable: String =
    Using.resource(Source.fromResource("prompts/qtable_prompt.txt"))(_.mkString)