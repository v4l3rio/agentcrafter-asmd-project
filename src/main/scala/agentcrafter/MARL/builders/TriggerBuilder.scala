package agentcrafter.MARL.builders

import agentcrafter.MARL.*
import agentcrafter.common.State

import scala.collection.mutable

/**
 * Builder for configuring triggers that activate when specific agents reach certain positions.
 *
 * Triggers can cause various effects such as opening walls, ending episodes, or giving rewards when a specific agent
 * reaches a designated position in the grid.
 *
 * @param who
 *   The agent identifier that can activate this trigger
 * @param r
 *   Row coordinate where the trigger is located
 * @param c
 *   Column coordinate where the trigger is located
 * @param parent
 *   The parent SimulationBuilder
 */
class TriggerBuilder(who: String, x: Int, y: Int, parent: SimulationBuilder):
  private val eff = mutable.Buffer.empty[Effect]

  /**
   * Adds an effect to open a wall when this trigger is activated.
   *
   * @param x
   *   Row coordinate of the wall to open
   * @param y
   *   Column coordinate of the wall to open
   * @return
   *   This builder instance for method chaining
   */
  def openWall(x: Int, y: Int): TriggerBuilder =
    eff += OpenWall(State(x, y))
    this

  /**
   * Adds an effect to end the current episode when this trigger is activated.
   *
   * @return
   *   This builder instance for method chaining
   */
  def endEpisode(): TriggerBuilder =
    eff += EndEpisode
    this

  /**
   * Adds an effect to give a reward bonus when this trigger is activated.
   *
   * @param bonus
   *   The reward amount to give (can be positive or negative)
   * @return
   *   This builder instance for method chaining
   */
  def give(bonus: Double): TriggerBuilder =
    eff += Reward(bonus)
    this

  /**
   * Builds and adds this trigger to the parent simulation.
   *
   * @return
   *   The parent SimulationBuilder for continued configuration
   */
  private[MARL] def build(): SimulationBuilder =
    parent.addTrigger(Trigger(who, State(x, y), eff.toList))
    parent
