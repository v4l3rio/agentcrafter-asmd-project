package agentcrafter.MARL.visualizers

import agentcrafter.MARL.WorldSpec
import agentcrafter.common.{Action, QLearner}

import java.awt.Font
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import scala.swing.{MainFrame, Panel, ScrollPane}

/**
 * Visualizer for Q-Tables of individual agents
 */
class QTableVisualizer(agentId: String, learner: QLearner, spec: WorldSpec):

  private val cellSize = 60
  private val fontSize = 10

  // Get all states that have Q-values
  private def getQTableData: Array[Array[String]] = {
    val qValues = learner.QTableSnapshot
    val data = collection.mutable.ArrayBuffer[Array[String]]()

    // Group by state
    val byState = qValues.groupBy(_._1._1)

    for (state <- byState.keys.toSeq.sortBy(s => (s.r, s.c))) {
      val stateActions = byState(state)
      val row = Array.ofDim[String](7) // State(r,c), Up, Down, Left, Right, Stay
      row(0) = s"(${state.r},${state.c})"

      // Fill action values
      for (action <- Action.values) {
        val value = stateActions.getOrElse((state, action), 0.0)
        val actionIndex = action match {
          case Action.Up => 1
          case Action.Down => 2
          case Action.Left => 3
          case Action.Right => 4
          case Action.Stay => 5
        }
        row(actionIndex) = f"$value%.2f"
      }

      data += row
    }

    data.toArray
  }

  private val tableModel = new DefaultTableModel(
    Array[Object]("State", "Up", "Down", "Left", "Right", "Stay"),
    0
  ) {
    override def isCellEditable(row: Int, column: Int): Boolean = false
  }

  private val table = new JTable(tableModel)
  table.setFont(new Font("Monospaced", Font.PLAIN, 12))
  table.getTableHeader.setFont(new Font("Monospaced", Font.BOLD, 12))

  private val scrollPane = new ScrollPane {
    contents = scala.swing.Component.wrap(table)
    preferredSize = new java.awt.Dimension(600, 400)  // Leggermente più largo per la colonna Stay
  }

  val frame: MainFrame = new MainFrame {
    title = s"Q-Table: $agentId"
    contents = scrollPane
    size = new java.awt.Dimension(620, 450)
    location = new java.awt.Point(
      50 + agentId.hashCode.abs % 300,
      50 + agentId.hashCode.abs % 200
    )
    visible = true
  }

  def update(): Unit = {
    // Clear existing data
    tableModel.setRowCount(0)

    // Add new data
    val data = getQTableData
    for (row <- data) {
      tableModel.addRow(row.map(_.asInstanceOf[Object]))
    }

    table.repaint()
  }