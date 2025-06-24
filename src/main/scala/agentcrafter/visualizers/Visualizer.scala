package agentcrafter.marl.visualizers

import agentcrafter.common.{Action, Constants, State}
import agentcrafter.marl.{OpenWall, WorldSpec}

import java.awt.{Color, Graphics2D}
import scala.swing.*

/**
 * Unified visualization component for both single-agent Q-Learning and multi-agent simulations.
 *
 * This class creates a Swing-based GUI that can display:
 *   - Single-agent Q-Learning with detailed debug information (Q-values, actions, exploration mode)
 *   - Multi-agent simulations with reward tracking and episode statistics
 *   - Grid world environments with walls, goals, switches, and dynamic elements
 *   - Real-time agent movement and state updates
 *
 * Features from both visualizers:
 *   - Configurable cell size and update delay
 *   - Support for static and dynamic walls (openable walls)
 *   - Multiple agent visualization with distinct colors
 *   - Comprehensive information display (rewards, episodes, exploration mode)
 *   - Q-value debugging for single-agent scenarios
 *   - Switch/trigger visualization
 *
 * @param windowTitle
 *   Window title for the visualization
 * @param rows
 *   Number of rows in the grid
 * @param cols
 *   Number of columns in the grid
 * @param cell
 *   Size of each grid cell in pixels (default: 60)
 * @param delayMs
 *   Delay between updates in milliseconds (default: 100)
 */
