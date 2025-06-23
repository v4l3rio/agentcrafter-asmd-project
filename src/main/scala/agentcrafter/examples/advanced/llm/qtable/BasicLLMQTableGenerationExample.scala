package agentcrafter.examples.advanced.llm.qtable

import agentcrafter.llmqlearning.dsl.LLMQLearning
import agentcrafter.llmqlearning.dsl.LLMProperty.Model
import agentcrafter.marl.dsl.*


object BasicLLMQTableGenerationExample extends App with LLMQLearning:

  simulation:
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"
    grid:
      10 x 10
    asciiWalls:
      """##########
        |#.......##
        |#.####...#
        |#.#..#.#.#
        |#.#.##.#.#
        |#.#....#.#
        |#..####...
        |##.#......
        |#......#.#
        |#.#......#
        """
    agent:
      Name >> "Explorer"
      Start >> (5, 8)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0
        EpsMin >> 0
        Warm >> 500
        Optimistic >> 0.2
      Goal >> (3, 4)
      onGoal:
        Give >> 100
        EndEpisode >> true
    Episodes >> 1
    Steps >> 500
    Delay >> 50
    WithGUI >> true