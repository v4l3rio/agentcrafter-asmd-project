package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for QLearner using ScalaCheck. These tests verify stochastic properties and invariants of the
 * Q-learning algorithm.
 */
object QLearnerProperties extends Properties("QLearner") with Matchers:
  
  private val TEST_GRID_SIZE: Int = 5
  private val TEST_REWARD_MIN: Double = -100.0
  private val TEST_REWARD_MAX: Double = 100.0
  private val TEST_ALPHA_MIN: Double = 0.01
  private val TEST_ALPHA_MAX: Double = 1.0
  private val TEST_GAMMA_MIN: Double = 0.0
  private val TEST_GAMMA_MAX: Double = 1.0
  private val TEST_EPSILON_MIN: Double = 0.0
  private val TEST_EPSILON_MAX: Double = 1.0
  private val TEST_WARM_MIN: Int = 1
  private val TEST_WARM_MAX: Int = 1000
  private val TEST_OPTIMISTIC_MIN: Double = -1.0
  private val TEST_OPTIMISTIC_MAX: Double = 1.0

  private val stateGen: Gen[State] = for {
    x <- Gen.choose(0, TEST_GRID_SIZE - 1)
    y <- Gen.choose(0, TEST_GRID_SIZE - 1)
  } yield State(x, y)

  private val actionGen: Gen[Action] = Gen.oneOf(Action.values)

  private val rewardGen: Gen[Double] = Gen.choose(TEST_REWARD_MIN, TEST_REWARD_MAX)

  private val learningParamsGen: Gen[LearningConfig] = for {
    alpha <- Gen.choose(TEST_ALPHA_MIN, TEST_ALPHA_MAX)
    gamma <- Gen.choose(TEST_GAMMA_MIN, TEST_GAMMA_MAX)
    eps0 <- Gen.choose(0.1, TEST_EPSILON_MAX)
    epsMin <- Gen.choose(TEST_EPSILON_MIN, 0.5)
    warm <- Gen.choose(TEST_WARM_MIN, TEST_WARM_MAX)
    optimistic <- Gen.choose(TEST_OPTIMISTIC_MIN, TEST_OPTIMISTIC_MAX)
  } yield {
    val actualEpsMin = math.min(epsMin, eps0)
    LearningConfig(alpha, gamma, eps0, actualEpsMin, warm, optimistic)
  }

  private def createLearner(params: LearningConfig = LearningConfig()): QLearner =
    val env = GridWorld(rows = TEST_GRID_SIZE, cols = TEST_GRID_SIZE, walls = Set.empty)
    QLearner(
      goalState = State(TEST_GRID_SIZE - 1, TEST_GRID_SIZE - 1),
      goalReward = TEST_REWARD_MAX,
      updateFunction = env.step,
      resetFunction = () => State(0, 0),
      learningConfig = params
    )

  // Test that verifies Q-values are initialized with the optimistic value from learning configuration
  // Optimistic initialization encourages exploration by starting with higher expected rewards
  property("Q-values start with optimistic initialization") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createLearner(params)
      val qValue = learner.getQValue(state, action)
      qValue == params.optimistic
  }

  // Test that verifies epsilon-greedy exploration parameter decays correctly after warm-up period
  // Epsilon should remain constant during warm-up, then decay towards minimum value
  property("epsilon decays correctly after warm-up") = forAll(learningParamsGen) { params =>
    val learner = createLearner(params)
    val initialEps = learner.eps

    // Simulate warm-up period where epsilon should remain constant
    (1 to params.warm).foreach(_ => learner.incEp())
    val epsAfterWarmup = learner.eps

    // After warm-up, epsilon should start decaying
    learner.incEp()
    val epsAfterDecay = learner.eps

    // Verify epsilon behavior during different phases
    val warmupCorrect = (initialEps == params.eps0) && (epsAfterWarmup == params.eps0)
    val decayCorrect =
      if (params.eps0 > params.epsMin) {
        epsAfterDecay <= epsAfterWarmup
      } else {
        epsAfterDecay == epsAfterWarmup
      }
    val boundsCorrect = learner.eps >= params.epsMin && learner.eps <= params.eps0

    warmupCorrect && decayCorrect && boundsCorrect
  }

  // Test that verifies Q-value updates follow the standard Q-learning formula
  // Q(s,a) = (1-α) * Q(s,a) + α * (r + γ * max(Q(s',a')))
  property("Q-value updates follow Q-learning formula") =
    forAll(learningParamsGen, stateGen, actionGen, rewardGen, stateGen) {
      (params, state, action, reward, nextState) =>
        val learner = createLearner(params)
        val initialQ = learner.getQValue(state, action)

        // Calculate maximum Q-value for next state (used in Q-learning formula)
        val maxNextQ = Action.values.map(a => learner.getQValue(nextState, a)).max

        // Perform Q-value update
        learner.update(state, action, reward, nextState)
        val updatedQ = learner.getQValue(state, action)

        // Calculate expected Q-value using Q-learning formula
        val expectedQ = (1 - params.alpha) * initialQ + params.alpha * (reward + params.gamma * maxNextQ)

        // Verify the update matches the expected formula (within numerical precision)
        math.abs(updatedQ - expectedQ) < 1e-10
    }

  // Test that verifies action selection follows epsilon-greedy policy
  // With epsilon=0, the agent should always choose the action with highest Q-value (exploitation)
  property("action selection follows epsilon-greedy policy") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      // Create learner with no exploration (epsilon = 0)
      val learner = createLearner(params.copy(eps0 = 0.0, epsMin = 0.0))

      // Set different Q-values for each action to create a clear best choice
      Action.values.zipWithIndex.foreach { case (action, idx) =>
        learner.update(state, action, idx * 10.0, State(0, 0))
      }

      // Choose action and verify it's the optimal one
      val (chosenAction, wasExploring) = learner.choose(state)
      val qValues = Action.values.map(a => learner.getQValue(state, a))
      val maxQ = qValues.max
      val chosenQ = learner.getQValue(state, chosenAction)

      // Verify no exploration occurred and optimal action was chosen
      !wasExploring && (chosenQ == maxQ)
  }

  // Test that verifies Q-values increase when repeatedly updated with positive rewards
  // This simulates learning a path towards the goal and validates basic learning behavior
  property("Q-values increase towards goal in simple path") = forAll(Gen.choose(0.1, 1.0), Gen.choose(0.1, 1.0)) {
    (alpha, gamma) =>

      val actualGamma = math.max(gamma, 0.1)
      val params = LearningConfig(alpha = alpha, gamma = actualGamma, eps0 = 0.0, epsMin = 0.0)
      val learner = createLearner(params)

      // Set up a state-action pair that leads directly to the goal
      val state = State(3, 4)
      val action = Action.Down
      val goalState = State(4, 4)

      val initialQ = learner.getQValue(state, action)
      
      // Repeatedly update with goal reward to simulate successful path learning
      (1 to 10).foreach { _ =>
        learner.update(state, action, learner.getGoalReward, goalState)
      }

      val finalQ = learner.getQValue(state, action)

      // Verify Q-value increased due to positive reward updates
      finalQ > initialQ
  }

  // Test that verifies uniform action distribution when epsilon = 1.0 (pure exploration)
  // With maximum exploration, all actions should be chosen with roughly equal probability
  property("action distribution is uniform when epsilon is 1.0") = {
    val params = LearningConfig(eps0 = 1.0, epsMin = 1.0)
    val learner = createLearner(params)
    val state = State(0, 0)
    val n = 10000
    val counts = scala.collection.mutable.Map.empty[Action, Int].withDefaultValue(0)
    // Sample many actions to test distribution
    (1 to n).foreach { _ =>
      val (action, _) = learner.choose(state)
      counts(action) += 1
    }
    val expected = n.toDouble / Action.values.size
    val tolerance = expected * 0.10 // 10% tolerance for statistical variation
    // Verify all actions are chosen with roughly equal frequency
    counts.values.forall(count => math.abs(count - expected) < tolerance)
  }

  // Test that verifies the optimal action is chosen most frequently with low epsilon and trained Q-values
  // After training, the agent should exploit the learned optimal action while occasionally exploring
  property("optimal action is chosen most often when epsilon is low and Q is trained") = {
    val params = LearningConfig(eps0 = 0.1, epsMin = 0.1)
    val learner = createLearner(params)
    val state = State(0, 0)
    // Train the learner to recognize Action.Up as optimal (high reward)
    Action.values.zipWithIndex.foreach { case (action, idx) =>
      val reward = if (action == Action.Up) 100.0 else 0.0
      learner.update(state, action, reward, state)
    }
    val n = 10000
    val counts = scala.collection.mutable.Map.empty[Action, Int].withDefaultValue(0)
    // Sample many actions to test exploitation vs exploration balance
    (1 to n).foreach { _ =>
      val (action, _) = learner.choose(state)
      counts(action) += 1
    }
    val optimalCount = counts(Action.Up)
    val nonOptimalCounts = Action.values.filter(_ != Action.Up).map(counts)
    
    // Verify optimal action is chosen significantly more often than any other action
    optimalCount > nonOptimalCounts.max * 2
  }
