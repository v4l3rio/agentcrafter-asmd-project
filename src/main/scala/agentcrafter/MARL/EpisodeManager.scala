package agentcrafter.MARL

import agentcrafter.common.{Action, GridWorld, Learner, State, StepResult}
import scala.collection.mutable

/**
 * Manages the execution of individual episodes in a Multi-Agent Reinforcement Learning (MARL) simulation.
 * 
 * This class orchestrates the complex interactions between multiple learning agents in a shared
 * environment, handling state transitions, trigger activations, dynamic environment changes,
 * and coordinated Q-learning updates. It maintains the episode state and ensures proper
 * synchronization between agents while managing environmental effects and rewards.
 * 
 * Key responsibilities:
 * - Coordinating simultaneous agent actions
 * - Managing dynamic environment changes (wall removal, triggers)
 * - Applying environmental effects and bonus rewards
 * - Tracking episode termination conditions
 * - Facilitating Q-learning updates for all agents
 * 
 * @param spec The complete world specification including agents, triggers, and environment parameters
 * @param agentsQL Map of agent IDs to their respective Q-learning instances
 */
class EpisodeManager(spec: WorldSpec, agentsQL: Map[String, Learner]):
  
  private val staticWalls = spec.staticWalls.to(mutable.Set)
  private val dynamicWalls = mutable.Set.empty[State]
  private var episodeDone = false
  private var activeTriggers: List[Trigger] = spec.triggers
  
  private val agentMap = spec.agents.map(a => a.id -> a).toMap
  private var agentPositions = agentMap.view.mapValues(_.start).toMap
  
  private var episodeReward = 0.0
  
  /**
   * Creates a GridWorld instance reflecting the current environment state.
   * 
   * This method generates a new GridWorld with the current wall configuration,
   * accounting for both static walls (permanent obstacles) and dynamic walls
   * that may have been removed by trigger effects during the episode.
   * 
   * @return A GridWorld instance with the current wall configuration
   */
  private def currentGrid: GridWorld =
    GridWorld(spec.rows, spec.cols, staticWalls.toSet -- dynamicWalls, spec.stepPenalty)
  
  /**
   * Applies a list of trigger effects and calculates the total bonus reward.
   * 
   * This method processes various environmental effects that can be triggered
   * by agent actions, including wall removal, bonus rewards, and episode termination.
   * Effects are applied immediately and may modify the environment state.
   * 
   * @param effects List of effects to apply to the environment
   * @return The total bonus reward accumulated from all reward effects
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
   * Executes a complete episode of the MARL simulation.
   * 
   * This method runs the main episode loop, coordinating agent actions,
   * environment transitions, and Q-learning updates until the episode
   * terminates (either by reaching a goal, hitting the step limit, or
   * through a trigger effect).
   * 
   * @param onStep Optional callback function called after each step for visualization
   *               or monitoring purposes. Receives (EpisodeState, stepNumber, isDone)
   * @return EpisodeResult containing the final outcome and statistics
   */
  def runEpisode(onStep: (EpisodeState, Int, Boolean) => Unit = (_, _, _) => ()): EpisodeResult =
    var steps = 0
    
    while !episodeDone && steps < spec.stepLimit do
      val stepResult = executeStep()
      steps += 1
      episodeReward += stepResult.totalReward
      
      // Call visualization callback
      val currentState = EpisodeState(agentPositions, dynamicWalls.toSet, episodeDone, episodeReward)
      onStep(currentState, steps, stepResult.anyAgentExploring)
    
    EpisodeResult(steps, episodeReward, agentPositions, dynamicWalls.toSet, episodeDone)
  
  /**
   * Executes a single step in the episode.
   */
  private def executeStep(): StepResult =
    // 1. Choose actions for all agents
    val jointActionsWithExploration: Map[String, (Action, Boolean)] =
      agentsQL.map { case (id, ql) => id -> ql.choose(agentPositions(id)) }
    
    val jointActions: Map[String, Action] = jointActionsWithExploration.view.mapValues(_._1).toMap
    val anyAgentExploring = jointActionsWithExploration.values.exists(_._2)
    
    // 2. Execute actions and get new positions
    val (nextPositions, stepRewards) = executeActions(jointActions)
    
    // 3. Process triggers
    val triggerRewards = processTriggers(nextPositions)
    
    // 4. Update Q-learning for all agents
    updateQLearning(jointActions, stepRewards, triggerRewards, nextPositions)
    
    // 5. Update state
    agentPositions = nextPositions
    
    val totalReward = (stepRewards.values.sum + triggerRewards.values.sum)
    StepResult(agentPositions, totalReward, anyAgentExploring)
  
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
   * Updates Q-learning for all agents.
   */
  private def updateQLearning(actions: Map[String, Action], 
                             stepRewards: Map[String, Double],
                             triggerRewards: Map[String, Double],
                             nextPositions: Map[String, State]): Unit =
    agentsQL.foreach { case (id, learner) =>
      val action = actions(id)
      val totalReward = stepRewards.getOrElse(id, 0.0) + triggerRewards.getOrElse(id, 0.0)
      learner.updateWithGoal(agentPositions(id), action, totalReward, nextPositions(id))
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

/**
 * Result of executing a single step.
 */
case class StepResult(positions: Map[String, State], totalReward: Double, anyAgentExploring: Boolean)

/**
 * Result of executing a complete episode.
 */
case class EpisodeResult(steps: Int, totalReward: Double, finalPositions: Map[String, State], 
                        openedWalls: Set[State], completed: Boolean)

/**
 * Current state of an episode for visualization.
 */
case class EpisodeState(positions: Map[String, State], openedWalls: Set[State], 
                       done: Boolean, reward: Double)