class Visualizer(
  windowTitle: String = "Unified Agent Visualizer",
  rows: Int,
  cols: Int,
  cell: Int = Constants.DEFAULT_VISUALIZATION_CELL_SIZE,
  delayMs: Int = Constants.DEFAULT_STEP_DELAY_MS
):

  val frame: MainFrame =
    new MainFrame:
      title = windowTitle
      contents = panel
      centerOnScreen()
      visible = true
  private val colors = Array(
    Color.blue,
    Color.magenta,
    Color.cyan,
    Color.pink,
    Color.red
  )
  private val panel = new Panel:
    preferredSize = new java.awt.Dimension(cols * cell, rows * cell + Constants.INFO_PANEL_HEIGHT)

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)

      for x <- 0 until cols; y <- 0 until rows do
        val p = State(x, y)
        val screenX = x * cell
        val screenY = y * cell
        val isWall = staticWalls.contains(p) && !openWalls.contains(p)
        g.setColor(if isWall then Color.darkGray else Color.white)
        g.fillRect(screenX, screenY, cell, cell)
        g.setColor(Color.lightGray)
        g.drawRect(screenX, screenY, cell, cell)

      def fillCells(cells: Set[State], color: Color): Unit =
        cells.foreach { p =>
          g.setColor(color)
          g.fillRect(p.x * cell, p.y * cell, cell, cell)
        }

      fillCells(switchCells, Color.yellow)

      fillCells(goalCells, Color.green)

      if isSingleAgent then
        startPos.foreach { s =>
          g.setColor(Color.cyan)
          g.fillRect(s.x * cell, s.y * cell, cell, cell)
        }
        goalPos.foreach { s =>
          g.setColor(Color.green)
          g.fillRect(s.x * cell, s.y * cell, cell, cell)
        }

        singleAgentPos.foreach { pos =>
          g.setColor(Color.blue)
          val m = cell / Constants.AGENT_CIRCLE_MARGIN_DIVISOR
          g.fillOval(
            pos.x * cell + m,
            pos.y * cell + m,
            cell - Constants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m,
            cell - Constants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m
          )
        }
      else

        val m = cell / Constants.AGENT_CIRCLE_MARGIN_DIVISOR
        multiAgentState.zipWithIndex.foreach { case ((id, p), idx) =>
          val col = colors(idx % colors.length)
          g.setColor(col)
          g.fillOval(
            p.x * cell + m,
            p.y * cell + m,
            cell - Constants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m,
            cell - Constants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m
          )
        }

      g.setColor(Color.black)
      val infoY = size.height - Constants.INFO_TEXT_Y_OFFSET

      g.drawString(s"Step: $step", Constants.INFO_TEXT_X_POSITION, infoY)
      g.drawString(s"Episode: $episode", Constants.INFO_TEXT_X_POSITION, infoY + Constants.INFO_TEXT_LINE_SPACING)
      g.drawString(
        s"Mode: ${if explorationMode then "Exploration" else "Exploitation"}",
        Constants.INFO_TEXT_X_POSITION,
        infoY + Constants.INFO_TEXT_LINE_SPACING * 2
      )
      g.drawString(
        s"Episode Reward: ${"%.2f".format(episodeReward)}",
        Constants.INFO_TEXT_X_POSITION,
        infoY + Constants.INFO_TEXT_LINE_SPACING * 3
      )
      g.drawString(
        s"Epsilon: ${"%.3f".format(epsilon)}",
        Constants.INFO_TEXT_X_POSITION,
        infoY + Constants.INFO_TEXT_LINE_SPACING * 4
      )
  private var singleAgentPos: Option[State] = None
  private var startPos: Option[State] = None
  private var goalPos: Option[State] = None
  private var lastQInfo = ""
  private var multiAgentState: Map[String, State] = Map.empty
  private var goalCells: Set[State] = Set.empty
  private var switchCells: Set[State] = Set.empty
  private var staticWalls: Set[State] = Set.empty
  private var openWalls: Set[State] = Set.empty
  private var step = 0
  private var episode = 0
  private var explorationMode = true
  private var episodeReward = 0.0
  private var epsilon = 0.0
  private var isSingleAgent = true

  /**
   * Configure for single-agent Q-Learning visualization
   */
  def configureSingleAgent(start: State, goal: State, walls: Set[State]): Unit =
    isSingleAgent = true
    startPos = Some(start)
    goalPos = Some(goal)
    staticWalls = walls
    singleAgentPos = Some(start)
    goalCells = Set(goal)
    switchCells = Set.empty

  /**
   * Configure for multi-agent simulation visualization
   */
  def configureMultiAgent(spec: WorldSpec): Unit =
    isSingleAgent = false
    staticWalls = spec.staticWalls
    goalCells = spec.agents.map(_.goal).toSet
    switchCells = spec.triggers.collect {
      case t if t.effects.exists(_.isInstanceOf[OpenWall]) => t.at
    }.toSet

  /**
   * Update for single-agent Q-Learning with detailed debug information
   */
  def updateSingleAgent(
    pos: State,
    action: Action,
    explore: Boolean,
    qValues: Array[Double],
    stepNum: Int = 0,
    episodeNum: Int = 0
  ): Unit =
    singleAgentPos = Some(pos)
    explorationMode = explore
    step = stepNum
    episode = episodeNum
    lastQInfo = f"s=(${pos.x},${pos.y})  a=$action  mode=${if explore then "explore" else "exploit"}  " +
      f"Q=[U:${qValues(0)}%.1f  D:${qValues(1)}%.1f  L:${qValues(2)}%.1f  R:${qValues(3)}%.1f]"
    panel.repaint()
    Thread.sleep(delayMs)

  /**
   * Update for multi-agent simulation
   */
  def updateMultiAgent(
    agentStates: Map[String, State],
    openedWalls: Set[State],
    stepNum: Int
  ): Unit =
    multiAgentState = agentStates
    openWalls = openedWalls
    step = stepNum
    panel.repaint()
    Thread.sleep(delayMs)

  /**
   * Update simulation statistics (works for both modes)
   */
  def updateSimulationInfo(
    episodeNum: Int,
    exploration: Boolean,
    epReward: Double,
    eps: Double
  ): Unit =
    episode = episodeNum
    explorationMode = exploration
    episodeReward = epReward
    epsilon = eps

  /**
   * Simple update method for basic position changes
   */
  def updatePosition(pos: State): Unit =
    if isSingleAgent then
      singleAgentPos = Some(pos)
    panel.repaint()
    Thread.sleep(delayMs)

  /**
   * Update walls (for dynamic environments)
   */
  def updateWalls(walls: Set[State], opened: Set[State] = Set.empty): Unit =
    staticWalls = walls
    openWalls = opened
    panel.repaint()

  /**
   * Close the visualization window
   */
  def close(): Unit =
    frame.dispose()
