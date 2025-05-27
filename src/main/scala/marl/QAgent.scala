package marl

import scala.collection.mutable.ArrayBuffer
import scala.math.max
import scala.util.Random

class QAgent(
              env:    GridWorld,
              alpha:  Double = 0.1,
              gamma:  Double = 0.99,
              eps0:   Double = 0.9,
              epsMin: Double = 0.01,   // lower than before
              decay:  Double = 0.995
            ):
  private val rng      = new Random()
  private val A        = Action.values
  private val Q        = Array.ofDim[Double](env.rows, env.cols, A.length)
  private var _eps     = eps0
  def eps: Double = _eps          // public read-only

  private def pick(s: State): Action =
    if rng.nextDouble() < _eps then A(rng.nextInt(A.length))
    else
      val row = Q(s.r)(s.c)
      A(row.indexOf(row.max))

  /** run one episode – returns (reachedGoal?, steps, traj) */
  def episode(maxSteps: Int = 200, exploitOnly: Boolean = false)
  : (Boolean, Int, List[State]) =
    val oldEps = _eps
    if exploitOnly then _eps = 0.0                // evaluation mode
    var s     = env.reset()
    val path  = ArrayBuffer[State](s)
    var steps = 0
    var done  = false
    while !done && steps < maxSteps do
      val a            = pick(s)
      val (s2, r, end) = env.step(s, a)

      // -- learning only if not evaluation run
      if !exploitOnly then
        val bestNext = Q(s2.r)(s2.c).max
        Q(s.r)(s.c)(a.ordinal) =
          (1-alpha)*Q(s.r)(s.c)(a.ordinal) + alpha*(r + gamma*bestNext)

      s     = s2
      done  = end
      steps += 1
      path  += s
    if !exploitOnly then _eps = max(epsMin, _eps * decay)
    else _eps = oldEps                                  // restore ε
    (done, steps, path.toList)