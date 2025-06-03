package common

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
 * @param rows The number of rows in the grid (height)
 * @param cols The number of columns in the grid (width)
 * @param start The starting position for agents
 * @param goal The goal position that provides positive reward
 * @param walls Set of states that represent impassable obstacles
 * 
 * @example
 * {{{
 * // Create a simple 5x5 grid with custom start/goal
 * val env = GridWorld(
 *   rows = 5,
 *   cols = 5,
 *   start = State(0, 0),
 *   goal = State(4, 4),
 *   walls = Set(State(2, 2), State(2, 3))
 * )
 * 
 * // Take a step in the environment
 * val (nextState, reward, done) = env.step(State(0, 0), Action.Right)
 * }}}
 */
class GridWorld(
                 val rows:  Int = 13,
                 val cols:  Int = 15,
                 val start: State = State(0, 0),
                 val goal:  State = State(9, 8),
                 val walls: Set[State] = Set(
                   State(1,2), State(2,2)
                 )
               ):
  /** Penalty applied for each step taken (encourages shorter paths) */
  private val stepPenalty = -3.0
  /** Reward given when the agent reaches the goal state */
  private val goalReward  = 50.0
  
  /**
   * Resets the environment to the initial state.
   * 
   * @return The starting state of the environment
   */
  def reset(): State = start
  
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
   * @return A tuple containing:
   *         - next state after the action
   *         - reward received for this transition
   *         - boolean indicating if the episode is done (goal reached)
   * 
   * @example
   * {{{
   * val env = GridWorld()
   * val (nextState, reward, done) = env.step(State(0, 0), Action.Right)
   * // nextState = State(0, 1), reward = -3.0, done = false
   * }}}
   */
  def step(s: State, a: Action): (State, Double, Boolean) =
    val (dr, dc) = a.delta
    val next0 = State(
      (s.r + dr).clamp(0, rows - 1),
      (s.c + dc).clamp(0, cols - 1)
    )
    val next   = if walls.contains(next0) then s else next0
    val done   = next == goal
    val reward = if done then goalReward else stepPenalty
    (next, reward, done)

/**
 * Extension method to clamp an integer value between bounds.
 * 
 * @param i The integer to clamp
 * @param lo The lower bound (inclusive)
 * @param hi The upper bound (inclusive)
 * @return The clamped value
 */
extension (i: Int) private inline def clamp(lo: Int, hi: Int) = math.min(math.max(i, lo), hi)