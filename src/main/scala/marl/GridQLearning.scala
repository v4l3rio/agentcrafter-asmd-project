package marl

import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import scala.math.{max, abs}

//////////////////////////
// 1. Data models
//////////////////////////
case class State(row: Int, col: Int)
enum Action derives CanEqual:
  case Up, Down, Left, Right
  def delta = this match
    case Up    => (-1, 0)
    case Down  => ( 1, 0)
    case Left  => ( 0,-1)
    case Right => ( 0, 1)

//////////////////////////
// 2. Grid-world
//////////////////////////
class GridWorld(
                 val rows:  Int = 5,
                 val cols:  Int = 5,
                 val start: State = State(0, 0),
                 val goal:  State = State(4, 4),
                 val walls: Set[State] = Set(State(1,2), State(2,2), State(3,2))
               ):
  private inline val stepPenalty = -1.0
  private inline val goalReward  = 50.0

  def reset(): State = start

  /** returns (nextState, reward, done) */
  def step(s: State, a: Action): (State, Double, Boolean) =
    val (dr, dc) = a.delta
    val cand = State(
      (s.row + dr).clamp(0, rows - 1),
      (s.col + dc).clamp(0, cols - 1)
    )
    val next   = if walls.contains(cand) then s else cand
    val done   = next == goal
    val reward = if done then goalReward else stepPenalty
    (next, reward, done)

extension (i: Int) private inline def clamp(lo: Int, hi: Int) = math.min(math.max(i, lo), hi)

//////////////////////////
// 3. Agent
//////////////////////////
class QAgent(
              env:    GridWorld,
              alpha:  Double = 0.1,
              gamma:  Double = 0.99,
              eps0:   Double = 0.9,
              epsMin: Double = 0.05,
              decay:  Double = 0.995
            ):
  private val rng      = new Random()
  private val actions  = Action.values
  private val q        = Array.ofDim[Double](env.rows, env.cols, actions.length)
  private var ε        = eps0
  def epsilon: Double  = ε                      // public read-only accessor

  /** pick action using ε-greedy */
  private def choose(s: State) =
    if rng.nextDouble() < ε
    then actions(rng.nextInt(actions.length))
    else
      val qs = q(s.row)(s.col)
      actions(qs.indexOf(qs.max))

  /** one episode; returns: (totalReward, steps, trajectory) */
  def runEpisode(maxSteps: Int = 200): (Double, Int, List[State]) =
    var s      = env.reset()
    var totR   = 0.0
    var steps  = 0
    val path   = ArrayBuffer[State](s)
    var done   = false
    while !done && steps < maxSteps do
      val a                 = choose(s)
      val (s2, r, d)        = env.step(s, a)
      val bestNext          = q(s2.row)(s2.col).max
      q(s.row)(s.col)(a.ordinal) =
        (1 - alpha) * q(s.row)(s.col)(a.ordinal) +
          alpha * (r + gamma * bestNext)

      s       = s2
      totR   += r
      done    = d
      steps  += 1
      path   += s
    ε = max(epsMin, ε * decay)
    (totR, steps, path.toList)

  /* Greedy policy for display */
  def bestAction(s: State): Action =
    val qs = q(s.row)(s.col)
    actions(qs.indexOf(qs.max))
end QAgent

//////////////////////////
// 4. Pretty helpers
//////////////////////////
def showTrajectory(env: GridWorld, traj: List[State]): Unit =
  val lookup = traj.zipWithIndex.toMap     // earliest visit wins
  for r <- 0 until env.rows do
    for c <- 0 until env.cols do
      val s = State(r, c)
      val ch =
        if env.walls.contains(s) then "##"
        else if s == env.goal     then "G "
        else if s == env.start    then "S "
        else lookup.get(s) match
          case Some(i) if i < 36  => ('a' + i).toChar + " " // a,b,c,… shows order
          case Some(_)            => "* "
          case None               => ". "
      print(ch)
    println()
  println()

//////////////////////////
// 5. Driver -- Stage 1
//////////////////////////
@main def Train(): Unit =
  val env      = GridWorld()
  val agent    = QAgent(env)
  val episodes = 10_000
  val stats    = ArrayBuffer.empty[Int]   // steps to goal

  val printEvery = 500

  for ep <- 1 to episodes do
    val (_, steps, path) = agent.runEpisode()
    stats += steps
    if ep % printEvery == 0 then
      val avg = stats.takeRight(printEvery).sum.toDouble / printEvery
      println(f"Episode $ep%5d | ε = ${agent.epsilon}%.3f | avg steps(last $printEvery) = $avg%.1f")
      showTrajectory(env, path)