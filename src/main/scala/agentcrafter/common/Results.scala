package agentcrafter.common

/**
 * Base trait for all result types in the AgentCrafter framework.
 *
 * This provides a common interface for different types of results that can occur during reinforcement learning
 * operations, from single-agent steps to multi-agent episodes.
 */
sealed trait Result

/**
 * Represents the result of taking a single step in an environment.
 *
 * This case class encapsulates the outcome of an agent's action in the environment, containing both the resulting state
 * and the reward received for the transition. Used primarily for single-agent environments and basic step operations.
 *
 * @param state
 *   The new state after taking the action
 * @param reward
 *   The immediate reward received for the state transition
 */
case class StepResult(state: State, reward: Double) extends Result

/**
 * Represents the result of executing a single step in a multi-agent episode.
 *
 * This extends the basic step concept to handle multiple agents simultaneously, tracking positions of all agents, total
 * reward across the system, and exploration status.
 *
 * @param positions
 *   Map of agent IDs to their current states/positions
 * @param totalReward
 *   The combined reward from all agents and environmental effects
 * @param anyAgentExploring
 *   Whether any agent is currently in exploration mode
 */
case class EpisodeStepResult(positions: Map[String, State], totalReward: Double, anyAgentExploring: Boolean)
    extends Result

/**
 * Represents the result of executing a complete episode in a multi-agent simulation.
 *
 * This captures the final outcome and statistics of an entire episode run, including completion status, final
 * positions, and environmental changes.
 *
 * @param steps
 *   The total number of steps taken during the episode
 * @param totalReward
 *   The cumulative reward earned throughout the episode
 * @param finalPositions
 *   Map of agent IDs to their final states/positions
 * @param openedWalls
 *   Set of wall positions that were opened during the episode
 * @param completed
 *   Whether the episode completed successfully (vs. timeout/failure)
 */
case class EpisodeResult(
  steps: Int,
  totalReward: Double,
  finalPositions: Map[String, State],
  openedWalls: Set[State],
  completed: Boolean
) extends Result

/**
 * Represents the current state of an episode for visualization and monitoring.
 *
 * This provides a snapshot of the episode state that can be used by visualization components and monitoring systems
 * without exposing internal episode management details.
 *
 * @param positions
 *   Map of agent IDs to their current states/positions
 * @param openedWalls
 *   Set of wall positions that have been opened
 * @param done
 *   Whether the episode has terminated
 * @param reward
 *   The cumulative reward earned so far
 */
case class EpisodeState(positions: Map[String, State], openedWalls: Set[State], done: Boolean, reward: Double)
    extends Result
