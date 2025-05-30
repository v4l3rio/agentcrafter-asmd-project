package common

import scala.collection.mutable
import scala.util.Random
import scala.math.max

/**
 * Unified Q-Learning implementation that supports both general state spaces
 * and optimized grid-based environments
 */
class QLearner(
                var id: String = "agent",
                alpha: Double = 0.1, 
                gamma: Double = 0.99,
                eps0: Double = 0.9, 
                epsMin: Double = 0.15,
                warm: Int = 10_000, 
                optimistic: Double = 0.5,
                // Optional grid optimization
                gridEnv: Option[GridWorld] = None
              ):

  private val rng = Random()
  private val A = Action.values

  // Choose storage based on whether grid environment is provided
  private val useGridOptimization = gridEnv.isDefined
  
  // Map-based storage for general use
  private val QMap = mutable.Map.empty[(State, Action), Double]
    .withDefaultValue(optimistic)
  
  // Array-based storage for grid optimization
  private val QArray = gridEnv.map(env => 
    Array.fill(env.rows, env.cols, A.length)(optimistic)
  )

  def id(newId: String): Unit = id = newId

  private var ep = 0
  private def eps = if ep < warm then eps0
  else math.max(epsMin, eps0 - (eps0-epsMin)*(ep-warm)/warm)

  private def getQValueInternal(state: State, action: Action): Double = 
    if useGridOptimization then
      QArray.get(state.r)(state.c)(action.ordinal)
    else
      QMap(state -> action)

  private def setQValue(state: State, action: Action, value: Double): Unit =
    if useGridOptimization then
      QArray.get(state.r)(state.c)(action.ordinal) = value
    else
      QMap(state -> action) = value

  private def greedy(p: State): Action =
    val vals = A.map(a => getQValueInternal(p, a))
    val m = vals.max
    val best = A.zip(vals).collect{ case (a,v) if v==m => a }
    best(rng.nextInt(best.length))

  def choose(p: State): (Action, Boolean) =
    if rng.nextDouble() < eps then (A(rng.nextInt(A.length)), true)
    else (greedy(p), false)

  def update(p: State, a: Action, r: Double, p2: State): Unit =
    val bestNext = A.map(a2 => getQValueInternal(p2, a2)).max
    val newValue = (1-alpha)*getQValueInternal(p, a) + alpha*(r + gamma*bestNext)
    setQValue(p, a, newValue)

  def incEp(): Unit = ep += 1

  def getQTable: Map[(State, Action), Double] = 
    if useGridOptimization then
      val env = gridEnv.get
      (for {
        r <- 0 until env.rows
        c <- 0 until env.cols
        a <- A
      } yield (State(r, c), a) -> QArray.get(r)(c)(a.ordinal)).toMap
    else
      QMap.toMap

  def getQValue(state: State, action: Action): Double = 
    if useGridOptimization then
      QArray.get(state.r)(state.c)(action.ordinal)
    else
      QMap(state -> action)

  def getId: String = id

  def getEpsilon: Double = eps

  // Additional methods for grid-based episode management (from QAgent)
  def episode(maxSteps: Int = 200, exploitOnly: Boolean = false)
  : Option[(Boolean, Int, List[(State, Action, Boolean, Array[Double])])] =
    gridEnv.map { env =>
      if !exploitOnly then incEp()

      var s = env.reset()
      var steps = 0
      var done = false
      val log = scala.collection.mutable.ArrayBuffer.empty[
        (State, Action, Boolean, Array[Double])]

      while !done && steps < maxSteps do
        val (a, explore) = choose(s)
        val (s2, r, end) = env.step(s, a)

        if !exploitOnly then
          update(s, a, r, s2)

        val qValues = if useGridOptimization then
          QArray.get(s.r)(s.c).clone
        else
          A.map(action => getQValueInternal(s, action)).toArray

        log += ((s, a, explore, qValues))
        s = s2; done = end; steps += 1

      (done, steps, log.toList)
    }