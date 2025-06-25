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

  private val stateGen: Gen[State] =
    for
      x <- Gen.choose(0, 4)
      y <- Gen.choose(0, 4)
    yield State(x, y)
  
  private val baseConfig = LearningConfig(alpha = 1.0, gamma = 0.0, optimistic = 0.0)

  // Test that verifies the QTable correctly identifies the action with the highest Q-value
  // This is fundamental to Q-learning: the agent should always choose the action with maximum expected reward
  property("best action has the maximal Q-value") =
    forAll(stateGen, Gen.listOf(Gen.choose(-5.0, 5.0))) { (state, rewards) =>
      val cfg = baseConfig
      val q = new QTable(cfg)
      val next = state
      // Update Q-values for all actions with random rewards
      rewards.zip(Action.values).foreach { case (r, a) => q.updateValue(state, a, r, next) }
      // Get the best action according to the QTable
      val best = q.getBestAction(state)(using Random(0))
      // Verify that the best action indeed has the maximum Q-value
      val qValues = Action.values.map(a => q.getValue(state, a))
      qValues.max == q.getValue(state, best)
    }

  // Test that verifies proper tie-breaking when multiple actions have the same maximum Q-value
  // When there are ties, the agent should randomly select among the best actions to ensure exploration
  property("tie-breaking chooses among all best actions") = forAll(stateGen) { state =>
    val cfg = baseConfig
    val q = new QTable(cfg)
    val next = state
    // Create a tie by giving two actions the same highest Q-value
    q.updateValue(state, Action.Up, 5.0, next)
    q.updateValue(state, Action.Left, 5.0, next)
    val rng = Random(0)
    given Random = rng
    // Count how many times each action is selected over multiple calls
    val counts = scala.collection.mutable.Map[Action, Int]().withDefaultValue(0)
    (1 to 50).foreach(_ => counts(q.getBestAction(state)) += 1)
    // Verify that both tied actions are selected at least once (proper tie-breaking)
    counts(Action.Up) > 0 && counts(Action.Left) > 0
  }

  // Test that verifies QTable snapshots are immutable and preserve historical Q-values
  // This is crucial for tracking learning progress and ensuring data integrity during analysis
  property("snapshots remain immutable after updates") = forAll(stateGen) { state =>
    val cfg = baseConfig
    val q = new QTable(cfg)
    val next = state
    // Set initial Q-value and create first snapshot
    q.updateValue(state, Action.Right, 2.0, next)
    val snap1 = q.createSnapshot()
    // Update Q-value and create second snapshot
    q.updateValue(state, Action.Right, 4.0, next)
    val snap2 = q.createSnapshot()
    // Verify snapshots are different and each preserves its respective Q-value
    snap1 != snap2 && snap1((state, Action.Right)) == 2.0 && snap2((state, Action.Right)) == 4.0
  }
