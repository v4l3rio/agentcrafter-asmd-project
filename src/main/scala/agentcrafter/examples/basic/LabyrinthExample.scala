package agentcrafter.examples.basic

import agentcrafter.marl.dsl.*

/**
 * Labyrinth Scenario: A single agent must navigate through a maze to reach the exit.
 * The maze has walls forming corridors and the agent must learn the optimal path.
 */
object LabyrinthExample extends App with SimulationDSL:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*

  simulation:
    grid:
      12 x 12

    asciiWalls:
      """############
        |#..........#
        |#.####.#####
        |#....#...#.#
        |####.#.#.#.#
        |#....#.#...#
        |#.####.###.#
        |#.#........#
        |#.#.######.#
        |#...#....#.#
        |#####....#.#
        |############"""

    agent:
      Name >> "Explorer"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 1_000
        Optimistic >> 0.2
      Goal >> (10, 10)
      onGoal:
        Give >> 100
        EndEpisode >> true
    Penalty >> -3.0
    Episodes >> 15_000
    Steps >> 500
    ShowAfter >> 10_000
    Delay >> 100
    WithGUI >> true