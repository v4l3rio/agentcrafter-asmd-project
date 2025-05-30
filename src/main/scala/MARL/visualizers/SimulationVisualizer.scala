package MARL.visualizers

import MARL.{OpenWall, WorldSpec}
import common.State

import java.awt.{Color, Graphics2D}
import scala.swing.{MainFrame, Panel}

/**
 * Visualization component for the DSL simulation
 */
class SimulationVisualizer(spec: WorldSpec, cell: Int = 48, delay: Int = 70):
  private var state: Map[String, State] = Map.empty
  private var openWalls = Set.empty[State]
  private var step = 0

  /* Identify "goal" cells and switch cells */
  private val goalCells = spec.agents.flatMap(_.goal).toSet
  private val switchCells = spec.triggers.collect {
    case t if t.effects.exists(_.isInstanceOf[OpenWall]) => t.at
  }.toSet

  private val colors = Array(
    Color.blue, Color.magenta, new Color(255, 140, 0), Color.cyan,
    Color.pink, Color.green, Color.red, new Color(128, 0, 128)
  )

  private val panel = new Panel:
    preferredSize = new java.awt.Dimension(spec.cols * cell, spec.rows * cell + 25)

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)
      for r <- 0 until spec.rows; c <- 0 until spec.cols do
        val p = State(r, c);
        val x = c * cell;
        val y = r * cell
        val isWall = spec.staticWalls.contains(p) && !openWalls.contains(p)
        g.setColor(if isWall then Color.darkGray else Color.white)
        g.fillRect(x, y, cell, cell)
        g.setColor(Color.lightGray); g.drawRect(x, y, cell, cell)

      /* special cells */
      def fill(ps: Set[State], col: Color): Unit =
        ps.foreach { p => g.setColor(col); g.fillRect(p.c * cell, p.r * cell, cell, cell) }

      fill(switchCells, Color.yellow)
      fill(goalCells, Color.green)

      /* agents */
      val m = cell / 6
      state.zipWithIndex.foreach { case ((id, p), idx) =>
        val col = colors(idx % colors.length)
        g.setColor(col)
        g.fillOval(p.c * cell + m, p.r * cell + m, cell - 2 * m, cell - 2 * m)
      }

      g.setColor(Color.black)
      g.drawString(s"step=$step", 10, size.height - 8)

  val frame: MainFrame = new MainFrame:
    title = "MARL DSL visual"
    contents = panel;
    centerOnScreen();
    visible = true

  def update(st: Map[String, State], open: Set[State], k: Int): Unit =
    state = st;
    openWalls = open;
    step = k
    panel.repaint();
    Thread.sleep(spec.stepDelay)