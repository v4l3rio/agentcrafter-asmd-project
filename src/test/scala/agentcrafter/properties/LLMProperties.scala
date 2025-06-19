package agentcrafter.properties

import agentcrafter.common.*
import agentcrafter.llmqlearning.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for LLM integration using ScalaCheck.
 * These tests verify LLM configuration and basic integration properties
 * while avoiding expensive API calls and complex Q-table operations.
 */
object LLMProperties extends Properties("LLM") with Matchers:


    val result1 = testMatch(stateString1)
    val result2 = testMatch(stateString2)
    val result3 = testMatch(stateString3)
  private val stateGen: Gen[State] = for {
    r <- Gen.choose(0, 4)
    c <- Gen.choose(0, 4)
  } yield State(r, c)
  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)
  private val qValueGen: Gen[Double] = Gen.choose(-10.0, 10.0)


  property("LLM configuration maintains valid state") = forAll(llmConfigGen) { config =>
    config.model.nonEmpty &&
      config.wallsModel.nonEmpty &&
      config.enabled || !config.enabled &&
      config.wallsEnabled || !config.wallsEnabled
  }


  property("LLM property setters update configuration") = forAll(modelGen) { model =>
    val config = LLMConfig()

    given LLMConfig = config

    LLMDSLProperties.Enabled >> true
    LLMDSLProperties.Model >> model
    LLMDSLProperties.WallsEnabled >> true
    LLMDSLProperties.WallsModel >> model

    config.enabled &&
      config.model == model &&
      config.wallsEnabled &&
      config.wallsModel == model
  }


  property("JSON decoration stripping works correctly") = forAll(validJsonGen) { json =>

    val stripped = if json.trim.startsWith("```") then
      json.trim.stripPrefix("```json").stripPrefix("```").stripSuffix("```")
    else json

    val finalStripped = stripped.replaceAll("(?i)^json\\s*", "").replaceAll("(?i)^here.*?:\\s*", "").trim


    !finalStripped.contains("```") && finalStripped.trim.nonEmpty
  }


  property("LLM config handles boolean toggles") = forAll(Gen.oneOf(true, false), Gen.oneOf(true, false)) { (enabled, wallsEnabled) =>
    val config = LLMConfig()

    given LLMConfig = config

    LLMDSLProperties.Enabled >> enabled
    LLMDSLProperties.WallsEnabled >> wallsEnabled

    config.enabled == enabled && config.wallsEnabled == wallsEnabled
  }


  property("Model names are preserved correctly") = forAll(modelGen, modelGen) { (model1, model2) =>
    val config = LLMConfig()

    given LLMConfig = config

    LLMDSLProperties.Model >> model1
    LLMDSLProperties.WallsModel >> model2

    config.model == model1 && config.wallsModel == model2
  }


  property("LLM configuration supports chained updates") = forAll(llmConfigGen) { originalConfig =>
    val config = LLMConfig()

    given LLMConfig = config


    LLMDSLProperties.Enabled >> originalConfig.enabled
    LLMDSLProperties.Model >> originalConfig.model
    LLMDSLProperties.WallsEnabled >> originalConfig.wallsEnabled
    LLMDSLProperties.WallsModel >> originalConfig.wallsModel
    LLMDSLProperties.WallsPrompt >> originalConfig.wallsPrompt


    config.enabled == originalConfig.enabled &&
      config.model == originalConfig.model &&
      config.wallsEnabled == originalConfig.wallsEnabled &&
      config.wallsModel == originalConfig.wallsModel &&
      config.wallsPrompt == originalConfig.wallsPrompt
  }


  property("LLM configuration handles edge cases") =
    val config = LLMConfig()

    given LLMConfig = config


    LLMDSLProperties.Model >> ""
    LLMDSLProperties.WallsPrompt >> ""


    config.model.isEmpty && config.wallsPrompt.isEmpty
  


  property("LLM config has sensible defaults") =
    val config = LLMConfig()

    !config.enabled &&
      config.model == "gpt-4o" &&
      !config.wallsEnabled &&
      config.wallsModel == "gpt-4o" &&
      config.wallsPrompt.isEmpty
  


  property("State parsing handles various formats") = forAll(stateGen) { state =>
    val stateString1 = s"(${state.x}, ${state.y})"
    val stateString2 = s"(${state.x},${state.y})"
    val stateString3 = s"( ${state.x} , ${state.y} )"


    val regex = """\(\s*(\d+)\s*,\s*(\d+)\s*\)""".r

    def testMatch(str: String): Boolean = str match
      case regex(r, c) =>
        try {
          r.toInt == state.x && c.toInt == state.y
        } catch {
          case _: NumberFormatException => false
        }
      case _ => false
    }
  private val modelGen: Gen[String] = Gen.oneOf("gpt-4o", "gpt-4", "gpt-3.5-turbo")
  private val llmConfigGen: Gen[LLMConfig] = for {
    enabled <- Gen.oneOf(true, false)
    model <- modelGen
    wallsEnabled <- Gen.oneOf(true, false)
    wallsModel <- modelGen
    wallsPrompt <- Gen.alphaNumStr
  } yield LLMConfig(enabled, model, wallsEnabled, wallsModel, wallsPrompt)
  private val validJsonGen: Gen[String] = Gen.oneOf(
    """{"(0, 0)": {"Up": 1.0, "Down": 0.5}}""",
    """{"(1, 1)": {"Left": 2.0, "Right": 1.5}}""",
    """```json\n{"(2, 2)": {"Stay": 0.0}}\n```"""
  )

    result1 && result2 && result3
  }


  property("LLM configurations are independent") = forAll(modelGen, modelGen) { (model1, model2) =>
    val config1 = LLMConfig()
    val config2 = LLMConfig()

    given LLMConfig = config1


    LLMDSLProperties.Model >> model1
    LLMDSLProperties.Enabled >> true


    config1.model == model1 &&
      config1.enabled &&
      config2.model == "gpt-4o" &&
      !config2.enabled
  }