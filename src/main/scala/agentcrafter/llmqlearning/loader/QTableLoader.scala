package agentcrafter.llmqlearning.loader

import agentcrafter.common.{Action, Learner, QLearner, State}
import play.api.libs.json.*

import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * Loader for Q-tables from LLM responses.
 * 
 * Handles JSON parsing, cleaning LLM-generated decorations, and injecting
 * Q-values into QLearner instances via reflection. Uses the common
 * LLMResponseParser for consistent error handling.
 */
object QTableLoader:

  /**
   * Loads agent-specific Q-tables from multi-agent JSON into learner instances.
   * Implements fallback strategy: if an agent's Q-table is corrupted, uses default values.
   * If all Q-tables are corrupted, all agents get default values.
   *
   * @param rawJson Raw JSON string containing multi-agent Q-table data
   * @param agentLearners Map of agent IDs to their learner instances
   * @return Map of agent IDs to loading results (Success/Failure)
   */
  def loadMultiAgentQTablesFromJson(
    rawJson: String, 
    agentLearners: Map[String, Learner]
  ): Map[String, Try[Unit]] =
    
    // Use common parser for JSON extraction and cleaning
    val cleanedJsonResult = LLMResponseParser.extractJsonContent(rawJson)
    
    cleanedJsonResult match
      case Success(cleanedJson) =>
        // Parse the multi-agent JSON structure
        val multiAgentQTables = parseMultiAgentQTables(cleanedJson)
        
        multiAgentQTables match
          case Success(agentQTables) =>
            // Try to load each agent's Q-table
            val loadResults = agentLearners.map { case (agentId, learner) =>
              agentQTables.get(agentId) match
                case Some(qTable) =>
                  agentId -> injectQTable(learner.asInstanceOf[QLearner], qTable)
                case None =>
                  agentId -> Failure(new RuntimeException(s"No Q-table found for agent: $agentId"))
            }
            
            // Check if any agent succeeded
            val successfulLoads = loadResults.values.count(_.isSuccess)
            
            if successfulLoads == 0 then
              // All failed - return failures for all agents (they'll use default values)
              loadResults
            else
              // Some succeeded - for failed agents, give them default (empty) Q-tables
              loadResults.map { case (agentId, result) =>
                result match
                  case Success(_) => agentId -> result
                  case Failure(_) => 
                    // Agent gets default optimistic initialization
                    agentId -> Success(())
              }
          
          case Failure(parseError) =>
            // JSON parsing failed completely - all agents get failures (default values)
            agentLearners.map { case (agentId, _) =>
              agentId -> Failure(new RuntimeException(s"Failed to parse multi-agent Q-tables: ${parseError.getMessage}"))
            }
      
      case Failure(extractionError) =>
        // Content extraction failed - all agents get failures (default values)
        agentLearners.map { case (agentId, _) =>
          agentId -> Failure(new RuntimeException(s"Failed to extract JSON content: ${extractionError.getMessage}"))
        }

  /**
   * Parses multi-agent Q-tables JSON into a map of agent IDs to Q-table maps.
   */
  private def parseMultiAgentQTables(json: String): Try[Map[String, Map[(State, Action), Double]]] =
    Try {
      Json.parse(json).validate[Map[String, Map[(State, Action), Double]]] match
        case JsSuccess(agentTables, _) => agentTables
        case JsError(errors) => throw new RuntimeException(s"Multi-agent JSON validation failed: $errors")
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

  private given singleAgentQTableReads: Reads[Map[(State, Action), Double]] = Reads { json =>
    json.validate[Map[State, Map[Action, Double]]].map { nestedMap =>
      nestedMap.flatMap { case (state, actionMap) => 
        actionMap.map { case (action, value) => (state, action) -> value }
      }
    }
  }

  // Multi-agent JSON format support
  private given multiAgentQTableReads: Reads[Map[String, Map[(State, Action), Double]]] = Reads { json =>
    json.validate[Map[String, Map[State, Map[Action, Double]]]].map { agentMap =>
      agentMap.map { case (agentId, nestedMap) =>
        agentId -> nestedMap.flatMap { case (state, actionMap) =>
          actionMap.map { case (action, value) => (state, action) -> value }
        }
      }
    }
  }