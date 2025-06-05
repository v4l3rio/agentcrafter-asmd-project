package agentcrafter.gridqlearning

import agentcrafter.common.*
import scala.collection.mutable.ArrayBuffer

/**
 * Utility object for visualizing agent trajectories in a grid world.
 *
 * Provides methods to draw ASCII representations of grid worlds with
 * agent paths, showing the order in which states were visited.
 */
object Draw:
  /**
   * Character sequence used to represent visit order in trajectory visualization.
   * Uses letters a-z, then A-Z, then '*' for states visited beyond 52 steps.
   */
  private val glyphs = ('a' to 'z') ++ ('A' to 'Z') :+ '*'

  /**
   * Visualizes an agent's trajectory through a grid world.
   *
   * Prints an ASCII representation of the grid where:
   * - "##" represents walls
   * - "S " represents the start position
   * - "G " represents the goal position
   * - Letters (a-z, A-Z) show the order of first visits to each state
   * - ". " represents unvisited states
   *
   * @param env The grid world environment
   * @param path List of states representing the agent's trajectory
   */
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


/**
 * Main training program for Q-Learning on a grid world.
 *
 * This program trains a Q-learning agent on a default grid world environment
 * for 10,000 episodes. It provides periodic progress reports showing the
 * current epsilon value and average steps per episode, and periodically
 * evaluates the learned policy by running greedy episodes.
 *
 * Training features:
 * - Reports progress every 500 episodes
 * - Evaluates greedy policy every 1,000 episodes
 * - Visualizes the greedy trajectory during evaluation
 * - Tracks only successful episodes (those that reach the goal)
 */
@main def Train(): Unit =
  val env      = GridWorld()
  val agent    = QLearner(gridEnv = env)
  val episodes = 10_000
  val stepsOK  = ArrayBuffer.empty[Int]   // only episodes that finish

  val reportEvery   = 500   // Report progress every 500 episodes
  val evaluateEvery = 1_000 // Evaluate greedy policy every 1,000 episodes

  for ep <- 1 to episodes do
    val (done, steps, _) = agent.episode()
    if done then stepsOK += steps

    if ep % reportEvery == 0 then
      val last = stepsOK.takeRight(reportEvery)
      val mean = if last.nonEmpty then last.sum.toDouble/last.size else Double.NaN
      println(f"Ep $ep%6d | ε=${agent.eps}%.3f | ⟨steps⟩=${mean}%.2f")

    if ep % evaluateEvery == 0 then
      val (_, sEval, trajEval) = agent.episode()
      println(s"Greedy policy needs $sEval steps:")
      val states: List[State] = trajEval.map(_._1)
      Draw.traj(env, states)