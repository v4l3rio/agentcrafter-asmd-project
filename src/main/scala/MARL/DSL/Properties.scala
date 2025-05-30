package MARL.DSL

import scala.annotation.targetName
import MARL.builders.{AgentBuilder, SimulationBuilder, TriggerBuilder, WallLineBuilder}

case class SimulationWrapper(var builder: SimulationBuilder)
case class AgentWrapper(var builder: AgentBuilder)
case class LineWallConfig(direction: String, from: (Int, Int), to: (Int, Int))
case class LearnerConfig(
                          var alpha: Double = 0.1,
                          var gamma: Double = 0.9,
                          var eps0: Double = 0.9,
                          var epsMin: Double = 0.15,
                          var warm: Int = 10_000,
                          var optimistic: Double = 0.0
                        )

enum AgentProperty[T]:
  case Name extends AgentProperty[String]
  case Start extends AgentProperty[(Int, Int)]
  case Goal extends AgentProperty[(Int, Int)]
  case Reward extends AgentProperty[Double]
  @targetName("to")
  infix def >>(obj: T)(using agentWrapper:AgentWrapper): AgentBuilder = this match
    case AgentProperty.Name => agentWrapper.builder.name(obj.asInstanceOf[String])
    case AgentProperty.Start =>
      val (r, c) = obj.asInstanceOf[(Int, Int)]
      agentWrapper.builder.start(r,c)
    case AgentProperty.Goal =>
      val (r, c) = obj.asInstanceOf[(Int, Int)]
      agentWrapper.builder.goal(r, c)
    case AgentProperty.Reward => agentWrapper.builder.reward(obj.asInstanceOf[Double])

enum TriggerProperty[T]:
  case OpenWall extends TriggerProperty[(Int, Int)]
  case Give extends TriggerProperty[Double]
  case EndEpisode extends TriggerProperty[Boolean]
  @targetName("to")
  infix def >>(obj: T)(using tb: TriggerBuilder): Unit = this match
    case TriggerProperty.OpenWall =>
      val (r, c) = obj.asInstanceOf[(Int, Int)]
      tb.openWall(r, c)
    case TriggerProperty.Give =>
      val bonus = obj.asInstanceOf[Double]
      tb.give(bonus)
    case TriggerProperty.EndEpisode =>
      val shouldEnd = obj.asInstanceOf[Boolean]
      if shouldEnd then tb.endEpisode()

enum WallProperty[T]:
  case Line extends WallProperty[LineWallConfig]
  case Block extends WallProperty[(Int, Int)]

  @targetName("to")
  infix def >>(obj: T)(using wrapper: SimulationWrapper): Unit = this match
    case WallProperty.Line =>
      val lineConfig = obj.asInstanceOf[LineWallConfig]
      lineConfig.direction match
        case "horizontal" => // Support both spellings
          val minCol = math.min(lineConfig.from._2, lineConfig.to._2)
          val maxCol = math.max(lineConfig.from._2, lineConfig.to._2)
          for col <- minCol to maxCol do
            wrapper.builder = wrapper.builder.wall(lineConfig.from._1, col)
        case "vertical" =>
          val minRow = math.min(lineConfig.from._1, lineConfig.to._1)
          val maxRow = math.max(lineConfig.from._1, lineConfig.to._1)
          for row <- minRow to maxRow do
            wrapper.builder = wrapper.builder.wall(row, lineConfig.from._2)
    case WallProperty.Block =>
      val (r, c) = obj.asInstanceOf[(Int, Int)]
      wrapper.builder = wrapper.builder.wall(r, c)


enum LineProperty[T]:
  case Direction extends LineProperty[String]
  case From extends LineProperty[(Int, Int)]
  case To extends LineProperty[(Int, Int)]

  @targetName("to")
  infix def >>(obj: T)(using lineBuilder: WallLineBuilder): WallLineBuilder = this match
    case LineProperty.Direction =>
      lineBuilder.direction = Some(obj.asInstanceOf[String])
      lineBuilder
    case LineProperty.From =>
      lineBuilder.from = Some(obj.asInstanceOf[(Int, Int)])
      lineBuilder
    case LineProperty.To =>
      lineBuilder.to = Some(obj.asInstanceOf[(Int, Int)])
      lineBuilder

object block:
  @targetName("to")
  infix def >>(position: (Int, Int))(using wrapper: SimulationWrapper): Unit =
    WallProperty.Block >> position

enum LearnerProperty[T]:
  case Alpha extends LearnerProperty[Double]
  case Gamma extends LearnerProperty[Double]
  case Eps0 extends LearnerProperty[Double]
  case EpsMin extends LearnerProperty[Double]
  case Warm extends LearnerProperty[Int]
  case Optimistic extends LearnerProperty[Double]
  @targetName("to")
  infix def >>(obj: T)(using config: LearnerConfig): Unit = this match
    case LearnerProperty.Alpha => config.alpha = obj.asInstanceOf[Double]
    case LearnerProperty.Gamma => config.gamma = obj.asInstanceOf[Double]
    case LearnerProperty.Eps0 => config.eps0 = obj.asInstanceOf[Double]
    case LearnerProperty.EpsMin => config.epsMin = obj.asInstanceOf[Double]
    case LearnerProperty.Warm => config.warm = obj.asInstanceOf[Int]
    case LearnerProperty.Optimistic => config.optimistic = obj.asInstanceOf[Double]