package agentcrafter.bdd

import agentcrafter.common.{Action, GridWorld, QLearner, State}
import agentcrafter.llmqlearning.loader.QTableLoader
import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

import scala.compiletime.uninitialized
import scala.util.{Failure, Success, Try}

class MultiAgentQTableSteps extends ScalaDsl with EN with Matchers:

  private var agent1: QLearner = uninitialized
  private var agent2: QLearner = uninitialized
  private var agents: Map[String, QLearner] = Map.empty
  private var jsonString: String = ""
  private var results: Map[String, Try[Unit]] = Map.empty

  private def createLearner(goal: State): QLearner =
    val env = GridWorld(rows = 3, cols = 3, walls = Set.empty)
    QLearner(
      goalState = goal,
      goalReward = 100.0,
      updateFunction = env.step,
      resetFunction = () => State(0, 0)
    )

  Given("""two learner agents""") { () =>
    agent1 = createLearner(State(2, 2))
    agent2 = createLearner(State(0, 2))
    agents = Map("Explorer" -> agent1, "Pathfinder" -> agent2)
  }

  Given("""a multi-agent JSON with one valid and one invalid table""") { () =>
    jsonString =
      """{
        "Explorer": {
          "(0,0)": {"Up": 1.5, "Right": 3.0, "Stay": 0.0}
        },
        "Pathfinder": {
          "(0,0)": {"InvalidAction": 1.0}
        }
      }"""
  }

  Given("""a completely invalid multi-agent JSON""") { () =>
    jsonString = "This is not valid JSON at all"
  }

  When("""I load the multi-agent Q-tables""") { () =>
    results = QTableLoader.loadMultiAgentQTablesFromJson(jsonString, agents)
  }

  Then("""the first agent should load its Q-table successfully""") { () =>
    results("Explorer") shouldBe a[Success[?]]
    agent1.getQValue(State(0, 0), Action.Right) shouldBe 3.0
  }

  Then("""the second agent should fall back to default Q-values""") { () =>
    results("Pathfinder") shouldBe a[Success[?]]
    val defaultValue = agent2.getLearningConfig.optimistic
    agent2.getQValue(State(0, 0), Action.Up) shouldBe defaultValue
  }

  Then("""both agents should report failures""") { () =>
    results("Explorer") shouldBe a[Failure[?]]
    results("Pathfinder") shouldBe a[Failure[?]]
  }
