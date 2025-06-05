package agentcrafter.gridqlearning

import agentcrafter.common.{Action, GridWorld, QLearner, State, StepResult}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ArrayBuffer

class GridQLearningIntegrationTest extends AnyFunSuite with Matchers:

  test("QLearner should work with grid environment"):
    val env = GridWorld(
      rows = 3,
      cols = 3,
      start = State(0, 0),
      goal = State(2, 2),
      walls = Set.empty
    )
    val agent = QLearner(
      alpha = 0.15,
      gamma = 0.9,
      eps0 = 0.5,
      epsMin = 0.1,
      warm = 100,
      gridEnv = env
    )
    
    // Test that episode method works
    val (done, steps, trajectory) = agent.episode()

    steps should be > 0
    trajectory should not be empty
    trajectory.head._1 shouldBe env.start

