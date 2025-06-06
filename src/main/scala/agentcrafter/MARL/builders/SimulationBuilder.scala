package agentcrafter.MARL.builders

import scala.collection.mutable
import agentcrafter.MARL.{AgentSpec, Runner, Trigger, WorldSpec}
import agentcrafter.common.State

/**
 * Builder for creating simulations using a fluent DSL interface
 */
class SimulationBuilder:
  private var rows = 5
  private var cols = 5
  private val walls = mutable.Set.empty[State]
  private val agents = mutable.Map.empty[String, AgentSpec]
  private val triggers = mutable.Buffer.empty[Trigger]
  private var nEpisodes = 10_000
  private var stepLimit  = 400
  private var stepDelay = 70 // ms between steps in GUI mode
  private var showAfter = 0

  private var gui = false

  def withGUI(flag: Boolean): SimulationBuilder = {
    gui = flag; this
  }

  /* grid size */
  def grid(r: Int, c: Int): SimulationBuilder = { rows = r; cols = c; this }

  /* add walls */
  def wall(r: Int, c: Int): SimulationBuilder = { walls += State(r, c); this }

  def wallsFromAscii(s: String): SimulationBuilder =
    val lines = s.stripMargin.split("\n").map(_.trim)
    lines.zipWithIndex.foreach { case (line, r) =>
      line.zipWithIndex.foreach { case (ch, c) =>
        if ch == '#' then walls += State(r, c)
      }
    }; this

  def agent(id: String): AgentBuilder = new AgentBuilder(this)

  def addAgent(id: String, spec: AgentSpec): Unit = {
    agents += id -> spec
  }

  def addTrigger(trigger: Trigger): Unit = {
    triggers += trigger
  }

  // Getter methods for AgentBuilder
  def getRows: Int = rows
  def getCols: Int = cols
  def getWalls: Set[State] = walls.toSet

  /** Internal helper to create a trigger builder. */
  private[MARL] def newTrigger(who: String, r: Int, c: Int): TriggerBuilder =
    new TriggerBuilder(who, r, c, this)

  def steps(n: Int): SimulationBuilder = { stepLimit = n; this }

  def delay(ms: Int): SimulationBuilder = { stepDelay = ms; this }

  def showAfter(n: Int): SimulationBuilder = { showAfter = n; this }

  /* trainer options */
  def episodes(n: Int): SimulationBuilder = { nEpisodes = n; this }

  def play(): Unit =
    val spec = WorldSpec(rows, cols, walls.toSet, triggers.toList, agents.values.toList, nEpisodes, stepLimit, stepDelay, showAfter)
    new Runner(spec, gui).run()