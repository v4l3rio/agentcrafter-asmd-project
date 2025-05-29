package DSL

import DSL.QLearner
import DSL.{Effect, WorldSpec}
import common.{Action, State}

import scala.collection.mutable

/**
 * Runner: executes episodes and (optionally) shows the GUI
 */
class Runner(spec: WorldSpec, showGui: Boolean):

  /* ---------- optional visualization --------------------------------- */
  private var vis: Option[Visual] = None          // lazy
  private var qTableVis: List[QTableVisualizer] = List.empty

  private def maybeInitGui(ep: Int): Unit =
    if vis.isEmpty && ep >= spec.showAfter then
      vis = Some(new Visual(spec))                // finestra principale
      // Crea visualizzatori Q-table per ogni agente
      qTableVis = agentsQL.map { case (id, learner) =>
        new QTableVisualizer(id, learner, spec)
      }.toList

  /* ---------- global episode state ----------------------------------- */
  private val staticWalls = spec.staticWalls.to(mutable.Set)
  private val dynamicWalls = mutable.Set.empty[State] // opened walls
  private var episodeDone = false

  private val agentMap = spec.agents.map(a => a.id -> a).toMap // id → spec
  private var state = agentMap.view.mapValues(_.start).toMap // id → pos

  /* ---------- geometric helpers ------------------------------------- */
  private inline def clamp(x: Int, lo: Int, hi: Int) = Math.min(Math.max(x, lo), hi)

  private def isWall(p: State): Boolean =
    staticWalls.contains(p) && !dynamicWalls.contains(p)

  private def move(p: State, a: Action): State =
    val cand = State(clamp(p.r + a.delta._1, 0, spec.rows - 1),
      clamp(p.c + a.delta._2, 0, spec.cols - 1))
    if isWall(cand) then p else cand

  /* ---------- trigger effects --------------------------------------- */
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
      // 1. actions
      val jointActions: Map[String, Action] =
        agentsQL.map { case (id, ql) => id -> ql.choose(state(id))._1 }

      // 2. transition
      val nextPos = jointActions.foldLeft(state) { case (acc, (id, act)) =>
        acc + (id -> move(acc(id), act))
      }

      // 3. trigger & bonus
      val triggered = spec.triggers.filter(t => nextPos(t.who) == t.at)
      val bonus = triggered.map(t => applyEffects(t.effects)).sum

      // 4. goal and parametric rewards
      val reached: Set[String] =
        agentMap.collect { case (id, spec) if spec.goal.contains(nextPos(id)) => id }.toSet

      def rewardFor(id: String,
                    bonus: Double,
                    reached: Set[String]): Double =
        bonus - 1.0 +                            // penalità di passo
          reached.find(_ == id)                    // + goalReward se è fra i raggiunti
            .map(_ => agentMap(id).goalReward)
            .getOrElse(0.0)

      episodeDone ||= reached.nonEmpty

      // 5. update Q
      agentsQL.foreach { case (id, learner) =>
        val act = jointActions(id)
        val r = rewardFor(id, bonus, reached)
        learner.update(state(id), act, r, nextPos(id))
      }

      state = nextPos
      steps += 1
      vis.foreach(_.update(state, dynamicWalls.toSet, steps))
      // Update Q-table visualizers every 10 steps to avoid too frequent updates
      if steps % 10 == 0 then qTableVis.foreach(_.update())
    steps

  /* ---------- training cycle ---------------------------------------- */
  def run(): Unit =
    for ep <- 1 to spec.episodes do
      maybeInitGui(ep)
      resetEpisode()
      val steps = runEpisode()
      agentsQL.values.foreach(_.incEp())

      if ep % 1000 == 0 then
        println(s"Episode $ep finished in $steps steps")
        greedyDemo()

  private def greedyDemo(): Unit =
    resetEpisode()
    var done = false
    var k    = 0

    while !done && k < spec.stepLimit do
      // 1. ridisegna
      vis.foreach(_.update(state, dynamicWalls.toSet, k))

      // 2. politica greedy (ε = 0) per ogni agente
      val actions: Map[String, Action] =
        agentsQL.map { case (id, ql) => id -> ql.choose(state(id))._1 }

      // 3. transizione
      val nxt = actions.foldLeft(state) { case (acc, (id, act)) =>
        acc + (id -> move(acc(id), act))
      }

      // 4. applica eventuali trigger
      spec.triggers
        .filter(t => nxt(t.who) == t.at)
        .foreach(t => applyEffects(t.effects))

      // 5. CONDIZIONE DI USCITA PARAMETRICA
      done = agentMap.exists { case (id, spec) =>
        spec.goal.contains(nxt(id)) // true se quell'agente ha un goal
      }

      // 6. aggiorna stato e step
      state = nxt
      k    += 1
    end while

    // ultimo frame
    vis.foreach(_.update(state, dynamicWalls.toSet, k))

  private def resetEpisode(): Unit =
    state = agentMap.view.mapValues(_.start).toMap
    dynamicWalls.clear();
    episodeDone = false