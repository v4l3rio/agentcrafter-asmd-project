package agentcrafter.common

case class StepResult(state: State, reward: Double)

object GridWorld:
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
   * 2. Applies boundary clamping to keep the agent within the grid
   * 3. Checks for wall collisions (agent stays in place if hitting a wall)
   * 4. Determines if the episode is complete (goal reached)
   * 5. Calculates the appropriate reward
   *
   * @param s The current state of the agent
   * @param a The action to be executed
   * @return StepResult containing next state and reward
   */
  def step(s: State, a: Action): StepResult  =
    val (dr, dc) = a.delta
    val next0 = State(
      (s.r + dr).clamp(0, rows - 1),
      (s.c + dc).clamp(0, cols - 1)
    )
    val next = if walls.contains(next0) then s else next0
    // if you want to extend the implementation to make different
    // states give different rewards you can do it here,
    // for now each step gives a penalty
    StepResult(next, stepPenalty)

/**
 * Extension method to clamp an integer value between bounds.
 *
 * @return The clamped value
 */
extension (i: Int)
  /** Clamps the integer between the specified lower and upper bounds. */
  private inline def clamp(lo: Int, hi: Int) = math.min(math.max(i, lo), hi)