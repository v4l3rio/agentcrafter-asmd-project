package llmqlearning

import MARL.DSL.{AgentProperty, LearnerProperty, LineProperty, SimulationDSL, TriggerProperty, WallProperty, block}
import llmqlearning.LLMProperty.*

object SimulationApp extends App with LLMQLearning:
  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  import MARL.DSL.SimulationProperty.*
  
  simulation:
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"
    grid:
      10 x 10
    asciiWalls(
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
        """.stripMargin)
    agent:
      Name >> "Explorer"
      Start >> (5, 8)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0.15 // with llm we can afford a lower initial exploration rate
        EpsMin >> 0.02
        Warm >> 500
        Optimistic >> 0.2
      Goal >> (3,4)
      Reward >> 100.0
    Episodes >> 1
    Steps >> 500
    Delay >> 50
    WithGUI >> true