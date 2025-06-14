package agentcrafter.examples.basic

import agentcrafter.MARL.visualizers.Visualizer
import agentcrafter.common.{GridWorld, LearningParameters, QLearner, State}

/**
 * Main training program with real-time visualization.
 *
 * This program trains a Q-learning agent on a grid world while providing
 * real-time visual feedback. The visualization shows the agent's movement
 * and decision-making process during greedy policy evaluation episodes.
 *
 * Training configuration:
 * - High initial exploration (ε₀ = 0.9)
 * - Warm-up period of 1,000 episodes with full exploration
 * - Optimistic initialization (Q₀ = 5.0)
 * - Visual evaluation every 500 episodes
 *
 * The visualization displays:
 * - Grid world layout with walls, start, and goal
 * - Agent position and movement
 * - Current state, action, exploration mode, and Q-values
 */
@main def TrainVisualExplain(): Unit =
  val start = State(0, 0) // agent start position
  val goal = State(5, 5) // agent goal position

  val env = GridWorld(rows = 10, cols = 10, walls = Set(State(1, 1), State(1, 2), State(1, 3), State(2, 3), State(3, 3), State(4, 3), State(5, 3), State(6, 3), State(7, 3), State(8, 3)))
  val agent = QLearner(
    goalState = goal,
    goalReward = 50,
    updateFunction = env.step,
    resetFunction = () => start,
    learningParameters = LearningParameters(
      eps0 = 0.95,
      epsMin = 0.1,
      warm = 1_000,
      optimistic = 5.0
    )
  )
  val vis = Visualizer("Q-Learning live debug", env.rows, env.cols, cell = 60, delayMs = 100)
  vis.configureSingleAgent(start, goal, env.walls)

  val episodes = 10_000
  val testEvery = 500

  for ep <- 1 to episodes do
    agent.episode() // training
    if ep % testEvery == 0 then
      val (_, _, path) = agent.episode() // greedy run
      path.foreach { case (s, a, e, q) => vis.updateSingleAgent(s, a, e, q) }
      println(f"Ep $ep%5d | ε=${agent.eps}%.3f | steps=${path.size}")