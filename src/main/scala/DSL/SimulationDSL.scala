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
    .agent("Opener").start(8,1)
    .withLearner(
      alpha = 0.2,
      gamma = 0.99,  // Discount factor più alto per valorizzare reward futuri
      eps0 = 0.95,
      epsMin = 0.1,
      optimistic = 0.1
    ).noGoal()
    // Runner agent
    .agent("Runner").start(1,1).goal(2,4).reward(50)  // ← Reward ridotto
    .withLearner(
      alpha = 0.15,
      gamma = 0.99,
      eps0 = 0.95,
      epsMin = 0.1,
      optimistic = 0.1
    ).end()
    .on("Opener",8,6).openWall(2,3).give(20)  // ← Reward ridotto
    .episodes(40_000).steps(200).showAfter(38_000)
    .delay(100)
    .withGUI(true)
    .play()