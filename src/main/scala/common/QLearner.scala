package common

import scala.collection.mutable
import scala.util.Random
import scala.math.max

/**
 * Unified Q-Learning implementation that supports both general state spaces
 * and optimized grid-based environments.
 * 
 * This class implements the Q-Learning algorithm, a model-free reinforcement learning
 * method that learns the optimal action-value function Q(s,a). It supports two storage
 * modes for efficiency:
 * - Map-based storage for general state spaces
 * - Array-based storage for grid environments (memory and performance optimized)
 * 
 * Key features:
 * - Epsilon-greedy exploration with decay schedule
 * - Configurable learning parameters (alpha, gamma)
 * - Warm-up period for exploration
 * - Optimistic initialization of Q-values
 * - Episode management for grid environments
 * - Support for both learning and exploitation modes
 * 
 * The algorithm follows the standard Q-Learning update rule:
 * Q(s,a) ← (1-α)Q(s,a) + α[r + γ max Q(s',a')]
 * 
 * @param id Unique identifier for this agent
 * @param alpha Learning rate (0 < α ≤ 1), controls how much new information overrides old
 * @param gamma Discount factor (0 ≤ γ ≤ 1), determines importance of future rewards
 * @param eps0 Initial exploration rate for epsilon-greedy policy
 * @param epsMin Minimum exploration rate after decay
 * @param warm Number of episodes for warm-up period (constant exploration)
 * @param optimistic Initial Q-value for all state-action pairs (optimistic initialization)
 * @param gridEnv Optional grid environment for optimized array-based storage
 * 
 * @example
 * {{{
 * // Create a Q-learner for general use
 * val learner = QLearner(id = "agent1", alpha = 0.1, gamma = 0.9)
 * 
 * // Create a Q-learner optimized for grid environments
 * val env = GridWorld(rows = 5, cols = 5)
 * val gridLearner = QLearner(id = "grid_agent", gridEnv = Some(env))
 * 
 * // Update Q-value based on experience
 * learner.update(State(0,0), Action.Right, 10.0, State(0,1))
 * 
 * // Choose action using epsilon-greedy policy
 * val (action, wasExploring) = learner.choose(State(0,0))
 * }}}
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

  /** Random number generator for stochastic action selection */
  private val rng = Random()
  /** Array of all possible actions */
  private val A = Action.values

  /** Flag indicating whether to use grid-optimized storage */
  private val useGridOptimization = gridEnv.isDefined
  
  /** Map-based Q-table storage for general state spaces */
  private val QMap = mutable.Map.empty[(State, Action), Double]
    .withDefaultValue(optimistic)
  
  /** Array-based Q-table storage for grid environments (memory efficient) */
  private val QArray = gridEnv.map(env => 
    Array.fill(env.rows, env.cols, A.length)(optimistic)
  )

  /**
   * Updates the agent's identifier.
   * 
   * @param newId The new identifier for this agent
   */
  def id(newId: String): Unit = id = newId

  /** Current episode number for epsilon decay calculation */
  private var ep = 0
  
  /**
   * Calculates the current exploration rate (epsilon) based on episode number.
   * 
   * During the warm-up period, epsilon remains at eps0. After warm-up,
   * it decays linearly from eps0 to epsMin over the next 'warm' episodes.
   * 
   * @return Current epsilon value for epsilon-greedy policy
   */
  private def eps = if ep < warm then eps0
  else math.max(epsMin, eps0 - (eps0-epsMin)*(ep-warm)/warm)

  /**
   * Internal method to retrieve Q-value for a state-action pair.
   * 
   * Uses either array-based or map-based storage depending on configuration.
   * 
   * @param state The state to query
   * @param action The action to query
   * @return The Q-value for the given state-action pair
   */
  private def getQValueInternal(state: State, action: Action): Double = 
    if useGridOptimization then
      QArray.get(state.r)(state.c)(action.ordinal)
    else
      QMap(state -> action)

  /**
   * Internal method to set Q-value for a state-action pair.
   * 
   * Uses either array-based or map-based storage depending on configuration.
   * 
   * @param state The state to update
   * @param action The action to update
   * @param value The new Q-value to set
   */
  private def setQValue(state: State, action: Action, value: Double): Unit =
    if useGridOptimization then
      QArray.get(state.r)(state.c)(action.ordinal) = value
    else
      QMap(state -> action) = value

  /**
   * Selects the greedy action (highest Q-value) for a given state.
   * 
   * If multiple actions have the same maximum Q-value, one is chosen randomly
   * to break ties.
   * 
   * @param p The state for which to select the greedy action
   * @return The action with the highest Q-value (ties broken randomly)
   */
  private def greedy(p: State): Action =
    val vals = A.map(a => getQValueInternal(p, a))
    val m = vals.max
    val best = A.zip(vals).collect{ case (a,v) if v==m => a }
    best(rng.nextInt(best.length))

  /**
   * Chooses an action using epsilon-greedy policy.
   * 
   * With probability epsilon, a random action is chosen (exploration).
   * With probability (1-epsilon), the greedy action is chosen (exploitation).
   * 
   * @param p The current state
   * @return A tuple containing:
   *         - the chosen action
   *         - boolean indicating whether this was an exploratory action
   */
  def choose(p: State): (Action, Boolean) =
    if rng.nextDouble() < eps then (A(rng.nextInt(A.length)), true)
    else (greedy(p), false)

  /**
   * Updates the Q-value for a state-action pair using the Q-learning update rule.
   * 
   * Implements: Q(s,a) ← (1-α)Q(s,a) + α[r + γ max Q(s',a')]
   * 
   * @param p The current state
   * @param a The action taken
   * @param r The reward received
   * @param p2 The next state reached
   */
  def update(p: State, a: Action, r: Double, p2: State): Unit =
    val bestNext = A.map(a2 => getQValueInternal(p2, a2)).max
    val newValue = (1-alpha)*getQValueInternal(p, a) + alpha*(r + gamma*bestNext)
    setQValue(p, a, newValue)

  /**
   * Increments the episode counter.
   * 
   * This affects the epsilon decay schedule for exploration.
   */
  def incEp(): Unit = ep += 1

  /**
   * Returns a complete copy of the Q-table as an immutable map.
   * 
   * For grid-optimized storage, this converts the internal array representation
   * to a map format. For general storage, this returns a copy of the internal map.
   * 
   * @return Map from (State, Action) pairs to their Q-values
   */
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

  /**
   * Retrieves the Q-value for a specific state-action pair.
   * 
   * @param state The state to query
   * @param action The action to query
   * @return The Q-value for the given state-action pair
   */
  def getQValue(state: State, action: Action): Double = 
    if useGridOptimization then
      QArray.get(state.r)(state.c)(action.ordinal)
    else
      QMap(state -> action)

  /**
   * Returns the agent's identifier.
   * 
   * @return The current agent ID
   */
  def getId: String = id

  /**
   * Returns the current exploration rate (epsilon).
   * 
   * @return The current epsilon value used in epsilon-greedy policy
   */
  def getEpsilon: Double = eps

  /**
   * Runs a complete episode in the grid environment (if available).
   * 
   * This method is only available when the QLearner is configured with a GridWorld.
   * It runs the agent from the start state until either the goal is reached or
   * the maximum number of steps is exceeded.
   * 
   * The method can operate in two modes:
   * - Learning mode (default): Updates Q-values and increments episode counter
   * - Exploitation mode: Only follows the current policy without learning
   * 
   * @param maxSteps Maximum number of steps allowed in the episode
   * @param exploitOnly If true, no learning occurs (pure exploitation)
   * @return Option containing a tuple with:
   *         - Boolean: whether the episode completed successfully (goal reached)
   *         - Int: number of steps taken
   *         - List: trajectory log with (state, action, wasExploring, qValues) for each step
   *         Returns None if no grid environment is configured
   * 
   * @example
   * {{{
   * val env = GridWorld()
   * val learner = QLearner(gridEnv = Some(env))
   * 
   * // Run a learning episode
   * learner.episode() match {
   *   case Some((done, steps, trajectory)) =>
   *     println(s"Episode completed: $done, Steps: $steps")
   *   case None =>
   *     println("No grid environment configured")
   * }
   * 
   * // Run an exploitation-only episode
   * val result = learner.episode(exploitOnly = true)
   * }}}
   */
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