package agentcrafter.common

/**
 * Represents a reinforcement learning environment.
 * 
 * This trait defines the interface for all environments in the AgentCrafter framework.
 * Environments are responsible for managing state transitions, rewards, and termination conditions.
 */
trait Environment:
  /**
   * Number of rows in the grid environment.
   * 
   * @return The total number of rows in the environment grid
   */
  def rows: Int
  
  /**
   * Number of columns in the grid environment.
   * 
   * @return The total number of columns in the environment grid
   */
  def cols: Int
  
  /**
   * Executes a single step in the environment given a state and action.
   * 
   * This method implements the core environment dynamics, determining the next state,
   * reward, and whether the episode should terminate based on the current state and
   * the action taken by the agent.
   * 
   * @param state The current state of the agent in the environment
   * @param action The action to be executed by the agent
   * @return A StepResult containing the next state, reward, and termination flag
   */
  def step(state: State, action: Action): StepResult
