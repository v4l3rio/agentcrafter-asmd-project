package agentcrafter.marl.dsl

import agentcrafter.marl.builders.{AgentBuilder, SimulationBuilder, TriggerBuilder, WallLineBuilder}

import scala.annotation.targetName

/**
 * Wrapper class for SimulationBuilder used in DSL context.
 *
 * This wrapper provides a mutable reference to a SimulationBuilder instance
 * that can be modified through DSL operations.
 *
 * @param builder
 *   The SimulationBuilder instance being wrapped
 */
case class SimulationWrapper(var builder: SimulationBuilder)

/**
 * Wrapper class for AgentBuilder used in DSL context.
 *
 * This wrapper provides a mutable reference to an AgentBuilder instance
 * that can be modified through DSL operations.
 *
 * @param builder
 *   The AgentBuilder instance being wrapped
 */
case class AgentWrapper(var builder: AgentBuilder)

/**
 * Configuration for creating wall lines in the simulation grid.
 *
 * @param direction
 *   The orientation of the wall line ("horizontal" or "vertical")
 * @param from
 *   Starting coordinates (row, column) of the wall line
 * @param to
 *   Ending coordinates (row, column) of the wall line
 */
case class LineWallConfig(direction: String, from: (Int, Int), to: (Int, Int))

/**
 * Configuration parameters for Q-learning algorithms in the DSL context.
 *
 * This class provides mutable configuration parameters that can be modified
 * through the DSL syntax during agent configuration.
 *
 * @param alpha
 *   Learning rate (0.0 to 1.0) - controls how much new information overrides old information
 * @param gamma
 *   Discount factor (0.0 to 1.0) - determines the importance of future rewards
 * @param eps0
 *   Initial exploration rate for epsilon-greedy policy
 * @param epsMin
 *   Minimum exploration rate after warm-up period
 * @param warm
 *   Number of episodes for the warm-up period before epsilon decay begins
 * @param optimistic
 *   Initial optimistic value for unvisited state-action pairs
 */
case class LearnerConfig(
  var alpha: Double = 0.1,
  var gamma: Double = 0.9,
  var eps0: Double = 0.9,
  var epsMin: Double = 0.15,
  var warm: Int = 10_000,
  var optimistic: Double = 0.0,
)

/**
 * Configuration parameters for LLM-generated wall creation.
 *
 * This class provides mutable configuration parameters for generating
 * walls using Large Language Models through the DSL syntax.
 *
 * @param model
 *   The LLM model identifier to use for wall generation
 * @param prompt
 *   The prompt template for requesting wall layouts from the LLM
 */
case class WallLLMConfig(
  var model: String = "",
  var prompt: String = ""
)

/**
 * DSL properties for configuring simulation-wide parameters.
 *
 * This enumeration provides type-safe property setters for simulation configuration
 * through the DSL syntax. Each case corresponds to a specific simulation parameter
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum SimulationProperty[T]:
  /** Step penalty applied to agents for each action taken */
  case Penalty extends SimulationProperty[Double]
  /** Total number of episodes to run in the simulation */
  case Episodes extends SimulationProperty[Int]
  /** Maximum number of steps allowed per episode */
  case Steps extends SimulationProperty[Int]
  /** Number of episodes after which to show the GUI visualization */
  case ShowAfter extends SimulationProperty[Int]
  /** Delay in milliseconds between simulation steps for visualization */
  case Delay extends SimulationProperty[Int]
  /** Whether to enable the graphical user interface */
  case WithGUI extends SimulationProperty[Boolean]

  @targetName("to")
  infix def >>(obj: T)(using wrapper: SimulationWrapper): Unit = this match
    case SimulationProperty.Penalty => wrapper.builder = wrapper.builder.stepPenalty(obj.asInstanceOf[Double])
    case SimulationProperty.Episodes => wrapper.builder = wrapper.builder.episodes(obj.asInstanceOf[Int])
    case SimulationProperty.Steps => wrapper.builder = wrapper.builder.steps(obj.asInstanceOf[Int])
    case SimulationProperty.ShowAfter => wrapper.builder = wrapper.builder.showAfter(obj.asInstanceOf[Int])
    case SimulationProperty.Delay => wrapper.builder = wrapper.builder.delay(obj.asInstanceOf[Int])
    case SimulationProperty.WithGUI => wrapper.builder = wrapper.builder.withGUI(obj.asInstanceOf[Boolean])

/**
 * DSL properties for configuring individual agent parameters.
 *
 * This enumeration provides type-safe property setters for agent configuration
 * through the DSL syntax. Each case corresponds to a specific agent parameter
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum AgentProperty[T]:
  /** Unique identifier name for the agent */
  case Name extends AgentProperty[String]
  /** Starting position coordinates (row, column) for the agent */
  case Start extends AgentProperty[(Int, Int)]
  /** Goal position coordinates (row, column) for the agent */
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

