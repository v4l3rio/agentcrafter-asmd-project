package agentcrafter.visualizers

import agentcrafter.common.State

/**
 * ASCII-based visualization utility for grid world environments and agent trajectories.
 *
 * This object provides pure functional methods for rendering grid worlds as ASCII art, showing agent paths, walls,
 * start/goal positions, and visit sequences. The visualization uses a character-based system to represent the order in
 * which states were visited, making it easy to analyze agent behavior and learning progress in console environments.
 *
 * Key features:
 *   - Pure functional rendering (no side effects in core logic)
 *   - Movement sequence visualization using alphabetic characters (a-z, A-Z)
 *   - Clear representation of walls (##), start (S), goal (G), and unvisited cells (.)
 *   - Support for both string generation and direct console output
 *   - Handles revisits by showing the most recent visit to each state
 *
 * The visualization is particularly useful for:
 *   - Debugging agent behavior
 *   - Analyzing learning trajectories
 *   - Quick visualization in REPL environments
 *   - Educational demonstrations of reinforcement learning
 */
object ConsoleVisualizer:

  private val glyphs: IndexedSeq[Char] =
    ('a' to 'z') ++ ('A' to 'Z')

  /**
   * Convenience method for direct console output.
   *
   * This side-effecting helper method prints the ASCII visualization directly to the console using println. It's
   * designed for quick visualization during development, debugging, or REPL sessions. The core rendering logic remains
   * pure and is delegated to asciiString.
   *
   * @param start
   *   The starting state of the agent
   * @param goal
   *   The goal state of the agent
   * @param walls
   *   Set of wall positions that block movement
   * @param path
   *   List of states representing the agent's trajectory
   * @param rows
   *   Number of rows in the grid
   * @param cols
   *   Number of columns in the grid
   */
  def render(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): Unit =
    println(asciiString(start, goal, walls, path, rows, cols))

  /**
   * Creates a complete ASCII string representation of the grid world.
   *
   * This pure function generates a single string containing the entire grid visualization with newlines separating each
   * row. The result can be printed directly or used for further processing.
   *
   * @param start
   *   The starting state of the agent
   * @param goal
   *   The goal state of the agent
   * @param walls
   *   Set of wall positions that block movement
   * @param path
   *   List of states representing the agent's trajectory
   * @param rows
   *   Number of rows in the grid
   * @param cols
   *   Number of columns in the grid
   * @return
   *   A complete ASCII representation as a single string
   */
  def asciiString(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): String =
    asciiLines(start, goal, walls, path, rows, cols).mkString("\n")

  /**
   * Generates ASCII representation lines for the grid world.
   *
   * This pure function creates a vector of strings, one for each row of the grid, representing the environment and
   * agent trajectory. Each state is rendered with appropriate symbols based on its role and visit status.
   *
   * Rendering symbols:
   *   - "##" for walls (impassable obstacles)
   *   - "S " for the start position
   *   - "G " for the goal position
   *   - "a"-"z", "A"-"Z" for states in movement sequence (most recent visit shown)
   *   - "* " for movements beyond the 52-character alphabet
   *   - ". " for unvisited, passable states
   *
   * @param start
   *   The starting state of the agent
   * @param goal
   *   The goal state of the agent
   * @param walls
   *   Set of wall positions that block movement
   * @param path
   *   List of states representing the agent's trajectory
   * @param rows
   *   Number of rows in the grid
   * @param cols
   *   Number of columns in the grid
   * @return
   *   Vector of strings, one per grid row
   */
  private def asciiLines(
    start: State,
    goal: State,
    walls: Set[State],
    path: List[State],
    rows: Int,
    cols: Int
  ): Vector[String] =

    // Track all movements in sequence, overwriting previous visits
    val stateToStep: Map[State, Int] =
      path.drop(1).zipWithIndex.foldLeft(Map.empty[State, Int]) {
        case (m, (s, i)) => m.updated(s, i) // Always update, allowing overwrite
      }

    (0 until rows).toVector.map { x =>
      (0 until cols).map { y =>
        val s = State(x, y)
        if walls.contains(s) then "##"
        else if s == start then "S "
        else if s == goal then "G "
        else
          stateToStep.get(s) match
            case Some(i) => s"${glyph(i)} "
            case None => ". "
      }.mkString
    }

  @inline private def glyph(i: Int): Char =
    if i < glyphs.length then glyphs(i) else '*'
