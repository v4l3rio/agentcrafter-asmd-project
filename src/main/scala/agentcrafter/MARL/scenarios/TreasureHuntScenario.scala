package agentcrafter.MARL.scenarios

import agentcrafter.MARL.DSL.{SimulationDSL, *}

/**
 * Treasure Hunt Scenario: A creative multi-agent cooperation scenario.
 * 
 * Three agents must work together to collect treasure:
 * - "KeyMaster" must collect keys by visiting key locations
 * - "Guardian" must clear obstacles by reaching guard posts
 * - "Treasure Hunter" must reach the final treasure location
 * 
 * The agents must coordinate: keys unlock doors, guards clear paths,
 * and only when all preparations are complete can the treasure be claimed.
 */
object TreasureHuntScenario extends App with SimulationDSL:
  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      12 x 15
    
    walls:
      // Create treasure chamber walls
      line:
        Direction >> "horizontal"
        From >> (8, 10)
        To >> (8, 14)
      line:
        Direction >> "horizontal"
        From >> (11, 10)
        To >> (11, 14)
      line:
        Direction >> "vertical"
        From >> (8, 10)
        To >> (11, 10)
      line:
        Direction >> "vertical"
        From >> (8, 14)
        To >> (11, 14)
      
      // Locked doors (will be opened by keys)
      block >> (9, 10)  // Door 1
      block >> (10, 10) // Door 2
      
      // Obstacle walls (will be cleared by guardian)
      line:
        Direction >> "horizontal"
        From >> (4, 5)
        To >> (4, 9)
      block >> (6, 7)   // Obstacle 1
      block >> (7, 7)   // Obstacle 2
      
      // Maze-like structure
      line:
        Direction >> "vertical"
        From >> (1, 3)
        To >> (3, 3)
      line:
        Direction >> "horizontal"
        From >> (2, 1)
        To >> (2, 2)
      block >> (5, 2)
      block >> (1, 8)
      block >> (2, 11)
    
    // KeyMaster: Collects keys to unlock doors
    agent:
      Name >> "KeyMaster"
      Start >> (0, 0)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 1.0
      Goal >> (1, 12)  // Key collection point
      onGoal:
        Give >> 80.0  // Reward for collecting keys
        OpenWall >> (9, 10)
        EndEpisode >> false  // Continue until all keys are collected
    
    // Guardian: Clears obstacles and guards passages
    agent:
      Name >> "Guardian"
      Start >> (0, 14)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 0.8
      Goal >> (5, 7)   // Guard post
      onGoal:
        Give >> 60.0  // Reward for clearing obstacles
        OpenWall >> (6, 7) // Clear obstacle 1
        OpenWall >> (7, 7)
        EndEpisode >> false  // Continue until all obstacles are cleared
    
    // Treasure Hunter: Claims the final treasure
    agent:
      Name >> "TreasureHunter"
      Start >> (11, 0)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 0.5
      Goal >> (9, 12)  // Treasure location
      onGoal:
        Give >> 100.0  // Big reward for reaching treasure
        EndEpisode >> true  // End episode when treasure is claimed

    Episodes >> 20_000
    Steps >> 600
    ShowAfter >> 17_000
    Delay >> 150
    WithGUI >> true