package agentcrafter.MARL

import agentcrafter.common.Learner
import agentcrafter.MARL.visualizers.{QTableVisualizer, Visualizer}

/**
 * Runner: orchestrates MARL simulation execution using decomposed components.
 * Maintains the same API while delegating responsibilities to specialized managers.
 */
class Runner(spec: WorldSpec, showGui: Boolean):
  
  private val agentMap = spec.agents.map(a => a.id -> a).toMap
  private val agentsQL: Map[String, Learner] = agentMap.map { case (id, agentSpec) => id -> agentSpec.learner }
  
  private val episodeManager = new EpisodeManager(spec, agentsQL)
  
  // Direct visualization components
  private var visualizer: Option[Visualizer] = None
  private var qTableVisualizers: List[QTableVisualizer] = List.empty
  private var isVisualizationActive = false
  
  private var totalReward = 0.0
  private var currentEpisode = 0

  /**
   * Initializes visualization if the episode threshold is reached.
   */
  private def maybeInitializeVisualization(episode: Int): Unit =
    if !isVisualizationActive && episode >= spec.showAfter && showGui then
      visualizer = Some(new Visualizer(
        "MARL Simulation", 
        spec.rows, 
        spec.cols, 
        cell = 48, 
        delayMs = spec.stepDelay
      ))
      
      visualizer.foreach(_.configureMultiAgent(spec))
      
      qTableVisualizers = agentsQL.map { case (id, learner) =>
        new QTableVisualizer(id, learner, spec)
      }.toList
      
      isVisualizationActive = true

  /* ---------- single episode ---------------------------------------- */
  private def runEpisode(): Int =
    val result = episodeManager.runEpisode { (state, steps, anyAgentExploring) =>
      if isVisualizationActive then
        visualizer.foreach { viz =>
          viz.updateMultiAgent(state.positions, state.openedWalls, steps)
          val currentEpsilon = agentsQL.values.headOption.map(_.eps).getOrElse(0.0)
          viz.updateSimulationInfo(currentEpisode, anyAgentExploring, state.reward, currentEpsilon)
        }
        
        // Update Q-table visualizers periodically
        if steps % 10 == 0 then
          qTableVisualizers.foreach(_.update())
    }
    
    result.steps

  /* ---------- training cycle ---------------------------------------- */
  def run(): Unit =
    for ep <- 1 to spec.episodes do
      maybeInitializeVisualization(ep)
      episodeManager.resetEpisode()
      currentEpisode = ep
      
      val steps = runEpisode()
      episodeManager.incrementEpisode()
      
      val episodeResult = episodeManager.getCurrentState
      totalReward += episodeResult.reward

      if ep % 1000 == 0 then
        println(s"Episode $ep finished in $steps steps")
