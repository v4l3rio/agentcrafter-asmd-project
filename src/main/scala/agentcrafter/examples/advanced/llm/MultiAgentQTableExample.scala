package agentcrafter.examples.advanced.llm

import agentcrafter.MARL.DSL.*
import agentcrafter.llmqlearning.LLMProperty.{Enabled, Model}
import agentcrafter.llmqlearning.LLMQLearning

/**
 * Example demonstrating multi-agent QTable loading from LLM.
 * This scenario tests the system's ability to load separate Q-tables
 * for multiple agents working in a cooperative environment.
 */
object MultiAgentQTableExample extends App with LLMQLearning:
  import AgentProperty.*
  import LearnerProperty.*
  import TriggerProperty.*
  import WallProperty.*
  import agentcrafter.MARL.DSL.SimulationProperty.*
  
  simulation:
    // Enable LLM for Q-table generation
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"
    
    grid:
      10 x 12
    
    // Create a complex environment with multiple challenges
    asciiWalls(
      """############
        |#..........#
        |#.####.###.#
        |#.#..#...#.#
        |#.#..##..#.#
        |#.#......#.#
        |#.########.#
        |#..........#
        |#.####.####
        |#..........#
        """.stripMargin)
    
    // Agent 1: Pathfinder - explores and finds optimal routes
    agent:
      Name >> "Pathfinder"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.05  // Low learning rate since we're using LLM Q-table
        Gamma >> 0.99
        Eps0 >> 0.1   // Low exploration with LLM guidance
        EpsMin >> 0.01
        Warm >> 200   // Shorter warm-up with pre-trained Q-table
        Optimistic >> 0.1
      Goal >> (5, 6)  // Central checkpoint
      onGoal:
        Give >> 50.0
        EndEpisode >> false  // Continue for cooperative task
    
    // Agent 2: Collector - gathers resources after pathfinder
    agent:
      Name >> "Collector"
      Start >> (8, 1)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0.1
        EpsMin >> 0.01
        Warm >> 200
        Optimistic >> 0.1
      Goal >> (3, 8)  // Resource location
      onGoal:
        Give >> 75.0
        EndEpisode >> false
    
    // Agent 3: Deliverer - completes the mission
    agent:
      Name >> "Deliverer"
      Start >> (8, 10)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0.1
        EpsMin >> 0.01
        Warm >> 200
        Optimistic >> 0.1
      Goal >> (1, 10)  // Final delivery point
      onGoal:
        Give >> 100.0  // Highest reward for mission completion
        EndEpisode >> true  // End when mission is complete
    
    // Simulation parameters optimized for LLM-assisted learning
    Episodes >> 500   // Fewer episodes needed with pre-trained Q-tables
    Steps >> 200      // Shorter episodes with better initial policy
    Delay >> 100
    WithGUI >> true