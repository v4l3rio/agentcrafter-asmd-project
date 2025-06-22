package agentcrafter.marl

import agentcrafter.common.*

import scala.collection.mutable

/**
 * Manages the execution of individual episodes in a Multi-Agent Reinforcement Learning (marl) simulation.
 *
 * This class orchestrates the complex interactions between multiple learning agents in a shared environment, handling
 * state transitions, trigger activations, dynamic environment changes, and coordinated Q-learning updates. It maintains
 * the episode state and ensures proper synchronization between agents while managing environmental effects and rewards.
 *
 * Key responsibilities:
 *   - Coordinating simultaneous agent actions
 *   - Managing dynamic environment changes (wall removal, triggers)
 *   - Applying environmental effects and bonus rewards
 *   - Tracking episode termination conditions
 *   - Facilitating Q-learning updates for all agents
 *
 * @param spec
 *   The complete world specification including agents, triggers, and environment parameters
 * @param agentsQL
 *   Map of agent IDs to their respective Q-learning instances
 */
class EpisodeManager(spec: WorldSpec, agentsQL: Map[String, Learner]):

  private val staticWalls = spec.staticWalls.to(mutable.Set)
  private val dynamicWalls = mutable.Set.empty[State]
  private val agentMap = spec.agents.map(a => a.id -> a).toMap
  private var episodeDone = false
  private var activeTriggers: List[Trigger] = spec.triggers
  private var agentPositions = agentMap.view.mapValues(_.start).toMap

  private var episodeReward = 0.0

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

    while !episodeDone && steps < spec.stepLimit do
      val stepResult = executeStep()
      steps += 1
      episodeReward += stepResult.totalReward

      val currentState = EpisodeState(agentPositions, dynamicWalls.toSet, episodeDone, episodeReward)
      onStep(currentState, steps, stepResult.anyAgentExploring)

    EpisodeResult(steps, episodeReward, agentPositions, dynamicWalls.toSet, episodeDone)

  /**
   * Executes a single step in the episode.
   */
  private def executeStep(): EpisodeStepResult =

    val jointActionsWithExploration: Map[String, (Action, Boolean)] =
      agentsQL.map { case (id, ql) => id -> ql.choose(agentPositions(id)) }

    val jointActions: Map[String, Action] = jointActionsWithExploration.view.mapValues(_._1).toMap
    val anyAgentExploring = jointActionsWithExploration.values.exists(_._2)

    val (nextPositions, stepRewards) = executeActions(jointActions)

    val triggerRewards = processTriggers(nextPositions)

    updateQLearning(jointActions, stepRewards, triggerRewards, nextPositions)

    agentPositions = nextPositions

    val totalReward = (stepRewards.values.sum + triggerRewards.values.sum)
    EpisodeStepResult(agentPositions, totalReward, anyAgentExploring)

  /**
   * Executes actions for all agents and returns new positions and step rewards.
   */
  private def executeActions(actions: Map[String, Action]): (Map[String, State], Map[String, Double]) =
    actions.foldLeft(agentPositions -> Map.empty[String, Double]) {
      case ((posAcc, rewAcc), (id, action)) =>
        val stepResult = currentGrid.step(posAcc(id), action)
        (posAcc + (id -> stepResult.state), rewAcc + (id -> stepResult.reward))
    }

  /**
   * Creates a GridWorld instance reflecting the current environment state.
   *
   * This method generates a new GridWorld with the current wall configuration, accounting for both static walls
   * (permanent obstacles) and dynamic walls that may have been removed by trigger effects during the episode.
   *
   * @return
   *   A GridWorld instance with the current wall configuration
   */
  private def currentGrid: GridWorld =
    GridWorld(spec.rows, spec.cols, staticWalls.toSet -- dynamicWalls, spec.stepPenalty)

  /**
   * Processes triggers and returns trigger-based rewards.
   */
  private def processTriggers(positions: Map[String, State]): Map[String, Double] =
    val (fired, remaining) = activeTriggers.partition(t => positions(t.who) == t.at)
    activeTriggers = remaining

    val triggerRewards = mutable.Map.empty[String, Double].withDefaultValue(0.0)
    fired.foreach { trigger =>
      val bonus = applyEffects(trigger.effects)
      triggerRewards(trigger.who) += bonus
    }
    triggerRewards.toMap

  /**
   * Applies a list of trigger effects and calculates the total bonus reward.
   *
   * This method processes various environmental effects that can be triggered by agent actions, including wall removal,
   * bonus rewards, and episode termination. Effects are applied immediately and may modify the environment state.
   *
   * @param effects
   *   List of effects to apply to the environment
   * @return
   *   The total bonus reward accumulated from all reward effects
   */
  private def applyEffects(effects: List[Effect]): Double =
    var bonus = 0.0
    effects.foreach {
      case OpenWall(pos) => dynamicWalls += pos
      case Reward(x) => bonus += x
      case EndEpisode => episodeDone = true
    }
    bonus

  /**
   * Updates Q-learning for all agents.
   */
  private def updateQLearning(
    actions: Map[String, Action],
    stepRewards: Map[String, Double],
    triggerRewards: Map[String, Double],
    nextPositions: Map[String, State]
  ): Unit =
    agentsQL.foreach { case (id, learner) =>
      val action = actions(id)
      val totalReward = stepRewards.getOrElse(id, 0.0) + triggerRewards.getOrElse(id, 0.0)
      learner.update(agentPositions(id), action, totalReward, nextPositions(id))
    }

  /**
   * Resets the episode state for a new episode.
   */
  def resetEpisode(): Unit =
    agentPositions = agentMap.view.mapValues(_.start).toMap
    dynamicWalls.clear()
    episodeDone = false
    activeTriggers = spec.triggers
    episodeReward = 0.0

  /**
   * Gets the current state for visualization.
   */
  def getCurrentState: EpisodeState =
    EpisodeState(agentPositions, dynamicWalls.toSet, episodeDone, episodeReward)

  /**
   * Gets the current epsilon value for exploration tracking.
   */
  def getCurrentEpsilon: Double =
    agentsQL.values.headOption.map(_.eps).getOrElse(0.0)

  /**
   * Increments episode count for all learners.
   */
  def incrementEpisode(): Unit =
    agentsQL.values.foreach(_.incEp())
