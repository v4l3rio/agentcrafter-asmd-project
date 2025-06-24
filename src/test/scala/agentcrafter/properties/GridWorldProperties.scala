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
    val env = GridWorld(rows = rows, cols = cols, walls = Set.empty, stepPenalty = penalty)

    // Test wrapping: State(x, y) where x is column, y is row
    // Up: y decreases, wraps from 0 to rows-1
    val up = env.step(State(1, 0), Action.Up)
    // Down: y increases, wraps from rows-1 to 0
    val down = env.step(State(1, rows - 1), Action.Down)
    // Left: x decreases, wraps from 0 to cols-1
    val left = env.step(State(0, 1), Action.Left)
    // Right: x increases, wraps from cols-1 to 0
    val right = env.step(State(cols - 1, 1), Action.Right)

    (up.state.y == rows - 1) && (down.state.y == 0) &&
    (left.state.x == cols - 1) && (right.state.x == 0) &&
    List(up, down, left, right).forall(_.reward == penalty)
  }

  property("walls block movement and keep reward constant") = forAll(gridSizeGen, stepPenaltyGen) { (size, penalty) =>
    val (rows, cols) = size
    // place a single wall and attempt to move into it
    // Ensure wall is within bounds: from State(0, 0) moving Right goes to State(1, 0)
    val wall = State(1, 0)
    val env = GridWorld(rows = rows, cols = cols, walls = Set(wall), stepPenalty = penalty)
    val result = env.step(State(0, 0), Action.Right)
    result.state == State(0, 0) && result.reward == penalty
  }

  property("step penalty is always returned") =
    forAll(gridSizeGen, stepPenaltyGen, actionGen) { (size, penalty, action) =>
      val (rows, cols) = size
      val env = GridWorld(rows = rows, cols = cols, walls = Set.empty, stepPenalty = penalty)
      val StepResult(_, reward) = env.step(State(0, 0), action)
      reward == penalty
    }
