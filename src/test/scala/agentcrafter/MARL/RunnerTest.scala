package agentcrafter.MARL

import agentcrafter.MARL.{AgentSpec, EndEpisode, OpenWall, Reward, Runner, Trigger, WorldSpec}
import agentcrafter.common.{GridWorld, LearningParameters, QLearner, State}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class RunnerTest extends AnyFunSuite with Matchers:


  private def createSimpleWorldSpec(
                                     rows: Int = 5,
                                     cols: Int = 5,
                                     penalty: Double = -3.0,
                                     staticWalls: Set[State] = Set.empty,
                                     agents: List[AgentSpec] = List.empty,
                                     triggers: List[Trigger] = List.empty,
                                     episodes: Int = 10,
                                     stepLimit: Int = 50,
                                     stepDelay: Int = 0,
                                     showAfter: Int = 100
                                   ): WorldSpec =
    WorldSpec(rows, cols, penalty, staticWalls, triggers, agents, episodes, stepLimit, stepDelay, showAfter)

  private def createSimpleAgent(id: String, start: State, goal: State, reward: Double = 0.0): AgentSpec =
    val env = createSimpleGrid()
    val learner = QLearner(
      goalState = goal,
      goalReward = reward,
      updateFunction = env.step,
      resetFunction = () => start,
      learningParameters = LearningParameters(alpha = 0.5, gamma = 0.9, eps0 = 0.1)
    )
    AgentSpec(id, start, goal, learner)

  private def createSimpleGrid(): GridWorld =
    GridWorld(rows = 3, cols = 3, walls = Set.empty)


  test("Runner should handle empty world without agents"):
    val spec = createSimpleWorldSpec()
    val runner = new Runner(spec, showGui = false)

    noException should be thrownBy runner.run()

  test("Runner should handle multiple agents"):
    val agent1 = createSimpleAgent("Agent1", State(0, 0), State(4, 4), 100.0)
    val agent2 = createSimpleAgent("Agent2", State(1, 1), State(3, 3), 50.0)
    val spec = createSimpleWorldSpec(agents = List(agent1, agent2))
    val runner = new Runner(spec, showGui = false)

    noException should be thrownBy runner.run()


  test("Runner should handle world with walls"):
    val walls = Set(State(1, 1), State(2, 2), State(3, 3))
    val agent = createSimpleAgent("Agent1", State(0, 0), State(4, 4), 100.0)
    val spec = createSimpleWorldSpec(staticWalls = walls, agents = List(agent))
    val runner = new Runner(spec, showGui = false)

    noException should be thrownBy runner.run()

  test("Runner should handle triggers with OpenWall effects"):
    val agent = createSimpleAgent("Agent1", State(0, 0), State(4, 4), 100.0)
    val trigger = Trigger("Agent1", State(2, 2), List(OpenWall(State(1, 1))))
    val walls = Set(State(1, 1))
    val spec = createSimpleWorldSpec(
      staticWalls = walls,
      agents = List(agent),
      triggers = List(trigger)
    )
    val runner = new Runner(spec, showGui = false)

    noException should be thrownBy runner.run()

  test("Runner should handle complex triggers with multiple effects"):
    val agent = createSimpleAgent("Agent1", State(0, 0), State(4, 4), 100.0)
    val trigger = Trigger("Agent1", State(2, 2), List(
      OpenWall(State(1, 1)),
      Reward(25.0),
      EndEpisode
    ))
    val walls = Set(State(1, 1))
    val spec = createSimpleWorldSpec(
      staticWalls = walls,
      agents = List(agent),
      triggers = List(trigger)
    )
    val runner = new Runner(spec, showGui = false)

    noException should be thrownBy runner.run()

  test("Runner should handle agents starting at same position"):
    val agent1 = createSimpleAgent("Agent1", State(2, 2), State(4, 4), 100.0)
    val agent2 = createSimpleAgent("Agent2", State(2, 2), State(0, 0), 50.0)
    val spec = createSimpleWorldSpec(agents = List(agent1, agent2))
    val runner = new Runner(spec, showGui = false)

    noException should be thrownBy runner.run()
