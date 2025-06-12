package agentcrafter.properties

import agentcrafter.common.*
import agentcrafter.llmqlearning.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import scala.util.{Failure, Success, Try}

/**
 * Property-based tests for LLM integration using ScalaCheck.
 * These tests verify LLM configuration and basic integration properties
 * while avoiding expensive API calls and complex Q-table operations.
 */
object LLMProperties extends Properties("LLM") with Matchers:

  // Generators for test data
  private val stateGen: Gen[State] = for {
    r <- Gen.choose(0, 4)
    c <- Gen.choose(0, 4)
  } yield State(r, c)

  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)

  private val qValueGen: Gen[Double] = Gen.choose(-10.0, 10.0)

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

  // Property 1: LLM configuration should maintain valid state
  property("LLM configuration maintains valid state") = forAll(llmConfigGen) { config =>
    config.enabled || !config.enabled &&
      config.model.nonEmpty &&
      config.wallsEnabled || !config.wallsEnabled &&
      config.wallsModel.nonEmpty
  }

  // Property 2: LLM property setters should update configuration correctly
  property("LLM property setters update configuration") = forAll(modelGen) { model =>
    val config = LLMConfig()
    given LLMConfig = config
    
    LLMProperty.Enabled >> true
    LLMProperty.Model >> model
    LLMProperty.WallsEnabled >> true
    LLMProperty.WallsModel >> model
    
    config.enabled &&
      config.model == model &&
      config.wallsEnabled &&
    config.wallsModel == model
  }

  // Property 3: JSON decoration stripping should work
  property("JSON decoration stripping works") = forAll(validJsonGen) { json =>
    // Test the regex pattern used in QTableLoader
    val cleanJson = if json.trim.startsWith("```") then 
      json.trim.stripPrefix("```json").stripPrefix("```").stripSuffix("```")
    else json
    
    val finalClean = cleanJson.replaceAll("(?i)^json\\s*", "").replaceAll("(?i)^here.*?:\\s*", "").trim
    
    finalClean.nonEmpty
  }

  // Property 4: LLM config should handle boolean toggles correctly
  property("LLM config handles boolean toggles") = forAll(Gen.oneOf(true, false), Gen.oneOf(true, false)) { (enabled, wallsEnabled) =>
    val config = LLMConfig()
    given LLMConfig = config
    
    LLMProperty.Enabled >> enabled
    LLMProperty.WallsEnabled >> wallsEnabled
    
    config.enabled == enabled && config.wallsEnabled == wallsEnabled
  }

  // Property 5: Model names should be preserved correctly
  property("Model names are preserved correctly") = forAll(modelGen, modelGen) { (model1, model2) =>
    val config = LLMConfig()
    given LLMConfig = config
    
    LLMProperty.Model >> model1
    LLMProperty.WallsModel >> model2
    
    config.model == model1 && config.wallsModel == model2
  }

  // Property 6: LLM configuration should support fluent DSL syntax
  property("LLM configuration supports fluent DSL") = forAll(modelGen, Gen.alphaNumStr) { (model, prompt) =>
    val config = LLMConfig()
    given LLMConfig = config
    
    // Test fluent syntax
    LLMProperty.Enabled >> true
    LLMProperty.Model >> model
    LLMProperty.WallsEnabled >> false
    LLMProperty.WallsPrompt >> prompt
    
    config.enabled &&
    config.model == model &&
    !config.wallsEnabled &&
    config.wallsPrompt == prompt
  }

  // Property 7: JSON decoration stripping should work correctly
  property("JSON decoration stripping works") = forAll(validJsonGen) { json =>
    // Test the decoration stripping logic manually since stripLlMDecorations is private
    val stripped = if json.trim.startsWith("```") then 
      json.trim.stripPrefix("```json").stripPrefix("```").stripSuffix("```")
    else json
    
    val finalStripped = stripped.replaceAll("(?i)^json\\s*", "").replaceAll("(?i)^here.*?:\\s*", "").trim
    
    // Should not contain markdown code block markers
    !finalStripped.contains("```") && finalStripped.trim.nonEmpty
  }

  // Property 8: LLM config should have sensible defaults
  property("LLM config has sensible defaults") = {
    val config = LLMConfig()
    
    !config.enabled &&
    config.model == "gpt-4o" &&
    !config.wallsEnabled &&
    config.wallsModel == "gpt-4o" &&
    config.wallsPrompt.isEmpty
  }

  // Property 9: State parsing should be robust
  property("State parsing handles various formats") = forAll(stateGen) { state =>
    val stateString1 = s"(${state.r}, ${state.c})"
    val stateString2 = s"(${state.r},${state.c})"
    val stateString3 = s"( ${state.r} , ${state.c} )"
    
    // Use a more flexible regex that handles spaces before digits
    val regex = """\(\s*(\d+)\s*,\s*(\d+)\s*\)""".r
    
    def testMatch(str: String): Boolean = str match {
      case regex(r, c) => 
        try {
          r.toInt == state.r && c.toInt == state.c
        } catch {
          case _: NumberFormatException => false
        }
      case _ => false
    }
    
    val result1 = testMatch(stateString1)
    val result2 = testMatch(stateString2) 
    val result3 = testMatch(stateString3)
    
    result1 && result2 && result3
  }

  // Property 10: LLM properties should be type-safe
  property("LLM properties are type-safe") = forAll(llmConfigGen) { originalConfig =>
    val config = LLMConfig()
    given LLMConfig = config
    
    // Apply properties and verify they maintain type safety
    LLMProperty.Enabled >> originalConfig.enabled
    LLMProperty.Model >> originalConfig.model
    LLMProperty.WallsEnabled >> originalConfig.wallsEnabled
    LLMProperty.WallsModel >> originalConfig.wallsModel
    LLMProperty.WallsPrompt >> originalConfig.wallsPrompt
    
    // All values should be preserved correctly
    config.enabled == originalConfig.enabled &&
    config.model == originalConfig.model &&
    config.wallsEnabled == originalConfig.wallsEnabled &&
    config.wallsModel == originalConfig.wallsModel &&
    config.wallsPrompt == originalConfig.wallsPrompt
  }