/**
 * DSL properties for configuring trigger effects.
 *
 * This enumeration provides type-safe property setters for trigger configuration
 * through the DSL syntax. Each case corresponds to a specific trigger effect
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum TriggerProperty[T]:
  /** Opens a wall at the specified coordinates (row, column) */
  case OpenWall extends TriggerProperty[(Int, Int)]
  /** Gives a bonus reward to the triggering agent */
  case Give extends TriggerProperty[Double]
  /** Ends the current episode when triggered */
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

/**
 * DSL properties for configuring wall creation in the simulation.
 *
 * This enumeration provides type-safe property setters for wall configuration
 * through the DSL syntax. Each case corresponds to a specific wall creation method
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum WallProperty[T]:
  /** Creates a line of walls using LineWallConfig specification */
  case Line extends WallProperty[LineWallConfig]
  /** Creates a single wall block at specified coordinates */
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

/**
 * DSL properties for configuring wall line creation parameters.
 *
 * This enumeration provides type-safe property setters for wall line configuration
 * through the DSL syntax. Each case corresponds to a specific line parameter
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum LineProperty[T]:
  /** Direction of the wall line ("horizontal" or "vertical") */
  case Direction extends LineProperty[String]
  /** Starting coordinates (row, column) of the wall line */
  case From extends LineProperty[(Int, Int)]
  /** Ending coordinates (row, column) of the wall line */
  case To extends LineProperty[(Int, Int)]

  @targetName("to")
  infix def >>(obj: T)(using lineBuilder: WallLineBuilder): WallLineBuilder =
    this match
      case LineProperty.Direction =>
        lineBuilder.withDirection(obj.asInstanceOf[String])
      case LineProperty.From =>
        val (x, y) = obj.asInstanceOf[(Int, Int)]
        lineBuilder.withFrom(x, y)
      case LineProperty.To =>
        val (x, y) = obj.asInstanceOf[(Int, Int)]
        lineBuilder.withTo(x, y)

object block:
  @targetName("to")
  infix def >>(position: (Int, Int))(using wrapper: SimulationWrapper): Unit =
    WallProperty.Block >> position

/**
 * DSL properties for configuring Q-learning algorithm parameters.
 *
 * This enumeration provides type-safe property setters for learner configuration
 * through the DSL syntax. Each case corresponds to a specific learning parameter
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum LearnerProperty[T]:
  /** Learning rate (0.0 to 1.0) - controls how much new information overrides old information */
  case Alpha extends LearnerProperty[Double]
  /** Discount factor (0.0 to 1.0) - determines the importance of future rewards */
  case Gamma extends LearnerProperty[Double]
  /** Initial exploration rate for epsilon-greedy policy */
  case Eps0 extends LearnerProperty[Double]
  /** Minimum exploration rate after warm-up period */
  case EpsMin extends LearnerProperty[Double]
  /** Number of episodes for the warm-up period before epsilon decay begins */
  case Warm extends LearnerProperty[Int]
  /** Initial optimistic value for unvisited state-action pairs */
  case Optimistic extends LearnerProperty[Double]

  @targetName("to")
  infix def >>(obj: T)(using config: LearnerConfig): Unit = this match
    case LearnerProperty.Alpha => config.alpha = obj.asInstanceOf[Double]
    case LearnerProperty.Gamma => config.gamma = obj.asInstanceOf[Double]
    case LearnerProperty.Eps0 => config.eps0 = obj.asInstanceOf[Double]
    case LearnerProperty.EpsMin => config.epsMin = obj.asInstanceOf[Double]
    case LearnerProperty.Warm => config.warm = obj.asInstanceOf[Int]
    case LearnerProperty.Optimistic => config.optimistic = obj.asInstanceOf[Double]

/**
 * DSL properties for configuring LLM-generated wall creation.
 *
 * This enumeration provides type-safe property setters for LLM wall generation
 * configuration through the DSL syntax. Each case corresponds to a specific
 * LLM configuration parameter.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum WallLLMProperty[T]:
  /** The LLM model identifier to use for wall generation */
  case Model extends WallLLMProperty[String]
  /** The prompt template for requesting wall layouts from the LLM */
  case Prompt extends WallLLMProperty[String]

  @targetName("to")
  infix def >>(obj: T)(using config: WallLLMConfig): Unit = this match
    case WallLLMProperty.Model => config.model = obj.asInstanceOf[String]
    case WallLLMProperty.Prompt => config.prompt = obj.asInstanceOf[String]
