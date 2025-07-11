package agentcrafter.examples.advanced.llm.wall

import agentcrafter.llmqlearning.dsl.LLMQLearning
import agentcrafter.llmqlearning.dsl.LLMWallProperty.Model

/**
 * Example demonstrating the wallsFromLLM feature with cooperative switch mechanism.
 * This creates a simulation where the map layout is generated by an LLM and
 * one agent must activate a switch to open a wall for another agent.
 */
object MarlLLMWallsGenerationExample extends App with LLMQLearning:

  simulation:
    grid:
      15 x 12


    wallsFromLLM:
      Model >> "gpt-4o"
      Prompt >>
        """
        Create a challenging maze-like environment with the following characteristics:
        - A complex labyrinth with multiple paths
        - Several dead ends to make navigation challenging
        - At least 2-3 different routes between start and goal areas
        - Interesting chokepoints and open areas
        - The maze should encourage exploration and strategic thinking
        - Include some rooms connected by narrow corridors
        - Ensure there are walls that can be opened by a switch clicked by SwitchOperator agent
        """


    agent:
      Name >> "SwitchOperator"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.95
        Eps0 >> 0.8
        EpsMin >> 0.05
        Warm >> 2_000
        Optimistic >> 0.4
      Goal >> (3, 3)
      onGoal:
        Give >> 75.0
        OpenWall >> (6, 7)
        EndEpisode >> false


    agent:
      Name >> "Runner"
      Start >> (10, 1)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 1_000
        Optimistic >> 0.3
      Goal >> (10, 9)
      onGoal:
        Give >> 150.0
        EndEpisode >> true

    Episodes >> 5_000
    Steps >> 500
    Delay >> 100
    WithGUI >> true