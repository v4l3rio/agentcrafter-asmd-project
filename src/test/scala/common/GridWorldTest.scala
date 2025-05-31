package common

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GridWorldTest extends AnyFunSuite with Matchers:

  test("GridWorld should initialize with default parameters"):
    val env = GridWorld()
    env.rows shouldBe 13
    env.cols shouldBe 15
    env.start shouldBe State(0, 0)
    env.goal shouldBe State(9, 8)
    env.walls should not be empty

  test("GridWorld should initialize with custom parameters"):
    val customStart = State(1, 1)
    val customGoal = State(5, 5)
    val customWalls = Set(State(2, 2), State(3, 3))
    
    val env = GridWorld(
      rows = 10,
      cols = 10,
      start = customStart,
      goal = customGoal,
      walls = customWalls
    )
    
    env.rows shouldBe 10
    env.cols shouldBe 10
    env.start shouldBe customStart
    env.goal shouldBe customGoal
    env.walls shouldBe customWalls

  test("reset should return start position"):
    val env = GridWorld(start = State(2, 3))
    env.reset() shouldBe State(2, 3)

  test("step should move agent correctly in open space"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(2, 2),
      goal = State(4, 4),
      walls = Set.empty
    )
    
    val (nextState, reward, done) = env.step(State(2, 2), Action.Right)
    nextState shouldBe State(2, 3)
    reward shouldBe -3.0 // step penalty
    done shouldBe false
  

  test("step should prevent movement into walls"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(1, 1),
      goal = State(4, 4),
      walls = Set(State(1, 2))
    )
    
    val (nextState, reward, done) = env.step(State(1, 1), Action.Right)
    nextState shouldBe State(1, 1) // should stay in place
    reward shouldBe -3.0
    done shouldBe false

  test("step should prevent movement outside grid boundaries"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(0, 0),
      goal = State(4, 4),
      walls = Set.empty
    )
    
    // Try to move up from top edge
    val (nextState1, _, _) = env.step(State(0, 0), Action.Up)
    nextState1 shouldBe State(0, 0)
    
    // Try to move left from left edge
    val (nextState2, _, _) = env.step(State(0, 0), Action.Left)
    nextState2 shouldBe State(0, 0)
    
    // Try to move down from bottom edge
    val (nextState3, _, _) = env.step(State(4, 4), Action.Down)
    nextState3 shouldBe State(4, 4)
    
    // Try to move right from right edge
    val (nextState4, _, _) = env.step(State(4, 4), Action.Right)
    nextState4 shouldBe State(4, 4)
  

  test("step should detect goal reaching"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(0, 0),
      goal = State(2, 2),
      walls = Set.empty
    )
    
    val (nextState, reward, done) = env.step(State(2, 1), Action.Right)
    nextState shouldBe State(2, 2)
    reward shouldBe 50.0 // goal reward
    done shouldBe true
  

  test("step with Stay action should keep agent in same position"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(2, 2),
      goal = State(4, 4),
      walls = Set.empty
    )
    
    val (nextState, reward, done) = env.step(State(2, 2), Action.Stay)
    nextState shouldBe State(2, 2)
    reward shouldBe -3.0 //TODO: THIS NEED TO CHANGE
    done shouldBe false
  

  test("step should handle goal at start position"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(2, 2),
      goal = State(2, 2),
      walls = Set.empty
    )
    
    val (nextState, reward, done) = env.step(State(2, 2), Action.Stay)
    nextState shouldBe State(2, 2)
    reward shouldBe 50.0 // goal reward
    done shouldBe true
  

  test("multiple steps should work correctly"):
    val env = GridWorld(
      rows = 5,
      cols = 5,
      start = State(0, 0),
      goal = State(2, 2),
      walls = Set.empty
    )
    
    var currentState = env.reset()
    var totalReward = 0.0
    var steps = 0
    var done = false
    
    // Move right twice, then down twice
    val actions = List(Action.Right, Action.Right, Action.Down, Action.Down)
    
    for (action <- actions if !done) {
      val (nextState, reward, isDone) = env.step(currentState, action)
      currentState = nextState
      totalReward += reward
      done = isDone
      steps += 1
    }
    
    currentState shouldBe State(2, 2)
    done shouldBe true
    steps shouldBe 4
    totalReward shouldBe (3 * -3.0 + 50.0) // 3 step penalties + goal reward
  
