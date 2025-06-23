package agentcrafter.properties

import agentcrafter.common.*
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Gen, Properties}
import org.scalatest.matchers.should.Matchers

/**
 * Property-based tests for QLearner using ScalaCheck. These tests verify stochastic properties and invariants of the
 * Q-learning algorithm.
 */
object QLearnerProperties extends Properties("QLearner") with Matchers:

  // Test constants
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

  property("Q-values start with optimistic initialization") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createLearner(params)
      val qValue = learner.getQValue(state, action)
      qValue == params.optimistic
  }

  property("epsilon decays correctly after warm-up") = forAll(learningParamsGen) { params =>
    val learner = createLearner(params)
    val initialEps = learner.eps

    (1 to params.warm).foreach(_ => learner.incEp())
    val epsAfterWarmup = learner.eps

    learner.incEp()
    val epsAfterDecay = learner.eps

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

  property("Q-value updates follow Q-learning formula") =
    forAll(learningParamsGen, stateGen, actionGen, rewardGen, stateGen) {
      (params, state, action, reward, nextState) =>
        val learner = createLearner(params)
        val initialQ = learner.getQValue(state, action)

        val maxNextQ = Action.values.map(a => learner.getQValue(nextState, a)).max

        learner.update(state, action, reward, nextState)
        val updatedQ = learner.getQValue(state, action)

        val expectedQ = (1 - params.alpha) * initialQ + params.alpha * (reward + params.gamma * maxNextQ)

        math.abs(updatedQ - expectedQ) < 1e-10
    }

  property("action selection follows epsilon-greedy policy") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      val learner = createLearner(params.copy(eps0 = 0.0, epsMin = 0.0))

      Action.values.zipWithIndex.foreach { case (action, idx) =>
        learner.update(state, action, idx * 10.0, State(0, 0))
      }

      val (chosenAction, wasExploring) = learner.choose(state)
      val qValues = Action.values.map(a => learner.getQValue(state, a))
      val maxQ = qValues.max
      val chosenQ = learner.getQValue(state, chosenAction)

      !wasExploring && (chosenQ == maxQ)
  }

  property("Q-table snapshot is consistent with getQValue") = forAll(learningParamsGen, stateGen, actionGen) {
    (params, state, action) =>
      val learner = createLearner(params)
      val snapshot = learner.QTableSnapshot
      val directValue = learner.getQValue(state, action)
      val snapshotValue = snapshot.getOrElse((state, action), params.optimistic)

      directValue == snapshotValue
  }

  property("episodes are deterministic with same conditions") = forAll(learningParamsGen) { params =>

    val learner1 = createLearner(params)
    val learner2 = createLearner(params)

    learner1.eps == learner2.eps
  }

  property("learning parameters maintain valid bounds") = forAll(learningParamsGen) { params =>
    (params.alpha >= 0.0 && params.alpha <= 1.0) &&
    (params.gamma >= 0.0 && params.gamma <= 1.0) &&
    (params.eps0 >= 0.0 && params.eps0 <= 1.0) &&
    (params.epsMin >= 0.0 && params.epsMin <= 1.0) &&
    (params.epsMin <= params.eps0) &&
    (params.warm >= 0)
  }

  property("Q-values increase towards goal in simple path") = forAll(Gen.choose(0.1, 1.0), Gen.choose(0.1, 1.0)) {
    (alpha, gamma) =>

      val actualGamma = math.max(gamma, 0.1)
      val params = LearningConfig(alpha = alpha, gamma = actualGamma, eps0 = 0.0, epsMin = 0.0)
      val learner = createLearner(params)

      val state = State(3, 4)
      val action = Action.Down
      val goalState = State(4, 4)

      val initialQ = learner.getQValue(state, action)

      // Use the goal reward when reaching the goal state
      (1 to 10).foreach { _ =>
        learner.update(state, action, learner.getGoalReward, goalState)
      }

      val finalQ = learner.getQValue(state, action)

      finalQ > initialQ
  }

  property("chosen actions are always valid") = forAll(learningParamsGen, stateGen) {
    (params, state) =>
      val learner = createLearner(params)
      val (action, _) = learner.choose(state)

      Action.values.contains(action)
  }

  property("action distribution is uniform when epsilon is 1.0") = {
    val params = LearningConfig(eps0 = 1.0, epsMin = 1.0)
    val learner = createLearner(params)
    val state = State(0, 0)
    val n = 10000
    val counts = scala.collection.mutable.Map.empty[Action, Int].withDefaultValue(0)
    (1 to n).foreach { _ =>
      val (action, _) = learner.choose(state)
      counts(action) += 1
    }
    val expected = n.toDouble / Action.values.size
    val tolerance = expected * 0.10 // 10% tolerance
    counts.values.forall(count => math.abs(count - expected) < tolerance)
  }

  property("optimal action is chosen most often when epsilon is low and Q is trained") = {
    val params = LearningConfig(eps0 = 0.1, epsMin = 0.1)
    val learner = createLearner(params)
    val state = State(0, 0)
    // Train Q-values: make Action.Up optimal
    Action.values.zipWithIndex.foreach { case (action, idx) =>
      val reward = if (action == Action.Up) 100.0 else 0.0
      learner.update(state, action, reward, state)
    }
    val n = 10000
    val counts = scala.collection.mutable.Map.empty[Action, Int].withDefaultValue(0)
    (1 to n).foreach { _ =>
      val (action, _) = learner.choose(state)
      counts(action) += 1
    }
    val optimalCount = counts(Action.Up)
    val nonOptimalCounts = Action.values.filter(_ != Action.Up).map(counts)
    // Optimal should be chosen much more often than any non-optimal
    optimalCount > nonOptimalCounts.max * 2
  }
