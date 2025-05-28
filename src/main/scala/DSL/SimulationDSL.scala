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
 * Demo application showing how to use the DSL
 */
@main def DSLDemo(): Unit =
  import dsl.*
  Simulation()
    .grid(10, 10)
    .wallsFromAscii(
      """...........
        |...###.....
        |...#.#.....
        |...###.....
        |...........
        |...........
        |...........
        |...........
        |...........
        |...........""".stripMargin)
    .agent("A").start(8, 1).goal(0, 0).reward(0).end()
    .agent("B").start(1, 1).goal(4, 9).reward(120).end()
    .on("A", 8, 6).openWall(2, 3).give(30)
    .on("B", 4, 9).endEpisode().give(120)
    .episodes(25_000)
    .withGUI(true)
    .play()