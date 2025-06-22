package agentcrafter.examples.basic

import agentcrafter.common.*
import agentcrafter.visualizers.ConsoleVisualizer

import scala.collection.immutable.Vector

/**
 * Main training entry‑point for Q‑Learning on a GridWorld.
 * Prints progress reports and periodically visualises the greedy policy
 * using the (pure) Draw helpers.
 */
@main def Train(): Unit =
  val start = State(0, 1)
  val goal = State(5, 4)

  val env = GridWorld(rows = 10, cols = 10, walls = Set(State(1, 2), State(1, 3), State(2, 3), State(3, 3), State(4, 3), State(5, 3), State(6, 3), State(7, 3), State(8, 3)))
  val agent = QLearner(
    goalState = goal,
    goalReward = 50,
    updateFunction = env.step, // Function to update the environment state based on the agent's action
    resetFunction = () => start
  )
  val episodes = 20_000

  var successes = Vector.empty[Int]

  val reportEvery = 500
  val evaluateEvery = 1_000

  for ep <- 1 to episodes do
    val (done, steps, _) = agent.episode()
    if done then successes :+= steps

    if ep % reportEvery == 0 then
      val window = successes.takeRight(reportEvery)
      val mean = if window.nonEmpty then window.sum.toDouble / window.size else Double.NaN
      println(f"Ep $ep%6d | ε=${agent.eps}%.3f | ⟨steps⟩=$mean%.2f")

    if ep % evaluateEvery == 0 then
      val (_, greedySteps, trajEval) = agent.episode()
      println(s"Greedy policy needs $greedySteps steps:")
      // Reconstruct complete movement sequence including blocked moves
      val allStates = trajEval.foldLeft(List(start)) { case (states, (fromState, action, _, _)) =>
        val nextState = env.step(fromState, action).state
        states :+ nextState
      }
      println(ConsoleVisualizer.asciiString(start, goal, env.walls, allStates, env.rows, env.cols))
