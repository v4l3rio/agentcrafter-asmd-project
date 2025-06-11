package agentcrafter.MARL.builders

import scala.collection.mutable
import agentcrafter.MARL.{AgentSpec, Runner, Trigger, WorldSpec}
import agentcrafter.common.State

/**
 * Builder for creating Multi-Agent Reinforcement Learning (MARL) simulations.
 * 
 * This class provides a fluent API for configuring simulations with multiple agents,
 * environmental obstacles (walls), triggers, and various simulation parameters.
 * The builder pattern allows for flexible and readable simulation setup.
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
  private var stepPenalty = -3.0 // default step penalty for agents

  private var gui = false

  /**
   * Enables or disables the graphical user interface for the simulation.
   * 
   * @param flag True to show GUI, false to run headless
   * @return This builder instance for method chaining
   */
  def withGUI(flag: Boolean): SimulationBuilder = {
    gui = flag
    this
  }

  /**
   * Sets the grid dimensions for the simulation environment.
   * 
   * @param r Number of rows in the grid
   * @param c Number of columns in the grid
   * @return This builder instance for method chaining
   */
  def grid(r: Int, c: Int): SimulationBuilder = { rows = r; cols = c; this }

  /**
   * Sets the penalty applied for each step taken by agents.
   * 
   * @param penalty The negative reward applied per step (typically negative)
   * @return This builder instance for method chaining
   */
  def stepPenalty(penalty: Double): SimulationBuilder = {
    stepPenalty = penalty
    this
  }

  /**
   * Adds a wall at the specified grid position.
   * 
   * @param r Row coordinate of the wall
   * @param c Column coordinate of the wall
   * @return This builder instance for method chaining
   */
  def wall(r: Int, c: Int): SimulationBuilder = { walls += State(r, c); this }

  /**
   * Adds walls based on ASCII art representation.
   * 
   * @param s ASCII string where '#' represents walls and '.' represents empty spaces
   * @return This builder instance for method chaining
   */
  def wallsFromAscii(s: String): SimulationBuilder =
    val lines = s.stripMargin.split("\n").map(_.trim)
    lines.zipWithIndex.foreach { case (line, r) =>
      line.zipWithIndex.foreach { case (ch, c) =>
        if ch == '#' then walls += State(r, c)
      }
    }; this

  /**
   * Creates a new agent builder for configuring an agent.
   * 
   * @param id Unique identifier for the agent
   * @return A new AgentBuilder instance for configuring the agent
   */
  def agent(id: String): AgentBuilder = new AgentBuilder(this)

  /**
   * Adds a configured agent to the simulation.
   * 
   * @param id Unique identifier for the agent
   * @param spec Complete specification of the agent's configuration
   */
  def addAgent(id: String, spec: AgentSpec): Unit = {
    agents += id -> spec
  }

  /**
   * Adds a trigger to the simulation.
   * 
   * @param trigger The trigger configuration to add
   */
  def addTrigger(trigger: Trigger): Unit = {
    triggers += trigger
  }

  /**
   * Gets the number of rows in the grid.
   * 
   * @return The number of rows
   */
  def getRows: Int = rows
  
  /**
   * Gets the number of columns in the grid.
   * 
   * @return The number of columns
   */
  def getCols: Int = cols
  
  /**
   * Gets the set of wall positions.
   * 
   * @return An immutable set of wall positions
   */
  def getWalls: Set[State] = walls.toSet

  /** Internal helper to create a trigger builder. */
  private[MARL] def newTrigger(who: String, r: Int, c: Int): TriggerBuilder =
    new TriggerBuilder(who, r, c, this)

  /**
   * Sets the maximum number of steps per episode.
   * 
   * @param n Maximum steps allowed per episode
   * @return This builder instance for method chaining
   */
  def steps(n: Int): SimulationBuilder = { stepLimit = n; this }

  /**
   * Sets the delay between simulation steps for visualization.
   * 
   * @param ms Delay in milliseconds between steps
   * @return This builder instance for method chaining
   */
  def delay(ms: Int): SimulationBuilder = { stepDelay = ms; this }

  /**
   * Sets after how many episodes to start showing the GUI.
   * 
   * @param n Episode number after which to show visualization
   * @return This builder instance for method chaining
   */
  def showAfter(n: Int): SimulationBuilder = { showAfter = n; this }

  /**
   * Sets the total number of episodes to run.
   * 
   * @param n Number of episodes to execute
   * @return This builder instance for method chaining
   */
  def episodes(n: Int): SimulationBuilder = { nEpisodes = n; this }

  /**
   * Builds and starts the simulation with the configured parameters.
   * 
   * This method creates the world specification, initializes agents and learners,
   * and starts the simulation runner.
   */
  def build(): Unit =
    val spec = WorldSpec(rows, cols, stepPenalty, walls.toSet, triggers.toList, agents.values.toList, nEpisodes, stepLimit, stepDelay, showAfter)
    new Runner(spec, gui).run()