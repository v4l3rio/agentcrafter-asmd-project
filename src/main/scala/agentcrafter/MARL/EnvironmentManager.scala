package agentcrafter.marl

import agentcrafter.common.*

import scala.collection.mutable

/**
 * Manages environment-related operations in a multi-agent simulation.
 * 
 * This class follows the Single Responsibility Principle by focusing solely on:
 * - Environment state management (walls, triggers)
 * - Action execution in the environment
 * - Trigger processing and effects application
 * - Grid world state management
 */
class EnvironmentManager(spec: WorldSpec):

  private val staticWalls = spec.staticWalls.to(mutable.Set)
  private val dynamicWalls = mutable.Set.empty[State]
  private var activeTriggers: List[Trigger] = spec.triggers
  private var episodeDone = false

  /**
   * Creates a GridWorld instance reflecting the current environment state.
   */
  def getCurrentGrid: GridWorld =
    GridWorld(spec.rows, spec.cols, staticWalls.toSet -- dynamicWalls, spec.stepPenalty)

  /**
   * Gets the current set of opened walls.
   */
  def getOpenedWalls: Set[State] = dynamicWalls.toSet

  /**
   * Checks if the episode is done.
   */
  def isEpisodeDone: Boolean = episodeDone

  /**
   * Executes actions for all agents and returns new positions and step rewards.
   */
  def executeActions(actions: Map[String, Action], currentPositions: Map[String, State]): (Map[String, State], Map[String, Double]) =
    actions.foldLeft(currentPositions -> Map.empty[String, Double]) {
      case ((posAcc, rewAcc), (id, action)) =>
        val stepResult = getCurrentGrid.step(posAcc(id), action)
        (posAcc + (id -> stepResult.state), rewAcc + (id -> stepResult.reward))
    }

  /**
   * Processes triggers and returns trigger-based rewards.
   */
  def processTriggers(positions: Map[String, State]): Map[String, Double] =
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
   * Resets the environment state for a new episode.
   */
  def resetEnvironment(): Unit =
    dynamicWalls.clear()
    episodeDone = false
    activeTriggers = spec.triggers