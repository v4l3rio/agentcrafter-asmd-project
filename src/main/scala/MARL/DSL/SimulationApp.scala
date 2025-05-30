package MARL.DSL

object SimulationApp extends App with SimulationDSL:
  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  
  simulation:
    grid:
      10 x 10
    walls:
      line:
        Direction >> "vertical"
        From >> (1, 3)
        To >> (1, 5)
      line:
        Direction >> "vertical"
        From >> (1, 3)
        To >> (3, 3)
      line:
        Direction >> "vertical"
        From >> (1, 5)
        To >> (3, 5)
      line:
        Direction >> "vertical"
        From >> (3, 3)
        To >> (3, 5)
      block >> (7, 7)
    agent:
      Name >> "Runner"
      Start >> (1, 9)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.99
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 1_000
        Optimistic >> 0.5
      Goal >> (2, 4)
      Reward >> 100.0
    on("Runner", 8, 6):
      OpenWall >> (2, 3)
      EndEpisode >> false
      Give >> 30
    episodes(10_000)
    steps(400)
    showAfter(9_000)
    delay(100)
    withGUI(true)