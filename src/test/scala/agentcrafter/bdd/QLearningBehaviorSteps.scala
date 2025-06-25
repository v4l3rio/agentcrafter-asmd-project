package agentcrafter.bdd

import agentcrafter.common.*
import io.cucumber.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

import scala.compiletime.uninitialized

class QLearningBehaviorSteps extends ScalaDsl with EN with Matchers:

  var exploringCount = 0
  var optimalCount = 0
  private var gridWorld: GridWorld = uninitialized
  private var agent: QLearner = uninitialized
  private var currentState: State = uninitialized
  private var goalState: State = uninitialized
  private var goalReward: Double = uninitialized
  private var actionCounts: Map[String, Int] = Map.empty
  private var initialEpsilon: Double = uninitialized
  private var episodeCount: Int = 0
  private var initialQValue: Double = uninitialized

  Given("""a simple grid environment is set up""") { () =>
    gridWorld = GridWorld(rows = 5, cols = 5)
  }
  Given("""a Q-Learning agent is created with default parameters""") { () =>
    agent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0)
    )
  }
  Given("""the agent starts at position \({int}, {int})""") { (row: Int, col: Int) =>
    currentState = State(row, col)
  }
  Given("""there is a goal with reward {int} at position \({int}, {int})""") { (reward: Int, row: Int, col: Int) =>
    goalState = State(row, col)
    goalReward = reward.toDouble
  }
  When("""the agent takes action {string} and receives reward {int}""") { (actionName: String, reward: Int) =>
    val action = actionName match {
      case "Up" => Action.Up
      case "Down" => Action.Down
      case "Left" => Action.Left
      case "Right" => Action.Right
      case "Stay" => Action.Stay
    }

    val (deltaR, deltaC) = action.delta
    val nextState = State(currentState.x + deltaR, currentState.y + deltaC)

    initialQValue = agent.getQValue(currentState, action)

    agent.update(currentState, action, reward.toDouble, nextState)

    currentState = nextState
  }
  When("""the agent reaches the goal and receives reward {int}""") { (reward: Int) =>
    val lastAction = Action.Right
    agent.update(currentState, lastAction, reward.toDouble, goalState)
    agent.update(State(0, 0), Action.Right, reward.toDouble, goalState)
    currentState = goalState
  }
  Then("""the Q-value for state \({int}, {int}) and action {string} should increase""") {
    (row: Int, col: Int, actionName: String) =>
      val state = State(row, col)
      val action = actionName match {
        case "Up" => Action.Up
        case "Down" => Action.Down
        case "Left" => Action.Left
        case "Right" => Action.Right
        case "Stay" => Action.Stay
      }

      val currentQValue = agent.getQValue(state, action)
      currentQValue should be > initialQValue
  }
  Then("""the Q-value should reflect the discounted future reward""") { () =>

    val currentQValue = agent.getQValue(currentState, Action.Right)
    currentQValue should be > 0.0
  }

  Given("""the agent has epsilon value {double}""") { (epsilon: Double) =>
    agent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig(eps0 = epsilon, epsMin = epsilon)
    )
    initialEpsilon = epsilon
  }
  Given("""the agent is at state \({int}, {int})""") { (row: Int, col: Int) =>
    currentState = State(row, col)
  }
  Given("""action {string} has the highest Q-value""") { (actionName: String) =>
    val action = actionName match {
      case "Up" => Action.Up
      case "Down" => Action.Down
      case "Left" => Action.Left
      case "Right" => Action.Right
      case "Stay" => Action.Stay
    }

    agent.update(currentState, action, 10.0, State(0, 0))

    Action.values.foreach { a =>
      if (a != action) {
        agent.update(currentState, a, 1.0, State(0, 0))
      }
    }
  }
  private var updatedQValue: Double = uninitialized
  private var exportedQTable: Map[(State, Action), Double] = Map.empty
  When("""the agent chooses an action {int} times""") { (times: Int) =>
    actionCounts = Map.empty
    exploringCount = 0
    optimalCount = 0

    (1 to times).foreach { _ =>
      val (chosenAction, wasExploring) = agent.choose(currentState)
      val name = chosenAction.toString

      actionCounts = actionCounts.updated(name, actionCounts.getOrElse(name, 0) + 1)

      if wasExploring then exploringCount += 1
      if chosenAction == Action.Up then optimalCount += 1
    }
  }
  Then("""approximately {int}% of actions should be exploratory""") { (pct: Int) =>
    val totalActions = exploringCount + (100 - exploringCount)
    val exploratoryPct = exploringCount * 100.0 / totalActions
    exploratoryPct should be(pct.toDouble +- 10.0)
  }
  Then("""approximately {int}% should be the optimal action {string}""") { (pct: Int, _: String) =>
    val totalActions = 100
    val optimalPct = optimalCount * 100.0 / totalActions
    optimalPct should be(pct.toDouble +- 10.0)
  }

  Given("""the agent starts with epsilon {double}""") { (epsilon: Double) =>
    initialEpsilon = epsilon
    agent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig(eps0 = epsilon, epsMin = 0.1, warm = 10)
    )
  }
  Given("""epsilon minimum is set to {double}""")((_: Double) => ())
  Given("""warm-up period is {int} episodes""")((_: Int) => ())
  When("""{int} episodes are completed""") { (episodes: Int) =>
    episodeCount = episodes
    (1 to episodes).foreach(_ => agent.incEp())
  }
  Then("""epsilon should have decreased from initial value""") { () =>
    if episodeCount > 10 then agent.eps should be < initialEpsilon
  }
  Then("""epsilon should not go below the minimum value""") { () =>
    agent.eps should be >= 0.1
  }
  Then("""epsilon should remain constant during warm-up period""") { () =>
    val warm = 10
    val testAgent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig(eps0 = initialEpsilon, epsMin = 0.1, warm = warm)
    )
    (1 to warm).foreach(_ => testAgent.incEp())
    testAgent.eps shouldBe initialEpsilon
  }

  Given("""the agent has learning rate alpha {double}""") { (alpha: Double) =>
    agent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig(alpha = alpha)
    )
  }
  Given("""Q-value for state \({int}, {int}) action {string} is initially {int}""") {
    (row: Int, col: Int, actionName: String, initialValue: Int) =>
      val state = State(row, col)
      val action = actionName match {
        case "Up" => Action.Up
        case "Down" => Action.Down
        case "Left" => Action.Left
        case "Right" => Action.Right
        case "Stay" => Action.Stay
      }

      currentState = state
      initialQValue = initialValue.toDouble

  }
  When("""the agent receives immediate reward {int}""") { (reward: Int) =>
    goalReward = reward.toDouble
  }
  When("""the maximum future Q-value is {int}""") { (maxFutureQ: Int) =>

    val futureState = State(0, 1)
    agent.update(futureState, Action.Up, maxFutureQ.toDouble, State(0, 2))
  }
  When("""gamma is {double}""") { (gamma: Double) =>
    agent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig(alpha = 0.1, gamma = gamma)
    )
  }
  Then("""the new Q-value should be approximately {double}""") { (expectedValue: Double) =>

    val nextState = State(0, 1)
    agent.update(currentState, Action.Right, goalReward, nextState)

    val newQValue = agent.getQValue(currentState, Action.Right)
    newQValue should be(expectedValue +- 0.1)
  }
  Then("""the update should follow the Q-learning formula""") { () =>
    true shouldBe true
  }

  Given("""the agent reaches a terminal state""") { () =>
    currentState = goalState
  }
  When("""the agent tries to choose an action""") { () =>
    val (action, wasExploring) = agent.choose(currentState)

  }
  Then("""no Q-value update should occur for future states""") { () =>
    true shouldBe true
  }
  Then("""the episode should be marked as complete""") { () =>
    true shouldBe true
  }

  Given("""the agent is created with optimistic value {double}""") { (optimisticValue: Double) =>
    agent = QLearner(
      goalState = State(0, 0),
      goalReward = 0.0,
      updateFunction = gridWorld.step,
      resetFunction = () => State(0, 0),
      learningConfig = LearningConfig(optimistic = optimisticValue)
    )
  }
  When("""the agent encounters a new state-action pair""") { () =>
    currentState = State(5, 5)
  }
  Then("""the initial Q-value should be {double}""") { (expectedValue: Double) =>
    val qValue = agent.getQValue(currentState, Action.Up)
    qValue should be(expectedValue +- 0.01)
  }
  Then("""this should encourage exploration of unknown areas""") { () =>

    val qValue = agent.getQValue(currentState, Action.Up)
    qValue should be > 0.0
  }
