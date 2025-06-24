package agentcrafter.bdd

import agentcrafter.common.LearningConfig
import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

import scala.compiletime.uninitialized

class LearningConfigValidationSteps extends ScalaDsl with EN with Matchers:

  private var config: LearningConfig = uninitialized

  Given("""a learning configuration with alpha {double} gamma {double} eps0 {double} epsMin {double} warm {int} optimistic {double}""") {
    (alpha: Double, gamma: Double, eps0: Double, epsMin: Double, warm: Int, optimistic: Double) =>
      config = LearningConfig(alpha, gamma, eps0, epsMin, warm, optimistic)
  }

  Then("""the configuration should be valid""") { () =>
    config.isValid shouldBe true
  }

  Then("""the configuration should be invalid""") { () =>
    config.isValid shouldBe false
  }