package agentcrafter.marl.builders

import agentcrafter.marl.dsl.LineWallConfig

/**
 * Builder for configuring wall lines in a Multi-Agent Reinforcement Learning simulation.
 *
 * This class provides a fluent API for setting up wall line properties including direction,
 * starting position, and ending position. Wall lines can be configured to create horizontal
 * or vertical walls between two points in the grid.
 *
 * @param parent
 *   The parent SimulationBuilder that this wall line belongs to
 */
class WallLineBuilder(parent: SimulationBuilder):
  private var direction: Option[String] = None
  private var from: Option[(Int, Int)] = None
  private var to: Option[(Int, Int)] = None

  /**
   * Sets the direction for this wall line.
   *
   * @param dir
   *   The direction of the wall line ("horizontal" or "vertical")
   * @return
   *   This builder instance for method chaining
   */
  def withDirection(dir: String): WallLineBuilder =
    direction = Some(dir)
    this

  /**
   * Sets the starting position for this wall line.
   *
   * @param x
   *   Row coordinate of the starting position
   * @param y
   *   Column coordinate of the starting position
   * @return
   *   This builder instance for method chaining
   */
  def withFrom(x: Int, y: Int): WallLineBuilder =
    from = Some((x, y))
    this

  /**
   * Sets the ending position for this wall line.
   *
   * @param x
   *   Row coordinate of the ending position
   * @param y
   *   Column coordinate of the ending position
   * @return
   *   This builder instance for method chaining
   */
  def withTo(x: Int, y: Int): WallLineBuilder =
    to = Some((x, y))
    this

  /**
   * Checks if all required properties have been set.
   *
   * @return
   *   True if direction, from, and to are all specified
   */
  def isComplete: Boolean =
    direction.isDefined && from.isDefined && to.isDefined

  /**
   * Gets the current direction setting.
   *
   * @return
   *   The direction option
   */
  def getDirection: Option[String] = direction

  /**
   * Gets the current from position setting.
   *
   * @return
   *   The from position option
   */
  def getFrom: Option[(Int, Int)] = from

  /**
   * Gets the current to position setting.
   *
   * @return
   *   The to position option
   */
  def getTo: Option[(Int, Int)] = to

  /**
   * Builds and adds the wall line to the parent simulation builder.
   *
   * @return
   *   The parent SimulationBuilder for method chaining
   * @throws IllegalArgumentException
   *   if direction, from, or to are not specified
   */
  def build(): SimulationBuilder =
    (direction, from, to) match
      case (Some(dir), Some(fromPos), Some(toPos)) =>
        val lineConfig = LineWallConfig(dir, fromPos, toPos)
        dir match
          case "horizontal" =>
            val minCol = math.min(fromPos._2, toPos._2)
            val maxCol = math.max(fromPos._2, toPos._2)
            for col <- minCol to maxCol do
              parent.wall(fromPos._1, col)
          case "vertical" =>
            val minRow = math.min(fromPos._1, toPos._1)
            val maxRow = math.max(fromPos._1, toPos._1)
            for row <- minRow to maxRow do
              parent.wall(row, fromPos._2)
        parent
      case _ => throw new IllegalArgumentException("Wall line must have direction, from, and to specified")
