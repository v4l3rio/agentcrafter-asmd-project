package agentcrafter.MARL.scenarios

import agentcrafter.MARL.DSL.{SimulationDSL, *}

/**
 * Wall Scenario: Two agents cooperate to solve a task.
 * - Agent "Opener" must reach a switch to open a wall
 * - Agent "Runner" must reach a goal on the other side of the wall
 * - The simulation ends when both agents reach their respective goals
 */
object WallScenario extends App with SimulationDSL:
  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      8 x 12
    
    walls:
      // Create a vertical wall dividing the grid
      line:
        Direction >> "vertical"
        From >> (1, 6)
        To >> (6, 6)
      // Add some obstacles
      block >> (2, 2)
      block >> (3, 2)
      block >> (5, 9)
      block >> (6, 9)
    
    // Agent that needs to open the wall
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
      Goal >> (6, 2)  // Switch location
      Reward >> 50.0
    
    // Agent that needs to reach the other side
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
      Goal >> (6, 10)  // Goal on the other side
      Reward >> 100.0
    
    // Trigger: When Opener reaches the switch, open the wall
    on("Opener", 6, 2):
      OpenWall >> (4, 6)  // Open a passage in the wall
      Give >> 25.0  // Bonus reward for opening the wall
    
    Episodes >> 12_000
    Steps >> 300
    ShowAfter >> 10_000
    Delay >> 100
    WithGUI >> true