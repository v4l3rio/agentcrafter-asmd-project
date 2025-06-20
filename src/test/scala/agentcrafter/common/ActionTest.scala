package agentcrafter.common

import agentcrafter.common.Action
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFunSuite with Matchers:

  test("Action.Up should have correct delta"): 
    Action.Up.delta shouldBe (-1, 0)

  test("Action.Down should have correct delta"): 
    Action.Down.delta shouldBe (1, 0)

  test("Action.Left should have correct delta"): 
    Action.Left.delta shouldBe (0, -1)

  test("Action.Right should have correct delta"): 
    Action.Right.delta shouldBe (0, 1)

  test("Action.Stay should have correct delta"): 
    Action.Stay.delta shouldBe (0, 0)
