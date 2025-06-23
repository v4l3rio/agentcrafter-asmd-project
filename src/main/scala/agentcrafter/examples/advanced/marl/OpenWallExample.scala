package agentcrafter.examples.advanced.marl


import agentcrafter.marl.dsl.*

/**
 * Wall Scenario: Two agents cooperate to solve a task.
 * - Agent "Opener" must reach a switch to open a wall
 * - Agent "Runner" must reach a goal on the other side of the wall
 * - The simulation ends when both agents reach their respective goals
 */
object OpenWallExample extends App with SimulationDSL:


  simulation:
    grid:
      8 x 12

    walls:
      line:
        Direction >> "vertical"
        From >> (6, 2)
        To >> (6, 5)
      line:
        Direction >> "horizontal"
        From >> (2, 2)
        To >> (3, 2)
      line:
        Direction >> "horizontal"
        From >> (2, 5)
        To >> (3, 5)
      line:
        Direction >> "horizontal"
        From >> (9, 2)
        To >> (10, 2)
      line:
        Direction >> "vertical"
        From >> (11, 5)
        To >> (11, 7)
      line:
        Direction >> "horizontal"
        From >> (9, 5)
        To >> (10, 5)
      line:
        Direction >> "horizontal"
        From >> (9, 7)
        To >> (10, 7)

      block >> (4, 7)
      block >> (9, 6)


    agent:
      Name >> "Opener"
      Start >> (7, 1)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.5
      Goal >> (2, 6)
      onGoal:
        Give >> 25.0
        OpenWall >> (9, 6)
        EndEpisode >> false


    agent:
      Name >> "Runner"
      Start >> (4, 1)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.5
      Goal >> (10, 6)
      onGoal:
        Give >> 55.0
        EndEpisode >> true

    Penalty >> -2.0
    Episodes >> 12_000
    Steps >> 300
    ShowAfter >> 10_000
    Delay >> 100
    WithGUI >> true