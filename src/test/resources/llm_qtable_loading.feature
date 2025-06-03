Feature: LLM Q-Table Loading
  As a developer using the LLM Q-Learning system
  I want to load Q-Tables from LLM-generated JSON
  So that agents can start with pre-trained knowledge

  Background:
    Given a Q-Learner instance is created
  # OK
  Scenario: Loading valid Q-Table JSON
    Given a valid Q-Table JSON with multiple states and actions
    When I load the Q-Table from JSON
    Then the Q-Table should be loaded successfully
    And the Q-values should match the JSON data
    And all actions should have correct Q-values
  # OK
  Scenario: Loading Q-Table with markdown formatting
    Given a Q-Table JSON wrapped in markdown code blocks
    When I load the Q-Table from JSON
    Then the Q-Table should be loaded successfully
    And the markdown formatting should be stripped
    And the Q-values should be correctly parsed
 # OK
  Scenario: Loading Q-Table with LLM prefixes
    Given a Q-Table JSON with LLM response prefixes like "Here is the JSON:"
    When I load the Q-Table from JSON
    Then the Q-Table should be loaded successfully
    And the prefixes should be ignored
    And the Q-values should be correctly extracted
  # OK
  Scenario: Handling invalid JSON format
    Given an invalid JSON string with syntax errors
    When I attempt to load the Q-Table from JSON
    Then the loading should fail gracefully
    And an appropriate error message should be provided
    And the Q-Learner should remain unchanged
  # OK
  Scenario: Handling unknown actions
    Given a Q-Table JSON with invalid action names
    When I attempt to load the Q-Table from JSON
    Then the loading should fail with an unknown action error
    And the Q-Learner should remain in a consistent state
  # OK
  Scenario: Loading empty Q-Table
    Given an empty JSON object
    When I load the Q-Table from JSON
    Then the loading should succeed
    And the Q-Learner should have no Q-values set