package common

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.util.Random

class QLearnerTest extends AnyFunSuite with Matchers:

  test("QLearner should initialize with default parameters"):
    val learner = QLearner()
    learner.getId shouldBe "agent"
    learner.getEpsilon shouldBe 0.9 // initial epsilon
  
  test("QLearner should initialize with custom parameters"):
    val learner = QLearner(
      id = "test_agent",
      alpha = 0.2,
      gamma = 0.95,
      eps0 = 0.8,
      epsMin = 0.1,
      warm = 5000,
      optimistic = 1.0
    )
    
    learner.getId shouldBe "test_agent"
    learner.getEpsilon shouldBe 0.8

  test("QLearner should update Q-values correctly"):
    val learner = QLearner(alpha = 0.5, gamma = 0.9, optimistic = 0.0)
    val state1 = State(0, 0)
    val state2 = State(0, 1)
    val action = Action.Right
    
    // Initial Q-value should be optimistic value
    learner.getQValue(state1, action) shouldBe 0.0
    
    // Update Q-value
    learner.update(state1, action, 10.0, state2)
    
    // Q-value should be updated according to Q-learning formula
    // Q(s,a) = (1-α)Q(s,a) + α(r + γ*max(Q(s',a')))
    // = (1-0.5)*0.0 + 0.5*(10.0 + 0.9*0.0) = 5.0
    learner.getQValue(state1, action) shouldBe 5.0

  test("QLearner should handle epsilon decay correctly"):
    val learner = QLearner(
      eps0 = 1.0,
      epsMin = 0.1,
      warm = 100
    )
    
    // Initially should be at eps0
    learner.getEpsilon shouldBe 1.0
    
    // During warm-up period, epsilon should stay at eps0
    for (_ <- 1 to 50) {
      learner.incEp()
    }
    learner.getEpsilon shouldBe 1.0
    
    // After warm-up, epsilon should start decaying
    for (_ <- 51 to 150) {
      learner.incEp()
    }
    learner.getEpsilon should be < 1.0
    learner.getEpsilon should be >= 0.1
    
    // After many episodes, should reach minimum
    for (_ <- 151 to 1000) {
      learner.incEp()
    }
    learner.getEpsilon shouldBe 0.1

  test("QLearner should choose actions with exploration vs exploitation"):
    val learner = QLearner(eps0 = 0.0, epsMin = 0.0) // no exploration
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
    val learner = QLearner(optimistic = 1.0)
    val state1 = State(0, 0)
    val state2 = State(1, 1)
    
    learner.update(state1, Action.Up, 5.0, state2)
    learner.update(state2, Action.Down, 3.0, state1)
    
    val qTable = learner.getQTable
    
    qTable should contain key (state1, Action.Up)
    qTable should contain key (state2, Action.Down)
    qTable((state1, Action.Up)) should not be 1.0 // should be updated
    qTable((state2, Action.Down)) should not be 1.0 // should be updated


  test("QLearner episode method should work with grid environment"):
    val env = GridWorld(
      rows = 3,
      cols = 3,
      start = State(0, 0),
      goal = State(2, 2),
      walls = Set.empty
    )
    val learner = QLearner(gridEnv = Some(env))
    
    val result = learner.episode(maxSteps = 100)
    result shouldBe defined
    
    val (done, steps, trajectory) = result.get
    steps should be > 0
    steps should be <= 50
    trajectory should not be empty
    trajectory.length shouldBe steps
    
    // First state should be start
    trajectory.head._1 shouldBe env.start
    
    // If done, last state should be goal
    if (done) {
      val lastState = trajectory.last._1
      // The last logged state might not be the goal due to how logging works
      // but the episode should have reached the goal
      lastState shouldBe env.goal
    }
  

  test("QLearner episode with exploitOnly should not update Q-values"):
    val env = GridWorld(
      rows = 3,
      cols = 3,
      start = State(0, 0),
      goal = State(2, 2),
      walls = Set.empty
    )
    val learner = QLearner(gridEnv = Some(env), optimistic = 0.0)
    
    // Get initial Q-table
    val initialQTable = learner.getQTable
    
    // Run exploit-only episode
    learner.episode(exploitOnly = true)
    
    // Q-table should be unchanged
    val finalQTable = learner.getQTable
    initialQTable shouldBe finalQTable
  

  test("QLearner should handle id changes"):
    val learner = QLearner(id = "original")
    learner.getId shouldBe "original"
    
    learner.id("new_id")
    learner.getId shouldBe "new_id"

  test("QLearner without grid environment should use map storage"):
    val learner = QLearner(gridEnv = None, optimistic = 2.0)
    val state = State(100, 200) // arbitrary coordinates
    val action = Action.Up
    
    // Should work with any state coordinates
    learner.getQValue(state, action) shouldBe 2.0
    learner.update(state, action, 5.0, State(99, 200))
    learner.getQValue(state, action) should not be 2.0

  test("QLearner Q-learning update formula should be correct"):
    val alpha = 0.3
    val gamma = 0.8
    val learner = QLearner(alpha = alpha, gamma = gamma, optimistic = 0.0)
    
    val s1 = State(0, 0)
    val s2 = State(0, 1)
    val s3 = State(0, 2)
    val action = Action.Right
    
    // Set up some Q-values for s2
    learner.update(s2, Action.Up, 10.0, s3)
    learner.update(s2, Action.Down, 5.0, s3)
    val maxQs2 = math.max(learner.getQValue(s2, Action.Up), learner.getQValue(s2, Action.Down))
    
    val initialQ = learner.getQValue(s1, action)
    val reward = 7.0
    
    learner.update(s1, action, reward, s2)
    
    val expectedQ = (1 - alpha) * initialQ + alpha * (reward + gamma * maxQs2)
    val actualQ = learner.getQValue(s1, action)
    
    actualQ shouldBe expectedQ +- 0.001
  
