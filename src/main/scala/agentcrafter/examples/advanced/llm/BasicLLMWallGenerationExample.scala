package agentcrafter.examples.advanced.llm

import agentcrafter.marl.dsl.*
import agentcrafter.llmqlearning.LLMQLearning

/**
 * Simple example demonstrating the wallsFromLLM feature.
 * This shows basic usage with a single agent navigating an LLM-generated environment.
 */
object BasicLLMWallGenerationExample extends App with LLMQLearning:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*
  import WallLLMProperty.*

  simulation:
    grid:
      8 x 10


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
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_000
        Optimistic >> 0.4
      Goal >> (6, 8)
      onGoal:
        Give >> 100.0
        EndEpisode >> true

    Episodes >> 3_000
    Steps >> 300
    ShowAfter >> 2_500
    Delay >> 100
    WithGUI >> true