package agentcrafter.llmqlearning

import agentcrafter.common.Action.{Down, Up}
import agentcrafter.common.{Action, QLearner, State}
import play.api.libs.json.*

import scala.util.{Failure, Success, Try}
import scala.collection.mutable

/**
 * Utility for loading Q-Tables from JSON format
 */
object QTableLoader:
  
  /**
   * Loads a Q-Table from JSON string into a QLearner instance
   * 
   * @param jsonString JSON representation of the Q-Table
   * @param learner The QLearner instance to load the Q-Table into
   * @return Try[Unit] indicating success or failure
   */
  def loadQTableFromJson(jsonString: String, learner: QLearner): Try[Unit] = 
    Try {
      // Clean the JSON string by removing markdown code block formatting
      val cleanedJson = cleanJsonString(jsonString)
      val json = Json.parse(cleanedJson)
      
      // Parse JSON and populate Q-Table using the public interface
      json.as[JsObject].fields.foreach { case (stateKey, actionsJson) =>
        // Parse state coordinates from string like "(0, 0)"
        val stateCoords = parseStateCoordinates(stateKey)
        val state = State(stateCoords._1, stateCoords._2)
        
        // Parse actions and their Q-values
        actionsJson.as[JsObject].fields.foreach { case (actionName, qValueJson) =>
          val qValue = qValueJson.as[Double]
          val action = actionName match {
            case "Up" => Up
            case "Down" => Down
            case "Left" => Action.Left
            case "Right" => Action.Right
            case "Stay" => Action.Stay
            case _ => throw new IllegalArgumentException(s"Unknown action: $actionName")
          }
          
          // Use reflection to set Q-values since there's no public setter
          setQValueViaReflection(learner, state, action, qValue)
        }
      }
    }.recoverWith {
      case ex: Exception => 
        Failure(new RuntimeException(s"Failed to load Q-Table from JSON: ${ex.getMessage}", ex))
    }
  
  /**
   * Cleans JSON string by removing markdown code block formatting and other common LLM artifacts
   */
  private def cleanJsonString(jsonString: String): String = {
    val trimmed = jsonString.trim
    
    // Remove markdown code block markers
    val withoutCodeBlocks = if (trimmed.startsWith("```")) {
      val lines = trimmed.split("\n")
      val startIndex = if (lines.head.startsWith("```json") || lines.head == "```") 1 else 0
      val endIndex = lines.lastIndexWhere(_.trim == "```")
      
      if (endIndex > startIndex) {
        lines.slice(startIndex, endIndex).mkString("\n")
      } else {
        // If no closing ```, remove just the opening
        lines.drop(startIndex).mkString("\n")
      }
    } else {
      trimmed
    }
    
    // Additional cleaning for common LLM artifacts
    val cleaned = withoutCodeBlocks
      .trim
      .replaceAll("^[Jj]son\\s*", "") // Remove "json" or "Json" at the beginning
      .replaceAll("^[Hh]ere.*?:\\s*", "") // Remove "Here is the JSON:" type prefixes
      .trim
    
    cleaned
  }
  
  /**
   * Sets a Q-value using reflection to access private methods/fields
   */
  private def setQValueViaReflection(learner: QLearner, state: State, action: Action, value: Double): Unit = 
    try {
      // Try to access the private setQValue method
      val setQValueMethod = learner.getClass.getDeclaredMethod("setQValue", classOf[State], classOf[Action], classOf[Double])
      setQValueMethod.setAccessible(true)
      setQValueMethod.invoke(learner, state, action, value.asInstanceOf[AnyRef])
    } catch {
      case _: Exception =>
        // Fallback: try to access QMap directly
        try {
          val qMapField = learner.getClass.getDeclaredField("QMap")
          qMapField.setAccessible(true)
          val qMap = qMapField.get(learner).asInstanceOf[mutable.Map[(State, Action), Double]]
          qMap((state, action)) = value
        } catch {
          case ex: Exception =>
            throw new RuntimeException(s"Could not set Q-value for state $state, action $action: ${ex.getMessage}")
        }
    }
  
  /**
   * Parses state coordinates from string format "(r, c)"
   */
  private def parseStateCoordinates(stateStr: String): (Int, Int) = 
    val cleaned = stateStr.trim.stripPrefix("(").stripSuffix(")")
    val parts = cleaned.split(",").map(_.trim.toInt)
    (parts(0), parts(1))
  
  /**
   * Converts a Q-Table to JSON format
   * 
   * @param learner The QLearner instance to extract Q-Table from
   * @return JSON string representation of the Q-Table
   */
  def qTableToJson(learner: QLearner): String = 
    val qTable = learner.getQTable
    val grouped = qTable.groupBy(_._1._1) // Group by State
    
    val jsonObject = grouped.map { case (state, stateActions) =>
      val stateKey = s"(${state.r}, ${state.c})"
      val actionsJson = stateActions.map { case ((_, action), qValue) =>
        action.toString -> JsNumber(qValue)
      }.toMap
      stateKey -> JsObject(actionsJson)
    }.toMap
    
    Json.stringify(JsObject(jsonObject))