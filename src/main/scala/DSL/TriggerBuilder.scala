package DSL

import common.State
import scala.collection.mutable

class TriggerBuilder(who: String, r: Int, c: Int, parent: SimulationBuilder):
  private val eff = mutable.Buffer.empty[Effect]
  
  def openWall(r: Int, c: Int): TriggerBuilder = 
    eff += OpenWall(State(r, c))
    this
    
  def endEpisode(): TriggerBuilder = 
    eff += EndEpisode
    this
    
  def give(bonus: Double): SimulationBuilder = 
    eff += Reward(bonus)
    finish()
    
  private def finish(): SimulationBuilder =
    parent.addTrigger(Trigger(who, State(r, c), eff.toList))
    parent