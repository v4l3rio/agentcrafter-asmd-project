package agentcrafter.marl.visualizers

import agentcrafter.marl.WorldSpec
import agentcrafter.common.{Action, Learner, State, Constants}
import scala.swing.*
import java.awt.Font
import javax.swing.table.DefaultTableModel
import javax.swing.JTable



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

  private val cellSize = Constants.DEFAULT_VISUALIZATION_CELL_SIZE
  private val fontSize = Constants.QTABLE_FONT_SIZE
  private val tableModel = new DefaultTableModel(
    Array[Object]("State", "Up", "Down", "Left", "Right", "Stay"),
    0
  ) {
    override def isCellEditable(row: Int, column: Int): Boolean = false
  }

  private val table = new JTable(tableModel)
  table.setFont(new Font("Monospaced", Font.PLAIN, Constants.QTABLE_FONT_SIZE))
  table.getTableHeader.setFont(new Font("Monospaced", Font.BOLD, Constants.QTABLE_FONT_SIZE))

  private val scrollPane = new ScrollPane {
    contents = scala.swing.Component.wrap(table)
    preferredSize = new java.awt.Dimension(Constants.QTABLE_SCROLL_PANE_WIDTH, Constants.QTABLE_SCROLL_PANE_HEIGHT)
  }

  val frame: MainFrame = new MainFrame {
    title = s"Q-Table: $agentId"
    contents = scrollPane
    size = new java.awt.Dimension(Constants.QTABLE_WINDOW_WIDTH, Constants.QTABLE_WINDOW_HEIGHT)
    location = new java.awt.Point(
      Constants.WINDOW_POSITION_OFFSET + agentId.hashCode.abs % Constants.WINDOW_POSITION_X_RANGE,
      Constants.WINDOW_POSITION_OFFSET + agentId.hashCode.abs % Constants.WINDOW_POSITION_Y_RANGE
    )
    visible = true
  }

  /**
   * Updates the Q-table display with the latest values from the learner.
   *
   * This method should be called periodically to refresh the visualization with the most recent Q-values as the agent
   * continues learning.
   */
  def update(): Unit =
    // Clear existing data
    tableModel.setRowCount(Constants.QTABLE_INITIAL_ROW_COUNT)

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
      val row = Array.ofDim[String](Constants.QTABLE_TOTAL_COLUMNS) // State(r,c), Up, Down, Left, Right, Stay
      row(Constants.QTABLE_STATE_COLUMN_INDEX) = s"(${state.x},${state.y})"

      // Fill action values
      for (action <- Action.values) {
        val value = stateActions.getOrElse((state, action), Constants.DEFAULT_UNVISITED_Q_VALUE)
        val actionIndex = action match {
          case Action.Up => Constants.QTABLE_ACTION_UP_INDEX
          case Action.Down => Constants.QTABLE_ACTION_DOWN_INDEX
          case Action.Left => Constants.QTABLE_ACTION_LEFT_INDEX
          case Action.Right => Constants.QTABLE_ACTION_RIGHT_INDEX
          case Action.Stay => Constants.QTABLE_ACTION_STAY_INDEX
        }
        row(actionIndex) = f"$value%.2f"
      }
      data += row
    }

    data.toArray
