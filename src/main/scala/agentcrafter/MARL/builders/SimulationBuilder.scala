package agentcrafter.marl.builders

import agentcrafter.common.*
import agentcrafter.marl.*
import agentcrafter.marl.dsl.*
import agentcrafter.marl.visualizers.Visualizer

import scala.collection.mutable



/**
 * Builder for creating Multi-Agent Reinforcement Learning (MARL) simulations.
 *
 * This class provides a fluent API for configuring simulations with multiple agents, environmental obstacles (walls),
 * triggers, and various simulation parameters. The builder pattern allows for flexible and readable simulation setup.
 */
class SimulationBuilder:
  private val walls = mutable.Set.empty[State]
  private val agents = mutable.Map.empty[String, AgentSpec]
  private val triggers = mutable.Buffer.empty[Trigger]
  private var rows = Constants.DEFAULT_GRID_ROWS
  private var cols = Constants.DEFAULT_GRID_COLS
  private var nEpisodes = Constants.DEFAULT_TRAINING_EPISODES
  private var stepLimit = Constants.DEFAULT_SIMULATION_STEP_LIMIT
  private var stepDelay = Constants.DEFAULT_STEP_DELAY_MS
  private var showAfter = Constants.DEFAULT_SHOW_AFTER_EPISODES
  private var stepPenalty = Constants.DEFAULT_STEP_PENALTY

  private var gui = false

  /**
   * Enables or disables the graphical user interface for the simulation.
   *
   * @param flag
   *   True to show GUI, false to run headless
   * @return
   *   This builder instance for method chaining
   */
  def withGUI(flag: Boolean): SimulationBuilder =
    gui = flag
    this

  /**
   * Sets the grid dimensions for the simulation environment.
   *
   * @param rows
   *   Number of rows in the grid
   * @param cols
   *   Number of columns in the grid
   * @return
   *   This builder instance for method chaining
   */
  def grid(rows: Int, cols: Int): SimulationBuilder =
    this.rows = rows; this.cols = cols; this

  private[marl] def newTrigger(who: String, x: Int, y: Int): TriggerBuilder =
    new TriggerBuilder(who, x, y, this)

  /**
   * Creates a new wall line builder for configuring a line of walls.
   *
   * @return
   *   A new WallLineBuilder instance for configuring the wall line
   */
  def newWallLine(): WallLineBuilder = new WallLineBuilder(this)

  /**
   * Sets the penalty applied for each step taken by agents.
   *
   * @param penalty
   *   The negative reward applied per step (typically negative)
   * @return
   *   This builder instance for method chaining
   */
  def stepPenalty(penalty: Double): SimulationBuilder =
    stepPenalty = penalty
    this

  /**
   * Adds a wall at the specified grid position.
   *
   * @param r
   *   Row coordinate of the wall
   * @param c
   *   Column coordinate of the wall
   * @return
   *   This builder instance for method chaining
   */
  def wall(x: Int, y: Int): SimulationBuilder =
    walls += State(x, y); this

  /**
   * Adds walls based on ASCII art representation.
   *
   * @param s
   *   ASCII string where '#' represents walls and '.' represents empty spaces
   * @return
   *   This builder instance for method chaining
   */
  def wallsFromAscii(s: String): SimulationBuilder =
    val lines = s.stripMargin.split("\n").map(_.trim)
    lines.zipWithIndex.foreach { case (line, x) =>
      line.zipWithIndex.foreach { case (ch, y) =>
        if ch == '#' then walls += State(x, y)
      }
    };
    this

  /**
   * Creates a new agent builder for configuring an agent.
   *
   * @param id
   *   Unique identifier for the agent
   * @return
   *   A new AgentBuilder instance for configuring the agent
   */
  def agent(id: String): AgentBuilder = new AgentBuilder(this)

  /**
   * Adds a configured agent to the simulation.
   *
   * @param id
   *   Unique identifier for the agent
   * @param spec
   *   Complete specification of the agent's configuration
   */
  def addAgent(id: String, spec: AgentSpec): Unit =
    agents += id -> spec

  /**
   * Adds a trigger to the simulation.
   *
   * @param trigger
   *   The trigger configuration to add
   */
  def addTrigger(trigger: Trigger): Unit =
    triggers += trigger

  /**
   * Gets the number of rows in the grid.
   *
   * @return
   *   The number of rows
   */
  def getRows: Int = rows

  /**
   * Gets the number of columns in the grid.
   *
   * @return
   *   The number of columns
   */
  def getCols: Int = cols

  /**
   * Gets the set of wall positions.
   *
   * @return
   *   An immutable set of wall positions
   */
  def getWalls: Set[State] = walls.toSet

  /**
   * Gets the map of agents in the simulation.
   *
   * @return
   *   An immutable map of agent IDs to their specifications
   */
  def getAgents: Map[String, AgentSpec] = agents.toMap

  /**
   * Sets the maximum number of steps per episode.
   *
   * @param n
   *   Maximum steps allowed per episode
   * @return
   *   This builder instance for method chaining
   */
  def steps(n: Int): SimulationBuilder =
    stepLimit = n; this

  /**
   * Sets the delay between simulation steps for visualization.
   *
   * @param ms
   *   Delay in milliseconds between steps
   * @return
   *   This builder instance for method chaining
   */
  def delay(ms: Int): SimulationBuilder =
    stepDelay = ms; this

  /**
   * Sets after how many episodes to start showing the GUI.
   *
   * @param n
   *   Episode number after which to show visualization
   * @return
   *   This builder instance for method chaining
   */
  def showAfter(n: Int): SimulationBuilder =
    showAfter = n; this

  /**
   * Sets the total number of episodes to run.
   *
   * @param n
   *   Number of episodes to execute
   * @return
   *   This builder instance for method chaining
   */
  def episodes(n: Int): SimulationBuilder =
    nEpisodes = n; this

  /**
   * Builds and starts the simulation with the configured parameters.
   *
   * This method creates the world specification, initializes agents and learners, and starts the simulation runner.
   */
  def build(): Unit =
    val spec = WorldSpec(
      rows,
      cols,
      stepPenalty,
      walls.toSet,
      triggers.toList,
      agents.values.toList,
      nEpisodes,
      stepLimit,
      stepDelay,
      showAfter
    )
    new Runner(spec, gui).run()

  /**
   * Generates a string representation of the current simulation configuration.
   * This includes grid dimensions, walls, agents, triggers, and simulation parameters.
   *
   * @return
   *   A comprehensive string representation of the simulation state
   */
  override def toString: String =
    val sb = new StringBuilder
    sb.append(s"simulation:\n")
    sb.append(s"  grid: ${rows} x ${cols}\n")
    
    if walls.nonEmpty then
      sb.append(s"  walls:\n")
      walls.toSeq.sortBy(w => (w.x, w.y)).foreach { wall =>
        sb.append(s"    (${wall.x}, ${wall.y})\n")
      }
    
    if agents.nonEmpty then
      sb.append(s"  agents:\n")
      agents.values.foreach { agent =>
        sb.append(s"    agent:\n")
        sb.append(s"      name: ${agent.id}\n")
        sb.append(s"      start: (${agent.start.x}, ${agent.start.y})\n")
        sb.append(s"      goal: (${agent.goal.x}, ${agent.goal.y})\n")
      }
    
    if triggers.nonEmpty then
      sb.append(s"  triggers:\n")
      triggers.foreach { trigger =>
        sb.append(s"    trigger:\n")
        sb.append(s"      agent: ${trigger.who}\n")
        sb.append(s"      position: (${trigger.at.x}, ${trigger.at.y})\n")
        sb.append(s"      effects: ${trigger.effects.size} effect(s)\n")
      }
    
    sb.append(s"  episodes: ${nEpisodes}\n")
    sb.append(s"  stepLimit: ${stepLimit}\n")
    sb.append(s"  stepDelay: ${stepDelay}\n")
    sb.append(s"  showAfter: ${showAfter}\n")
    sb.append(s"  stepPenalty: ${stepPenalty}\n")
    sb.append(s"  gui: ${gui}\n")
    
    sb.toString
