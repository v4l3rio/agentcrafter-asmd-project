package marl

import scala.util.Random
import scala.math.max

class QAgent(
              env: GridWorld,
              alpha: Double = 0.1,
              gamma: Double = 0.99,
              epsStart: Double = 0.9,
              epsMin: Double   = 0.05,
              warmUpEpisodes: Int = 1_000,
              optimistic: Double = 5.0
            ):
  private val rng   = new Random()
  private val A     = Action.values
  private val Q     = Array.fill(env.rows, env.cols, A.length)(optimistic)

  private var episodeCnt = 0
  def eps: Double =                                 // ϵ piece-wise
    if episodeCnt < warmUpEpisodes then epsStart
    else
      // decrescita lineare negli episodi rimanenti
      val frac = (episodeCnt - warmUpEpisodes).toDouble / warmUpEpisodes
      max(epsMin, epsStart - (epsStart - epsMin) * frac)

  /** tie-break casuale fra le azioni con valore massimo */
  private def argMaxRandom(qRow: Array[Double]): Action =
    val maxVal = qRow.max
    val bestIdx = qRow.zipWithIndex.collect { case (v,i) if v == maxVal => i }
    A(bestIdx(rng.nextInt(bestIdx.length)))

  /** pick ⇒ (action, explore?) */
  private def pick(s: State): (Action, Boolean) =
    if rng.nextDouble() < eps then (A(rng.nextInt(A.length)), true)
    else (argMaxRandom(Q(s.r)(s.c)), false)

  /** episodio ⇒ (goal?, steps, log) */
  def episode(maxSteps: Int = 200, exploitOnly: Boolean = false)
  : (Boolean, Int, List[(State, Action, Boolean, Array[Double])]) =
    if !exploitOnly then episodeCnt += 1

    val savedEps = eps
    var s     = env.reset()
    var steps = 0
    var done  = false
    val log   = scala.collection.mutable.ArrayBuffer.empty[
      (State, Action, Boolean, Array[Double])]

    while !done && steps < maxSteps do
      val (a, explore) = pick(s)
      val (s2, r, end) = env.step(s, a)

      if !exploitOnly then
        val bestNext = Q(s2.r)(s2.c).max
        Q(s.r)(s.c)(a.ordinal) =
          (1 - alpha) * Q(s.r)(s.c)(a.ordinal) +
            alpha * (r + gamma * bestNext)

      log += ((s, a, explore, Q(s.r)(s.c).clone))
      s = s2; done = end; steps += 1

    (done, steps, log.toList)
