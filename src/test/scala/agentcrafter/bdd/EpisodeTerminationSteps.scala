package agentcrafter.bdd

import agentcrafter.common.{GridWorld, QLearner, State, StepResult, EpisodeOutcome}
import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

import scala.compiletime.uninitialized

class EpisodeTerminationSteps extends ScalaDsl with EN with Matchers:

  private var learner: QLearner = uninitialized
  private var outcome: EpisodeOutcome = uninitialized

  private def createStuckLearner(): QLearner =
    val update = (_: State, _: agentcrafter.common.Action) => StepResult(State(0, 0), 0.0)
    QLearner(
      goalState = State(1, 1),
      goalReward = 0.0,
      updateFunction = update,
      resetFunction = () => State(0, 0)
    )

  Given("""a learner in an environment that never reaches the goal""") { () =>
    learner = createStuckLearner()
  }

  Given("""the learner starts at the goal state""") { () =>
    val env = GridWorld(rows = 1, cols = 1, walls = Set.empty)
    learner = QLearner(State(0, 0), 1.0, env.step, () => State(0, 0))
  }

  When("""an episode runs with a max step limit of {int}""") { (limit: Int) =>
    outcome = learner.episode(limit)
  }

  Then("""the episode should end unsuccessfully after {int} steps""") { (steps: Int) =>
    val (success, count, _) = outcome
    success shouldBe false
    count shouldBe steps
  }

  Then("""the episode should end immediately with success""") { () =>
    val (success, count, _) = outcome
    success shouldBe true
    count shouldBe 0
  }