package agentcrafter.llmqlearning

import agentcrafter.common.*
import agentcrafter.llmqlearning.loader.QTableLoader
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success}

class MultiAgentQTableLoaderTest extends AnyFunSuite with Matchers:

  private def createLearner(goalState: State): QLearner =
    val env = GridWorld(rows = 3, cols = 3, walls = Set.empty)
    QLearner(
      goalState = goalState,
      goalReward = 100.0,
      updateFunction = env.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig()
    )

  test("MultiAgent QTableLoader should load valid multi-agent JSON Q-Tables"):
    val agent1 = createLearner(State(2, 2))
    val agent2 = createLearner(State(0, 2))
    val agentLearners = Map(
      "Explorer" -> agent1,
      "Pathfinder" -> agent2
    )

    val validMultiAgentJson =
      """
    {
      "Explorer": {
        "(0, 0)": {"Up": 1.5, "Down": 2.0, "Left": 0.5, "Right": 3.0, "Stay": 0.0},
        "(0, 1)": {"Up": 2.5, "Down": 1.0, "Left": 1.5, "Right": 2.0, "Stay": 0.5}
      },
      "Pathfinder": {
        "(1, 0)": {"Up": 0.5, "Down": 3.5, "Left": 2.0, "Right": 1.0, "Stay": 1.5},
        "(1, 1)": {"Up": 1.0, "Down": 2.5, "Left": 0.5, "Right": 3.5, "Stay": 2.0}
      }
    }
    """

    val results = QTableLoader.loadMultiAgentQTablesFromJson(validMultiAgentJson, agentLearners)
    
    // Both agents should load successfully
    results("Explorer") shouldBe a[Success[?]]
    results("Pathfinder") shouldBe a[Success[?]]

    // Verify Q-values were loaded correctly
    agent1.getQValue(State(0, 0), Action.Right) shouldBe 3.0
    agent1.getQValue(State(0, 1), Action.Up) shouldBe 2.5
    agent2.getQValue(State(1, 0), Action.Down) shouldBe 3.5
    agent2.getQValue(State(1, 1), Action.Right) shouldBe 3.5

  test("MultiAgent QTableLoader should handle partial corruption gracefully"):
    val agent1 = createLearner(State(2, 2))
    val agent2 = createLearner(State(0, 2))
    val agentLearners = Map(
      "Explorer" -> agent1,
      "Pathfinder" -> agent2
    )

    // JSON with one valid agent and one missing agent
    val partiallyValidJson =
      """
    {
      "Explorer": {
        "(0, 0)": {"Up": 1.5, "Down": 2.0, "Left": 0.5, "Right": 3.0, "Stay": 0.0}
      }
    }
    """

    val results = QTableLoader.loadMultiAgentQTablesFromJson(partiallyValidJson, agentLearners)
    
    // Explorer should succeed, Pathfinder should get default (Success with empty table)
    results("Explorer") shouldBe a[Success[?]]
    results("Pathfinder") shouldBe a[Success[?]]

    // Verify Explorer's Q-values were loaded
    agent1.getQValue(State(0, 0), Action.Right) shouldBe 3.0
    
    // Pathfinder should have default optimistic values (from LearningConfig)
    val defaultValue = agent2.getQValue(State(1, 1), Action.Up)
    defaultValue should be >= 0.0 // Should be optimistic default

  test("MultiAgent QTableLoader should handle complete corruption"):
    val agent1 = createLearner(State(2, 2))
    val agent2 = createLearner(State(0, 2))
    val agentLearners = Map(
      "Explorer" -> agent1,
      "Pathfinder" -> agent2
    )

    val invalidJson = "This is not valid JSON at all"

    val results = QTableLoader.loadMultiAgentQTablesFromJson(invalidJson, agentLearners)
    
    // Both agents should fail (and thus use default values)
    results("Explorer") shouldBe a[Failure[?]]
    results("Pathfinder") shouldBe a[Failure[?]]

  test("MultiAgent QTableLoader should handle JSON with markdown code blocks"):
    val agent1 = createLearner(State(2, 2))
    val agentLearners = Map("Explorer" -> agent1)

    val jsonWithMarkdown =
      """
    ```json
    {
      "Explorer": {
        "(0, 0)": {"Up": 1.5, "Down": 2.0, "Left": 0.5, "Right": 3.0, "Stay": 0.0}
      }
    }
    ```
    """

    val results = QTableLoader.loadMultiAgentQTablesFromJson(jsonWithMarkdown, agentLearners)
    
    results("Explorer") shouldBe a[Success[?]]
    agent1.getQValue(State(0, 0), Action.Right) shouldBe 3.0

  test("MultiAgent QTableLoader should handle empty agent map"):
    val emptyAgentLearners = Map.empty[String, QLearner]
    val validJson = """{"Explorer": {"(0,0)": {"Up": 1.0}}}"""

    val results = QTableLoader.loadMultiAgentQTablesFromJson(validJson, emptyAgentLearners)
    
    results shouldBe empty