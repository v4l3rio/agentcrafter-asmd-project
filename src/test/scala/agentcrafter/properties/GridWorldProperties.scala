package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for the GridWorld environment.
 * These tests focus on edge cases like toroidal wrapping
 * and wall collisions.
 */
object GridWorldProperties extends Properties("GridWorld") with Matchers:

  private val gridSizeGen: Gen[(Int, Int)] = for
    rows <- Gen.choose(2, 10)
    cols <- Gen.choose(2, 10)
  yield (rows, cols)

  private val stepPenaltyGen: Gen[Double] = Gen.choose(-10.0, -1.0)
  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)

  property("toroidal wrapping works on all edges") = forAll(gridSizeGen, stepPenaltyGen) { (size, penalty) =>
    val (rows, cols) = size
    val env = GridWorld(rows = rows, cols = cols, walls = Set.empty, stepPenalty = penalty)

    val up = env.step(State(0, 1), Action.Up)
    val down = env.step(State(rows - 1, 1), Action.Down)
    val left = env.step(State(1, 0), Action.Left)
    val right = env.step(State(1, cols - 1), Action.Right)

    (up.state.x == rows - 1) && (down.state.x == 0) &&
      (left.state.y == cols - 1) && (right.state.y == 0) &&
      List(up, down, left, right).forall(_.reward == penalty)
  }

  property("walls block movement and keep reward constant") = forAll(gridSizeGen, stepPenaltyGen) { (size, penalty) =>
    val (rows, cols) = size
    // place a single wall and attempt to move into it
    val wall = State(1, 1)
    val env = GridWorld(rows = rows, cols = cols, walls = Set(wall), stepPenalty = penalty)
    val result = env.step(State(1, 0), Action.Right)
    result.state == State(1, 0) && result.reward == penalty
  }

  property("step penalty is always returned") = forAll(gridSizeGen, stepPenaltyGen, actionGen) { (size, penalty, action) =>
    val (rows, cols) = size
    val env = GridWorld(rows = rows, cols = cols, walls = Set.empty, stepPenalty = penalty)
    val StepResult(_, reward) = env.step(State(0, 0), action)
    reward == penalty
  }