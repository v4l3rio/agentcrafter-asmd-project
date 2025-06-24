package agentcrafter.common

import agentcrafter.common.Constants

/**
 * Factory object for creating GridWorld instances.
 *
 * Provides convenient factory methods with sensible defaults for creating grid world environments commonly used in
 * reinforcement learning experiments.
 */
object GridWorld:
  /**
   * Creates a new GridWorld instance with configurable parameters.
   *
   * @param rows
   *   The number of rows in the grid (default: 13)
   * @param cols
   *   The number of columns in the grid (default: 15)
   * @param walls
   *   Set of wall positions that block agent movement (default: some example walls)
   * @param stepPenalty
   *   The penalty applied for each step taken (default: -3.0)
   * @return
   *   A new GridWorld instance with the specified configuration
   */
  def apply(
    rows: Int = Constants.DEFAULT_GRID_ROWS,
    cols: Int = Constants.DEFAULT_GRID_COLS,
    walls: Set[State] = Constants.DEFAULT_GRID_WALLS,
    stepPenalty: Double = Constants.DEFAULT_STEP_PENALTY
  ): GridWorld = new GridWorld(rows, cols, walls, stepPenalty)

/**
 * Represents a 2D grid-based reinforcement learning environment.
 *
 * This class implements a standard grid world where agents can move between cells, encounter walls (obstacles), and
 * receive rewards for reaching goals. The environment follows typical RL conventions with states, actions, rewards, and
 * episode termination.
 *
 * Key features:
 *   - Configurable grid dimensions
 *   - Customizable start and goal positions
 *   - Wall obstacles that block movement
 *   - Reward structure with step penalties and goal rewards
 *   - Toroidal wrapping (agents moving out of bounds appear on the opposite side)
 *
 * The coordinate system uses (row, column) indexing starting from (0, 0) at the top-left.
 *
 * @param rows
 *   The number of rows in the grid (height)
 * @param cols
 *   The number of columns in the grid (width)
 * @param walls
 *   Set of states that represent impassable obstacles
 */
class GridWorld private (override val rows: Int, override val cols: Int, val walls: Set[State], stepPenalty: Double)
    extends Environment:

  /**
   * Executes one step in the environment given a current state and action.
   *
   * This method implements the core environment dynamics:
   *   1. Calculates the intended next position based on the action 2. Checks if the movement is valid (within bounds
   *      and not hitting walls) 3. If invalid, the agent stays in the current position 4. Determines if the episode is
   *      complete (goal reached) 5. Calculates the appropriate reward
   *
   * @param s
   *   The current state of the agent
   * @param a
   *   The action to be executed
   * @return
   *   StepResult containing next state and reward
   */
  override def step(s: State, a: Action): StepResult =
    val (dc, dr) = a.delta // delta is (column_change, row_change)
    val intendedNextRaw = State(s.x + dr, s.y + dc) // x=row, y=column
    // Toroidal wrapping
    val wrappedX = (intendedNextRaw.x + rows) % rows // x is row index
    val wrappedY = (intendedNextRaw.y + cols) % cols // y is column index
    val intendedNext = State(wrappedX, wrappedY)

    val isValidMove = !walls.contains(intendedNext)
    val next = if isValidMove then intendedNext else s
    StepResult(next, stepPenalty)
