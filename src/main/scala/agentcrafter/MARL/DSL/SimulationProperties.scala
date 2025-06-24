package agentcrafter.marl.dsl

import agentcrafter.marl.builders.SimulationBuilder

import scala.annotation.targetName

/**
 * Wrapper class for SimulationBuilder used in DSL context.
 *
 * This wrapper provides a mutable reference to a SimulationBuilder instance that can be modified through DSL
 * operations.
 *
 * @param builder
 *   The SimulationBuilder instance being wrapped
 */
case class SimulationWrapper(var builder: SimulationBuilder)

/**
 * DSL properties for configuring simulation-wide parameters.
 *
 * This enumeration provides type-safe property setters for simulation configuration through the DSL syntax. Each case
 * corresponds to a specific simulation parameter and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum SimulationProperty[T]:
  /** Step penalty applied to agents for each action taken */
  case Penalty extends SimulationProperty[Double]

  /** Total number of episodes to run in the simulation */
  case Episodes extends SimulationProperty[Int]

  /** Maximum number of steps allowed per episode */
  case Steps extends SimulationProperty[Int]

  /** Number of episodes after which to show the GUI visualization */
  case ShowAfter extends SimulationProperty[Int]

  /** Delay in milliseconds between simulation steps for visualization */
  case Delay extends SimulationProperty[Int]

  /** Whether to enable the graphical user interface */
  case WithGUI extends SimulationProperty[Boolean]

  @targetName("to")
  infix def >>(obj: T)(using wrapper: SimulationWrapper): Unit = this match
    case SimulationProperty.Penalty => wrapper.builder = wrapper.builder.stepPenalty(obj.asInstanceOf[Double])
    case SimulationProperty.Episodes => wrapper.builder = wrapper.builder.episodes(obj.asInstanceOf[Int])
    case SimulationProperty.Steps => wrapper.builder = wrapper.builder.steps(obj.asInstanceOf[Int])
    case SimulationProperty.ShowAfter => wrapper.builder = wrapper.builder.showAfter(obj.asInstanceOf[Int])
    case SimulationProperty.Delay => wrapper.builder = wrapper.builder.delay(obj.asInstanceOf[Int])
    case SimulationProperty.WithGUI => wrapper.builder = wrapper.builder.withGUI(obj.asInstanceOf[Boolean])
