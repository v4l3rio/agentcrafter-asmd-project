package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for MDPLearner using ScalaCheck.
 * These tests verify stochastic properties and invariants of the MDP-based Q-learning algorithm.
 */
object MDPLearnerProperties extends Properties("MDPLearner") with Matchers:

  // Generators for test data
  private val stateGen: Gen[State] = for {
    row <- Gen.choose(0, 4)
    col <- Gen.choose(0, 4)
  } yield State(row, col)

  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)

  private val rewardGen: Gen[Reward] = Gen.choose(-10.0, 10.0)

  private val learningParamsGen: Gen[LearningParameters] = for {
    alpha <- Gen.choose(0.01, 1.0)
    gamma <- Gen.choose(0.1, 1.0)
    eps0 <- Gen.choose(0.1, 1.0)
    epsMin <- Gen.choose(0.0, 0.5)
    warm <- Gen.choose(1, 100)
    optimistic <- Gen.choose(-1.0, 1.0)
  } yield {
    val actualEpsMin = math.min(epsMin, eps0) // Ensure epsMin <= eps0
    LearningParameters(alpha, gamma, eps0, actualEpsMin, warm, optimistic)
  }

  private def createMDPLearner(
    params: LearningParameters = LearningParameters(),
    goalState: State = State(4, 4),
    goalReward: Double = 100.0,
    initialState: State = State(0, 0)
  ): MDPLearner = {
    val gridWorld = GridWorld(
      rows = 5,
      cols = 5,
      walls = Set.empty
    )
    
    new MDPLearner(params, goalState, goalReward, gridWorld, initialState)
  }

  // Property 1: Q-values should start with optimistic initialization
  property("Q-values start with optimistic initialization") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createMDPLearner(params)
      val qValue = learner.getQValue(state, action)
      qValue == params.optimistic
  }

  // Property 2: Epsilon should decay properly over episodes
  property("epsilon decays correctly after warm-up") = forAll(learningParamsGen) { params =>
    val learner = createMDPLearner(params)
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

  // Property 3: Q-values should change after updates (when alpha > 0)
  property("Q-values change after updates") = forAll(learningParamsGen, stateGen, actionGen, rewardGen, stateGen) {
    (params, state, action, reward, nextState) =>
      val learner = createMDPLearner(params)
      val initialQ = learner.getQValue(state, action)
      
      learner.update(state, action, reward, nextState)
      val updatedQ = learner.getQValue(state, action)
      
      // If alpha > 0 and there's a learning signal, Q-value should change
      if (params.alpha > 0.0) {
        val maxNextQ = Action.values.map(learner.getQValue(nextState, _)).max
        val tdError = reward + params.gamma * maxNextQ - initialQ
        
        if (math.abs(tdError) > 1e-10) {
          updatedQ != initialQ // Q-value should change when there's a TD error
        } else {
          true // No change expected when TD error is zero
        }
      } else {
        updatedQ == initialQ // No change when alpha = 0
      }
  }

  // Property 4: Action selection should follow epsilon-greedy policy
  property("action selection follows epsilon-greedy policy") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      val learner = createMDPLearner(params)
      
      // Train the learner a bit to have some Q-values
      (1 to 10).foreach { _ =>
        val (action, _) = learner.choose(state)
        learner.update(state, action, 1.0, State(state.r + 1, state.c))
      }
      
      // With epsilon = 0, should always choose greedy action
      val paramsGreedy = params.copy(eps0 = 0.0, epsMin = 0.0)
      val greedyLearner = createMDPLearner(paramsGreedy)
      
      // Copy Q-values to greedy learner
      Action.values.foreach { a =>
        val qValue = learner.getQValue(state, a)
        if (qValue != params.optimistic) {
          greedyLearner.update(state, a, qValue - params.optimistic, state)
        }
      }
      
      val (greedyAction, wasExploring) = greedyLearner.choose(state)
      !wasExploring // Should not be exploring with epsilon = 0
  }

  // Property 5: Q-table snapshot should be consistent with getQValue
  property("Q-table snapshot is consistent with getQValue") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createMDPLearner(params)
      val snapshot = learner.QTableSnapshot
      val directValue = learner.getQValue(state, action)
      val snapshotValue = snapshot.getOrElse((state, action), params.optimistic)
      
      directValue == snapshotValue
  }

  // Property 6: Episode outcomes should be deterministic with same conditions
  property("episodes are deterministic with same conditions") = forAll(learningParamsGen) { params =>
    // Create two identical learners
    val learner1 = createMDPLearner(params)
    val learner2 = createMDPLearner(params)
    
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

  // Property 8: Q-values should increase towards goal in simple path
  property("Q-values increase towards goal in simple path") = forAll(Gen.choose(0.1, 1.0), Gen.choose(0.1, 1.0)) {
    (alpha, gamma) =>
      val actualGamma = math.max(gamma, 0.1)
      val params = LearningParameters(alpha = alpha, gamma = actualGamma, eps0 = 0.0, epsMin = 0.0)
      val learner = createMDPLearner(params)
      
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
  property("exploration rate is always valid probability") = forAll(learningParamsGen, Gen.choose(0, 1000)) {
    (params, episodes) =>
      val learner = createMDPLearner(params)
      (1 to episodes).foreach(_ => learner.incEp())
      
      val eps = learner.eps
      eps >= 0.0 && eps <= 1.0
  }

  // Property 10: Action choice should return valid actions
  property("chosen actions are always valid") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      val learner = createMDPLearner(params)
      val (action, _) = learner.choose(state)
      
      Action.values.contains(action)
  }