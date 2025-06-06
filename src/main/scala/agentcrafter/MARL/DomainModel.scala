package agentcrafter.MARL

import agentcrafter.common.{Action, QLearner, State}

/**
 * Domain model for Multi-Agent Reinforcement Learning (MARL) simulations.
 * 
 * This file defines the core data structures used to represent MARL environments,
 * including effects, triggers, agent specifications, and world configurations.
 */

/**
 * Represents an effect that can be triggered during simulation.
 * 
 * Effects modify the environment or simulation state when certain conditions are met.
 */
sealed trait Effect

/**
 * Effect that removes a wall at the specified position.
 * 
 * @param pos The state/position where the wall should be removed
 */
case class OpenWall(pos: State) extends Effect

/**
 * Effect that immediately ends the current episode.
 */
case object EndEpisode extends Effect

/**
 * Effect that provides an additional reward to the triggering agent.
 * 
 * @param delta The reward amount (can be positive or negative)
 */
case class Reward(delta: Double) extends Effect

/**
 * Represents a conditional trigger in the environment.
 * 
 * Triggers activate when a specific agent reaches a specific location,
 * causing one or more effects to be applied.
 * 
 * @param who The ID of the agent that can trigger this effect
 * @param at The state/position where the trigger is located
 * @param effects List of effects to apply when triggered
 */
case class Trigger(who: String, at: State, effects: List[Effect])

/**
 * Specification for a single agent in the MARL environment.
 * 
 * @param id Unique identifier for the agent
 * @param start Starting position of the agent
 * @param goal Optional goal position (if None, agent has no specific goal)
 * @param learner The Q-learning algorithm instance for this agent
 */
case class AgentSpec(id: String,
                     start: State,
                     goal: State,
                     learner: QLearner)

/**
 * Complete specification for a MARL world/environment.
 * 
 * This contains all the information needed to run a multi-agent simulation,
 * including world dimensions, obstacles, agents, and simulation parameters.
 * 
 * @param rows Number of rows in the grid world
 * @param cols Number of columns in the grid world
 * @param staticWalls Set of positions that are permanently blocked
 * @param triggers List of conditional triggers in the environment
 * @param agents List of agent specifications
 * @param episodes Number of episodes to run in the simulation
 * @param stepLimit Maximum steps per episode before forced termination
 * @param stepDelay Delay in milliseconds between steps (for visualization)
 * @param showAfter Episode number after which to start showing visualization
 */
case class WorldSpec(rows: Int,
                     cols: Int,
                     staticWalls: Set[State],
                     triggers: List[Trigger],
                     agents: List[AgentSpec],
                     episodes: Int,
                     stepLimit: Int,
                     stepDelay: Int,
                     showAfter: Int)