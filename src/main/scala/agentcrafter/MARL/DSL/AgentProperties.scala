package agentcrafter.marl.dsl

import agentcrafter.marl.builders.AgentBuilder
import scala.annotation.targetName

/**
 * Wrapper class for AgentBuilder used in DSL context.
 *
 * This wrapper provides a mutable reference to an AgentBuilder instance
 * that can be modified through DSL operations.
 *
 * @param builder
 *   The AgentBuilder instance being wrapped
 */
case class AgentWrapper(var builder: AgentBuilder)

/**
 * DSL properties for configuring individual agent parameters.
 *
 * This enumeration provides type-safe property setters for agent configuration
 * through the DSL syntax. Each case corresponds to a specific agent parameter
 * and enforces the correct value type at compile time.
 *
 * @tparam T
 *   The type of value this property accepts
 */
enum AgentProperty[T]:
  /** Unique identifier name for the agent */
  case Name extends AgentProperty[String]
  /** Starting position coordinates (row, column) for the agent */
  case Start extends AgentProperty[(Int, Int)]
  /** Goal position coordinates (row, column) for the agent */
  case Goal extends AgentProperty[(Int, Int)]

  @targetName("to")
  infix def >>(obj: T)(using agentWrapper: AgentWrapper): AgentBuilder = this match
    case AgentProperty.Name => agentWrapper.builder.name(obj.asInstanceOf[String])
    case AgentProperty.Start =>
      val (x, y) = obj.asInstanceOf[(Int, Int)]
      agentWrapper.builder.start(x, y)
    case AgentProperty.Goal =>
        val (x, y) = obj.asInstanceOf[(Int, Int)]
        agentWrapper.builder.goal(x, y)
