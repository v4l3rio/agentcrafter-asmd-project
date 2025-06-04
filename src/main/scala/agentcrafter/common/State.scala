package agentcrafter.common

/**
 * Represents a position in a 2D grid environment.
 * 
 * This case class encapsulates the coordinates of a state in a grid-based world,
 * using a row-column coordinate system where:
 * - `r` (row) represents the vertical position, with 0 typically being the top
 * - `c` (column) represents the horizontal position, with 0 typically being the left
 * 
 * States are used throughout the reinforcement learning system to represent:
 * - Agent positions in the environment
 * - Goal locations
 * - Wall positions
 * - Start positions
 * 
 * @param r The row coordinate (vertical position)
 * @param c The column coordinate (horizontal position)
 * 
 * @example
 * {{{
 * val startState = State(0, 0)     // Top-left corner
 * val goalState = State(9, 8)      // Row 9, Column 8
 * val wallState = State(5, 3)      // Wall at row 5, column 3
 * }}}
 */
case class State(r: Int, c: Int)