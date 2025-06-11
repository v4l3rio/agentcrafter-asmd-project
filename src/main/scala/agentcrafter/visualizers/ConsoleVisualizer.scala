package agentcrafter.visualizers

import agentcrafter.common.State

/**
 * Utility functions for producing (pure) ASCII renderings of an agent trajectory
 * in a GridWorld, plus an optional impure helper for quick REPL/CLI use.
 */
object ConsoleVisualizer:
  // finite alphabet a‒z, A‒Z; any step ≥52 is rendered as '*'
  private val glyphs: IndexedSeq[Char] =
    ('a' to 'z') ++ ('A' to 'Z')

  @inline private def glyph(i: Int): Char =
    if i < glyphs.length then glyphs(i) else '*'

  /**
   * Pure function: returns one ASCII line per grid row.
   * The first visit to each state is highlighted with a glyph
   * according to the visit index.
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

  /** Single String rendering (newline‑separated). Pure. */
  def asciiString(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): String =
    asciiLines(start, goal, walls, path, rows, cols).mkString("\n")

  /**
   * Convenience side‑effecting helper – delegates to println.
   * Keeps impure concern *outside* core logic.
   */
  def render(start: State, goal: State, walls: Set[State], path: List[State], rows: Int, cols: Int): Unit =
    println(asciiString(start, goal, walls, path, rows, cols))
