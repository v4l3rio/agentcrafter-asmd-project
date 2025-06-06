package agentcrafter.MARL.scenarios

import agentcrafter.MARL.DSL.{SimulationDSL, *}

/**
 * Four Agent Maze Scenario: Four agents with different goals in a mini maze.
 * Each agent starts from a corner and must reach a specific goal while avoiding
 * collisions and navigating through the maze structure.
 */
object FourAgentMazeScenario extends App with SimulationDSL:
  import AgentProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      10 x 10
    
    walls:
      // Create a mini maze structure
      // Outer walls
      line:
        Direction >> "horizontal"
        From >> (2, 2)
        To >> (2, 7)
      line:
        Direction >> "horizontal"
        From >> (7, 2)
        To >> (7, 7)
      line:
        Direction >> "vertical"
        From >> (2, 2)
        To >> (7, 2)
      line:
        Direction >> "vertical"
        From >> (2, 7)
        To >> (7, 7)
      
      // Internal maze walls
      line:
        Direction >> "vertical"
        From >> (3, 4)
        To >> (5, 4)
      line:
        Direction >> "horizontal"
        From >> (4, 5)
        To >> (4, 6)
      line:
        Direction >> "vertical"
        From >> (5, 3)
        To >> (6, 3)
      block >> (3, 6)
      block >> (6, 5)
    
    // Agent 1: North-West corner agent
    agent:
      Name >> "NorthWest"
      Start >> (0, 0)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.9
        Eps0 >> 0.85
        EpsMin >> 0.1
        Warm >> 2_000
        Optimistic >> 0.3
      Goal >> (5, 5)  // Center of maze
      Reward >> 80.0
    
    // Agent 2: North-East corner agent
    agent:
      Name >> "NorthEast"
      Start >> (0, 9)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.9
        Eps0 >> 0.85
        EpsMin >> 0.1
        Warm >> 2_000
        Optimistic >> 0.3
      Goal >> (3, 3)  // Inside maze, top-left area
      Reward >> 75.0
    
    // Agent 3: South-West corner agent
    agent:
      Name >> "SouthWest"
      Start >> (9, 0)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.9
        Eps0 >> 0.85
        EpsMin >> 0.1
        Warm >> 2_000
        Optimistic >> 0.3
      Goal >> (6, 6)  // Inside maze, bottom-right area
      Reward >> 85.0
    
    // Agent 4: South-East corner agent
    agent:
      Name >> "SouthEast"
      Start >> (9, 9)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.9
        Eps0 >> 0.85
        EpsMin >> 0.1
        Warm >> 2_000
        Optimistic >> 0.3
      Goal >> (4, 3)  // Inside maze, middle-left area
      Reward >> 90.0
    
    Episodes >> 18_000
    Steps >> 400
    ShowAfter >> 15_000
    Delay >> 120
    WithGUI >> true