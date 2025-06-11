package agentcrafter.gridqlearning

import agentcrafter.common.*
import agentcrafter.visualizers.ConsoleVisualizer

import scala.collection.immutable.Vector

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
      println(ConsoleVisualizer.asciiString(start, goal, env.walls, trajEval.map(_._1), env.rows, env.cols))
