Feature: LearningConfig validation
  As a developer configuring Q-learning
  I want invalid parameter sets to be rejected
  So that the algorithm runs with sensible values

  Scenario: Valid configuration passes validation
    Given a learning configuration with alpha 0.1 gamma 0.9 eps0 0.8 epsMin 0.1 warm 10 optimistic 0.5
    Then the configuration should be valid

  Scenario: Negative learning rate is invalid
    Given a learning configuration with alpha -0.1 gamma 0.9 eps0 0.8 epsMin 0.1 warm 10 optimistic 0.5
    Then the configuration should be invalid

  Scenario: EpsMin greater than initial epsilon is invalid
    Given a learning configuration with alpha 0.1 gamma 0.9 eps0 0.2 epsMin 0.5 warm 10 optimistic 0.5
    Then the configuration should be invalid