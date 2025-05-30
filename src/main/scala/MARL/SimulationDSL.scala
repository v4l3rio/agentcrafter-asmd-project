package DSL

/**
 * Entry point for the Simulation DSL
 */
object dsl:
  /**
   * Creates a new simulation builder to configure and run a simulation
   */
  def Simulation(): SimulationBuilder = new SimulationBuilder

/**
 * Demo application showing how to use the DSL with corrected Q-learning parameters
 */
@main def DSLDemo(): Unit =
  import dsl.*
  Simulation()
    .grid(10,10)
    .wallsFromAscii(
      """..........
        |...###....
        |...#.#....
        |...###....
        |..........
        |..........
        |..........
        |..........
        |..........
        |..........""".stripMargin)
    // Opener agent con ottimismo ridotto
    .agent("Runner")
      .start(1,9)
      .withLearner()
      .goal(2,4)
        .reward(100)
        .end()
    .agent("Opener")
      .start(8,1)
      .withLearner()
      .noGoal()
    .on("Opener",8,6)
    .openWall(2,3)
    .give(50)
    .episodes(10_000)
    .steps(400)
    .showAfter(9_000)
    .delay(100)
    .withGUI(true)
    .play()