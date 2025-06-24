package agentcrafter.common

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GridWorldTest extends AnyFunSuite with Matchers:

  // Test constants
  private val TEST_GRID_ROWS: Int = 3
  private val TEST_GRID_COLS: Int = 3
  private val TEST_STEP_PENALTY: Double = -3.0
  private val TEST_DEFAULT_GRID_ROWS: Int = 10
  private val TEST_DEFAULT_GRID_COLS: Int = 10

  test("GridWorld initializes with defaults"):
    val env = GridWorld()
    env.rows shouldBe TEST_DEFAULT_GRID_ROWS
    env.cols shouldBe TEST_DEFAULT_GRID_COLS
    env.walls should not be empty

  test("step moves in open space"):
    val env = GridWorld(rows = TEST_GRID_ROWS, cols = TEST_GRID_COLS, walls = Set.empty)
    val StepResult(next, reward) = env.step(State(1, 1), Action.Right)
    next shouldBe State(1, 2)  // Right: x=row stays same, y=column increases
    reward shouldBe TEST_STEP_PENALTY

  test("step blocked by walls and boundaries"):
    val env = GridWorld(rows = TEST_GRID_ROWS, cols = TEST_GRID_COLS, walls = Set(State(1, 2)))
    env.step(State(1, 1), Action.Right).state shouldBe State(1, 1) // Blocked by wall
    env.step(State(0, 0), Action.Up).state shouldBe State(2, 0) // Toroidal wrapping: Up from row 0 goes to row 2
    env.step(State(2, 2), Action.Right).state shouldBe State(2, 0) // Toroidal wrapping: Right from col 2 goes to col 0
