package agentcrafter.llmqlearning

import agentcrafter.common.{Action, QLearner, State}
import play.api.libs.json.*

import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

/**
 * Pure helpers to **serialise / deserialize** a Q‑table carried by a [[QLearner]].
 */
object QTableLoader:
  
  private given actionFormat: Format[Action] = new Format[Action]:
    private val map = Action.values.map(a => a.toString -> a).toMap
    def reads(j: JsValue): JsResult[Action] =
      j.validate[String].flatMap { str =>
        map.get(str) match
          case Some(a) => JsSuccess(a)
          case None    => JsError(s"unknown Action '$str'")
      }
    def writes(a: Action): JsValue = JsString(a.toString)

  private val stateRegex = """\((\d+)\s*,\s*(\d+)\)""".r

  private given stateKeyFormat: Format[State] = new Format[State]:
    def reads(j: JsValue): JsResult[State] = j.validate[String].flatMap {
      case stateRegex(r, c) => JsSuccess(State(r.toInt, c.toInt))
      case other            => JsError(s"invalid state key '$other'")
    }
    def writes(s: State): JsValue = JsString(s"(${s.r}, ${s.c})")

  private given qTableReads: Reads[Map[(State, Action), Double]] = Reads { json =>
    json.validate[Map[State, Map[Action, Double]]].map { nested =>
      nested.flatMap { case (st, actMap) => actMap.view.map { case (a, v) => (st, a) -> v } }
    }
  }

  private given qTableWrites: Writes[Map[(State, Action), Double]] = Writes { map =>
    val grouped: Map[State, Map[Action, Double]] =
      map.groupMapReduce(_._1._1) { case ((_, a), v) => Map(a -> v) }(_ ++ _)
    Json.toJson(grouped)
  }
  

  /** Load a Q‑table (produced by an LLM) into the learner; reflection is hidden here. */
  def loadQTableFromJson(raw: String, learner: QLearner): Try[Unit] =
    for
      cleaned <- Success(stripLlMDecorations(raw))
      table   <- Json.parse(cleaned).validate[Map[(State, Action), Double]].asEither
        .fold(err => Failure(new RuntimeException(err.toString)), Success.apply)
      _       <- inject(learner, table)
    yield ()

  /** Serialise the learner's current Q‑table. */
  def qTableToJson(learner: QLearner): String =
    Json.prettyPrint(Json.toJson(learner.QTableSnapshot))

  private def stripLlMDecorations(s: String): String =
    val noTicks =
      if s.trim.startsWith("```") then s.trim.stripPrefix("```json").stripPrefix("```").stripSuffix("```")
      else s
    noTicks.replaceAll("(?i)^json\\s*", "").replaceAll("(?i)^here.*?:\\s*", "").trim

  private def inject(learner: QLearner, table: Map[(State, Action), Double]) = Try {
    val qField   = learner.getClass.getDeclaredField("Q")
    qField.setAccessible(true)
    val qTable   = qField.get(learner)

    val mapField = qTable.getClass.getDeclaredField("table")
    mapField.setAccessible(true)
    val internal = mapField.get(qTable).asInstanceOf[mutable.Map[(State, Action), Double]]

    internal ++= table  // bulk update
  }.recoverWith { case NonFatal(e) =>
    Failure(new RuntimeException("Failed to inject Q‑values via reflection: " + e.getMessage, e))
  }
