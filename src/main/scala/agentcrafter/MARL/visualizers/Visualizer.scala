package agentcrafter.MARL.visualizers

import agentcrafter.MARL.{OpenWall, WorldSpec}
import agentcrafter.common.{State, Action}

import java.awt.{Color, Graphics2D}
import scala.swing.{MainFrame, Panel}

/**
 * Unified visualization component for both single-agent Q-Learning and multi-agent simulations.
 * 
 * This class creates a Swing-based GUI that can display:
 * - Single-agent Q-Learning with detailed debug information (Q-values, actions, exploration mode)
 * - Multi-agent simulations with reward tracking and episode statistics
 * - Grid world environments with walls, goals, switches, and dynamic elements
 * - Real-time agent movement and state updates
 * 
 * Features from both visualizers:
 * - Configurable cell size and update delay
 * - Support for static and dynamic walls (openable walls)
 * - Multiple agent visualization with distinct colors
 * - Comprehensive information display (rewards, episodes, exploration mode)
 * - Q-value debugging for single-agent scenarios
 * - Switch/trigger visualization
 * 
 * @param windowTitle Window title for the visualization
 * @param rows Number of rows in the grid
 * @param cols Number of columns in the grid
 * @param cell Size of each grid cell in pixels (default: 60)
 * @param delayMs Delay between updates in milliseconds (default: 100)
 */
class Visualizer(
  windowTitle: String = "Unified Agent Visualizer",
  rows: Int,
  cols: Int,
  cell: Int = 60,
  delayMs: Int = 100
):
  // Single-agent state
  private var singleAgentPos: Option[State] = None
  private var startPos: Option[State] = None
  private var goalPos: Option[State] = None
  private var lastQInfo = ""
  
  // Multi-agent state
  private var multiAgentState: Map[String, State] = Map.empty
  private var goalCells: Set[State] = Set.empty
  private var switchCells: Set[State] = Set.empty
  
  // Common state
  private var staticWalls: Set[State] = Set.empty
  private var openWalls: Set[State] = Set.empty
  private var step = 0
  private var episode = 0
  private var explorationMode = true
  private var accumulatedReward = 0.0
  private var totalReward = 0.0
  private var episodeReward = 0.0
  
  // Visualization mode
  private var isSingleAgent = true
  
  private val colors = Array(
    Color.blue, Color.magenta, new Color(255, 140, 0), Color.cyan,
    Color.pink, new Color(0, 128, 0), Color.red, new Color(128, 0, 128)
  )
  
  private val panel = new Panel:
    preferredSize = new java.awt.Dimension(cols * cell, rows * cell + 100)
    
    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      
      // Draw grid and walls
      for r <- 0 until rows; c <- 0 until cols do
        val p = State(r, c)
        val x = c * cell
        val y = r * cell
        val isWall = staticWalls.contains(p) && !openWalls.contains(p)
        g.setColor(if isWall then Color.darkGray else Color.white)
        g.fillRect(x, y, cell, cell)
        g.setColor(Color.lightGray)
        g.drawRect(x, y, cell, cell)
      
      // Draw special cells
      def fillCells(cells: Set[State], color: Color): Unit =
        cells.foreach { p =>
          g.setColor(color)
          g.fillRect(p.c * cell, p.r * cell, cell, cell)
        }
      
      // Draw switch cells (yellow)
      fillCells(switchCells, Color.yellow)
      
      // Draw goal cells (green)
      fillCells(goalCells, Color.green)
      
      // Draw single-agent specific elements
      if isSingleAgent then
        startPos.foreach { s =>
          g.setColor(Color.cyan)
          g.fillRect(s.c * cell, s.r * cell, cell, cell)
        }
        goalPos.foreach { s =>
          g.setColor(Color.green)
          g.fillRect(s.c * cell, s.r * cell, cell, cell)
        }
        
        // Draw single agent
        singleAgentPos.foreach { pos =>
          g.setColor(Color.blue)
          val m = cell / 6
          g.fillOval(pos.c * cell + m, pos.r * cell + m, cell - 2 * m, cell - 2 * m)
        }
      else
        // Draw multiple agents
        val m = cell / 6
        multiAgentState.zipWithIndex.foreach { case ((id, p), idx) =>
          val col = colors(idx % colors.length)
          g.setColor(col)
          g.fillOval(p.c * cell + m, p.r * cell + m, cell - 2 * m, cell - 2 * m)
        }
      
      // Draw information overlay
      g.setColor(Color.black)
      val infoY = size.height - 80
      
      if isSingleAgent then
        // Single-agent Q-learning info
        g.drawString(lastQInfo, 10, infoY + 60)
        g.drawString(s"Episode: $episode", 10, infoY)
        g.drawString(s"Step: $step", 10, infoY + 15)
        g.drawString(s"Mode: ${if explorationMode then "Exploration" else "Exploitation"}", 10, infoY + 30)
        if episodeReward != 0.0 then
          g.drawString(s"Episode Reward: ${"%.2f".format(episodeReward)}", 10, infoY + 45)
      else
        // Multi-agent simulation info
        g.drawString(s"Step: $step", 10, infoY)
        g.drawString(s"Episode: $episode", 10, infoY + 15)
        g.drawString(s"Mode: ${if explorationMode then "Exploration" else "Exploitation"}", 10, infoY + 30)
        g.drawString(s"Episode Reward: ${"%.2f".format(episodeReward)}", 10, infoY + 45)
        g.drawString(s"Total Reward: ${"%.2f".format(totalReward)}", 200, infoY)
        g.drawString(s"Avg Reward: ${if episode > 0 then "%.2f".format(totalReward / episode) else "0.00"}", 200, infoY + 15)
  
  val frame: MainFrame = new MainFrame:
    title = windowTitle
    contents = panel
    centerOnScreen()
    visible = true
  
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
    lastQInfo = f"s=(${pos.r},${pos.c})  a=$action  mode=${if explore then "explore" else "exploit"}  " +
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
    reward: Double,
    totalRew: Double,
    epRew: Double
  ): Unit =
    episode = episodeNum
    explorationMode = exploration
    accumulatedReward = reward
    totalReward = totalRew
    episodeReward = epRew
  
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