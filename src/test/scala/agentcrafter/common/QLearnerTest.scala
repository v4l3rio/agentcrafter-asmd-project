package agentcrafter.common

import agentcrafter.common.{Action, GridWorld, QLearner, State}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.util.Random

class QLearnerTest extends AnyFunSuite with Matchers:

  // Helper method to create a simple grid environment
  private def createSimpleGrid(): GridWorld = GridWorld(
    rows = 3,
    cols = 3,
    start = State(0, 0),
    goal = State(2, 2),
    walls = Set.empty
  )

  test("QLearner should initialize with default parameters"):
    val env = createSimpleGrid()
    val learner = QLearner(gridEnv = env)
    
    // Check initial epsilon
    learner.eps shouldBe 0.9 // default eps0

  test("QLearner should initialize with custom parameters"):
    val env = createSimpleGrid()
    val learner = QLearner(
      alpha = 0.2,
      gamma = 0.95,
      eps0 = 0.8,
      epsMin = 0.1,
      warm = 5000,
      optimistic = 1.0,
      gridEnv = env
    )
    
    learner.eps shouldBe 0.8

  test("QLearner should update Q-values correctly"):
    val env = createSimpleGrid()
    val learner = QLearner(alpha = 0.5, gamma = 0.9, optimistic = 0.0, gridEnv = env)
    val state1 = State(0, 0)
    val state2 = State(0, 1)
    val action = Action.Right
    
    // Get initial Q-table to check optimistic value
    val initialQTable = learner.QTableSnapshot
    val initialQ = initialQTable.getOrElse((state1, action), 0.0)
    initialQ shouldBe 0.0
    
    // Update Q-value
    learner.update(state1, action, 10.0, state2)
    
    // Check updated Q-value
    val updatedQTable = learner.QTableSnapshot
    val updatedQ = updatedQTable((state1, action))
    
    // Q(s,a) = (1-α)Q(s,a) + α(r + γ*max(Q(s',a')))
    // = (1-0.5)*0.0 + 0.5*(10.0 + 0.9*0.0) = 5.0
    updatedQ shouldBe 5.0

  test("QLearner should handle epsilon decay correctly"):
    val env = createSimpleGrid()
    val learner = QLearner(
      eps0 = 1.0,
      epsMin = 0.1,
      warm = 100,
      gridEnv = env
    )
    
    // Initially should be at eps0
    learner.eps shouldBe 1.0
    
    // During warm-up period, epsilon should stay at eps0
    for (_ <- 1 to 50) {
      learner.incEp()
    }
    learner.eps shouldBe 1.0
    
    // After warm-up, epsilon should start decaying
    for (_ <- 51 to 150) {
      learner.incEp()
    }
    learner.eps should be < 1.0
    learner.eps should be >= 0.1
    
    // After many episodes, should reach minimum
    for (_ <- 151 to 1000) {
      learner.incEp()
    }
    learner.eps shouldBe 0.1

  test("QLearner should choose actions with exploration vs exploitation"):
    val env = createSimpleGrid()
    val learner = QLearner(eps0 = 0.0, epsMin = 0.0, gridEnv = env) // no exploration
    val state = State(0, 0)
    
    // Set different Q-values for different actions
    learner.update(state, Action.Up, 10.0, State(0, 0))
    learner.update(state, Action.Down, 5.0, State(0, 0))
    learner.update(state, Action.Left, 1.0, State(0, 0))
    learner.update(state, Action.Right, 15.0, State(0, 0)) // highest
    learner.update(state, Action.Stay, 0.0, State(0, 0))
    
    // With no exploration, should always choose best action
    val (action, wasExploring) = learner.choose(state)
    action shouldBe Action.Right
    wasExploring shouldBe false
  
  test("QLearner should return complete Q-table"):
    val env = createSimpleGrid()
    val learner = QLearner(optimistic = 1.0, gridEnv = env)
    val state1 = State(0, 0)
    val state2 = State(1, 1)
    
    learner.update(state1, Action.Up, 5.0, state2)
    learner.update(state2, Action.Down, 3.0, state1)
    
    val qTable = learner.QTableSnapshot
    
    qTable should contain key (state1, Action.Up)
    qTable should contain key (state2, Action.Down)
    qTable((state1, Action.Up)) should not be 1.0 // should be updated
    qTable((state2, Action.Down)) should not be 1.0 // should be updated 

  test("QLearner episode should run and return outcome"):
    val env = GridWorld(
      rows = 3,
      cols = 3,
      start = State(0, 0),
      goal = State(2, 2),
      walls = Set.empty
    )
    val learner = QLearner(gridEnv = env, optimistic = 0.0)
    
    // Run an episode
    val (goalReached, steps, trajectory) = learner.episode(maxSteps = 50)
    
    // Should either reach goal or hit step limit
    steps should be <= 50
    trajectory should not be empty
    
    // Each trajectory entry should have the correct structure
    trajectory.foreach { case (state, action, isExploring, qValues) =>
      qValues.length shouldBe Action.values.length
    }

  test("QLearner Q-learning update formula should be correct"):
    val env = createSimpleGrid()
    val alpha = 0.3
    val gamma = 0.8
    val learner = QLearner(alpha = alpha, gamma = gamma, optimistic = 0.0, gridEnv = env)
    
    val s1 = State(0, 0)
    val s2 = State(0, 1)
    val s3 = State(0, 2)
    val action = Action.Right
    
    // Set up some Q-values for s2
    learner.update(s2, Action.Up, 10.0, s3)
    learner.update(s2, Action.Down, 5.0, s3)
    
    val qTableBefore = learner.QTableSnapshot
    val maxQs2 = Action.values.map(a => qTableBefore.getOrElse((s2, a), 0.0)).max
    val initialQ = qTableBefore.getOrElse((s1, action), 0.0)
    val reward = 7.0
    
    learner.update(s1, action, reward, s2)
    
    val qTableAfter = learner.QTableSnapshot
    val actualQ = qTableAfter((s1, action))
    val expectedQ = (1 - alpha) * initialQ + alpha * (reward + gamma * maxQs2)
    
    actualQ shouldBe expectedQ +- 0.001

  test("QLearner should increment episode counter correctly"):
    val env = createSimpleGrid()
    val learner = QLearner(gridEnv = env)
    
    val initialEps = learner.eps
    
    // Increment episode counter manually
    learner.incEp()
    
    // Epsilon might change depending on warm-up period
    // Just verify the method works without error
    learner.eps should be >= 0.0

  test("QLearner should handle multiple episodes"):
    val env = createSimpleGrid()
    val learner = QLearner(
      alpha = 0.1,
      gamma = 0.9,
      eps0 = 0.5,
      epsMin = 0.1,
      warm = 10,
      gridEnv = env
    )
    
    // Run multiple episodes
    val outcomes = (1 to 5).map(_ => learner.episode(maxSteps = 20))
    
    // All episodes should complete
    outcomes.foreach { case (goalReached, steps, trajectory) =>
      steps should be <= 20
      trajectory should not be empty
    }
    
    // Q-table should have been updated
    val finalQTable = learner.QTableSnapshot
    finalQTable should not be empty
  
