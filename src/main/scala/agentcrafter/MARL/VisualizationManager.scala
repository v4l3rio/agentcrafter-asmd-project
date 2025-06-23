package agentcrafter.marl

import agentcrafter.common.*
import agentcrafter.marl.visualizers.{QTableVisualizer, Visualizer}

/**
 * Manages visualization components for the multi-agent simulation.
 * 
 * This class follows the Single Responsibility Principle by focusing solely on:
 * - Visualization initialization and lifecycle
 * - Coordinating between main visualizer and Q-table visualizers
 * - Managing visualization state and updates
 */
class VisualizationManager(spec: WorldSpec, agentsQL: Map[String, Learner]):

  private var visualizer: Option[Visualizer] = None
  private var qTableVisualizers: List[QTableVisualizer] = List.empty
  private var isVisualizationActive = false

  /**
   * Checks if visualization is currently active.
   */
  def isActive: Boolean = isVisualizationActive

  /**
   * Initializes visualization if the episode threshold is reached.
   */
  def maybeInitializeVisualization(episode: Int, showGui: Boolean): Unit =
    if !isVisualizationActive && episode >= spec.showAfter && showGui then
      visualizer = Some(new Visualizer(
        "marl Simulation",
        spec.rows,
        spec.cols,
        cell = Constants.DEFAULT_VISUALIZATION_CELL_SIZE,
        delayMs = spec.stepDelay
      ))

      visualizer.foreach(_.configureMultiAgent(spec))

      qTableVisualizers = agentsQL.map { case (id, learner) =>
        new QTableVisualizer(id, learner, spec)
      }.toList

      isVisualizationActive = true

  /**
   * Updates the visualization with current episode state.
   */
  def updateVisualization(
    positions: Map[String, State],
    openedWalls: Set[State],
    steps: Int,
    currentEpisode: Int,
    anyAgentExploring: Boolean,
    reward: Double,
    currentEpsilon: Double
  ): Unit =
    if isVisualizationActive then
      visualizer.foreach { viz =>
        viz.updateMultiAgent(positions, openedWalls, steps)
        viz.updateSimulationInfo(currentEpisode, anyAgentExploring, reward, currentEpsilon)
      }

      if steps % Constants.QTABLE_UPDATE_FREQUENCY == 0 then
        qTableVisualizers.foreach(_.update())