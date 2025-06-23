package agentcrafter.examples.advanced.llm.wall

import agentcrafter.llmqlearning.dsl.LLMQLearning
import agentcrafter.marl.dsl.*

/**
 * Simple example demonstrating the wallsFromLLM feature.
 * This shows basic usage with a single agent navigating an LLM-generated environment.
 */
object BasicLLMWallGenerationExample extends App with LLMQLearning:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*
  import agentcrafter.llmqlearning.dsl.LLMWallProperty.*

  simulation:
    grid:
      6 x 6

    wallsFromLLM:
      Model >> "gpt-4o"
      Prompt >>
        """
        Create a simple but interesting maze for a single agent.
        """

    agent:
      Name >> "Runner"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 1_000
        Optimistic >> 0.4
      Goal >> (4, 4)
      onGoal:
        Give >> 100.0
        EndEpisode >> true

    Episodes >> 5_000
    Steps >> 300
    Delay >> 100
    WithGUI >> true