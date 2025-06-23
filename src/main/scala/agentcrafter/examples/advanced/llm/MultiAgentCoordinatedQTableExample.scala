package agentcrafter.examples.advanced.llm

import agentcrafter.marl.dsl.*
import agentcrafter.llmqlearning.dsl.LLMProperty.*
import agentcrafter.llmqlearning.dsl.LLMQLearning

/**
 * Advanced example demonstrating multi-agent Q-table generation from LLM.
 * This scenario showcases:
 * - Multiple agents with different goals requiring coordination
 * - LLM-generated Q-tables that consider agent interactions
 * - Robust fallback handling for corrupted Q-tables
 * - Agent-specific optimization while avoiding conflicts
 */
object MultiAgentCoordinatedQTableExample extends App with LLMQLearning:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*

  simulation:
    // Enable LLM for multi-agent Q-table generation
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"

    grid:
      5 x 5

    // Create a complex environment requiring coordination
    asciiWalls(
      """#####
        |#....
        |#.#.#
        |..#..
        |#.#..
        """.stripMargin)

    agent:
      Name >> "Lando"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0.1
        EpsMin >> 0.01
        Warm >> 100
        Optimistic >> 0.1
      Goal >> (4, 1)
      onGoal:
        Give >> 150.0
        EndEpisode >> false

    agent:
      Name >> "Carlos"
      Start >> (1, 2)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0.1
        EpsMin >> 0.01
        Warm >> 100
        Optimistic >> 0.1
      Goal >> (4, 4)
      onGoal:
        Give >> 150.0
        EndEpisode >> false

    agent:
      Name >> "Charles"
      Start >> (1, 3)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0.1
        EpsMin >> 0.01
        Warm >> 100
        Optimistic >> 0.1
      Goal >> (1, 4)
      onGoal:
        Give >> 150.0
        EndEpisode >> false

    Episodes >> 3
    Steps >> 400
    Delay >> 100
    WithGUI >> true