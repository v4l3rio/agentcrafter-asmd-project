package agentcrafter.llmqlearning

import agentcrafter.common.{Action, Learner, QLearner, State}
import play.api.libs.json.*

import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * Utility for loading Q-tables from JSON into learner instances.
 * 
 * Handles JSON parsing, cleaning LLM-generated decorations, and injecting
 * Q-values into QLearner instances via reflection.
 */
object QTableLoader:

  /**
   * Loads a Q-table from JSON string into a learner instance.
   *
   * @param rawJson Raw JSON string containing Q-table data
   * @param learner The learner instance to load the Q-table into
   * @return Success if loading succeeded, Failure with error details if it failed
   */
  def loadQTableFromJson(rawJson: String, learner: Learner): Try[Unit] =
    learner match
      case qLearner: QLearner =>
        for
          cleanedJson <- Try(cleanLLMDecorations(rawJson))
          qTable <- parseQTable(cleanedJson)
          _ <- injectQTable(qLearner, qTable)
        yield ()
      case _ => 
        Failure(new IllegalArgumentException("Only QLearner instances are supported"))

  /**
   * Removes common LLM decorations from JSON strings.
   */
  private def cleanLLMDecorations(json: String): String =
    val trimmed = json.trim
    val withoutCodeBlocks = 
      if trimmed.startsWith("```") then
        trimmed
          .stripPrefix("```json")
          .stripPrefix("```")
          .stripSuffix("```")
      else trimmed
    
    withoutCodeBlocks
      .replaceAll("(?i)^json\\s*", "")
      .replaceAll("(?i)^here.*?:\\s*", "")
      .trim

  /**
   * Parses cleaned JSON into a Q-table map.
   */
  private def parseQTable(json: String): Try[Map[(State, Action), Double]] =
    Try {
      Json.parse(json).validate[Map[(State, Action), Double]] match
        case JsSuccess(table, _) => table
        case JsError(errors) => throw new RuntimeException(s"JSON validation failed: $errors")
    }

  /**
   * Injects Q-table values into a QLearner using reflection.
   */
  private def injectQTable(learner: QLearner, qTable: Map[(State, Action), Double]): Try[Unit] =
    Try {
      val qTableField = learner.getClass.getDeclaredField("qTable")
      qTableField.setAccessible(true)
      val qTableInstance = qTableField.get(learner)

      val tableField = qTableInstance.getClass.getDeclaredField("table")
      tableField.setAccessible(true)
      val internalTable = tableField.get(qTableInstance).asInstanceOf[mutable.Map[(State, Action), Double]]

      internalTable ++= qTable
      () // Return Unit explicitly
    }.recoverWith { case NonFatal(e) =>
      Failure(new RuntimeException(s"Failed to inject Q-values via reflection: ${e.getMessage}", e))
    }

  // JSON Format definitions
  private val stateRegex = """\((\d+)\s*,\s*(\d+)\)""".r

  private given Format[Action] = new Format[Action]:
    private val actionMap = Action.values.map(a => a.toString -> a).toMap

    def reads(json: JsValue): JsResult[Action] =
      json.validate[String].flatMap { actionString =>
        actionMap.get(actionString) match
          case Some(action) => JsSuccess(action)
          case None => JsError(s"Unknown action: '$actionString'")
      }

    def writes(action: Action): JsValue = JsString(action.toString)

  private given Format[State] = new Format[State]:
    def reads(json: JsValue): JsResult[State] = 
      json.validate[String].flatMap {
        case stateRegex(x, y) => JsSuccess(State(x.toInt, y.toInt))
        case invalid => JsError(s"Invalid state format: '$invalid'")
      }

    def writes(state: State): JsValue = JsString(s"(${state.x}, ${state.y})")

  private given Reads[Map[(State, Action), Double]] = Reads { json =>
    json.validate[Map[State, Map[Action, Double]]].map { nestedMap =>
      nestedMap.flatMap { case (state, actionMap) => 
        actionMap.map { case (action, value) => (state, action) -> value }
      }
    }
  }
