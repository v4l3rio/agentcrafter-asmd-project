package agentcrafter.marl

import agentcrafter.common.State
import agentcrafter.marl.builders.{SimulationBuilder, WallLineBuilder}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WallLineBuilderTest extends AnyFunSuite with Matchers:

  test("WallLineBuilder should build horizontal lines"):
    val builder = SimulationBuilder()
    val line = WallLineBuilder(builder)
    line.withDirection("horizontal").withFrom(0,0).withTo(0,2).build()

    builder.getWalls should contain allOf (State(0,0), State(0,1), State(0,2))

  test("WallLineBuilder should build vertical lines even with reversed points"):
    val builder = SimulationBuilder()
    val line = WallLineBuilder(builder)
    line.withDirection("vertical").withFrom(2,1).withTo(0,1).build()

    builder.getWalls should contain allOf (State(0,1), State(1,1), State(2,1))

  test("WallLineBuilder should throw if incomplete"):
    val builder = SimulationBuilder()
    val line = WallLineBuilder(builder)
    line.withDirection("horizontal")
    an [IllegalArgumentException] should be thrownBy line.build()