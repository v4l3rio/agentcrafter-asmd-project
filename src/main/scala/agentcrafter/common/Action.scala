package agentcrafter.common

/**
 * Represents the possible actions an agent can take in a grid-based environment.
 *
 * This enumeration defines the five basic movement actions available to agents:
 * - Directional movements (Up, Down, Left, Right) that change the agent's position
 * - Stay action that keeps the agent in the current position
 *
 * Each action has an associated delta that represents the change in grid coordinates
 * when the action is performed. The coordinate system uses (row, column) indexing
 * where (0,0) is typically the top-left corner.
 *
 * @example
 * {{{
 * val action = Action.Up
 * val (deltaRow, deltaCol) = action.delta  // (-1, 0)
 * }}}
 */
enum Action(val delta: (Int, Int))derives CanEqual:
  case Up extends Action((-1, 0))
  case Down extends Action((1, 0))
  case Left extends Action((0, -1))
  case Right extends Action((0, 1))
  case Stay extends Action((0, 0))