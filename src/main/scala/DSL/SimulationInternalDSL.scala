package DSL

import scala.annotation.targetName

case class SimulationWrapper(var builder: SimulationBuilder)
case class AgentWrapper(var builder: AgentBuilder)
case class LearnerConfig(
                          var alpha: Double = 0.1,
                          var gamma: Double = 0.9,
                          var eps0: Double = 0.9,
                          var epsMin: Double = 0.15,
                          var warm: Int = 10_000,
                          var optimistic: Double = 0.0
                        )

trait SimulationInternalDSL extends App:

  def simulation(block: SimulationWrapper ?=> Unit) =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block
    wrapper.builder.play()

  def grid(size: (Int, Int))(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.grid(size._1, size._2)
  extension(n: Int) infix def x(other:Int):(Int, Int) = (n, other)

  def asciiWalls(ascii: String)(using wrapper: SimulationWrapper) =
    wrapper.builder = wrapper.builder.wallsFromAscii(ascii.stripMargin)

  def agent(block: AgentWrapper ?=> Unit)(using wrapper: SimulationWrapper) =
    given agentWrapper: AgentWrapper = AgentWrapper(new AgentBuilder(wrapper.builder))
    block
    wrapper.builder = agentWrapper.builder.end()

  enum AgentProperty[T]:
    case Name extends AgentProperty[String]
    case Start extends AgentProperty[(Int, Int)]
    case Goal extends AgentProperty[(Int, Int)]
    case Reward extends AgentProperty[Double]
    @targetName("to")
    infix def >>(obj: T)(using agentWrapper:AgentWrapper): AgentBuilder = this match
      case AgentProperty.Name => agentWrapper.builder.name(obj.asInstanceOf[String])
      case AgentProperty.Start =>
        val (r, c) = obj.asInstanceOf[(Int, Int)]
        agentWrapper.builder.start(r,c)
      case AgentProperty.Goal =>
        val (r, c) = obj.asInstanceOf[(Int, Int)]
        agentWrapper.builder.goal(r, c)
      case AgentProperty.Reward => agentWrapper.builder.reward(obj.asInstanceOf[Double])

  def withLearner(using agentWrapper: AgentWrapper)(block: LearnerConfig => Unit) =
    val config = LearnerConfig() // Configurazione predefinita
    block(config) // Applica le modifiche definite nel blocco
    agentWrapper.builder = agentWrapper.builder.withLearner(
      alpha = config.alpha,
      gamma = config.gamma,
      eps0 = config.eps0,
      epsMin = config.epsMin,
      warm = config.warm,
      optimistic = config.optimistic
    )



object SimulationApp extends SimulationInternalDSL:
  import AgentProperty.*
  simulation:
    grid:
      10 x 10
    asciiWalls:
      """..........
        |...###....
        |...#.#....
        |...###....
        |..........
        |..........
        |..........
        |..........
        |..........
        |.........."""
    agent:
      Name >> "Runner"
      Start >> (1, 9)
      withLearner:
        config =>
          config.alpha = 0.2
          config.gamma = 0.95
          config.eps0 = 0.8
          config.epsMin = 0.1
          config.warm = 15_000
          config.optimistic = 0.5
      Goal >> (2, 4)
      Reward >> 100.0


