package agentcrafter.marl

import agentcrafter.common.*

/**
 * Manages the execution of individual episodes in a Multi-Agent Reinforcement Learning (marl) simulation.
 *
 * This class orchestrates episode execution by coordinating specialized managers for different concerns:
 * - AgentManager: handles agent actions and Q-learning updates
 * - EnvironmentManager: manages environment state and trigger processing
 * - SimulationState: tracks episode progress and statistics
 *
 * Key responsibilities:
 *   - Orchestrating episode execution flow
 *   - Coordinating between specialized managers
 *   - Managing episode termination conditions
 *
 * @param spec
 *   The complete world specification including agents, triggers, and environment parameters
 */
class EpisodeManager(spec: WorldSpec):

  private val agentManager = new AgentManager(spec)
  private val environmentManager = new EnvironmentManager(spec)
  private val simulationState = new SimulationState()

  /**
   * Executes a complete episode of the marl simulation.
   *
   * This method runs the main episode loop, coordinating agent actions, environment transitions, and Q-learning updates
   * until the episode terminates (either by reaching a goal, hitting the step limit, or through a trigger effect).
   *
   * @param onStep
   *   Optional callback function called after each step for visualization or monitoring purposes. Receives
   *   (EpisodeState, stepNumber, isDone)
   * @return
   *   EpisodeResult containing the final outcome and statistics
   */
  def runEpisode(onStep: (EpisodeState, Int, Boolean) => Unit = (_, _, _) => ()): EpisodeResult =
    var steps = 0

    while !environmentManager.isEpisodeDone && steps < spec.stepLimit do
      val stepResult = executeStep()
      steps += 1
      simulationState.addEpisodeReward(stepResult.totalReward)

      val currentState = simulationState.createEpisodeState(
        agentManager.getPositions, 
        environmentManager.getOpenedWalls, 
        environmentManager.isEpisodeDone
      )
      onStep(currentState, steps, stepResult.anyAgentExploring)

    EpisodeResult(
      steps, 
      simulationState.getEpisodeReward, 
      agentManager.getPositions, 
      environmentManager.getOpenedWalls, 
      environmentManager.isEpisodeDone
    )

  /**
   * Executes a single step in the episode.
   */
  private def executeStep(): EpisodeStepResult =
    val (jointActions, anyAgentExploring) = agentManager.chooseJointActions()

    val (nextPositions, stepRewards) = environmentManager.executeActions(jointActions, agentManager.getPositions)

    val triggerRewards = environmentManager.processTriggers(nextPositions)

    agentManager.updateQLearning(jointActions, stepRewards, triggerRewards, nextPositions)
    agentManager.updatePositions(nextPositions)

    val totalReward = (stepRewards.values.sum + triggerRewards.values.sum)
    EpisodeStepResult(agentManager.getPositions, totalReward, anyAgentExploring)

  /**
   * Gets the agent manager for external access to agent operations.
   */
  def getAgentManager: AgentManager = agentManager

  /**
   * Resets the episode state for a new episode.
   */
  def resetEpisode(): Unit =
    agentManager.resetAgents()
    environmentManager.resetEnvironment()
    simulationState.resetEpisodeReward()

  /**
   * Gets the current state for visualization.
   */
  def getCurrentState: EpisodeState =
    simulationState.createEpisodeState(
      agentManager.getPositions,
      environmentManager.getOpenedWalls,
      environmentManager.isEpisodeDone
    )

  /**
   * Gets the current epsilon value for exploration tracking.
   */
  def getCurrentEpsilon: Double =
    agentManager.getCurrentEpsilon

  /**
   * Increments episode count for all learners.
   */
  def incrementEpisode(): Unit =
    agentManager.incrementEpisode()
