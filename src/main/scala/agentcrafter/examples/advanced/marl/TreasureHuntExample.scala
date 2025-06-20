package agentcrafter.marl.scenarios

import agentcrafter.marl.dsl.*

/**
 * Treasure Hunt Scenario: A creative multi-agent cooperation scenario.
 *
 * Three agents must work together to collect treasure:
 * - "WallOpener1" must reach switch 1 to open the first wall
 * - "WallOpener2" must reach switch 2 to open the second wall
 * - "Hunter" must reach the final treasure location inside the chamber
 *
 * The agents must coordinate: both switches must be activated to open
 * the walls before the hunter can reach the treasure inside.
 */
object TreasureHuntExample extends App with Simulationdsl:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*

  simulation:
    grid:
      10 x 8

    asciiWalls:
      """########
        |#..##..#
        |#.####.#
        |#.#.#..#
        |#.#.#..#
        |########
        |#......#
        |########
        |#......#
        |########"""


    agent:
      Name >> "WallOpener1"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 1.0
      Goal >> (4, 1)
      onGoal:
        Give >> 70.0
        OpenWall >> (7, 5)
        EndEpisode >> false


    agent:
      Name >> "WallOpener2"
      Start >> (1, 6)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 1.0
      Goal >> (3, 5)
      onGoal:
        Give >> 70.0
        OpenWall >> (5, 3)
        EndEpisode >> false


    agent:
      Name >> "Hunter"
      Start >> (8, 1)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 0.5
      Goal >> (4, 3)
      onGoal:
        Give >> 100.0
        EndEpisode >> true
    Penalty >> -3.0
    Episodes >> 20_000
    Steps >> 600
    ShowAfter >> 17_000
    Delay >> 150
    WithGUI >> true