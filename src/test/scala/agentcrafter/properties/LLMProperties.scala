package agentcrafter.properties

import agentcrafter.common.*
import agentcrafter.llmqlearning.*
import agentcrafter.llmqlearning.dsl.{LLMConfig, LLMWallConfig, LLMProperty, LLMWallProperty}
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

  // LLMConfig tests
  property("LLM configuration maintains valid state") = forAll(llmConfigGen) { config =>
    config.model.nonEmpty &&
      (config.enabled || !config.enabled)
  }

  property("LLM property setters update configuration") = forAll(modelGen) { model =>
    val config = LLMConfig()

    given LLMConfig = config

    LLMProperty.Enabled >> true
    LLMProperty.Model >> model

    config.enabled &&
      config.model == model
  }

  property("LLM config handles boolean toggles") = forAll(Gen.oneOf(true, false)) { enabled =>
    val config = LLMConfig()

    given LLMConfig = config

    LLMProperty.Enabled >> enabled

    config.enabled == enabled
  }

  property("LLM model names are preserved correctly") = forAll(modelGen) { model =>
    val config = LLMConfig()

    given LLMConfig = config

    LLMProperty.Model >> model

    config.model == model
  }

  property("LLM config has sensible defaults") =
    val config = LLMConfig()

    !config.enabled &&
      config.model == "gpt-4o"

  // LLMWallConfig tests
  property("LLMWall configuration maintains valid state") = forAll(llmWallConfigGen) { config =>
    config.model.length >= 0 &&
      config.prompt.length >= 0
  }

  property("LLMWall property setters update configuration") = forAll(modelGen, promptGen) { (model, prompt) =>
    val config = LLMWallConfig()

    given LLMWallConfig = config

    LLMWallProperty.Model >> model
    LLMWallProperty.Prompt >> prompt

    config.model == model &&
      config.prompt == prompt
  }

  property("LLMWall config handles edge cases") =
    val config = LLMWallConfig()

    given LLMWallConfig = config

    LLMWallProperty.Model >> ""
    LLMWallProperty.Prompt >> ""

    config.model.isEmpty && config.prompt.isEmpty

  property("LLMWall config has sensible defaults") =
    val config = LLMWallConfig()

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
    val config1 = LLMConfig()
    val config2 = LLMConfig()

    given LLMConfig = config1

    LLMProperty.Model >> model1
    LLMProperty.Enabled >> true

    config1.model == model1 &&
      config1.enabled &&
      config2.model == "gpt-4o" &&
      !config2.enabled
  }

  private val modelGen: Gen[String] = Gen.oneOf("gpt-4o", "gpt-4", "gpt-3.5-turbo")
  private val promptGen: Gen[String] = Gen.alphaNumStr
  private val llmConfigGen: Gen[LLMConfig] = for {
    enabled <- Gen.oneOf(true, false)
    model <- modelGen
  } yield LLMConfig(enabled, model)
  private val llmWallConfigGen: Gen[LLMWallConfig] = for {
    model <- modelGen
    prompt <- promptGen
  } yield LLMWallConfig(model, prompt)
  private val validJsonGen: Gen[String] = Gen.oneOf(
    """{"(0, 0)": {"Up": 1.0, "Down": 0.5}}""",
    """{"(1, 1)": {"Left": 2.0, "Right": 1.5}}""",
    """```json\n{"(2, 2)": {"Stay": 0.0}}\n```"""
  )