package agentcrafter.marl.dsl

import agentcrafter.marl.builders.WallLineBuilder
import scala.annotation.targetName

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
