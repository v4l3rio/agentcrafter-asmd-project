package marl

enum Action derives CanEqual:
  case Up, Down, Left, Right
  def delta: (Int, Int) = this match
    case Up    => (-1, 0)
    case Down  => ( 1, 0)
    case Left  => ( 0,-1)
    case Right => ( 0, 1)