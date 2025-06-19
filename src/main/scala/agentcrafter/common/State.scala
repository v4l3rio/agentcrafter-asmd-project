package agentcrafter.common

/**
 * Represents a position in a 2D grid environment.
 *
 * This case class encapsulates the coordinates of a state in a grid-based world, using a row-column coordinate system
 * where:
 *   - `x` (row) represents the vertical position, with 0 typically being the top
 *   - `y` (column) represents the horizontal position, with 0 typically being the left
 *
 * States are used throughout the reinforcement learning system to represent:
 *   - Agent positions in the environment
 *   - Goal locations
 *   - Wall positions
 *   - Start positions
 *
 * @param x
 *   The row coordinate (vertical position)
 * @param y
 *   The column coordinate (horizontal position)
 * @example
 * {{{
 * val startState = State(0, 0)
 * val goalState = State(9, 8)
 * val wallState = State(5, 3)
 * }}}
 */
case class State(x: Int, y: Int)
