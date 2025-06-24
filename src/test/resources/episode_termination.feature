Feature: Episode termination
  As a Q-learner using episodes
  I want episodes to stop at the correct conditions
  So that learning progresses properly

  Scenario: Episode stops when max steps reached
    Given a learner in an environment that never reaches the goal
    When an episode runs with a max step limit of 5
    Then the episode should end unsuccessfully after 5 steps

  Scenario: Episode succeeds immediately if starting at goal
    Given the learner starts at the goal state
    When an episode runs with a max step limit of 5
    Then the episode should end immediately with success