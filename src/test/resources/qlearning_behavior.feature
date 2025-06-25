Feature: Q-Learning Agent Behavior
  As a reinforcement learning system
  I want Q-Learning agents to learn optimal policies
  So that they can navigate environments effectively

  Background:
    Given a simple grid environment is set up
    And a Q-Learning agent is created with default parameters
    
  Scenario: Agent learns from positive rewards
    Given the agent starts at position (0, 0)
    And there is a goal with reward 100 at position (2, 2)
    When the agent takes action "Right" and receives reward 0
    And the agent reaches the goal and receives reward 100
    Then the Q-value for state (0, 0) and action "Right" should increase
    And the Q-value should reflect the discounted future reward

  Scenario: Epsilon-greedy exploration behavior
    Given the agent has epsilon value 0.5
    And the agent is at state (1, 1)
    And action "Up" has the highest Q-value
    When the agent chooses an action 100 times
    Then approximately 50% of actions should be exploratory
    And approximately 60% should be the optimal action "Up"

  Scenario: Epsilon decay over episodes
    Given the agent starts with epsilon 0.9
    And epsilon minimum is set to 0.1
    And warm-up period is 10 episodes
    When 50 episodes are completed
    Then epsilon should have decreased from initial value
    And epsilon should not go below the minimum value
    And epsilon should remain constant during warm-up period

  Scenario: Q-value updates follow learning rate
    Given the agent has learning rate alpha 0.1
    And Q-value for state (0, 0) action "Right" is initially 0
    When the agent receives immediate reward 10
    And the maximum future Q-value is 5
    And gamma is 0.9
    Then the new Q-value should be approximately 1.45
    And the update should follow the Q-learning formula

  Scenario: Agent handles terminal states
    Given the agent reaches a terminal state
    When the agent tries to choose an action
    Then no Q-value update should occur for future states
    And the episode should be marked as complete

  Scenario: Optimistic initialization effects
    Given the agent is created with optimistic value 5.0
    When the agent encounters a new state-action pair
    Then the initial Q-value should be 5.0
    And this should encourage exploration of unknown areas
    