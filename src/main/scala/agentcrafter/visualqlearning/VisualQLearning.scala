package agentcrafter.visualqlearning

import agentcrafter.common.{GridWorld, QLearner, State, Action as RLAction}

import scala.swing.{MainFrame, Panel}
import java.awt.{Color, Graphics2D}

/**
 * Real-time visualization component for Q-Learning training in grid worlds.
 * 
 * This class creates a Swing-based GUI that displays the grid world environment
 * and shows the agent's movement in real-time during training. It visualizes:
 * - The grid layout with walls, start, and goal positions
 * - The current agent position
 * - Debug information including state, action, exploration mode, and Q-values
 * 
 * @param env The grid world environment to visualize
 * @param cell Size of each grid cell in pixels (default: 80)
 * @param delayMs Delay between updates in milliseconds (default: 120) 
 * @example
 * {{{
 * val env = GridWorld()
 * val vis = Visualiser(env, cell = 60, delayMs = 100)
 * // Use vis.update() to show agent movements during training
 * }}}
 */
class Visualiser(env: GridWorld, cell: Int = 80, delayMs: Int = 120):
  private var agent   = env.start
  private var lastInfo = ""

  private val panel = new Panel:
    preferredSize = new java.awt.Dimension(env.cols * cell,
      env.rows * cell + 20)

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)

      // griglia e muri
      for r <- 0 until env.rows; c <- 0 until env.cols do
        val x = c * cell; val y = r * cell
        g.setColor(if env.walls contains State(r, c)
        then Color.darkGray else Color.white)
        g.fillRect(x, y, cell, cell)
        g.setColor(Color.lightGray); g.drawRect(x, y, cell, cell)

      // start e goal
      def drawCell(s: State, col: Color): Unit =
        g.setColor(col); g.fillRect(s.c * cell, s.r * cell, cell, cell)
      drawCell(env.start, Color.cyan)
      drawCell(env.goal , Color.green)

      // agente
      g.setColor(Color.blue)
      val m = cell / 6
      g.fillOval(agent.c * cell + m, agent.r * cell + m,
        cell - 2 * m, cell - 2 * m)

      // overlay testuale (usa size, NO panel.size)
      g.setColor(Color.black)
      g.drawString(lastInfo, 10, size.height - 5)

  val frame = new MainFrame:
    title     = "Q-Learning live debug"
    contents  = panel
    centerOnScreen()
    visible   = true

  /**
   * Updates the visualization with the agent's current state and debug information.
   * 
   * This method updates the agent's position, displays debug information about
   * the current state, action, exploration mode, and Q-values, then repaints
   * the panel and introduces a delay for smooth visualization.
   * 
   * @param pos The current state/position of the agent
   * @param act The action taken by the agent
   * @param explore Whether the agent is in exploration mode
   * @param q Array of Q-values for all actions in the current state
   */
  def update(pos: State,
             act: RLAction,
             explore: Boolean,
             q: Array[Double]): Unit =
    agent = pos
    lastInfo =
      f"s=(${pos.r},${pos.c})  a=$act  mode=${if explore then "explore" else "exploit"}  " +
        f"Q=[U:${q(0)}%.1f  D:${q(1)}%.1f  L:${q(2)}%.1f  R:${q(3)}%.1f]"
    panel.repaint()
    Thread.sleep(delayMs)

/**
 * Main training program with real-time visualization.
 * 
 * This program trains a Q-learning agent on a grid world while providing
 * real-time visual feedback. The visualization shows the agent's movement
 * and decision-making process during greedy policy evaluation episodes.
 * 
 * Training configuration:
 * - High initial exploration (ε₀ = 0.9)
 * - Warm-up period of 1,000 episodes with full exploration
 * - Optimistic initialization (Q₀ = 5.0)
 * - Visual evaluation every 500 episodes
 * 
 * The visualization displays:
 * - Grid world layout with walls, start, and goal
 * - Agent position and movement
 * - Current state, action, exploration mode, and Q-values
 */
@main def TrainVisualExplain(): Unit =
  val env       = GridWorld()
  val agent = QLearner(
    eps0 = 0.95,
    epsMin   = 0.05,
    warm = 1000,   // 1 000 episodi full-explore
    optimistic     = 5.0,     // Q0 ottimistico
    gridEnv = env
  )
  val vis       = Visualiser(env)
  val episodes  = 10_000
  val testEvery = 500

  for ep <- 1 to episodes do
    agent.episode()                              // training
    if ep % testEvery == 0 then
      val (_, _, path) = agent.episode() // greedy run
      path.foreach { case (s, a, e, q) => vis.update(s, a, e, q) }
      println(f"Ep $ep%5d | ε=${agent.eps}%.3f | steps=${path.size}")