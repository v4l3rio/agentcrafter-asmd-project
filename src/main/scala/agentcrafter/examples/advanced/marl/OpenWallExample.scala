package agentcrafter.examples.advanced.marl


import agentcrafter.marl.dsl.*

/**
 * Wall Scenario: Two agents cooperate to solve a task.
 * - Agent "Opener" must reach a switch to open a wall
 * - Agent "Runner" must reach a goal on the other side of the wall
 * - The simulation ends when both agents reach their respective goals
 */
object OpenWallExample extends App with SimulationDSL:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*
  import LineProperty.*

  simulation:
    grid:
      8 x 12

    walls:

      line:
        Direction >> "vertical"
        From >> (1, 6)
        To >> (6, 6)

      block >> (2, 2)
      block >> (3, 2)
      block >> (5, 9)
      block >> (6, 9)


    agent:
      Name >> "Opener"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.5
      Goal >> (6, 2)
      onGoal:
        Give >> 25.0
        OpenWall >> (4, 6)
        EndEpisode >> false


    agent:
      Name >> "Runner"
      Start >> (1, 2)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.5
      Goal >> (6, 10)
      onGoal:
        Give >> 55.0
        EndEpisode >> true
    Penalty >> -3.0
    Episodes >> 12_000
    Steps >> 300
    ShowAfter >> 10_000
    Delay >> 100
    WithGUI >> true