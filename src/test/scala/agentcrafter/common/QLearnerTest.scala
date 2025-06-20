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

  private def learner(lp: LearningParameters = LearningParameters()): QLearner =
    QLearner(
      goalState = State(2, 2),
      goalReward = 50.0,
      updateFunction = env().step,
      resetFunction = () => State(0, 0),
      learningParameters = lp
    )

  private def env() = GridWorld(rows = 3, cols = 3, walls = Set.empty)

  test("default epsilon"):
    val l = learner()
    l.eps shouldBe 0.9

  test("custom parameters are honoured"):
    val params = LearningParameters(alpha = 0.2, gamma = 0.8, eps0 = 0.5, epsMin = 0.1, warm = 5, optimistic = 0.0)
    val l = learner(params)
    l.eps shouldBe 0.5

  test("Q-value update"):
    val l = learner(LearningParameters(alpha = 0.5, gamma = 0.0, optimistic = 0.0))
    val s1 = State(0, 0)
    val s2 = State(0, 1)
    l.update(s1, Action.Right, 10.0, s2)
    l.getQValue(s1, Action.Right) shouldBe 5.0

  test("epsilon decays after warm up"):
    val l = learner(LearningParameters(eps0 = 1.0, epsMin = 0.2, warm = 2))
    l.eps shouldBe 1.0
    l.incEp();
    l.incEp();
    l.eps shouldBe 1.0
    l.incEp();
    l.eps should be < 1.0
    (1 to 20).foreach(_ => l.incEp())
    l.eps shouldBe 0.2

  test("episode returns trajectory"):
    val l = learner()
    val (done, steps, traj) = l.episode(maxSteps = 10)
    steps should be <= 10
    traj.nonEmpty shouldBe true
