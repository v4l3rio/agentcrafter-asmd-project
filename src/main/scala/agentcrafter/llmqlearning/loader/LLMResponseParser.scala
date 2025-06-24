package agentcrafter.llmqlearning.loader

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
 * Common utility for parsing and cleaning LLM responses.
 *
 * Provides shared functionality for cleaning LLM decorations, extracting content from various formats, and handling
 * common parsing errors across different LLM-generated content types.
 */
object LLMResponseParser:

  /**
   * Extracts JSON content from LLM response with fallback strategies.
   *
   * @param response
   *   Raw LLM response
   * @return
   *   Try containing cleaned JSON string
   */
  def extractJsonContent(response: String): Try[String] =
    Try {
      val cleaned = cleanLLMDecorations(response)

      // Look for JSON content between ```json and ``` tags first
      val jsonPattern = "```json\\s*\\n([\\s\\S]*?)\\n```".r
      jsonPattern.findFirstMatchIn(response) match
        case Some(m) => m.group(1).trim
        case None =>
          // Fallback: look for any content between ``` tags that looks like JSON
          val genericPattern = "```\\s*\\n([\\s\\S]*?)\\n```".r
          genericPattern.findFirstMatchIn(response) match
            case Some(m) =>
              val content = m.group(1).trim
              if content.startsWith("{") || content.startsWith("[") then content
              else cleaned
            case None => cleaned
    }

  /**
   * Removes common LLM decorations from response strings. Handles markdown code blocks, prefixes, and other common LLM
   * formatting.
   */
  def cleanLLMDecorations(response: String): String =
    val trimmed = response.trim

    // Handle markdown code blocks - remove ```json, ```ascii, and ``` markers
    val withoutCodeBlocks = trimmed
      .replaceAll("```json\\s*", "")
      .replaceAll("```ascii\\s*", "")
      .replaceAll("```\\s*", "")
      .replaceAll("(?i)^json\\s*", "")
      .replaceAll("(?i)^ascii\\s*", "")
      .replaceAll("(?i)^here.*?:\\s*", "")
      .trim

    withoutCodeBlocks

  /**
   * Extracts ASCII content from LLM response with fallback strategies.
   *
   * @param response
   *   Raw LLM response
   * @return
   *   Try containing ASCII art string
   */
  def extractAsciiContent(response: String): Try[String] =
    Try {
      // Look for ASCII content between ```ascii and ``` tags
      val asciiPattern = "```ascii\\s*\\n([\\s\\S]*?)\\n```".r
      asciiPattern.findFirstMatchIn(response) match
        case Some(m) => m.group(1).trim
        case None =>
          // Fallback: look for any content between ``` tags
          val genericPattern = "```\\s*\\n([\\s\\S]*?)\\n```".r
          genericPattern.findFirstMatchIn(response) match
            case Some(m) => m.group(1).trim
            case None =>
              // Last resort: try to find lines that look like ASCII art
              val lines = response.split("\\n")
              val asciiLines = lines.filter(line =>
                line.trim.nonEmpty &&
                line.forall(c => c == '#' || c == '.' || c == ' ')
              )
              if asciiLines.nonEmpty then asciiLines.mkString("\\n")
              else throw new RuntimeException("No ASCII content found in response")
    }

  /**
   * Generic content extraction with custom pattern matching.
   *
   * @param response
   *   Raw LLM response
   * @param patterns
   *   List of regex patterns to try in order
   * @return
   *   Try containing extracted content
   */
  def extractContentWithPatterns(response: String, patterns: List[Regex]): Try[String] =
    Try {
      patterns.foldLeft(Option.empty[String]) { (acc, pattern) =>
        acc.orElse {
          pattern.findFirstMatchIn(response).map(_.group(1).trim)
        }
      }.getOrElse {
        // Fallback to cleaned response
        cleanLLMDecorations(response)
      }
    }

  /**
   * Validates that extracted content is not empty and meets basic criteria.
   *
   * @param content
   *   Extracted content
   * @param contentType
   *   Type description for error messages
   * @return
   *   Try containing validated content
   */
  def validateContent(content: String, contentType: String): Try[String] =
    if content.trim.isEmpty then
      Failure(new RuntimeException(s"Extracted $contentType content is empty"))
    else
      Success(content.trim)
