package agentcrafter.marl

import agentcrafter.common.State
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SimulationStateTest extends AnyFunSuite with Matchers:

  test("SimulationState should track rewards and episodes correctly"):
    val state = SimulationState()

    state.getCurrentEpisode shouldBe 0
    state.getTotalReward shouldBe 0.0
    state.getEpisodeReward shouldBe 0.0

    state.setCurrentEpisode(5)
    state.getCurrentEpisode shouldBe 5

    state.addEpisodeReward(10.0)
    state.getEpisodeReward shouldBe 10.0

    val snapshot = state.createEpisodeState(Map("A" -> State(0,0)), Set(State(1,1)), done = false)
    snapshot.reward shouldBe 10.0
    snapshot.positions("A") shouldBe State(0,0)
    snapshot.openedWalls should contain (State(1,1))

    state.completeEpisode()
    state.getTotalReward shouldBe 10.0

    state.resetEpisodeReward()
    state.getEpisodeReward shouldBe 0.0