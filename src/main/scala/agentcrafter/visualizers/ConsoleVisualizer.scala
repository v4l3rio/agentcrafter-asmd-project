package agentcrafter.visualizers

import agentcrafter.common.State

/**
 * ASCII-based visualization utility for grid world environments and agent trajectories.
 *
 * This object provides pure functional methods for rendering grid worlds as ASCII art,
 * showing agent paths, walls, start/goal positions, and visit sequences. The visualization
 * uses a character-based system to represent the order in which states were visited,
 * making it easy to analyze agent behavior and learning progress in console environments.
 *
 * Key features:
 * - Pure functional rendering (no side effects in core logic)
 * - Visit order visualization using alphabetic characters (a-z, A-Z)
 * - Clear representation of walls (##), start (S), goal (G), and unvisited cells (.)
 * - Support for both string generation and direct console output
 * - Handles revisits by showing only the first visit to each state
 *
 * The visualization is particularly useful for:
 * - Debugging agent behavior
 * - Analyzing learning trajectories
 * - Quick visualization in REPL environments
 * - Educational demonstrations of reinforcement learning
 */
object ConsoleVisualizer:
  // finite alphabet a‒z, A‒Z; any step ≥52 is rendered as '*'
  private val glyphs: IndexedSeq[Char] =
    ('a' to 'z') ++ ('A' to 'Z')

  @inline private def glyph(i: Int): Char =
    if i < glyphs.length then glyphs(i) else '*'

  /**
   * Generates ASCII representation lines for the grid world.
   *
   * This pure function creates a vector of strings, one for each row of the grid,
   * representing the environment and agent trajectory. Each state is rendered with
   * appropriate symbols based on its role and visit status.
   *
   * Rendering symbols:
   * - "##" for walls (impassable obstacles)
   * - "S " for the start position
   * - "G " for the goal position
   * - "a"-"z", "A"-"Z" for visited states (in visit order)
   * - "* " for visits beyond the 52-character alphabet
   * - ". " for unvisited, passable states
   *
   * @param start The starting state of the agent
   * @param goal  The goal state of the agent
   * @param walls Set of wall positions that block movement
   * @param path  List of states representing the agent's trajectory
   * @param rows  Number of rows in the grid
   * @param cols  Number of columns in the grid
   * @return Vector of strings, one per grid row
   */
  private def asciiLines(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): Vector[String] =
    // Map first visit only (ignore revisits)
    val firstVisit: Map[State, Int] =
      path.drop(1).zipWithIndex.foldLeft(Map.empty[State, Int]) {
        case (m, (s, i)) => if m.contains(s) then m else m.updated(s, i)
      }

    (0 until rows).toVector.map { r =>
      (0 until cols).map { c =>
        val s = State(r, c)
        if walls.contains(s) then "##"
        else if s == start then "S "
        else if s == goal then "G "
        else firstVisit.get(s) match
          case Some(i) => s"${glyph(i)} "
          case None => ". "
      }.mkString
    }

  /**
   * Creates a complete ASCII string representation of the grid world.
   *
   * This pure function generates a single string containing the entire grid
   * visualization with newlines separating each row. The result can be printed
   * directly or used for further processing.
   *
   * @param start The starting state of the agent
   * @param goal  The goal state of the agent
   * @param walls Set of wall positions that block movement
   * @param path  List of states representing the agent's trajectory
   * @param rows  Number of rows in the grid
   * @param cols  Number of columns in the grid
   * @return A complete ASCII representation as a single string
   */
  def asciiString(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): String =
    asciiLines(start, goal, walls, path, rows, cols).mkString("\n")

  /**
   * Convenience method for direct console output.
   *
   * This side-effecting helper method prints the ASCII visualization directly
   * to the console using println. It's designed for quick visualization during
   * development, debugging, or REPL sessions. The core rendering logic remains
   * pure and is delegated to asciiString.
   *
   * @param start The starting state of the agent
   * @param goal  The goal state of the agent
   * @param walls Set of wall positions that block movement
   * @param path  List of states representing the agent's trajectory
   * @param rows  Number of rows in the grid
   * @param cols  Number of columns in the grid
   */
  def render(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): Unit =
    println(asciiString(start, goal, walls, path, rows, cols))
