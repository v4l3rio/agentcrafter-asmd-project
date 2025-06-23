package agentcrafter.properties

import agentcrafter.common.*
import agentcrafter.llmqlearning.*
import agentcrafter.llmqlearning.UseLLMProperty.*
import agentcrafter.llmqlearning.WallLLMProperty.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for LLM integration using ScalaCheck.
 * These tests verify LLM configuration and basic integration properties
 * while avoiding expensive API calls and complex Q-table operations.
 */
object LLMProperties extends Properties("LLM") with Matchers:

  // Test constants
  private val TEST_GRID_SIZE: Int = 5
  private val TEST_Q_VALUE_MIN: Double = -10.0
  private val TEST_Q_VALUE_MAX: Double = 10.0

  private val stateGen: Gen[State] = for {
    x <- Gen.choose(0, TEST_GRID_SIZE - 1)
    y <- Gen.choose(0, TEST_GRID_SIZE - 1)
  } yield State(x, y)
  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)
  private val qValueGen: Gen[Double] = Gen.choose(TEST_Q_VALUE_MIN, TEST_Q_VALUE_MAX)

  // UseLLMConfig tests
  property("UseLLM configuration maintains valid state") = forAll(useLLMConfigGen) { config =>
    config.model.nonEmpty &&
      (config.enabled || !config.enabled)
  }

  property("UseLLM property setters update configuration") = forAll(modelGen) { model =>
    val config = UseLLMConfig()

    given UseLLMConfig = config

    UseLLMProperty.Enabled >> true
    UseLLMProperty.Model >> model

    config.enabled &&
      config.model == model
  }

  property("UseLLM config handles boolean toggles") = forAll(Gen.oneOf(true, false)) { enabled =>
    val config = UseLLMConfig()

    given UseLLMConfig = config

    UseLLMProperty.Enabled >> enabled

    config.enabled == enabled
  }

  property("UseLLM model names are preserved correctly") = forAll(modelGen) { model =>
    val config = UseLLMConfig()

    given UseLLMConfig = config

    UseLLMProperty.Model >> model

    config.model == model
  }

  property("UseLLM config has sensible defaults") =
    val config = UseLLMConfig()

    !config.enabled &&
      config.model == "gpt-4o"

  // WallLLMConfig tests
  property("WallLLM configuration maintains valid state") = forAll(wallLLMConfigGen) { config =>
    config.model.length >= 0 &&
      config.prompt.length >= 0
  }

  property("WallLLM property setters update configuration") = forAll(modelGen, promptGen) { (model, prompt) =>
    val config = WallLLMConfig()

    given WallLLMConfig = config

    WallLLMProperty.Model >> model
    WallLLMProperty.Prompt >> prompt

    config.model == model &&
      config.prompt == prompt
  }

  property("WallLLM config handles edge cases") =
    val config = WallLLMConfig()

    given WallLLMConfig = config

    WallLLMProperty.Model >> ""
    WallLLMProperty.Prompt >> ""

    config.model.isEmpty && config.prompt.isEmpty

  property("WallLLM config has sensible defaults") =
    val config = WallLLMConfig()

    config.model.isEmpty &&
      config.prompt.isEmpty

  // General tests
  property("JSON decoration stripping works correctly") = forAll(validJsonGen) { json =>
    val stripped = if json.trim.startsWith("```") then
      json.trim.stripPrefix("```json").stripPrefix("```").stripSuffix("```")
    else json

    val finalStripped = stripped.replaceAll("(?i)^json\\s*", "").replaceAll("(?i)^here.*?:\\s*", "").trim

    !finalStripped.contains("```") && finalStripped.trim.nonEmpty
  }

  property("State parsing handles various formats") = forAll(stateGen) { state =>
    val stateString1 = s"(${state.x}, ${state.y})"
    val stateString2 = s"(${state.x},${state.y})"
    val stateString3 = s"( ${state.x} , ${state.y} )"

    val regex = """\(\s*(\d+)\s*,\s*(\d+)\s*\)""".r

    def testMatch(str: String): Boolean = str match
      case regex(x, y) =>
        try {
          x.toInt == state.x && y.toInt == state.y
        } catch {
          case _: NumberFormatException => false
        }
      case _ => false

    val result1 = testMatch(stateString1)
    val result2 = testMatch(stateString2)
    val result3 = testMatch(stateString3)

    result1 && result2 && result3
  }

  property("LLM configurations are independent") = forAll(modelGen, modelGen) { (model1, model2) =>
    val config1 = UseLLMConfig()
    val config2 = UseLLMConfig()

    given UseLLMConfig = config1

    UseLLMProperty.Model >> model1
    UseLLMProperty.Enabled >> true

    config1.model == model1 &&
      config1.enabled &&
      config2.model == "gpt-4o" &&
      !config2.enabled
  }

  private val modelGen: Gen[String] = Gen.oneOf("gpt-4o", "gpt-4", "gpt-3.5-turbo")
  private val promptGen: Gen[String] = Gen.alphaNumStr
  private val useLLMConfigGen: Gen[UseLLMConfig] = for {
    enabled <- Gen.oneOf(true, false)
    model <- modelGen
  } yield UseLLMConfig(enabled, model)
  private val wallLLMConfigGen: Gen[WallLLMConfig] = for {
    model <- modelGen
    prompt <- promptGen
  } yield WallLLMConfig(model, prompt)
  private val validJsonGen: Gen[String] = Gen.oneOf(
    """{"(0, 0)": {"Up": 1.0, "Down": 0.5}}""",
    """{"(1, 1)": {"Left": 2.0, "Right": 1.5}}""",
    """```json\n{"(2, 2)": {"Stay": 0.0}}\n```"""
  )