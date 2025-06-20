package agentcrafter.marl.dsl

import agentcrafter.marl.builders.{AgentBuilder, SimulationBuilder, TriggerBuilder, WallLineBuilder}

import scala.annotation.targetName

case class SimulationWrapper(var builder: SimulationBuilder)

case class AgentWrapper(var builder: AgentBuilder)

case class LineWallConfig(direction: String, from: (Int, Int), to: (Int, Int))

case class LearnerConfig(
  var alpha: Double = 0.1,
  var gamma: Double = 0.9,
  var eps0: Double = 0.9,
  var epsMin: Double = 0.15,
  var warm: Int = 10_000,
  var optimistic: Double = 0.0,
  var learnerType: String = "qlearner"
)

case class WallLLMConfig(
  var model: String = "",
  var prompt: String = ""
)

enum SimulationProperty[T]:
  case Penalty extends SimulationProperty[Double]
  case Episodes extends SimulationProperty[Int]
  case Steps extends SimulationProperty[Int]
  case ShowAfter extends SimulationProperty[Int]
  case Delay extends SimulationProperty[Int]
  case WithGUI extends SimulationProperty[Boolean]

  @targetName("to")
  infix def >>(obj: T)(using wrapper: SimulationWrapper): Unit = this match
    case SimulationProperty.Penalty => wrapper.builder = wrapper.builder.stepPenalty(obj.asInstanceOf[Double])
    case SimulationProperty.Episodes => wrapper.builder = wrapper.builder.episodes(obj.asInstanceOf[Int])
    case SimulationProperty.Steps => wrapper.builder = wrapper.builder.steps(obj.asInstanceOf[Int])
    case SimulationProperty.ShowAfter => wrapper.builder = wrapper.builder.showAfter(obj.asInstanceOf[Int])
    case SimulationProperty.Delay => wrapper.builder = wrapper.builder.delay(obj.asInstanceOf[Int])
    case SimulationProperty.WithGUI => wrapper.builder = wrapper.builder.withGUI(obj.asInstanceOf[Boolean])

enum AgentProperty[T]:
  case Name extends AgentProperty[String]
  case Start extends AgentProperty[(Int, Int)]
  case Goal extends AgentProperty[(Int, Int)]

  @targetName("to")
  infix def >>(obj: T)(using agentWrapper: AgentWrapper): AgentBuilder = this match
    case AgentProperty.Name => agentWrapper.builder.name(obj.asInstanceOf[String])
    case AgentProperty.Start =>
      val (x, y) = obj.asInstanceOf[(Int, Int)]
      agentWrapper.builder.start(x, y)
    case AgentProperty.Goal =>
      val (x, y) = obj.asInstanceOf[(Int, Int)]
      agentWrapper.builder.goal(x, y)

enum TriggerProperty[T]:
  case OpenWall extends TriggerProperty[(Int, Int)]
  case Give extends TriggerProperty[Double]
  case EndEpisode extends TriggerProperty[Boolean]

  @targetName("to")
  infix def >>(obj: T)(using tb: TriggerBuilder): Unit = this match
    case TriggerProperty.OpenWall =>
      val (x, y) = obj.asInstanceOf[(Int, Int)]
      tb.openWall(x, y)
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
        case "horizontal" =>
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
      val (x, y) = obj.asInstanceOf[(Int, Int)]
      wrapper.builder = wrapper.builder.wall(x, y)

enum LineProperty[T]:
  case Direction extends LineProperty[String]
  case From extends LineProperty[(Int, Int)]
  case To extends LineProperty[(Int, Int)]

  @targetName("to")
  infix def >>(obj: T)(using lineBuilder: WallLineBuilder): WallLineBuilder =
    this match
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
  case LearnerType extends LearnerProperty[String]

  @targetName("to")
  infix def >>(obj: T)(using config: LearnerConfig): Unit = this match
    case LearnerProperty.Alpha => config.alpha = obj.asInstanceOf[Double]
    case LearnerProperty.Gamma => config.gamma = obj.asInstanceOf[Double]
    case LearnerProperty.Eps0 => config.eps0 = obj.asInstanceOf[Double]
    case LearnerProperty.EpsMin => config.epsMin = obj.asInstanceOf[Double]
    case LearnerProperty.Warm => config.warm = obj.asInstanceOf[Int]
    case LearnerProperty.Optimistic => config.optimistic = obj.asInstanceOf[Double]
    case LearnerProperty.LearnerType => config.learnerType = obj.asInstanceOf[String]

enum WallLLMProperty[T]:
  case Model extends WallLLMProperty[String]
  case Prompt extends WallLLMProperty[String]

  @targetName("to")
  infix def >>(obj: T)(using config: WallLLMConfig): Unit = this match
    case WallLLMProperty.Model => config.model = obj.asInstanceOf[String]
    case WallLLMProperty.Prompt => config.prompt = obj.asInstanceOf[String]
