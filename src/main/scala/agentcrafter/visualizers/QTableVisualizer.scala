package agentcrafter.marl.visualizers

import agentcrafter.marl.WorldSpec
import agentcrafter.common.{Action, Learner, State}
import scala.swing.*
import java.awt.Font
import javax.swing.table.DefaultTableModel
import javax.swing.JTable

/**
 * Constants for QTableVisualizer UI components
 */
object QTableVisualizerConstants:
  /** Window width */
  val WINDOW_WIDTH: Int = 620
  /** Window height */
  val WINDOW_HEIGHT: Int = 450
  /** Window position offset base */
  val WINDOW_POSITION_OFFSET: Int = 50
  /** Window position X range */
  val WINDOW_POSITION_X_RANGE: Int = 300
  /** Window position Y range */
  val WINDOW_POSITION_Y_RANGE: Int = 200
  /** Table font size */
  val TABLE_FONT_SIZE: Int = 12
  /** Default cell size for visualization */
  val DEFAULT_VISUALIZATION_CELL_SIZE: Int = 60
  /** Scroll pane width */
  val SCROLL_PANE_WIDTH: Int = 600
  /** Scroll pane height */
  val SCROLL_PANE_HEIGHT: Int = 400
  /** Number of action columns (Up, Down, Left, Right, Stay) plus state column */
  val TOTAL_COLUMNS: Int = 6
  /** Action column indices */
  val ACTION_UP_INDEX: Int = 1
  val ACTION_DOWN_INDEX: Int = 2
  val ACTION_LEFT_INDEX: Int = 3
  val ACTION_RIGHT_INDEX: Int = 4
  val ACTION_STAY_INDEX: Int = 5
  /** State column index */
  val STATE_COLUMN_INDEX: Int = 0
  /** Default Q-value for unvisited state-action pairs */
  val DEFAULT_Q_VALUE: Double = 0.0
  /** Initial row count for table model */
  val INITIAL_ROW_COUNT: Int = 0

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
    size = new java.awt.Dimension(QTableVisualizerConstants.WINDOW_WIDTH, QTableVisualizerConstants.WINDOW_HEIGHT)
    location = new java.awt.Point(
      QTableVisualizerConstants.WINDOW_POSITION_OFFSET + agentId.hashCode.abs % QTableVisualizerConstants.WINDOW_POSITION_X_RANGE,
      QTableVisualizerConstants.WINDOW_POSITION_OFFSET + agentId.hashCode.abs % QTableVisualizerConstants.WINDOW_POSITION_Y_RANGE
    )
    visible = true
  }
  private val cellSize = QTableVisualizerConstants.DEFAULT_VISUALIZATION_CELL_SIZE
  private val fontSize = QTableVisualizerConstants.TABLE_FONT_SIZE
  private val tableModel = new DefaultTableModel(
    Array[Object]("State", "Up", "Down", "Left", "Right", "Stay"),
    0
  ) {
    override def isCellEditable(row: Int, column: Int): Boolean = false
  }

  private val table = new JTable(tableModel)
  table.setFont(new Font("Monospaced", Font.PLAIN, QTableVisualizerConstants.TABLE_FONT_SIZE))
  table.getTableHeader.setFont(new Font("Monospaced", Font.BOLD, QTableVisualizerConstants.TABLE_FONT_SIZE))

  private val scrollPane = new ScrollPane {
    contents = scala.swing.Component.wrap(table)
    preferredSize = new java.awt.Dimension(QTableVisualizerConstants.SCROLL_PANE_WIDTH, QTableVisualizerConstants.SCROLL_PANE_HEIGHT)
  }

  /**
   * Updates the Q-table display with the latest values from the learner.
   *
   * This method should be called periodically to refresh the visualization with the most recent Q-values as the agent
   * continues learning.
   */
  def update(): Unit =
    // Clear existing data
    tableModel.setRowCount(QTableVisualizerConstants.INITIAL_ROW_COUNT)

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
      val row = Array.ofDim[String](QTableVisualizerConstants.TOTAL_COLUMNS) // State(r,c), Up, Down, Left, Right, Stay
      row(QTableVisualizerConstants.STATE_COLUMN_INDEX) = s"(${state.x},${state.y})"

      // Fill action values
      for (action <- Action.values) {
        val value = stateActions.getOrElse((state, action), QTableVisualizerConstants.DEFAULT_Q_VALUE)
        val actionIndex = action match {
          case Action.Up => QTableVisualizerConstants.ACTION_UP_INDEX
          case Action.Down => QTableVisualizerConstants.ACTION_DOWN_INDEX
          case Action.Left => QTableVisualizerConstants.ACTION_LEFT_INDEX
          case Action.Right => QTableVisualizerConstants.ACTION_RIGHT_INDEX
          case Action.Stay => QTableVisualizerConstants.ACTION_STAY_INDEX
        }
        row(actionIndex) = f"$value%.2f"
      }
      data += row
    }

    data.toArray
