package agentcrafter.gridqlearning

import agentcrafter.common.*
import scala.collection.immutable.Vector

/**
 * Utility functions for producing (pure) ASCII renderings of an agent trajectory
 * in a GridWorld, plus an optional impure helper for quick REPL/CLI use.
 */
object Draw:
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


/**
 * Main training entry‑point for Q‑Learning on a GridWorld.
 * Prints progress reports and periodically visualises the greedy policy
 * using the (pure) Draw helpers.
 */
@main def Train(): Unit =
  val start = State(0, 0) // agent start position
  val goal = State(5, 5) // agent goal position

  val env = GridWorld(rows = 10, cols = 10, walls = Set(State(1, 1), State(1, 2), State(1, 3), State(2, 3), State(3, 3), State(4, 3), State(5, 3), State(6, 3), State(7, 3), State(8, 3)))
  val agent = QLearner(
    goalState = goal,
    goalReward = 50,
    updateFunction = env.step,
    resetFunction = () => start
  )
  val episodes = 20_000

  var successes = Vector.empty[Int]

  val reportEvery = 500 // progress log cadence
  val evaluateEvery = 1_000 // greedy‑policy evaluation cadence

  for ep <- 1 to episodes do
    val (done, steps, _) = agent.episode()
    if done then successes :+= steps

    if ep % reportEvery == 0 then
      val window = successes.takeRight(reportEvery)
      val mean = if window.nonEmpty then window.sum.toDouble / window.size else Double.NaN
      println(f"Ep $ep%6d | ε=${agent.eps}%.3f | ⟨steps⟩=$mean%.2f")

    if ep % evaluateEvery == 0 then
      val (_, greedySteps, trajEval) = agent.episode() // NB: still learns; refactor QLearner to expose pure greedy run if needed
      println(s"Greedy policy needs $greedySteps steps:")
      println(Draw.asciiString(start, goal, env.walls, trajEval.map(_._1), env.rows, env.cols))
