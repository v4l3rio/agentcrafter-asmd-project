package agentcrafter.marl

import agentcrafter.marl.managers.EnvironmentManager
import agentcrafter.marl.{WorldSpec, Trigger, OpenWall, Reward, EndEpisode, AgentSpec}
import agentcrafter.common.{State, QLearner, GridWorld, LearningConfig}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class EnvironmentManagerTest extends AnyFunSuite with Matchers:

  private def dummyAgent(id: String, start: State = State(0,0)): AgentSpec =
    val env = GridWorld(3,3, Set.empty)
    val learner = QLearner(
      goalState = State(2,2),
      goalReward = 0.0,
      updateFunction = env.step,
      resetFunction = () => start,
      learningConfig = LearningConfig()
    )
    AgentSpec(id, start, State(2,2), learner)

  private def baseSpec(triggers: List[Trigger]) =
    WorldSpec(
      rows = 3,
      cols = 3,
      stepPenalty = -1.0,
      staticWalls = Set(State(1,1)),
      triggers = triggers,
      agents = List(dummyAgent("A")),
      episodes = 1,
      stepLimit = 10,
      stepDelay = 0,
      showAfter = 100
    )

  test("processTriggers should open walls and keep episode running"):
    val trig = Trigger("A", State(0,0), List(OpenWall(State(1,1))))
    val envMgr = EnvironmentManager(baseSpec(List(trig)))

    val rewards = envMgr.processTriggers(Map("A" -> State(0,0)))
    rewards shouldBe Map("A" -> 0.0)
    envMgr.getOpenedWalls should contain (State(1,1))
    envMgr.isEpisodeDone shouldBe false

  test("processTriggers should apply reward and end episode"):
    val trig = Trigger("A", State(0,0), List(Reward(5.0), EndEpisode))
    val envMgr = EnvironmentManager(baseSpec(List(trig)))

    val rewards = envMgr.processTriggers(Map("A" -> State(0,0)))
    rewards shouldBe Map("A" -> 5.0)
    envMgr.isEpisodeDone shouldBe true

    envMgr.resetEnvironment()
    envMgr.getOpenedWalls shouldBe empty
    envMgr.isEpisodeDone shouldBe false

  test("triggers are removed after firing"):
    val trig = Trigger("A", State(0,0), List(Reward(1.0)))
    val envMgr = EnvironmentManager(baseSpec(List(trig)))

    envMgr.processTriggers(Map("A" -> State(0,0))) shouldBe Map("A" -> 1.0)
    envMgr.processTriggers(Map("A" -> State(0,0))) shouldBe empty