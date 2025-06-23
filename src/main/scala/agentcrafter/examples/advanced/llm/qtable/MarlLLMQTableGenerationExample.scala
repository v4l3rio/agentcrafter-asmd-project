package agentcrafter.examples.advanced.llm.qtable

import agentcrafter.llmqlearning.dsl.LLMProperty.Model
import agentcrafter.llmqlearning.dsl.LLMQLearning

object MarlLLMQTableGenerationExample extends App with LLMQLearning:

  simulation:
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"

    grid:
      5 x 5

    asciiWalls:
      """#####
        |#....
        |#.#.#
        |..#..
        |#.#..
        """

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