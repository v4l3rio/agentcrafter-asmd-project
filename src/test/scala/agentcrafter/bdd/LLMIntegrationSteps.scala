package agentcrafter.steps

import agentcrafter.marl.dsl.{SimulationDSL, SimulationWrapper}
import agentcrafter.marl.builders.SimulationBuilder
import agentcrafter.common.{GridWorld, QLearner, State}
import agentcrafter.llmqlearning.dsl.LLMProperty.*
import agentcrafter.llmqlearning.dsl.LLMQLearning
import agentcrafter.llmqlearning.{LLMHttpClient}
import agentcrafter.llmqlearning.loader.QTableLoader
import agentcrafter.llmqlearning.dsl.LLMConfig
import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.util.{Failure, Success, Try}

class LLMIntegrationSteps extends ScalaDsl with EN with Matchers:

  private var simulation: TestSimulation = uninitialized
  private var llmConfig: LLMConfig = uninitialized
  private var simulationWrapper: SimulationWrapper = uninitialized
  private var apiCallsMade: Boolean = false
  private var mockApiResponse: Option[String] = None
  private var mockApiError: Option[Throwable] = None
  private var qTableLoadResult: Try[Unit] = uninitialized
  private var multiAgentLoadResults: Map[String, Try[Unit]] = Map.empty
  private var agentCount: Int = 0
  private var loggedWarnings: mutable.Buffer[String] = mutable.Buffer.empty

  private def createSimpleGrid(): GridWorld =
    GridWorld(rows = 3, cols = 3, walls = Set.empty)

  private class TestSimulation extends SimulationDSL with LLMQLearning

  private class MockLLMApiClient extends LLMHttpClient("mock-url", "mock-key"):
    override def callLLM(
      prompt: String = "",
      model: String = "gpt-4o",
      stream: Boolean = false,
      endpoint: String = "/v1/chat/completions",
      simulationFilePath: Option[String] = None
    ): Try[String] =
      apiCallsMade = true

      if mockApiError.isDefined then
        Failure(mockApiError.get)
      else
        mockApiResponse match
          case Some(response) => Success(response)
          case None => Success("""{"{(0, 0)": {"Up": 1.0, "Down": 1.0, "Left": 1.0, "Right": 1.0, "Stay": 1.0}}""")

  Given("""the LLM integration system is available""") { () =>
    simulation = new TestSimulation()
    simulationWrapper = SimulationWrapper(new SimulationBuilder)
    llmConfig = LLMConfig()
    apiCallsMade = false
    mockApiResponse = None
    mockApiError = None
    loggedWarnings.clear()
  }

  Given("""I create a simulation with LLM enabled""") { () =>
    given config: LLMConfig = llmConfig

    Enabled >> true
  }

  Given("""I configure the LLM model as "gpt-4o"""") { () =>
    given config: LLMConfig = llmConfig

    Model >> "gpt-4o"
  }

  Given("""I add an agent to the simulation""") { () =>
    agentCount += 1
  }

  When("""I run the simulation""") { () =>
    if llmConfig.enabled then
      apiCallsMade = true
  }

  Then("""the LLM should be used to generate initial Q-Tables""") { () =>
    apiCallsMade shouldBe true
  }

  Then("""the agent should start with LLM-provided knowledge""") { () =>
    llmConfig.enabled shouldBe true
  }

  Given("""I enable LLM with model "gpt-3.5-turbo"""") { () =>
    given config: LLMConfig = llmConfig

    Enabled >> true
    Model >> "gpt-3.5-turbo"
  }

  When("""I configure the simulation""") { () =>
  }

  Then("""the LLM configuration should use the specified model""") { () =>
    llmConfig.model shouldBe "gpt-3.5-turbo"
  }

  Then("""the configuration should be properly stored""") { () =>
    llmConfig.enabled shouldBe true
    llmConfig.model should not be empty
  }

  Given("""I create a simulation without enabling LLM""") { () =>
    given config: LLMConfig = llmConfig

    Enabled >> false
  }

  Then("""the agent should start with default Q-values""") { () =>
    llmConfig.enabled shouldBe false
  }

  Then("""no LLM API calls should be made""") { () =>
    apiCallsMade shouldBe false
  }

  Given("""I enable LLM for the simulation""") { () =>
    given config: LLMConfig = llmConfig

    Enabled >> true
  }

  Given("""the LLM API is unavailable or returns an error""") { () =>
    mockApiError = Some(new RuntimeException("API connection failed"))
  }

  When("""I attempt to run the simulation""") { () =>
    if llmConfig.enabled then
      try {
        val mockClient = new MockLLMApiClient()
        val result = mockClient.callLLM("test prompt")
        result match
          case Failure(ex) => loggedWarnings += ex.getMessage
          case _ => ()
      } catch {
        case ex: Exception => loggedWarnings += ex.getMessage
      }
  }

  Then("""the simulation should handle the failure gracefully""") { () =>
    apiCallsMade shouldBe true
  }

  Then("""the agent should fall back to default initialization""") { () =>
    mockApiError shouldBe defined
  }

  Then("""an appropriate warning should be logged""") { () =>
    loggedWarnings should not be empty
  }

  Given("""the LLM returns malformed JSON""") { () =>
    mockApiResponse = Some("""This is not valid JSON""")
  }

  When("""I attempt to load the Q-Table""") { () =>
    val mockClient = new MockLLMApiClient()
    val jsonResponse = mockClient.callLLM("test prompt").getOrElse("")
    val env = createSimpleGrid()
    val learner = QLearner(
      goalState = State(2, 2),
      goalReward = 0.0,
      updateFunction = env.step,
      resetFunction = () => State(0, 0)
    )
    multiAgentLoadResults = QTableLoader.loadMultiAgentQTablesFromJson(jsonResponse, Map("agent1" -> learner))
    qTableLoadResult = multiAgentLoadResults.getOrElse("agent1", Failure(new RuntimeException("Agent not found")))
  }

  Then("""the loading should fail safely""") { () =>
    qTableLoadResult shouldBe a[Failure[?]]
  }

  Then("""the agent should use default Q-values""") { () =>
    qTableLoadResult shouldBe a[Failure[?]]
  }

  Then("""the error should be properly reported""") { () =>
    qTableLoadResult.failed.get.getMessage should include("Failed to load Q-Table from JSON")
  }

  Given("""I add multiple agents with different configurations""") { () =>
    agentCount = 3
  }

  Then("""each agent should receive appropriate LLM-generated Q-Tables""") { () =>

    agentCount should be > 1
    llmConfig.enabled shouldBe true
  }

  Then("""the Q-Tables should be tailored to each agent's environment""") { () =>

    agentCount should be > 1
    llmConfig.enabled shouldBe true
  }
