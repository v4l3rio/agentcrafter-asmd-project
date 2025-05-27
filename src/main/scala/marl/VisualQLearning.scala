package marl

import scala.swing.*
import java.awt.{Color, Graphics2D}

/* ---------- 4.  Visualiser ---------- */
class Visualiser(env: GridWorld, cell: Int = 80, delayMs: Int = 100):
  private var agentPos: State = env.start

  private val panel = new Panel:
    preferredSize = new java.awt.Dimension(env.cols*cell, env.rows*cell)
    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      for r <- 0 until env.rows do
        for c <- 0 until env.cols do
          val x = c * cell
          val y = r * cell
          g.setColor(env.walls.contains(State(r,c)) match
            case true  => Color.darkGray
            case false => Color.white
          )
          g.fillRect(x, y, cell, cell)
          g.setColor(Color.lightGray)
          g.drawRect(x, y, cell, cell)

      // start & goal
      def drawCell(s: State, color: Color): Unit =
        g.setColor(color); g.fillRect(s.c*cell, s.r*cell, cell, cell)

      drawCell(env.start, Color.cyan)
      drawCell(env.goal,  Color.green)

      // agent
      g.setColor(Color.blue)
      val margin = cell/6
      g.fillOval(agentPos.c*cell+margin, agentPos.r*cell+margin,
        cell-2*margin, cell-2*margin)

  val frame = new MainFrame:
    title = "Q-Learning GridWorld"
    contents = panel
    centerOnScreen()
    visible = true

  /** update agent location + repaint, then sleep a bit */
  def update(pos: State): Unit =
    agentPos = pos
    panel.repaint()
    Thread.sleep(delayMs)

/* ---------- 5.  Driver with animation ---------- */
@main def TrainVisual(): Unit =
  val env      = GridWorld()
  val agent    = QAgent(env)
  val vis      = Visualiser(env)
  val episodes = 10_000
  val testEvery = 500

  for ep <- 1 to episodes do
    agent.episode()                         // ← learning

    if ep % testEvery == 0 then
      // run a greedy episode just for the camera
      val (_, _, path) = agent.episode(exploitOnly = true)
      path.foreach { s => vis.update(s) }   // animation
      println(f"Episode $ep%6d | greedy path length = ${path.size}%d | ε = ${agent.eps}%.3f")