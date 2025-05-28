package DSL.model

import DSL.QLearner
import common.{Action, State}

/* ----------  Effects -------------------------------------------------------- */
sealed trait Effect
case class OpenWall(pos: State) extends Effect
case object EndEpisode extends Effect
case class Reward(delta: Double) extends Effect

/* ---- Trigger spec --------------------------------------------------- */
case class Trigger(who: String, at: State, effects: List[Effect])

/* ---- Agent spec ----------------------------------------------------- */
case class AgentSpec(id: String,
                     start: State,
                     goal: Option[State],
                     goalReward: Double,
                     learner: QLearner)

/* ---- World spec ----------------------------------------------------- */
case class WorldSpec(rows: Int,
                     cols: Int,
                     staticWalls: Set[State],
                     triggers: List[Trigger],
                     agents: List[AgentSpec],
                     episodes: Int,
                     stepLimit: Int,
                     stepDelay: Int,
                     showAfter: Int)