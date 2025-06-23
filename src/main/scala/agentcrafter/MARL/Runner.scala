package agentcrafter.marl

import agentcrafter.common.*
import agentcrafter.marl.managers.{EpisodeManager, VisualizationManager}


/**
 * Runner: orchestrates marl simulation execution using decomposed components.
 * 
 * This class follows the Single Responsibility Principle by focusing solely on:
 * - Overall simulation orchestration
 * - Coordinating between specialized managers
 * - Managing simulation lifecycle and reporting
 */
class Runner(spec: WorldSpec, showGui: Boolean):

  private val episodeManager = new EpisodeManager(spec)
  private val agentsQL: Map[String, Learner] = episodeManager.getAgentManager.getAgentsQL
  private val visualizationManager = new VisualizationManager(spec, agentsQL)
  private val simulationState = new SimulationState()

  def run(): Unit =
    for ep <- 1 to spec.episodes do
      visualizationManager.maybeInitializeVisualization(ep, showGui)
      episodeManager.resetEpisode()
      simulationState.setCurrentEpisode(ep)

      val steps = runEpisode()
      episodeManager.incrementEpisode()

      val episodeResult = episodeManager.getCurrentState
      simulationState.completeEpisode()

      if ep % Constants.EPISODE_REPORT_FREQUENCY == 0 then
        println(s"Episode $ep finished in $steps steps")

  private def runEpisode(): Int =
    val result = episodeManager.runEpisode { (state, steps, anyAgentExploring) =>
      visualizationManager.updateVisualization(
        state.positions,
        state.openedWalls,
        steps,
        simulationState.getCurrentEpisode,
        anyAgentExploring,
        state.reward,
        episodeManager.getCurrentEpsilon
      )
    }

    result.steps
