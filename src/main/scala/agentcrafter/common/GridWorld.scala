package agentcrafter.common

/**
 * Factory object for creating GridWorld instances.
 * 
 * Provides convenient factory methods with sensible defaults for creating
 * grid world environments commonly used in reinforcement learning experiments.
 */
object GridWorld:
  /**
   * Creates a new GridWorld instance with configurable parameters.
   * 
   * @param rows The number of rows in the grid (default: 13)
   * @param cols The number of columns in the grid (default: 15)
   * @param walls Set of wall positions that block agent movement (default: some example walls)
   * @param stepPenalty The penalty applied for each step taken (default: -3.0)
   * @return A new GridWorld instance with the specified configuration
   */
  def apply(rows: Int = 13,
            cols: Int = 15,
            walls: Set[State] = Set(
              State(1, 2), State(2, 2)
            ),
            stepPenalty: Double = -3.0): GridWorld = new GridWorld(rows, cols, walls, stepPenalty)

/**
 * Represents a 2D grid-based reinforcement learning environment.
 *
 * This class implements a standard grid world where agents can move between cells,
 * encounter walls (obstacles), and receive rewards for reaching goals. The environment
 * follows typical RL conventions with states, actions, rewards, and episode termination.
 *
 * Key features:
 * - Configurable grid dimensions
 * - Customizable start and goal positions
 * - Wall obstacles that block movement
 * - Reward structure with step penalties and goal rewards
 * - Boundary clamping (agents cannot move outside the grid)
 *
 * The coordinate system uses (row, column) indexing starting from (0, 0) at the top-left.
 *
 * @param rows  The number of rows in the grid (height)
 * @param cols  The number of columns in the grid (width)
 * @param walls Set of states that represent impassable obstacles
 */
class GridWorld private (val rows: Int, val cols: Int, val walls: Set[State], stepPenalty: Double) extends Environment:

  /**
   * Executes one step in the environment given a current state and action.
   *
   * This method implements the core environment dynamics:
   * 1. Calculates the intended next position based on the action
   * 2. Checks if the movement is valid (within bounds and not hitting walls)
   * 3. If invalid, the agent stays in the current position
   * 4. Determines if the episode is complete (goal reached)
   * 5. Calculates the appropriate reward
   *
   * @param s The current state of the agent
   * @param a The action to be executed
   * @return StepResult containing next state and reward
   */
  def step(s: State, a: Action): StepResult  =
    val (dr, dc) = a.delta
    val intendedNext = State(s.x + dr, s.y + dc)
    
    // Check if the intended move is valid (within bounds and not a wall)
    val isValidMove = intendedNext.x >= 0 && intendedNext.x < rows &&
                      intendedNext.y >= 0 && intendedNext.y < cols &&
                      !walls.contains(intendedNext)
    
    val next = if isValidMove then intendedNext else s
    // if you want to extend the implementation to make different
    // states give different rewards you can do it here,
    // for now each step gives a penalty
    StepResult(next, stepPenalty)