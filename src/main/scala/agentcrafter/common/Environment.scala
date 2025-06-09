package agentcrafter.common

/** Simple environment interface used by learning algorithms. */
trait Environment:
  /** Number of rows. */
  def rows: Int
  /** Number of columns. */
  def cols: Int
  /** Perform a transition from the given state using the provided action. */
  def step(state: State, action: Action): StepResult
