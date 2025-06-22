package agentcrafter.examples.basic

import agentcrafter.marl.visualizers.Visualizer
import agentcrafter.common.{GridWorld, LearningConfig, QLearner, State, Constants}

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
  val start = State(0, 0)
  val goal = State(5, 5)

  val env = GridWorld(rows = 10, cols = 10, walls = Set(State(1, 1), State(1, 2), State(1, 3), State(2, 3), State(3, 3), State(4, 3), State(5, 3), State(6, 3), State(7, 3), State(8, 3)))
  val agent = QLearner(
    goalState = goal,
    goalReward = 50,
    updateFunction = env.step,
    resetFunction = () => start,
    learningConfig = LearningConfig(
      eps0 = 0.95,
      epsMin = 0.1,
      warm = 1_000,
      optimistic = 5.0
    )
  )
  val vis = Visualizer("Q-Learning live debug", env.rows, env.cols, cell = 60, delayMs = 50)
  vis.configureSingleAgent(start, goal, env.walls)

  val episodes = 10_000
  val testEvery = 500
  val printEvery = 100

  for ep <- 1 to episodes do
    agent.episode()
    if ep % testEvery == 0 then
      val (success, steps, path) = agent.episode()
      
      // Calculate episode reward: step penalties + goal reward if successful
      val stepPenalties = steps * Constants.DEFAULT_STEP_PENALTY
      val episodeReward = if success then stepPenalties + 50 else stepPenalties
      
      // Real-time visualization: show every step with accumulating reward
      var cumulativeReward = 0.0
      path.zipWithIndex.foreach { case ((s, a, e, q), stepIndex) => 
        // Add step penalty for each step
        cumulativeReward += Constants.DEFAULT_STEP_PENALTY
        
        // Add goal reward if this is the final step and successful
        val currentReward = if (stepIndex == path.length - 1 && success) then cumulativeReward + 50 else cumulativeReward
        
        vis.updateSingleAgent(s, a, e, q, stepIndex + 1, ep)
        vis.updateSimulationInfo(ep, stepIndex == path.length - 1 && success, currentReward, agent.eps)
      }
      
      println(f"Ep $ep%5d | ε=${agent.eps}%.3f | steps=${path.size} | reward=${episodeReward}%.1f")
    
    // Print progress less frequently to avoid console spam
    if ep % printEvery == 0 then
      println(f"Training progress: Ep $ep%5d | ε=${agent.eps}%.3f")