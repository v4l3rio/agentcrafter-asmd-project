package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for the GridWorld environment. These tests focus on edge cases like toroidal wrapping and wall
 * collisions.
 */
object GridWorldProperties extends Properties("GridWorld") with Matchers:

  private val gridSizeGen: Gen[(Int, Int)] =
    for
      rows <- Gen.choose(2, 10)
      cols <- Gen.choose(2, 10)
    yield (rows, cols)

  private val stepPenaltyGen: Gen[Double] = Gen.choose(-10.0, -1.0)
  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)

  property("toroidal wrapping works on all edges") = forAll(gridSizeGen, stepPenaltyGen) { (size, penalty) =>
    val (rows, cols) = size
    // Create a grid world without walls to test pure toroidal wrapping behavior
    val env = GridWorld(rows = rows, cols = cols, walls = Set.empty, stepPenalty = penalty)

    // Test wrapping behavior on all four edges of the grid
    // Note: State(x, y) where x is column index, y is row index
    
    // Test UP edge wrapping: moving up from top row (y=0) should wrap to bottom row (y=rows-1)
    // Start from State(1, 0) - column 1, top row
    val up = env.step(State(1, 0), Action.Up)
    
    // Test DOWN edge wrapping: moving down from bottom row should wrap to top row (y=0)
    // Start from State(1, rows-1) - column 1, bottom row
    val down = env.step(State(1, rows - 1), Action.Down)
    
    // Test LEFT edge wrapping: moving left from leftmost column (x=0) should wrap to rightmost column (x=cols-1)
    // Start from State(0, 1) - leftmost column, row 1
    val left = env.step(State(0, 1), Action.Left)
    
    // Test RIGHT edge wrapping: moving right from rightmost column should wrap to leftmost column (x=0)
    // Start from State(cols-1, 1) - rightmost column, row 1
    val right = env.step(State(cols - 1, 1), Action.Right)

    // Verify all wrapping behaviors work correctly
    (up.state.y == rows - 1) && (down.state.y == 0) &&
    (left.state.x == cols - 1) && (right.state.x == 0) &&
    // Also verify that step penalty is consistently applied regardless of wrapping
    List(up, down, left, right).forall(_.reward == penalty)
  }

  property("walls block movement and keep reward constant") = forAll(gridSizeGen, stepPenaltyGen) { (size, penalty) =>
    val (rows, cols) = size
    
    // Test that walls properly block agent movement and maintain consistent reward structure
    // Place a single wall at State(1, 0) - column 1, row 0
    // This ensures the wall is within grid bounds for any valid grid size (min 2x2)
    val wall = State(1, 0)
    val env = GridWorld(rows = rows, cols = cols, walls = Set(wall), stepPenalty = penalty)
    
    // Attempt to move from State(0, 0) to the wall at State(1, 0) using Action.Right
    // Expected behavior: agent should remain at original position due to wall collision
    val result = env.step(State(0, 0), Action.Right)
    
    // Verify two key behaviors:
    // 1. Agent position unchanged (wall blocks movement)
    // 2. Step penalty still applied (consistent reward structure)
    result.state == State(0, 0) && result.reward == penalty
  }

  property("step penalty is always returned") =
    forAll(gridSizeGen, stepPenaltyGen, actionGen) { (size, penalty, action) =>
      val (rows, cols) = size
      
      // Test that step penalty is consistently applied regardless of action type or grid configuration
      // Create a clean grid world without walls to isolate penalty behavior testing
      val env = GridWorld(rows = rows, cols = cols, walls = Set.empty, stepPenalty = penalty)
      
      // Execute any random action from State(0, 0) and extract the reward
      // This tests all possible actions: Up, Down, Left, Right, Stay
      val StepResult(_, reward) = env.step(State(0, 0), action)
      
      // Verify that regardless of action type or resulting state change,
      // the reward always equals the configured step penalty
      reward == penalty
    }
