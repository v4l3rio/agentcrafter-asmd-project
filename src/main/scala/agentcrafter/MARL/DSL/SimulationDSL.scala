package agentcrafter.marl.dsl

import agentcrafter.marl.builders.{AgentBuilder, SimulationBuilder, TriggerBuilder, WallLineBuilder}

/**
 * Domain-Specific Language (dsl) for creating Multi-Agent Reinforcement Learning simulations.
 *
 * This trait provides a fluent, declarative API for defining marl environments with agents, walls, triggers, and
 * simulation parameters. The dsl uses Scala 3's context functions and given/using syntax to create a clean, readable
 * configuration syntax.
 */
trait SimulationDSL:
  /**
   * Main entry point for defining a simulation.
   *
   * Creates a simulation context and executes the provided configuration block, then starts the simulation.
   *
   * @param block
   *   Configuration block that defines the simulation setup
   */
  def simulation(block: SimulationWrapper ?=> Unit): Unit =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)

    block
    wrapper.builder.build()

  /**
   * Defines the grid dimensions for the simulation world.
   *
   * @param size
   *   Tuple containing (rows, columns) for the grid
   */
  def grid(size: (Int, Int))(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.grid(size._1, size._2)

  /**
   * Infix operator to create grid size tuples in a readable format.
   *
   * @return
   *   Tuple representing grid dimensions
   * @example
   *   `10 x 10` creates a (10, 10) tuple
   */
  extension (n: Int) infix def x(other: Int): (Int, Int) = (n, other)

  def penalty(n: Double)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.stepPenalty(n)

  /**
   * Defines walls using ASCII art representation.
   *
   * @param ascii
   *   String containing ASCII representation of walls
   */
  def asciiWalls(ascii: String)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.wallsFromAscii(ascii.stripMargin)



  /**
   * Opens a walls configuration block.
   *
   * @param block
   *   Configuration block for defining walls
   */



  def walls(block: SimulationWrapper ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    block

  /**
   * Defines a line of walls using a configuration block.
   *
   * @param block
   *   Configuration block for the wall line (direction, from, to)
   * @throws IllegalArgumentException
   *   if direction, from, or to are not specified
   */
  def line(block: WallLineBuilder ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given lineBuilder: WallLineBuilder = wrapper.builder.newWallLine()

    block
    wrapper.builder = lineBuilder.build()

  /**
   * Defines an agent in the simulation.
   *
   * @param block
   *   Configuration block for the agent (id, start, goal, learner, etc.)
   */
  def agent(block: AgentWrapper ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given agentWrapper: AgentWrapper = AgentWrapper(new AgentBuilder(wrapper.builder))

    block
    wrapper.builder = agentWrapper.builder.build()

  /**
   * Configures the Q-learning parameters for an agent.
   *
   * @param block
   *   Configuration block for learner parameters (alpha, gamma, epsilon, etc.)
   */
  def withLearner(using agentWrapper: AgentWrapper)(block: LearnerConfig ?=> Unit): Unit =
    val config = LearnerConfig() // Default configuration

    given LearnerConfig = config

    block
    agentWrapper.builder = agentWrapper.builder.withLearner(
      alpha = config.alpha,
      gamma = config.gamma,
      eps0 = config.eps0,
      epsMin = config.epsMin,
      warm = config.warm,
      optimistic = config.optimistic
    )

  /**
   * Defines trigger effects for the current agent's goal position.
   */
  def onGoal(block: TriggerBuilder ?=> Unit)(using agentWrapper: AgentWrapper, wrapper: SimulationWrapper): Unit =
    val g = agentWrapper.builder.currentGoal
    val id = agentWrapper.builder.currentId
    val tb = wrapper.builder.newTrigger(id, g.x, g.y)

    given TriggerBuilder = tb

    block
    wrapper.builder = tb.build()

  /**
   * Trigger effect that removes a wall at the specified position.
   *
   * @param x
   *   Row position of the wall to remove
   * @param y
   *   Column position of the wall to remove
   */
  def openWall(x: Int, y: Int)(using tb: TriggerBuilder): Unit =
    tb.openWall(x, y)

  /**
   * Trigger effect that immediately ends the current episode.
   */
  def endEpisode()(using tb: TriggerBuilder): Unit =
    tb.endEpisode()

  /**
   * Trigger effect that gives a bonus reward to the triggering agent.
   *
   * @param bonus
   *   The reward amount (can be positive or negative)
   */
  def give(bonus: Double)(using tb: TriggerBuilder): Unit =
    tb.give(bonus)
