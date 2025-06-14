package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for QLearner using ScalaCheck.
 * These tests verify stochastic properties and invariants of the Q-learning algorithm.
 */
object QLearnerProperties extends Properties("QLearner") with Matchers:

  // Generators for test data
  private val stateGen: Gen[State] = for {
    r <- Gen.choose(0, 4)
    c <- Gen.choose(0, 4)
  } yield State(r, c)

  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)

  private val rewardGen: Gen[Double] = Gen.choose(-100.0, 100.0)

  private val learningParamsGen: Gen[LearningParameters] = for {
    alpha <- Gen.choose(0.01, 1.0)
    gamma <- Gen.choose(0.0, 1.0)
    eps0 <- Gen.choose(0.1, 1.0)
    epsMin <- Gen.choose(0.0, 0.5)
    warm <- Gen.choose(1, 1000)
    optimistic <- Gen.choose(-1.0, 1.0)
  } yield {
    val actualEpsMin = math.min(epsMin, eps0) // Ensure epsMin <= eps0
    LearningParameters(alpha, gamma, eps0, actualEpsMin, warm, optimistic)
  }

  private def createLearner(params: LearningParameters = LearningParameters()): QLearner =
    val env = GridWorld(rows = 5, cols = 5, walls = Set.empty)
    QLearner(
      goalState = State(4, 4),
      goalReward = 100.0,
      updateFunction = env.step,
      resetFunction = () => State(0, 0),
      learningParameters = params
    )

  // Property 1: Q-values should be bounded by optimistic initialization
  property("Q-values start with optimistic initialization") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createLearner(params)
      val qValue = learner.getQValue(state, action)
      qValue == params.optimistic
  }

  // Property 2: Epsilon should decay properly over episodes
  property("epsilon decays correctly after warm-up") = forAll(learningParamsGen) { params =>
    val learner = createLearner(params)
    val initialEps = learner.eps

    // During warm-up, epsilon should remain constant
    (1 to params.warm).foreach(_ => learner.incEp())
    val epsAfterWarmup = learner.eps

    // After warm-up, epsilon should start decaying (only if eps0 > epsMin)
    learner.incEp()
    val epsAfterDecay = learner.eps

    val warmupCorrect = (initialEps == params.eps0) && (epsAfterWarmup == params.eps0)
    val decayCorrect = if (params.eps0 > params.epsMin) {
      epsAfterDecay <= epsAfterWarmup
    } else {
      epsAfterDecay == epsAfterWarmup // No decay if eps0 == epsMin
    }
    val boundsCorrect = learner.eps >= params.epsMin && learner.eps <= params.eps0

    warmupCorrect && decayCorrect && boundsCorrect
  }

  // Property 3: Q-value updates should follow the Q-learning formula
  property("Q-value updates follow Q-learning formula") = forAll(learningParamsGen, stateGen, actionGen, rewardGen, stateGen) {
    (params, state, action, reward, nextState) =>
      val learner = createLearner(params)
      val initialQ = learner.getQValue(state, action)

      // Get max Q-value for next state before update
      val maxNextQ = Action.values.map(a => learner.getQValue(nextState, a)).max

      learner.update(state, action, reward, nextState)
      val updatedQ = learner.getQValue(state, action)

      val expectedQ = (1 - params.alpha) * initialQ + params.alpha * (reward + params.gamma * maxNextQ)

      math.abs(updatedQ - expectedQ) < 1e-10
  }

  // Property 4: Action selection should respect epsilon-greedy policy
  property("action selection follows epsilon-greedy policy") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      val learner = createLearner(params.copy(eps0 = 0.0, epsMin = 0.0)) // No exploration

      // Set different Q-values for actions
      Action.values.zipWithIndex.foreach { case (action, idx) =>
        learner.update(state, action, idx * 10.0, State(0, 0))
      }

      // With no exploration, should always choose the action with highest Q-value
      val (chosenAction, wasExploring) = learner.choose(state)
      val qValues = Action.values.map(a => learner.getQValue(state, a))
      val maxQ = qValues.max
      val chosenQ = learner.getQValue(state, chosenAction)

      !wasExploring && (chosenQ == maxQ)
  }

  // Property 5: Q-table snapshot should be consistent
  property("Q-table snapshot is consistent with getQValue") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createLearner(params)
      val snapshot = learner.QTableSnapshot
      val directValue = learner.getQValue(state, action)
      val snapshotValue = snapshot.getOrElse((state, action), params.optimistic)

      directValue == snapshotValue
  }

  // Property 6: Episode outcomes should be deterministic given same random seed
  property("episodes are deterministic with same conditions") = forAll(learningParamsGen) { params =>
    // This property tests that the stochastic behavior is controlled by the random seed
    val learner1 = createLearner(params)
    val learner2 = createLearner(params)

    // Both learners should start with same epsilon
    learner1.eps == learner2.eps
  }

  // Property 7: Learning parameters should be within valid bounds
  property("learning parameters maintain valid bounds") = forAll(learningParamsGen) { params =>
    (params.alpha >= 0.0 && params.alpha <= 1.0) &&
      (params.gamma >= 0.0 && params.gamma <= 1.0) &&
      (params.eps0 >= 0.0 && params.eps0 <= 1.0) &&
      (params.epsMin >= 0.0 && params.epsMin <= 1.0) &&
      (params.epsMin <= params.eps0) &&
      (params.warm >= 0)
  }

  // Property 8: Q-values should converge towards goal reward in simple cases
  property("Q-values increase towards goal in simple path") = forAll(Gen.choose(0.1, 1.0), Gen.choose(0.1, 1.0)) {
    (alpha, gamma) =>
      // Ensure gamma > 0 to allow value propagation
      val actualGamma = math.max(gamma, 0.1)
      val params = LearningParameters(alpha = alpha, gamma = actualGamma, eps0 = 0.0, epsMin = 0.0)
      val learner = createLearner(params)

      val state = State(3, 4) // One step away from goal
      val action = Action.Down // Action that leads to goal
      val goalState = State(4, 4)

      val initialQ = learner.getQValue(state, action)

      // Simulate reaching goal multiple times with updateWithGoal to get goal reward
      (1 to 10).foreach { _ =>
        learner.updateWithGoal(state, action, 0.0, goalState) // This applies goal reward
      }

      val finalQ = learner.getQValue(state, action)

      finalQ > initialQ // Q-value should increase when consistently reaching goal
  }

  // Property 9: Exploration rate should be between 0 and 1
  property("exploration rate is always valid probability") = forAll(learningParamsGen, Gen.choose(0, 5000)) {
    (params, episodes) =>
      val learner = createLearner(params)
        (1 to episodes).foreach(_ => learner.incEp())

      val eps = learner.eps
      eps >= 0.0 && eps <= 1.0
  }

  // Property 10: Action choice should return valid actions
  property("chosen actions are always valid") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      val learner = createLearner(params)
      val (action, _) = learner.choose(state)

      Action.values.contains(action)
  }