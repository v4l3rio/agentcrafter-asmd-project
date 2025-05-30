package MARL.DSL

import MARL.builders.{AgentBuilder, SimulationBuilder, TriggerBuilder, LineBuilder}

trait SimulationDSL:
  def simulation(block: SimulationWrapper ?=> Unit) =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block
    wrapper.builder.play()

  def grid(size: (Int, Int))(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.grid(size._1, size._2)
  extension(n: Int) infix def x(other:Int):(Int, Int) = (n, other)

  def asciiWalls(ascii: String)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.wallsFromAscii(ascii.stripMargin)

  def walls(block: SimulationWrapper ?=> Unit)(using wrapper: SimulationWrapper) =
    block

  def line(block: LineBuilder ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given lineBuilder: LineBuilder = LineBuilder()
    block
    // Create wall from the configured line builder
    (lineBuilder.direction, lineBuilder.from, lineBuilder.to) match
      case (Some(dir), Some(fromPos), Some(toPos)) =>
        WallProperty.Line >> LineWallConfig(dir, fromPos, toPos)
      case _ => throw new IllegalArgumentException("Line must have direction, from, and to specified")

  def agent(block: AgentWrapper ?=> Unit)(using wrapper: SimulationWrapper) =
    given agentWrapper: AgentWrapper = AgentWrapper(new AgentBuilder(wrapper.builder))
    block
    wrapper.builder = agentWrapper.builder.end()

  def withLearner(using agentWrapper: AgentWrapper)(block: LearnerConfig ?=> Unit) =
    val config = LearnerConfig() // Configurazione predefinita
    given LearnerConfig = config
    block // Applica le modifiche definite nel blocco
    agentWrapper.builder = agentWrapper.builder.withLearner(
      alpha = config.alpha,
      gamma = config.gamma,
      eps0 = config.eps0,
      epsMin = config.epsMin,
      warm = config.warm,
      optimistic = config.optimistic
    )

  def on(who: String, r: Int, c: Int)(block: TriggerBuilder ?=> Unit)(using wrapper: SimulationWrapper) =
    val tb = wrapper.builder.on(who, r, c)
    given TriggerBuilder = tb
    block

  def openWall(r: Int, c: Int)(using tb: TriggerBuilder): Unit =
    tb.openWall(r, c)

  def endEpisode()(using tb: TriggerBuilder): Unit =
    tb.endEpisode()

  def give(bonus: Double)(using tb: TriggerBuilder): Unit =
    tb.give(bonus)

  def episodes(n: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.episodes(n)

  def steps(n: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.steps(n)

  def showAfter(n: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.showAfter(n)

  def delay(ms: Int)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.delay(ms)

  def withGUI(flag: Boolean)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.withGUI(flag)