package DSL

import scala.annotation.targetName

case class SimulationWrapper(var builder: SimulationBuilder)
case class AgentWrapper(var builder: AgentBuilder)
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

enum WallProperty[T]:
  case Line extends WallProperty[LineWallConfig]
  case Block extends WallProperty[(Int, Int)]
  @targetName("to")
  infix def >>(obj: T)(using wrapper: SimulationWrapper): Unit = this match
    case WallProperty.Line =>
      val lineConfig = obj.asInstanceOf[LineWallConfig]
      lineConfig.direction match
        case "horizontal" | "horizzontal" => // Support both spellings
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

case class LineWallConfig(direction: String, from: (Int, Int), to: (Int, Int))

trait SimulationInternalDSL extends App:
  def simulation(block: SimulationWrapper ?=> Unit) =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block
    wrapper.builder.play()

  def grid(size: (Int, Int))(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.grid(size._1, size._2)
  extension(n: Int) infix def x(other:Int):(Int, Int) = (n, other)

  def asciiWalls(ascii: String)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.wallsFromAscii(ascii.stripMargin)

  def walls(block: SimulationWrapper ?=> Unit)(using wrapper: SimulationWrapper) =
    block

  object line:
    object direction:
      def horizontal: LineDirectionBuilder = LineDirectionBuilder("horizontal")
      def vertical: LineDirectionBuilder = LineDirectionBuilder("vertical")

  object block:
    @targetName("to")
    infix def >>(position: (Int, Int))(using wrapper: SimulationWrapper): Unit =
      WallProperty.Block >> position

  class LineDirectionBuilder(dir: String):
    object from:
      @targetName("to")
      infix def >>(fromPos: (Int, Int)): LineFromBuilder = LineFromBuilder(dir, fromPos)

  class LineFromBuilder(dir: String, fromPos: (Int, Int)):
    object to:
      @targetName("to")
      infix def >>(toPos: (Int, Int))(using wrapper: SimulationWrapper): Unit =
        WallProperty.Line >> LineWallConfig(dir, fromPos, toPos)

  def agent(block: AgentWrapper ?=> Unit)(using wrapper: SimulationWrapper) =
    given agentWrapper: AgentWrapper = AgentWrapper(new AgentBuilder(wrapper.builder))
    block
    wrapper.builder = agentWrapper.builder.end()

  def withLearner(using agentWrapper: AgentWrapper)(block: LearnerConfig ?=> Unit) =
    val config = LearnerConfig() // Configurazione predefinita
    given LearnerConfig = config
    block // Applica le modifiche definite nel blocco
    agentWrapper.builder = agentWrapper.builder.withLearner(
      alpha = config.alpha,
      gamma = config.gamma,
      eps0 = config.eps0,
      epsMin = config.epsMin,
      warm = config.warm,
      optimistic = config.optimistic
    )

  def on(who: String, r: Int, c: Int)(block: TriggerBuilder ?=> Unit)(using wrapper: SimulationWrapper) =
    val tb = wrapper.builder.on(who, r, c)
    given TriggerBuilder = tb
    block

  def openWall(r: Int, c: Int)(using tb: TriggerBuilder): Unit =
    tb.openWall(r, c)

  def endEpisode()(using tb: TriggerBuilder): Unit =
    tb.endEpisode()

  def give(bonus: Double)(using tb: TriggerBuilder): Unit =
    tb.give(bonus)

  def episodes(n: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.episodes(n)

  def steps(n: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.steps(n)

  def showAfter(n: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.showAfter(n)

  def delay(ms: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.delay(ms)

  def withGUI(flag: Boolean)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.withGUI(flag)


object SimulationApp extends SimulationInternalDSL:
  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  simulation:
    grid:
      10 x 10
    walls:
      (line.direction.horizontal.from >> (1, 3)).to >> (1, 5)
      (line.direction.vertical.from >> (1, 3)).to >> (3, 3)
      (line.direction.vertical.from >> (1, 5)).to >> (3, 5)
      (line.direction.horizontal.from >> (3, 3)).to >> (3, 5)
      block >> (2, 4)
    agent:
      Name >> "Runner"
      Start >> (1, 9)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.99
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 1_000
        Optimistic >> 0.5
      Goal >> (2, 4)
      Reward >> 100.0
    on("Runner", 8, 6):
      OpenWall >> (2, 3)
      EndEpisode >> false
      Give >> 30
    episodes(10_000)
    steps(400)
    showAfter(9_000)
    delay(100)
    withGUI(true)

