package common

class GridWorld(
                 val rows:  Int = 13,
                 val cols:  Int = 15,
                 val start: State = State(0, 0),
                 val goal:  State = State(9, 8),
                 val walls: Set[State] = Set(
                   State(1,2), State(2,2), State(3,2), State(3,7), State(9,5), State(9,9),
                   State(4,4), State(5,4), State(6,4), State(7,7), State(8,8), State(10,10),
                   State(2,5), State(2,6), State(3,5), State(4,5), State(5,5), State(6,6),
                    State(7,6), State(8,6), State(9,6), State(10,6), State(11,6), State(12,6),
                    State(11,7), State(12,7), State(13,7), State(14,7), State(14,8), State(14,9),
                    State(14,10), State(14,11), State(14,12), State(14,13), State(14,14)
                 )
               ):
  private val stepPenalty = -3.0
  private val goalReward  = 50.0
  def reset(): State = start
  def step(s: State, a: Action): (State, Double, Boolean) =
    val (dr, dc) = a.delta
    val next0 = State(
      (s.r + dr).clamp(0, rows - 1),
      (s.c + dc).clamp(0, cols - 1)
    )
    val next   = if walls.contains(next0) then s else next0
    val done   = next == goal
    val reward = if done then goalReward else stepPenalty
    (next, reward, done)
extension (i: Int) private inline def clamp(lo: Int, hi: Int) = math.min(math.max(i, lo), hi)