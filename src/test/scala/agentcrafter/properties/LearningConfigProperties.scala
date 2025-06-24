package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for LearningConfig focusing on epsilon decay
 * behaviour across episodes.
 */
object LearningConfigProperties extends Properties("LearningConfig") with Matchers:

  private val configGen: Gen[LearningConfig] = for
    alpha <- Gen.choose(0.0, 1.0)
    gamma <- Gen.choose(0.0, 1.0)
    eps0 <- Gen.choose(0.2, 1.0)
    epsMinRaw <- Gen.choose(0.0, 0.2)
    warm <- Gen.choose(1, 50)
    optimistic <- Gen.choose(0.0, 5.0)
  yield
    val epsMin = math.min(epsMinRaw, eps0)
    LearningConfig(alpha, gamma, eps0, epsMin, warm, optimistic)

  private val offsetGen: Gen[(Int, Int)] = for
    start <- Gen.choose(0, 80)
    end <- Gen.choose(start, start + 80)
  yield (start, end)

  property("epsilon is constant before warm-up") = forAll(configGen) { cfg =>
    cfg.calculateEpsilon(cfg.warm - 1) == cfg.eps0
  }

  property("epsilon never increases with episode number") = forAll(configGen, offsetGen) { (cfg, pair) =>
    val (e1, e2) = pair
    val eps1 = cfg.calculateEpsilon(e1)
    val eps2 = cfg.calculateEpsilon(e2)
    (e1 <= e2) ==> (eps1 >= eps2)
  }

  property("epsilon reaches floor after long training") = forAll(configGen) { cfg =>
    val episode = cfg.warm * 5
    cfg.calculateEpsilon(episode) == cfg.epsMin
  }