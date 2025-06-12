package agentcrafter.MARL.DSL

import agentcrafter.MARL.builders.{AgentBuilder, SimulationBuilder, TriggerBuilder, WallLineBuilder}

/**
 * Domain-Specific Language (DSL) for creating Multi-Agent Reinforcement Learning simulations.
 * 
 * This trait provides a fluent, declarative API for defining MARL environments with agents,
 * walls, triggers, and simulation parameters. The DSL uses Scala 3's context functions
 * and given/using syntax to create a clean, readable configuration syntax.
 * 
 */
trait SimulationDSL:
  /**
   * Main entry point for defining a simulation.
   * 
   * Creates a simulation context and executes the provided configuration block,
   * then starts the simulation.
   * 
   * @param block Configuration block that defines the simulation setup
   */
  def simulation(block: SimulationWrapper ?=> Unit): Unit =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block
    wrapper.builder.build()

  /**
   * Defines the grid dimensions for the simulation world.
   * 
   * @param size Tuple containing (rows, columns) for the grid
   */
  def grid(size: (Int, Int))(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.grid(size._1, size._2)
  
  /**
   * Infix operator to create grid size tuples in a readable format.
   *
   * @return Tuple representing grid dimensions
   * @example `10 x 10` creates a (10, 10) tuple
   */
  extension(n: Int) infix def x(other:Int):(Int, Int) = (n, other)

  def penalty(n: Double)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.stepPenalty(n)

  /**
   * Defines walls using ASCII art representation.
   * 
   * @param ascii String containing ASCII representation of walls
   */
  def asciiWalls(ascii: String)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.wallsFromAscii(ascii.stripMargin)

  /**
   * Generates walls from LLM using a configuration block.
   * 
   * @param block Configuration block for LLM wall generation (model, prompt)
   */
  def wallsFromLLM(block: WallLLMConfig ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    val config = WallLLMConfig()
    given WallLLMConfig = config
    block
    
    if config.model.nonEmpty && config.prompt.nonEmpty then
      import agentcrafter.llmqlearning.LLMWallGenerator
      val simulationFilePath = findSimulationFile()
      LLMWallGenerator.generateWallsFromLLM(wrapper.builder, config.model, config.prompt, Some(simulationFilePath)) match
        case Some(asciiWalls) =>
          println(s"LLM wall generation successful, loading walls...")
          LLMWallGenerator.loadWallsIntoBuilder(wrapper.builder, asciiWalls)
        case None =>
          println("LLM wall generation failed, proceeding without generated walls")
    else
      throw new IllegalArgumentException("wallsFromLLM requires both Model and Prompt to be specified")

  /**
   * Opens a walls configuration block.
   * 
   * @param block Configuration block for defining walls
   */
  /**
   * Finds the simulation file by inspecting the stack trace to locate the file
   * that contains the wallsFromLLM call.
   */
  private def findSimulationFile(): String =
    val stackTrace = Thread.currentThread().getStackTrace
    
    // Find the first stack frame that's not from this trait or system classes
    val callingFrame = stackTrace.find { frame =>
      !frame.getClassName.contains("SimulationDSL") &&
      !frame.getClassName.startsWith("java.") &&
      !frame.getClassName.startsWith("scala.") &&
      frame.getClassName.contains("agentcrafter")
    }
    
    callingFrame match
      case Some(frame) =>
        val className = frame.getClassName.stripSuffix("$")
        s"src/main/scala/${className.replace('.', '/')}.scala"
      case None =>
        throw new RuntimeException("Could not determine simulation file from stack trace")

  def walls(block: SimulationWrapper ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    block

  /**
   * Defines a line of walls using a configuration block.
   * 
   * @param block Configuration block for the wall line (direction, from, to)
   * @throws IllegalArgumentException if direction, from, or to are not specified
   */
  def line(block: WallLineBuilder ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given lineBuilder: WallLineBuilder = WallLineBuilder()
    block
    // Create wall from the configured line builder
    (lineBuilder.direction, lineBuilder.from, lineBuilder.to) match
      case (Some(dir), Some(fromPos), Some(toPos)) =>
        WallProperty.Line >> LineWallConfig(dir, fromPos, toPos)
      case _ => throw new IllegalArgumentException("Line must have direction, from, and to specified")

  /**
   * Defines an agent in the simulation.
   * 
   * @param block Configuration block for the agent (id, start, goal, learner, etc.)
   */
  def agent(block: AgentWrapper ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given agentWrapper: AgentWrapper = AgentWrapper(new AgentBuilder(wrapper.builder))
    block
    wrapper.builder = agentWrapper.builder.build()

  /**
   * Configures the Q-learning parameters for an agent.
   * 
   * @param block Configuration block for learner parameters (alpha, gamma, epsilon, etc.)
   */
  def withLearner(using agentWrapper: AgentWrapper)(block: LearnerConfig ?=> Unit): Unit =
    val config = LearnerConfig() // Configurazione predefinita
    given LearnerConfig = config
    block // Applica le modifiche definite nel blocco
    agentWrapper.builder = agentWrapper.builder.withLearner(
      alpha = config.alpha,
      gamma = config.gamma,
      eps0 = config.eps0,
      epsMin = config.epsMin,
      warm = config.warm,
      optimistic = config.optimistic,
      learnerType = config.learnerType
    )

  /**
   * Defines trigger effects for the current agent's goal position.
   */
  def onGoal(block: TriggerBuilder ?=> Unit)(using agentWrapper: AgentWrapper, wrapper: SimulationWrapper): Unit =
    val g = agentWrapper.builder.currentGoal
    val id = agentWrapper.builder.currentId
    val tb = wrapper.builder.newTrigger(id, g.r, g.c)
    given TriggerBuilder = tb
    block
    wrapper.builder = tb.build()

  /**
   * Trigger effect that removes a wall at the specified position.
   * 
   * @param r Row position of the wall to remove
   * @param c Column position of the wall to remove
   */
  def openWall(r: Int, c: Int)(using tb: TriggerBuilder): Unit =
    tb.openWall(r, c)

  /**
   * Trigger effect that immediately ends the current episode.
   */
  def endEpisode()(using tb: TriggerBuilder): Unit =
    tb.endEpisode()

  /**
   * Trigger effect that gives a bonus reward to the triggering agent.
   * 
   * @param bonus The reward amount (can be positive or negative)
   */
  def give(bonus: Double)(using tb: TriggerBuilder): Unit =
    tb.give(bonus)

  /**
   * Sets the number of episodes to run in the simulation.
   * 
   * @param n Number of episodes
   * @deprecated Use `Episodes >> n` syntax instead for consistency with other DSL properties
   */
  def episodes(n: Int)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.episodes(n)

  /**
   * Sets the maximum number of steps per episode.
   * 
   * @param n Maximum steps per episode
   * @deprecated Use `Steps >> n` syntax instead for consistency with other DSL properties
   */
  def steps(n: Int)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.steps(n)

  /**
   * Sets after which episode to start showing the GUI visualization.
   * 
   * @param n Episode number to start showing visualization
   * @deprecated Use `ShowAfter >> n` syntax instead for consistency with other DSL properties
   */
  def showAfter(n: Int)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.showAfter(n)

  /**
   * Sets the delay between simulation steps for visualization.
   * 
   * @param ms Delay in milliseconds
   * @deprecated Use `Delay >> ms` syntax instead for consistency with other DSL properties
   */
  def delay(ms: Int)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.delay(ms)

  /**
   * Enables or disables the graphical user interface.
   * 
   * @param flag True to enable GUI, false to disable
   * @deprecated Use `WithGUI >> flag` syntax instead for consistency with other DSL properties
   */
  def withGUI(flag: Boolean)(using wrapper: SimulationWrapper): Unit =
    wrapper.builder = wrapper.builder.withGUI(flag)