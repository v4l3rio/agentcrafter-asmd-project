package agentcrafter.MARL.builders

import scala.collection.mutable
import agentcrafter.MARL.{Effect, EndEpisode, OpenWall, Reward, Trigger}
import agentcrafter.common.State

class TriggerBuilder(who: String, r: Int, c: Int, parent: SimulationBuilder):
  private val eff = mutable.Buffer.empty[Effect]
  
  def openWall(r: Int, c: Int): TriggerBuilder = 
    eff += OpenWall(State(r, c))
    this
    
  def endEpisode(): TriggerBuilder = 
    eff += EndEpisode
    this
    
  def give(bonus: Double): TriggerBuilder =
    eff += Reward(bonus)
    this

  private[MARL] def build(): SimulationBuilder =
    parent.addTrigger(Trigger(who, State(r, c), eff.toList))
    parent