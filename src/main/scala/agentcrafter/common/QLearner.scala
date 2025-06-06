package agentcrafter.common

import scala.util.Random
import scala.collection.mutable
import scala.annotation.tailrec

import QLearner.*

case class LearningParameters(
                               alpha: Double = 0.1,
                               gamma: Double = 0.99,
                               eps0: Double = 0.9,
                               epsMin: Double = 0.15,
                               warm: Int = 10_000,
                               optimistic: Double = 0.5
                             ):
  def calculateEpsilon(ep: Int): Double =
    if ep < warm then eps0 else math.max(epsMin, eps0 - (eps0 - epsMin) * (ep - warm) / warm)


private type Trajectory = List[(State, Action, Boolean, Array[Double])]
type Reward = Double
type ID = String
type EpisodeOutcome = (Boolean, Int, Trajectory)
type UpdateFunction = (State, Action) => StepResult
type ResetFunction = () => State


extension [T](actions: Array[T])
  private def draw(using rng: Random) = actions(rng.nextInt(actions.length))

object QLearner:
  def apply(
             goalState: State,
             goalReward: Reward,
             updateFunction: UpdateFunction,
             resetFunction: ResetFunction,
             learningParameters: LearningParameters = LearningParameters(),
           ): QLearner =
    new QLearner(learningParameters, goalState, goalReward, updateFunction, resetFunction)

class QLearner private(
                        learningParameters: LearningParameters,
                        goalState: State,
                        goalReward: Reward,
                        updateFunction: UpdateFunction,
                        resetFunction: ResetFunction
                      ):
  /** Random number generator for stochastic action selection */
  private given rng: Random = Random()

  private class QTable:
    private val table: mutable.Map[(State, Action), Double] = mutable.Map().withDefaultValue(learningParameters.optimistic)

    def tableSnapshot(): Map[(State, Action), Double] =
      table.toMap

    def qValues(state: State): Array[Reward] = A.map(a => table(state -> a))

    def update(state: State, action: Action, reward: Reward, newState: State): Unit =
      val bestNext = A.map(a2 => table(newState, a2)).max
      val newValue = (1 - learningParameters.alpha) *
        table(state, action) + learningParameters.alpha *
        (reward + learningParameters.gamma * bestNext)
      table(state -> action) = newValue

    def greedy(p: State): Action =
      val actionValues = A.map(a => a -> table(p -> a))
      val maxValue = actionValues.map(_._2).max
      actionValues.collect { case (a, v) if v == maxValue => a }.draw


  enum Choice:
    case Exploring(action: Action)
    case Exploiting(state: State)


  /** Array of all possible actions */
  private val A = Action.values

  private val Q: QTable = QTable()

  private var ep = 0

  def eps: Double = learningParameters.calculateEpsilon(ep)

  private def chooseInternal(p: State): Choice =
    rng.nextDouble() match
      case d if d < eps => Choice.Exploring(A.draw)
      case _ => Choice.Exploiting(p)

  def episode(maxSteps: Int = 200): EpisodeOutcome =
    ep += 1

    @tailrec
    def loop(state: State, steps: Int, acc: List[(State, Action, Boolean, Array[Reward])]): EpisodeOutcome =
      if steps >= maxSteps then (false, steps, acc.reverse)
      else
        val (action, isExploring) = chooseInternal(state) match
          case Choice.Exploring(action) => (action, true)
          case Choice.Exploiting(state) => (Q.greedy(state), false)

        val StepResult(nextState, nextStateReward) = updateFunction(state, action)
        val isGoal = nextState == goalState
        val reward = if isGoal then goalReward else nextStateReward // Step penalty

        Q.update(state, action, reward, nextState)

        val newAcc = (state, action, isExploring, Q.qValues(state)) :: acc

        if isGoal then (true, steps + 1, newAcc.reverse)
        else loop(nextState, steps + 1, newAcc)

    val initialState = resetFunction()
    loop(initialState, 0, Nil)


  def QTableSnapshot: Map[(State, Action), Double] =
    Q.tableSnapshot()

  def getQValue(state: State, action: Action): Double =
    Q.tableSnapshot().getOrElse((state, action), learningParameters.optimistic)

  // MARL compatibility methods
  def choose(state: State): (Action, Boolean) =
    chooseInternal(state) match
      case Choice.Exploring(action) => (action, true)
      case Choice.Exploiting(_) => (Q.greedy(state), false)

  def update(state: State, action: Action, reward: Reward, nextState: State): Unit =
    Q.update(state, action, reward, nextState)

  def incEp(): Unit =
    ep += 1
