package agentcrafter.common

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for QLearner functionality.
 *
 * This test suite verifies the core behavior of the QLearner implementation,
 * including parameter handling, Q-value updates, action selection, and
 * learning convergence properties.
 */
class QLearnerTest extends AnyFunSuite with Matchers:

  // Test constants
  private val TEST_DEFAULT_EPSILON: Double = 0.8
  private val TEST_GRID_ROWS: Int = 3
  private val TEST_GRID_COLS: Int = 3
  private val TEST_GOAL_STATE: State = State(2, 2)
  private val TEST_GOAL_REWARD: Double = 50.0
  private val TEST_RESET_STATE: State = State(0, 0)
  private val TEST_MAX_STEPS: Int = 10
  private val TEST_ALPHA: Double = 0.5
  private val TEST_GAMMA: Double = 0.0
  private val TEST_OPTIMISTIC: Double = 0.0
  private val TEST_REWARD: Double = 10.0
  private val TEST_EXPECTED_Q_VALUE: Double = 5.0
  private val TEST_EPSILON_DECAY_EPS0: Double = 1.0
  private val TEST_EPSILON_DECAY_EPS_MIN: Double = 0.2
  private val TEST_EPSILON_DECAY_WARM: Int = 2
  private val TEST_EPSILON_DECAY_STEPS: Int = 20

  private def learner(lp: LearningConfig = LearningConfig()): QLearner =
    val env = GridWorld(
      rows = TEST_GRID_ROWS,
      cols = TEST_GRID_COLS,
      walls = Set.empty
    )
    QLearner(
      goalState = TEST_GOAL_STATE,
      goalReward = TEST_GOAL_REWARD,
      updateFunction = env.step,
      resetFunction = () => TEST_RESET_STATE,
      learningConfig = lp
    )

  private def env() = GridWorld(rows = TEST_GRID_ROWS, cols = TEST_GRID_COLS, walls = Set.empty)

  test("default epsilon"):
    val l = learner()
    l.eps shouldBe TEST_DEFAULT_EPSILON

  test("custom parameters are honoured"):
    val params = LearningConfig(alpha = 0.2, gamma = 0.8, eps0 = 0.5, epsMin = 0.1, warm = 5, optimistic = 0.0)
    val l = learner(params)
    l.eps shouldBe 0.5

  test("Q-value update"):
    val l = learner(LearningConfig(alpha = TEST_ALPHA, gamma = TEST_GAMMA, optimistic = TEST_OPTIMISTIC))
    val s1 = State(0, 0)
    val s2 = State(0, 1)
    l.update(s1, Action.Right, TEST_REWARD, s2)
    l.getQValue(s1, Action.Right) shouldBe TEST_EXPECTED_Q_VALUE

  test("epsilon decays after warm up"):
    val l = learner(LearningConfig(eps0 = TEST_EPSILON_DECAY_EPS0, epsMin = TEST_EPSILON_DECAY_EPS_MIN, warm = TEST_EPSILON_DECAY_WARM))
    l.eps shouldBe TEST_EPSILON_DECAY_EPS0
    l.incEp();
    l.incEp();
    l.eps shouldBe TEST_EPSILON_DECAY_EPS0
    l.incEp();
    l.eps should be < TEST_EPSILON_DECAY_EPS0
    (1 to TEST_EPSILON_DECAY_STEPS).foreach(_ => l.incEp())
    l.eps shouldBe TEST_EPSILON_DECAY_EPS_MIN

  test("episode returns trajectory"):
    val l = learner()
    val (done, steps, traj) = l.episode(maxSteps = TEST_MAX_STEPS)
    steps should be <= TEST_MAX_STEPS
    traj.nonEmpty shouldBe true
