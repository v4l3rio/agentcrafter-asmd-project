package DSL.model

import common.{State, Action}

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
                     goal: State,
                     goalReward: Double)

/* ---- World spec ----------------------------------------------------- */
case class WorldSpec(rows: Int,
                     cols: Int,
                     staticWalls: Set[State],
                     triggers: List[Trigger],
                     agents: List[AgentSpec],
                     episodes: Int)