package agentcrafter.common

/** Interface for reinforcement learning implementations. */
trait Learner:
  def choose(state: State): (Action, Boolean)
  def update(state: State, action: Action, reward: Reward, nextState: State): Unit
  def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit
  def episode(maxSteps: Int = 200): EpisodeOutcome
  def eps: Double
  def incEp(): Unit
  def QTableSnapshot: Map[(State, Action), Double]
  def getQValue(state: State, action: Action): Double