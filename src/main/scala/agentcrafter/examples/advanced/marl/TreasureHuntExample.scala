package agentcrafter.MARL.scenarios

import agentcrafter.MARL.DSL.*

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
object TreasureHuntExample extends App with SimulationDSL:

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

    // WallOpener1: Activates switch 1 to open first door
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
      Goal >> (4, 1) // Switch 1 location
      onGoal:
        Give >> 70.0 // Reward for activating switch 1
        OpenWall >> (7, 5) // Open door 1
        EndEpisode >> false // Continue until treasure is found

    // WallOpener2: Activates switch 2 to open second door
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
      Goal >> (3, 5) // Switch 2 location
      onGoal:
        Give >> 70.0 // Reward for activating switch 2
        OpenWall >> (5, 3) // Open door 2
        EndEpisode >> false // Continue until treasure is found

    // Hunter: Claims the final treasure inside the chamber
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
      Goal >> (4, 3) // Treasure location inside the chamber
      onGoal:
        Give >> 100.0 // Big reward for reaching treasure
        EndEpisode >> true // End episode when treasure is claimed
    Penalty >> -3.0
    Episodes >> 20_000
    Steps >> 600
    ShowAfter >> 17_000
    Delay >> 150
    WithGUI >> true