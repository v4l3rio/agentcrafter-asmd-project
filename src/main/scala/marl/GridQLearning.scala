package marl

import scala.collection.mutable.ArrayBuffer
import java.util.Locale


/* ---------- 4.  Pretty-printing ---------- */
object Draw:
  /** letters a-z, then A-Z, then '*', shows first visit order */
  private val glyphs = ('a' to 'z') ++ ('A' to 'Z') :+ '*'
  def traj(env: GridWorld, path: List[State]): Unit =
    val idx = path.tail.zipWithIndex.toMap
    for r <- 0 until env.rows do
      for c <- 0 until env.cols do
        val s = State(r,c)
        val ch =
          if env.walls contains s then "##"
          else if s == env.start  then "S "
          else if s == env.goal   then "G "
          else idx.get(s) match
            case Some(i) => s"${glyphs(math.min(i, glyphs.length-1))} "
            case None    => ". "
        print(ch)
      println()
    println()

/* ---------- 5.  Driver ---------- */
@main def Train(): Unit =
  Locale.setDefault(Locale.US)     // force dot decimal
  val env      = GridWorld()
  val agent    = QAgent(env)
  val episodes = 10_000
  val stepsOK  = ArrayBuffer.empty[Int]   // only episodes that finish

  val reportEvery   = 500
  val evaluateEvery = 1_000

  for ep <- 1 to episodes do
    val (done, steps, _) = agent.episode()
    if done then stepsOK += steps

    if ep % reportEvery == 0 then
      val last = stepsOK.takeRight(reportEvery)
      val mean = if last.nonEmpty then last.sum.toDouble/last.size else Double.NaN
      println(f"Ep $ep%6d | ε=${agent.eps}%.3f | ⟨steps⟩=${mean}%.2f")

    if ep % evaluateEvery == 0 then
      val (_, sEval, trajEval) = agent.episode(exploitOnly = true)
      println(s"Greedy policy needs $sEval steps:")
      Draw.traj(env, trajEval)