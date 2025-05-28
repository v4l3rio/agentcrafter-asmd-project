package DSL

import common.{Action, State}

import scala.util.Random

/**
 * Q-Learning implementation for the DSL simulation
 */
class QLearner(id: String,
               alpha: Double = 0.1, gamma: Double = 0.99,
               eps0: Double = 0.9, epsMin: Double = 0.05,
               warm: Int = 1500, optimistic: Double = 2.0):
  private val rng = Random()
  private val A = Action.values
  private val Q = scala.collection.mutable.Map
    .empty[(Map[String, State], Action), Double]
    .withDefaultValue(optimistic)
  private var ep = 0

  private def eps =
    if ep < warm then eps0
    else math.max(epsMin,
      eps0 - (eps0 - epsMin) * (ep - warm) / warm)

  private def greedy(s: Map[String, State]): Action =
    val values = A.map(a => Q(s -> a))
    val m = values.max
    val best = A.zip(values).collect { case (a, v) if v == m => a }
    best(rng.nextInt(best.length))

  def choose(s: Map[String, State]): (Action, Boolean) =
    if rng.nextDouble() < eps then (A(rng.nextInt(A.length)), true)
    else (greedy(s), false)

  def update(s: Map[String, State], a: Action, r: Double, s2: Map[String, State]): Unit =
    val bestNext = A.map(a2 => Q(s2 -> a2)).max
    Q((s, a)) = (1 - alpha) * Q((s, a)) + alpha * (r + gamma * bestNext)

  def incEp(): Unit = ep += 1