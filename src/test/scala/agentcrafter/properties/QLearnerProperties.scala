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
    val actualEpsMin = math.min(epsMin, eps0)
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
      val params = LearningParameters(alpha = alpha, gamma = actualGamma, eps0 = 0.0, epsMin = 0.0)
      val learner = createLearner(params)

      val state = State(3, 4)
      val action = Action.Down
      val goalState = State(4, 4)

      val initialQ = learner.getQValue(state, action)

      (1 to 10).foreach { _ =>
        learner.updateWithGoal(state, action, 0.0, goalState)
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
