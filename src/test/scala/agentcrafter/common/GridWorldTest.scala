package agentcrafter.common

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GridWorldTest extends AnyFunSuite with Matchers:

  test("GridWorld initializes with defaults"):
    val env = GridWorld()
    env.rows shouldBe 13
    env.cols shouldBe 15
    env.walls should not be empty

  test("step moves in open space"):
    val env = GridWorld(rows = 3, cols = 3, walls = Set.empty)
    val StepResult(next, reward) = env.step(State(1, 1), Action.Right)
    next shouldBe State(1, 2)
    reward shouldBe -3.0

  test("step blocked by walls and boundaries"):
    val env = GridWorld(rows = 3, cols = 3, walls = Set(State(1, 2)))
    env.step(State(1, 1), Action.Right).state shouldBe State(1, 1)
    env.step(State(0, 0), Action.Up).state shouldBe State(0, 0)
    env.step(State(2, 2), Action.Right).state shouldBe State(2, 2)
