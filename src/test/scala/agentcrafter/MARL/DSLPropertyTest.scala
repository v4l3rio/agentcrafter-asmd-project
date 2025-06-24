package agentcrafter.marl.dsl

import agentcrafter.marl.builders.{AgentBuilder, TriggerBuilder, WallLineBuilder, SimulationBuilder}
import agentcrafter.marl.dsl.*
import agentcrafter.marl.{OpenWall, Reward, EndEpisode}
import agentcrafter.common.State
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

object TestDSL extends SimulationDSL
import TestDSL.*

class DSLPropertyTest extends AnyFunSuite with Matchers:

  test("AgentProperty should set builder fields correctly"):
    val simBuilder = new SimulationBuilder()
    given agentWrapper: AgentWrapper = AgentWrapper(new AgentBuilder(simBuilder))

    AgentProperty.Name >> "agent1"
    AgentProperty.Start >> (1, 2)
    AgentProperty.Goal >> (3, 4)

    agentWrapper.builder.build()
    val spec = simBuilder.getAgents.getOrElse("agent1", fail("agent not added"))
    spec.start shouldBe State(1, 2)
    spec.goal shouldBe State(3, 4)

  test("LearnerProperty should mutate LearnerConfig"):
    val config = LearnerConfig()
    given LearnerConfig = config

    LearnerProperty.Alpha >> 0.2
    LearnerProperty.Gamma >> 0.8
    LearnerProperty.Eps0 >> 0.9
    LearnerProperty.EpsMin >> 0.1
    LearnerProperty.Warm >> 5
    LearnerProperty.Optimistic >> 2.0

    config.alpha shouldBe 0.2
    config.gamma shouldBe 0.8
    config.eps0 shouldBe 0.9
    config.epsMin shouldBe 0.1
    config.warm shouldBe 5
    config.optimistic shouldBe 2.0

  test("LineProperty should configure WallLineBuilder"):
    val simBuilder = new SimulationBuilder()
    given lineBuilder: WallLineBuilder = simBuilder.newWallLine()

    LineProperty.Direction >> "horizontal"
    LineProperty.From >> (0, 0)
    LineProperty.To >> (0, 2)

    lineBuilder.getDirection shouldBe Some("horizontal")
    lineBuilder.getFrom shouldBe Some((0, 0))
    lineBuilder.getTo shouldBe Some((0, 2))
    lineBuilder.isComplete shouldBe true

  test("WallProperty Line should add horizontal and vertical walls"):
    val simBuilder = new SimulationBuilder()
    given wrapper: SimulationWrapper = SimulationWrapper(simBuilder)

    WallProperty.Line >> LineWallConfig("horizontal", (1, 1), (1, 3))
    WallProperty.Line >> LineWallConfig("vertical", (0, 0), (2, 0))

    simBuilder.getWalls should contain allOf(State(1,1), State(1,2), State(1,3), State(0,0), State(1,0), State(2,0))

  test("WallProperty Block should add single wall"):
    val simBuilder = new SimulationBuilder()
    given wrapper: SimulationWrapper = SimulationWrapper(simBuilder)

    WallProperty.Block >> (2, 2)

    simBuilder.getWalls should contain (State(2, 2))

  test("TriggerProperty should add appropriate effects"):
    val simBuilder = new SimulationBuilder()
    val tb = new TriggerBuilder("agent", 0, 0, simBuilder)
    given triggerBuilder: TriggerBuilder = tb

    TriggerProperty.OpenWall >> (1, 1)
    TriggerProperty.Give >> 5.0
    TriggerProperty.EndEpisode >> false
    TriggerProperty.EndEpisode >> true

    val field = classOf[TriggerBuilder].getDeclaredField("eff")
    field.setAccessible(true)
    val effects = field.get(tb).asInstanceOf[mutable.Buffer[?]]
    effects.toList should contain inOrder (OpenWall(State(1,1)), Reward(5.0), EndEpisode)

  test("asciiWalls should parse '#' characters as walls"):
    val simBuilder = new SimulationBuilder()
    given wrapper: SimulationWrapper = SimulationWrapper(simBuilder)

    asciiWalls(
      """#..
        |.#.
        |..#""".stripMargin
    )

    simBuilder.getWalls should contain allOf(State(0,0), State(1,1), State(2,2))