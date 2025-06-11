package agentcrafter.MARL.scenarios

import agentcrafter.MARL.DSL.{SimulationDSL, *}

object MDPLearnerExample extends App with SimulationDSL:

  import AgentProperty.*
  import TriggerProperty.*
  import LearnerProperty.*
  import WallProperty.*
  import LineProperty.*
  import SimulationProperty.*

  simulation:
    Episodes >> 10000
    Steps >> 1000
    Penalty >> -2.0
    ShowAfter >> 100
    WithGUI >> true

    // Add some walls to make the environment more interesting
    walls:
      Block >> (2, 2)
      Block >> (3, 2)
    
    // Agent using the new MDP-based Q-learning
    agent:
      Name >> "MDPAgent"
      Start >> (0, 0)
      Goal >> (4, 4)
      withLearner:
        LearnerType >> "mdplearner"
        Alpha >> 0.1
        Gamma >> 0.9
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 1000
        Optimistic >> 0.0
      onGoal:
        Give >> 50.0  // Reward for reaching the goal
        EndEpisode >> true  // End episode after reaching goal
    
    // Agent using the traditional GridWorld Q-learning for comparison
    agent:
      Name >> "GridWorldAgent"
      Start >> (0, 1)
      Goal >> (4, 3)
      withLearner:
        LearnerType >> "qlearner"
        Alpha >> 0.1
        Gamma >> 0.9
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 1000
        Optimistic >> 0.0
      onGoal:
        Give >> 30.0 // Reward for reaching the goal
        EndEpisode >> true // End episode after reaching goal