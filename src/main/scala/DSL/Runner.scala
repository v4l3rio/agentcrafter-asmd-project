package DSL

import DSL.QLearner
import DSL.model.{Effect, WorldSpec}
import common.{Action, State}

import scala.collection.mutable

/**
 * Runner: executes episodes and (optionally) shows the GUI
 */
class Runner(spec: WorldSpec, showGui: Boolean):

  /* ---------- optional visualization --------------------------------- */
  private val vis: Option[Visual] =
    if showGui then Some(new Visual(spec)) else None

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
      case DSL.model.OpenWall(pos) => dynamicWalls += pos // remove wall
      case DSL.model.Reward(x) => bonus += x
      case DSL.model.EndEpisode => episodeDone = true
    }
    bonus

  /* map id → learner */
  private val agentsQL: Map[String, QLearner] =
    agentMap.keys.map(id => id -> QLearner(id)).toMap

  /* ---------- single episode ---------------------------------------- */
  private def runEpisode(maxSteps: Int): Int =
    var steps = 0
    while !episodeDone && steps < maxSteps do
      // 1. actions
      val jointActions: Map[String, Action] =
        agentsQL.map { case (id, ql) => id -> ql.choose(state)._1 }

      // 2. transition
      val nextPos = jointActions.foldLeft(state) { case (acc, (id, act)) =>
        acc + (id -> move(acc(id), act))
      }

      // 3. trigger & bonus
      val triggered = spec.triggers.filter(t => nextPos(t.who) == t.at)
      val bonus = triggered.map(t => applyEffects(t.effects)).sum

      // 4. goal and parametric rewards
      val reachedIds: Set[String] =
        agentMap.collect { case (id, spec) if nextPos(id) == spec.goal => id }.toSet

      val reachedReward: Double =
        reachedIds.map(id => agentMap(id).goalReward).sum

      val reward: Double =
        bonus + (if reachedIds.nonEmpty then reachedReward else -1.0)

      episodeDone ||= reachedIds.nonEmpty // end episode if someone made it

      // 5. update Q
      agentsQL.foreach { case (id, learner) =>
        val act = jointActions(id)
        learner.update(state, act, reward, nextPos)
      }

      state = nextPos
      steps += 1
      vis.foreach(_.update(state, dynamicWalls.toSet, steps))
    steps
    
  /* ---------- training cycle ---------------------------------------- */
  def run(): Unit =
    for ep <- 1 to spec.episodes do
      resetEpisode()
      val steps = runEpisode(400)
      agentsQL.values.foreach(_.incEp())

      if ep % 1000 == 0 then
        println(s"Episode $ep finished in $steps steps")
        greedyDemo()

  /* ---------- greedy demo + GUI ------------------------------------- */
  private def greedyDemo(): Unit =
    resetEpisode()
    var done = false;
    var k = 0
    while !done && k < 300 do
      vis.foreach(_.update(state, dynamicWalls.toSet, k))
      val actions = agentsQL.map { case (id, ql) => id -> ql.choose(state)._1 }
      val nxt = actions.foldLeft(state) { case (acc, (id, act)) =>
        acc + (id -> move(acc(id), act))
      }
      spec.triggers.filter(t => nxt(t.who) == t.at)
        .foreach(t => applyEffects(t.effects))
      done = nxt.get("B").contains(agentMap("B").goal)
      state = nxt;
      k += 1
    vis.foreach(_.update(state, dynamicWalls.toSet, k))

  private def resetEpisode(): Unit =
    state = agentMap.view.mapValues(_.start).toMap
    dynamicWalls.clear();
    episodeDone = false