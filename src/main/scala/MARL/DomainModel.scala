package MARL

import common.QLearner
import common.{Action, State}

sealed trait Effect
case class OpenWall(pos: State) extends Effect
case object EndEpisode extends Effect
case class Reward(delta: Double) extends Effect

case class Trigger(who: String, at: State, effects: List[Effect])

case class AgentSpec(id: String,
                     start: State,
                     goal: Option[State],
                     goalReward: Double,
                     learner: QLearner)

case class WorldSpec(rows: Int,
                     cols: Int,
                     staticWalls: Set[State],
                     triggers: List[Trigger],
                     agents: List[AgentSpec],
                     episodes: Int,
                     stepLimit: Int,
                     stepDelay: Int,
                     showAfter: Int)