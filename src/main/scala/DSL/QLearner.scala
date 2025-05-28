package DSL

import common.{Action, State}

import scala.collection.mutable
import scala.util.Random

/**
 * Q-Learning implementation for the DSL simulation
 */
class QLearner(
                id: String,
                alpha: Double = 0.1, gamma: Double = 0.99,
                eps0: Double = 0.9, epsMin: Double = 0.15,
                warm: Int = 10_000, optimistic: Double = 0.5):

  private val rng = Random()
  private val A   = Action.values

  // *** Q-table: (Pos,Action) invece di (Map,Action) ******************
  private val Q = mutable.Map.empty[(State, Action), Double]
    .withDefaultValue(optimistic)

  private var ep = 0
  private def eps = if ep < warm then eps0
  else math.max(epsMin,
    eps0 - (eps0-epsMin)*(ep-warm)/warm)

  private def greedy(p: State): Action =
    val vals  = A.map(a => Q(p -> a))
    val m     = vals.max
    val best  = A.zip(vals).collect{ case (a,v) if v==m => a }
    best(rng.nextInt(best.length))

  // --------- API -----------------------------------------------------
  def choose(p: State): (Action, Boolean) =
    if rng.nextDouble() < eps then (A(rng.nextInt(A.length)), true)
    else (greedy(p), false)

  def update(p: State, a: Action, r: Double, p2: State): Unit =
    val bestNext = A.map(a2 => Q(p2 -> a2)).max
    Q(p -> a) = (1-alpha)*Q(p -> a) + alpha*(r + gamma*bestNext)

  def incEp(): Unit = ep += 1

  // --------- New methods for Q-table access -------------------------
  def getQTable: Map[(State, Action), Double] = Q.toMap

  def getQValue(state: State, action: Action): Double = Q(state -> action)

  def getId: String = id

  def getEpsilon: Double = eps