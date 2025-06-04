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
enum Action derives CanEqual:
  case Up, Down, Left, Right, Stay
  def delta: (Int, Int) = this match
    case Up    => (-1, 0)
    case Down  => ( 1, 0)
    case Left  => ( 0,-1)
    case Right => ( 0, 1)
    case Stay  => ( 0, 0)