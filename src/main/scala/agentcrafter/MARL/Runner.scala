package agentcrafter.MARL

import agentcrafter.MARL.visualizers.{QTableVisualizer, Visualizer}
import agentcrafter.common.{Action, GridWorld, QLearner, State, StepResult}

import scala.collection.mutable

/**
 * Runner: executes episodes and (optionally) shows the GUI
 */
class Runner(spec: WorldSpec, showGui: Boolean):

  /* ---------- optional visualization --------------------------------- */
  private var vis: Option[Visualizer] = None // lazy
  private var qTableVis: List[QTableVisualizer] = List.empty

  private def maybeInitGui(ep: Int): Unit =
    if vis.isEmpty && ep >= spec.showAfter then
      vis = Some(new Visualizer("MARL Simulation", spec.rows, spec.cols, cell = 48, delayMs = spec.stepDelay))
      vis.foreach(_.configureMultiAgent(spec)) // configure for multi-agent mode
      // Crea visualizzatori Q-table per ogni agente
      qTableVis = agentsQL.map { case (id, learner) =>
        new QTableVisualizer(id, learner, spec)
      }.toList

  /* ---------- global episode state ----------------------------------- */
  private val staticWalls = spec.staticWalls.to(mutable.Set)
  private val dynamicWalls = mutable.Set.empty[State] // opened walls
  private var episodeDone = false
  private var activeTriggers: List[Trigger] = spec.triggers // triggers that can be activated

  private val agentMap = spec.agents.map(a => a.id -> a).toMap // id → spec
  private var state = agentMap.view.mapValues(_.start).toMap // id → pos

  // Reward tracking for GUI
  private var totalReward = 0.0
  private var episodeReward = 0.0
  private var currentEpisode = 0

  /**
   * The -- operator is subtracting the elements in dynamicWalls from the staticWalls set.
   * This means that any wall positions present in dynamicWalls will be excluded from the resulting set.
   * This operation ensures that the GridWorld reflects the current state of the environment,
   * where dynamic walls (walls that have been opened or removed during the simulation)
   * are no longer considered obstacles.
   */
  private def grid: GridWorld =
    GridWorld(spec.rows, spec.cols, staticWalls.toSet -- dynamicWalls, spec.stepPenalty)

  private def applyEffects(effs: List[Effect]): Double =
    var bonus = 0.0
    effs.foreach {
      case OpenWall(pos) => dynamicWalls += pos // remove wall
      case Reward(x) => bonus += x
      case EndEpisode => episodeDone = true
    }
    bonus

  /* map id → learner - now using the learner from AgentSpec */
  private val agentsQL: Map[String, QLearner] =
    agentMap.map { case (id, agentSpec) => id -> agentSpec.learner }

  /* ---------- single episode ---------------------------------------- */
  private def runEpisode(): Int =
    var steps = 0
    while !episodeDone && steps < spec.stepLimit do
      // 1. choose actions and track exploration behavior
      val jointActionsWithExploration: Map[String, (Action, Boolean)] =
        agentsQL.map { case (id, ql) => id -> ql.choose(state(id)) }
      val jointActions: Map[String, Action] = jointActionsWithExploration.view.mapValues(_._1).toMap
      val anyAgentExploring = jointActionsWithExploration.values.exists(_._2)

      // 2. transition using GridWorld for movement and step penalty
      val (nextPos, stepRewardsMap) =
        jointActions.foldLeft(state -> Map.empty[String, Double]) {
          case ((posAcc, rewAcc), (id, act)) =>
            val StepResult(next, reward) = grid.step(posAcc(id), act)
            (posAcc + (id -> next), rewAcc + (id -> reward))
        }

      val (fired, remaining) = activeTriggers.partition(t => nextPos(t.who) == t.at)
      activeTriggers = remaining

      // Apply effects and collect rewards per agent
      val agentRewards = mutable.Map.empty[String, Double].withDefaultValue(0.0)
      fired.foreach { trigger =>
        val bonus = applyEffects(trigger.effects)
        agentRewards(trigger.who) += bonus
      }

      def rewardFor(agentId: String): Double =
        stepRewardsMap.getOrElse(agentId, 0.0) + agentRewards(agentId)

      // 5. update Q
      agentsQL.foreach { case (id, learner) =>
        val act = jointActions(id)
        val r = rewardFor(id)
        learner.updateWithGoal(state(id), act, r, nextPos(id))
      }

      state = nextPos
      steps += 1

      // Update episode reward (including step penalty for each agent)
      val stepRewardSum = agentsQL.keys.map(id => rewardFor(id)).sum
      episodeReward += stepRewardSum

      // Update GUI with cumulative episode reward
      val isExploring = anyAgentExploring
      val currentEpsilon = agentsQL.values.headOption.map(_.eps).getOrElse(0.0)
      vis.foreach { v =>
        v.updateMultiAgent(state, dynamicWalls.toSet, steps)
        v.updateSimulationInfo(currentEpisode, isExploring, episodeReward, currentEpsilon)
      }
      // Update Q-table visualizers every 10 steps to avoid too frequent updates
      if steps % 10 == 0 then qTableVis.foreach(_.update())
    steps

  /* ---------- training cycle ---------------------------------------- */
  def run(): Unit =
    for ep <- 1 to spec.episodes do
      maybeInitGui(ep)
      resetEpisode()
      currentEpisode = ep
      episodeReward = 0.0
      val steps = runEpisode()
      agentsQL.values.foreach(_.incEp())

      // Update total reward
      totalReward += episodeReward

      if ep % 1000 == 0 then
        println(s"Episode $ep finished in $steps steps")
        greedyDemo()

  private def greedyDemo(): Unit =
    resetEpisode()
    var done = false
    var k = 0

    while !done && k < spec.stepLimit do
      // 1. ridisegna
      vis.foreach(_.updateMultiAgent(state, dynamicWalls.toSet, k))

      // 2. politica greedy (ε = 0) per ogni agente
      val actions: Map[String, Action] =
        agentsQL.map { case (id, ql) => id -> ql.choose(state(id))._1 }

      // 3. transizione
      val nxt = actions.foldLeft(state) { case (acc, (id, act)) =>
        val StepResult(next, _) = grid.step(acc(id), act)
        acc + (id -> next)
      }

      // 4. applica eventuali trigger
      val (firedDemo, remainingDemo) = activeTriggers.partition(t => nxt(t.who) == t.at)
      firedDemo.foreach(t => applyEffects(t.effects))
      activeTriggers = remainingDemo

      // 5. CONDIZIONE DI USCITA PARAMETRICA
      done = agentMap.exists { case (id, spec) =>
        spec.goal == nxt(id) // true se quell'agente ha un goal
      }

      // 6. aggiorna stato e step
      state = nxt
      k += 1
    end while

    // ultimo frame
    vis.foreach(_.updateMultiAgent(state, dynamicWalls.toSet, k))

  private def resetEpisode(): Unit =
    state = agentMap.view.mapValues(_.start).toMap
    dynamicWalls.clear()
    episodeDone = false
    activeTriggers = spec.triggers // Reset triggers for new episode
    episodeReward = 0.0
