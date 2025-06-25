package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for LearningConfig focusing on epsilon decay behaviour across episodes.
 */
object LearningConfigProperties extends Properties("LearningConfig") with Matchers:

  private val configGen: Gen[LearningConfig] =
    for
      alpha <- Gen.choose(0.0, 1.0)
      gamma <- Gen.choose(0.0, 1.0)
      eps0 <- Gen.choose(0.2, 1.0)
      epsMinRaw <- Gen.choose(0.0, 0.2)
      warm <- Gen.choose(1, 50)
      optimistic <- Gen.choose(0.0, 5.0)
    yield
      val epsMin = math.min(epsMinRaw, eps0)
      LearningConfig(alpha, gamma, eps0, epsMin, warm, optimistic)

  private val offsetGen: Gen[(Int, Int)] =
    for
      start <- Gen.choose(0, 80)
      end <- Gen.choose(start, start + 80)
    yield (start, end)

  property("epsilon is constant before warm-up") = forAll(configGen) { cfg =>
    // Test that epsilon remains at its initial value (eps0) during the warm-up period
    // During warm-up, the agent should maintain high exploration (no epsilon decay)
    // Check the episode just before warm-up ends (warm - 1)
    cfg.calculateEpsilon(cfg.warm - 1) == cfg.eps0
  }

  property("epsilon never increases with episode number") = forAll(configGen, offsetGen) { (cfg, pair) =>
    val (e1, e2) = pair
    
    // Test the monotonic decreasing property of epsilon decay
    // Epsilon should never increase as training progresses - this ensures exploration decreases over time
    // Compare epsilon values at two different episodes where e1 <= e2
    val eps1 = cfg.calculateEpsilon(e1)
    val eps2 = cfg.calculateEpsilon(e2)
    
    // If e1 <= e2, then eps1 should be >= eps2 (epsilon decreases or stays the same)
    (e1 <= e2) ==> (eps1 >= eps2)
  }

  property("epsilon reaches floor after long training") = forAll(configGen) { cfg =>
    // Test that epsilon eventually reaches and maintains its minimum value (epsMin)
    // After sufficient training episodes, exploration should stabilize at the minimum level
    // Use episode = warm * 5 to ensure we're well past the decay period
    val episode = cfg.warm * 5
    
    // Verify that epsilon has reached its floor value and won't decay further
    cfg.calculateEpsilon(episode) == cfg.epsMin
  }
