package agentcrafter.marl

import agentcrafter.common.*

/**
 * Manages agent-related operations and state in a multi-agent simulation.
 * 
 * This class follows the Single Responsibility Principle by focusing solely on:
 * - Agent initialization and mapping
 * - Agent action coordination
 * - Q-learning updates for all agents
 * - Agent state management
 */
class AgentManager(spec: WorldSpec):

  private val agentMap = spec.agents.map(a => a.id -> a).toMap
  private val agentsQL: Map[String, Learner] = agentMap.map { case (id, agentSpec) => id -> agentSpec.learner }
  private var agentPositions = agentMap.view.mapValues(_.start).toMap

  /**
   * Gets the Q-learning instances for all agents.
   */
  def getAgentsQL: Map[String, Learner] = agentsQL

  /**
   * Gets current positions of all agents.
   */
  def getPositions: Map[String, State] = agentPositions

  /**
   * Updates agent positions.
   */
  def updatePositions(newPositions: Map[String, State]): Unit =
    agentPositions = newPositions

  /**
   * Chooses actions for all agents and returns both actions and exploration status.
   */
  def chooseJointActions(): (Map[String, Action], Boolean) =
    val jointActionsWithExploration: Map[String, (Action, Boolean)] =
      agentsQL.map { case (id, ql) => id -> ql.choose(agentPositions(id)) }

    val jointActions: Map[String, Action] = jointActionsWithExploration.view.mapValues(_._1).toMap
    val anyAgentExploring = jointActionsWithExploration.values.exists(_._2)

    (jointActions, anyAgentExploring)

  /**
   * Updates Q-learning for all agents.
   */
  def updateQLearning(
    actions: Map[String, Action],
    stepRewards: Map[String, Double],
    triggerRewards: Map[String, Double],
    nextPositions: Map[String, State]
  ): Unit =
    agentsQL.foreach { case (id, learner) =>
      val action = actions(id)
      val totalReward = stepRewards.getOrElse(id, 0.0) + triggerRewards.getOrElse(id, 0.0)
      learner.update(agentPositions(id), action, totalReward, nextPositions(id))
    }

  /**
   * Resets all agents to their starting positions.
   */
  def resetAgents(): Unit =
    agentPositions = agentMap.view.mapValues(_.start).toMap

  /**
   * Gets the current epsilon value for exploration tracking.
   */
  def getCurrentEpsilon: Double =
    agentsQL.values.headOption.map(_.eps).getOrElse(0.0)

  /**
   * Increments episode count for all learners.
   */
  def incrementEpisode(): Unit =
    agentsQL.values.foreach(_.incEp())