package agentcrafter.MARL.scenarios

import agentcrafter.MARL.DSL.{SimulationDSL, *}

/**
 * Labyrinth Scenario: A single agent must navigate through a maze to reach the exit.
 * The maze has walls forming corridors and the agent must learn the optimal path.
 */
object LabyrinthScenario extends App with SimulationDSL:
  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      12 x 12
    
    // Create a labyrinth using ASCII art
    asciiWalls:
      """############
        |#..........#
        |#.####.###.#
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
      Start >> (1, 1)  // Top-left corner inside the maze
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 2_000
        Optimistic >> 1.0
      Goal >> (10, 10)
      Reward >> 100.0
    
    Episodes >> 15_000
    Steps >> 500
    ShowAfter >> 12_000
    Delay >> 80
    WithGUI >> true