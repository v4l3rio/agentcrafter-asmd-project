package agentcrafter.marl.visualizers

import agentcrafter.marl.{OpenWall, WorldSpec}
import agentcrafter.common.{Action, State}

import scala.swing.*
import scala.swing.event.*
import java.awt.{Color, Graphics2D}
import javax.swing.Timer

/**
 * Constants for Visualizer UI components
 */
object VisualizerConstants:
  /** Default cell size in pixels */
  val DEFAULT_CELL_SIZE: Int = 60
  /** Default delay between updates in milliseconds */
  val DEFAULT_DELAY_MS: Int = 100
  /** Additional height for info panel */
  val INFO_PANEL_HEIGHT: Int = 100
  /** Agent circle margin factor (cell size divided by this) */
  val AGENT_CIRCLE_MARGIN_FACTOR: Int = 6
  /** Agent circle margin multiplier */
  val AGENT_CIRCLE_MARGIN_MULTIPLIER: Int = 2
  /** Info text Y offset from bottom */
  val INFO_Y_OFFSET: Int = 80
  /** Info text X position */
  val INFO_X_POSITION: Int = 10
  /** Line spacing for info text */
  val INFO_LINE_SPACING: Int = 15
  /** RGB values for orange color */
  val ORANGE_RED: Int = 255
  val ORANGE_GREEN: Int = 140
  val ORANGE_BLUE: Int = 0
  /** RGB values for dark green color */
  val DARK_GREEN_RED: Int = 0
  val DARK_GREEN_GREEN: Int = 128
  val DARK_GREEN_BLUE: Int = 0
  /** RGB values for purple color */
  val PURPLE_RED: Int = 128
  val PURPLE_GREEN: Int = 0
  val PURPLE_BLUE: Int = 128
  /** Update frequency divider for performance */
  val UPDATE_FREQUENCY_DIVIDER: Int = 10

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
  cell: Int = VisualizerConstants.DEFAULT_CELL_SIZE,
  delayMs: Int = VisualizerConstants.DEFAULT_DELAY_MS
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
    new Color(VisualizerConstants.ORANGE_RED, VisualizerConstants.ORANGE_GREEN, VisualizerConstants.ORANGE_BLUE),
    Color.cyan,
    Color.pink,
    new Color(VisualizerConstants.DARK_GREEN_RED, VisualizerConstants.DARK_GREEN_GREEN, VisualizerConstants.DARK_GREEN_BLUE),
    Color.red,
    new Color(VisualizerConstants.PURPLE_RED, VisualizerConstants.PURPLE_GREEN, VisualizerConstants.PURPLE_BLUE)
  )
  private val panel = new Panel:
    preferredSize = new java.awt.Dimension(cols * cell, rows * cell + VisualizerConstants.INFO_PANEL_HEIGHT)

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
          g.fillRect(s.y * cell, s.x * cell, cell, cell)
        }
        goalPos.foreach { s =>
          g.setColor(Color.green)
          g.fillRect(s.y * cell, s.x * cell, cell, cell)
        }

        singleAgentPos.foreach { pos =>
          g.setColor(Color.blue)
          val m = cell / VisualizerConstants.AGENT_CIRCLE_MARGIN_FACTOR
          g.fillOval(pos.y * cell + m, pos.x * cell + m, cell - VisualizerConstants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m, cell - VisualizerConstants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m)
        }
      else

        val m = cell / VisualizerConstants.AGENT_CIRCLE_MARGIN_FACTOR
        multiAgentState.zipWithIndex.foreach { case ((id, p), idx) =>
          val col = colors(idx % colors.length)
          g.setColor(col)
          g.fillOval(p.y * cell + m, p.x * cell + m, cell - VisualizerConstants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m, cell - VisualizerConstants.AGENT_CIRCLE_MARGIN_MULTIPLIER * m)
        }

      g.setColor(Color.black)
      val infoY = size.height - VisualizerConstants.INFO_Y_OFFSET

      g.drawString(s"Step: $step", VisualizerConstants.INFO_X_POSITION, infoY)
      g.drawString(s"Episode: $episode", VisualizerConstants.INFO_X_POSITION, infoY + VisualizerConstants.INFO_LINE_SPACING)
      g.drawString(s"Mode: ${if explorationMode then "Exploration" else "Exploitation"}", VisualizerConstants.INFO_X_POSITION, infoY + VisualizerConstants.INFO_LINE_SPACING * 2)
      g.drawString(s"Episode Reward: ${"%.2f".format(episodeReward)}", VisualizerConstants.INFO_X_POSITION, infoY + VisualizerConstants.INFO_LINE_SPACING * 3)
      g.drawString(s"Epsilon: ${"%.3f".format(epsilon)}", VisualizerConstants.INFO_X_POSITION, infoY + VisualizerConstants.INFO_LINE_SPACING * 4)
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
