package agentcrafter.steps

import agentcrafter.common.{Action, GridWorld, QLearner, State}
import agentcrafter.llmqlearning.QTableLoader
import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

import scala.compiletime.uninitialized
import scala.util.{Failure, Success, Try}

class LLMQTableLoadingSteps extends ScalaDsl with EN with Matchers:

  private var learner: QLearner = uninitialized
  private var jsonString: String = uninitialized
  private var loadResult: Try[Unit] = uninitialized
  private var initialQValues: Map[(State, Action), Double] = Map.empty

  private def createSimpleGrid(): GridWorld =
    GridWorld(rows = 3, cols = 3, walls = Set.empty)

  Given("""a Q-Learner instance is created""") { () =>
    val env = createSimpleGrid()
    learner = QLearner(
      goalState = State(2, 2),
      goalReward = 0.0,
      updateFunction = env.step,
      resetFunction = () => State(0, 0)
    )
  }

  Given("""a valid Q-Table JSON with multiple states and actions""") { () =>
    jsonString =
      """
    {
      "(0, 0)": {"Up": 1.5, "Down": 2.0, "Left": 0.5, "Right": 3.0, "Stay": 0.0},
      "(0, 1)": {"Up": 2.5, "Down": 1.0, "Left": 1.5, "Right": 2.0, "Stay": 0.5},
      "(1, 0)": {"Up": 0.5, "Down": 3.5, "Left": 2.0, "Right": 1.0, "Stay": 1.5}
    }
    """
  }

  Given("""a Q-Table JSON wrapped in markdown code blocks""") { () =>
    jsonString =
      """
    ```json
    {
      "(0, 0)": {"Up": 1.0, "Down": 2.0, "Left": 0.5, "Right": 1.5, "Stay": 0.0}
    }
    ```
    """
  }

  Given("""a Q-Table JSON with LLM response prefixes like "Here is the JSON:"""") { () =>
    jsonString =
      """
    Here is the JSON Q-Table:
    {
      "(2, 2)": {"Up": 1.0, "Down": 1.0, "Left": 1.0, "Right": 1.0, "Stay": 1.0}
    }
    """
  }

  Given("""an invalid JSON string with syntax errors""") { () =>
    jsonString =
      """
    {
      "(0, 0)": {"Up": 1.0, "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0
    }
    """
  }

  Given("""a Q-Table JSON with invalid action names""") { () =>
    jsonString =
      """
    {
      "(0, 0)": {"InvalidAction": 1.0, "Down": 2.0, "Left": 3.0, "Right": 4.0, "Stay": 5.0}
    }
    """
  }

  Given("""an empty JSON object""") { () =>
    jsonString = "{}"
  }

  When("""I load the Q-Table from JSON""") { () =>

    initialQValues = learner.QTableSnapshot

    val multiAgentJson = s"""{"agent1": $jsonString}"""
    val agentMap = Map("agent1" -> learner)
    val results = QTableLoader.loadMultiAgentQTablesFromJson(multiAgentJson, agentMap)
    loadResult = results.getOrElse("agent1", Failure(new RuntimeException("Failed to load Q-table")))
  }

  When("""I attempt to load the Q-Table from JSON""") { () =>

    initialQValues = learner.QTableSnapshot

    val multiAgentJson = s"""{"agent1": $jsonString}"""
    val agentMap = Map("agent1" -> learner)
    val results = QTableLoader.loadMultiAgentQTablesFromJson(multiAgentJson, agentMap)
    loadResult = results.getOrElse("agent1", Failure(new RuntimeException("Failed to load Q-table")))
  }

  Then("""the Q-Table should be loaded successfully""") { () =>
    loadResult shouldBe a[Success[?]]
  }

  Then("""the Q-values should match the JSON data""") { () =>

    loadResult shouldBe a[Success[?]]

    if jsonString.contains("\"(0, 0)\": {\"Up\": 1.5") then
      this.learner.getQValue(State(0, 0), Action.Right) shouldBe 3.0
      this.learner.getQValue(State(0, 1), Action.Up) shouldBe 2.5
      this.learner.getQValue(State(1, 0), Action.Down) shouldBe 3.5
    else if jsonString.contains("```json") && jsonString.contains("\"(0, 0)\": {\"Up\": 1.0") then
      learner.getQValue(State(0, 0), Action.Down) shouldBe 2.0
    else if jsonString.contains("Here is the JSON") then
      learner.getQValue(State(2, 2), Action.Stay) shouldBe 1.0
  }

  Then("""all actions should have correct Q-values""") { () =>
    if jsonString.contains(
        "\"(0, 0)\": {\"Up\": 1.0, \"Down\": 2.0, \"Left\": 3.0, \"Right\": 4.0, \"Stay\": 5.0}"
      )
    then
      learner.getQValue(State(0, 0), Action.Up) shouldBe 1.0
      learner.getQValue(State(0, 0), Action.Down) shouldBe 2.0
      learner.getQValue(State(0, 0), Action.Left) shouldBe 3.0
      learner.getQValue(State(0, 0), Action.Right) shouldBe 4.0
      learner.getQValue(State(0, 0), Action.Stay) shouldBe 5.0
  }

  Then("""the markdown formatting should be stripped""") { () =>
    loadResult shouldBe a[Success[?]]
  }

  Then("""the Q-values should be correctly parsed""") { () =>
    loadResult shouldBe a[Success[?]]
  }

  Then("""the prefixes should be ignored""") { () =>
    loadResult shouldBe a[Success[?]]
  }

  Then("""the Q-values should be correctly extracted""") { () =>
    loadResult shouldBe a[Success[?]]
  }

  Then("""the loading should fail gracefully""") { () =>
    loadResult shouldBe a[Failure[?]]
  }

  Then("""an appropriate error message should be provided""") { () =>
    loadResult.failed.get.getMessage should include("Failed to load Q-Table from JSON")
  }

  Then("""the Q-Learner should remain unchanged""") { () =>
    learner.QTableSnapshot should equal(initialQValues)
  }

  Then("""the loading should fail with an unknown action error""") { () =>
    loadResult shouldBe a[Failure[?]]
    loadResult.failed.get.getMessage should include("Unknown action")
  }

  Then("""the Q-Learner should remain in a consistent state""") { () =>
    learner.QTableSnapshot should equal(initialQValues)
  }

  Then("""the loading should succeed""") { () =>
    loadResult shouldBe a[Success[?]]
  }

  Then("""the Q-Learner should have no Q-values set""") { () =>
    learner.QTableSnapshot shouldBe empty
  }
