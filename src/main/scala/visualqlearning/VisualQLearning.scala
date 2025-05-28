package visualqlearning

import scala.swing.{Panel, MainFrame}
import java.awt.{Color, Graphics2D}

import common.*
import common.{Action => RLAction}

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

  /** Aggiorna posizione agente + testo di debug e ridisegna */
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

/* ----------  Driver ---------- */
@main def TrainVisualExplain(): Unit =
  val env       = GridWorld()
  val agent = QAgent(
    env,
    epsStart = 0.9,
    epsMin   = 0.05,
    warmUpEpisodes = 1000,   // 1 000 episodi full-explore
    optimistic     = 5.0     // Q0 ottimistico
  )
  val vis       = Visualiser(env)
  val episodes  = 10_000
  val testEvery = 500

  for ep <- 1 to episodes do
    agent.episode()                              // training
    if ep % testEvery == 0 then
      val (_, _, path) = agent.episode(exploitOnly = true) // greedy run
      path.foreach { case (s, a, e, q) => vis.update(s, a, e, q) }
      println(f"Ep $ep%5d | ε=${agent.eps}%.3f | steps=${path.size}")