package agentcrafter.MARL.visualizers

import agentcrafter.MARL.WorldSpec
import agentcrafter.common.{Action, Learner}

import java.awt.Font
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import scala.swing.{MainFrame, Panel, ScrollPane}

/**
 * Visualizer for displaying Q-Tables of individual agents in a graphical interface.
 *
 * This class creates a Swing-based table that shows the Q-values for each state-action pair of a specific agent. The
 * table updates dynamically as the agent learns, providing real-time insight into the agent's value function.
 *
 * @param agentId
 *   Unique identifier for the agent whose Q-table is being visualized
 * @param learner
 *   The learning algorithm instance containing the Q-table data
 * @param spec
 *   World specification containing environment details
 */
class QTableVisualizer(agentId: String, learner: Learner, spec: WorldSpec):

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
  private val cellSize = 60
  private val fontSize = 10
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
    preferredSize = new java.awt.Dimension(600, 400) // Leggermente più largo per la colonna Stay
  }

  /**
   * Updates the Q-table display with the latest values from the learner.
   *
   * This method should be called periodically to refresh the visualization with the most recent Q-values as the agent
   * continues learning.
   */
  def update(): Unit =
    // Clear existing data
    tableModel.setRowCount(0)

    // Add new data
    val data = getQTableData
    for (row <- data)
      tableModel.addRow(row.map(_.asInstanceOf[Object]))

    table.repaint()

  /**
   * Extracts Q-table data and formats it for display in the table.
   *
   * @return
   *   A 2D array where each row represents a state and columns represent actions
   */
  private def getQTableData: Array[Array[String]] =
    val qValues = learner.QTableSnapshot
    val data = collection.mutable.ArrayBuffer[Array[String]]()

    // Group by state
    val byState = qValues.groupBy(_._1._1)

    for (state <- byState.keys.toSeq.sortBy(s => (s.x, s.y))) {
      val stateActions = byState(state)
      val row = Array.ofDim[String](6) // State(r,c), Up, Down, Left, Right, Stay
      row(0) = s"(${state.x},${state.y})"

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
