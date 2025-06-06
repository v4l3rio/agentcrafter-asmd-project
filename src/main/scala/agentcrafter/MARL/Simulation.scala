package agentcrafter.MARL

import agentcrafter.MARL.builders.SimulationBuilder

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
@main def MARLSimulationDemo(): Unit =
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
      .end()
    .agent("Opener")
      .start(8,1)
      .withLearner()
      .goal(8,6)
      .end()
    .episodes(10_000)
    .steps(400)
    .showAfter(9_000)
    .delay(100)
    .withGUI(true)
    .play()