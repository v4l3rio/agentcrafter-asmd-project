package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers
import scala.util.Random

/**
 * Property-based tests for QTable behaviour.
 */
object QTableProperties extends Properties("QTable") with Matchers:

  private val stateGen: Gen[State] = for
    x <- Gen.choose(0, 4)
    y <- Gen.choose(0, 4)
  yield State(x, y)

  // Config with deterministic updates for easier reasoning
  private val baseConfig = LearningConfig(alpha = 1.0, gamma = 0.0, optimistic = 0.0)

  property("best action has the maximal Q-value") = forAll(stateGen, Gen.listOf(Gen.choose(-5.0, 5.0))) { (state, rewards) =>
    val cfg = baseConfig
    val q = new QTable(cfg)
    val next = state
    rewards.zip(Action.values).foreach { case (r, a) => q.updateValue(state, a, r, next) }
    val best = q.getBestAction(state)(using Random(0))
    val qValues = Action.values.map(a => q.getValue(state, a))
    qValues.max == q.getValue(state, best)
  }

  property("tie-breaking chooses among all best actions") = forAll(stateGen) { state =>
    val cfg = baseConfig
    val q = new QTable(cfg)
    val next = state
    // Give two actions the same highest value
    q.updateValue(state, Action.Up, 5.0, next)
    q.updateValue(state, Action.Left, 5.0, next)
    val rng = Random(0)
    given Random = rng
    val counts = scala.collection.mutable.Map[Action, Int]().withDefaultValue(0)
    (1 to 50).foreach { _ => counts(q.getBestAction(state)) += 1 }
    counts(Action.Up) > 0 && counts(Action.Left) > 0
  }

  property("snapshots remain immutable after updates") = forAll(stateGen) { state =>
    val cfg = baseConfig
    val q = new QTable(cfg)
    val next = state
    q.updateValue(state, Action.Right, 2.0, next)
    val snap1 = q.createSnapshot()
    q.updateValue(state, Action.Right, 4.0, next)
    val snap2 = q.createSnapshot()
    snap1 != snap2 && snap1((state, Action.Right)) == 2.0 && snap2((state, Action.Right)) == 4.0
  }