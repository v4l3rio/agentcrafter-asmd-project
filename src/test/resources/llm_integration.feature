Feature: LLM Integration for Q-Learning
  As a developer using LLM-enhanced Q-Learning
  I want to integrate LLM-generated Q-Tables into simulations
  So that agents can benefit from pre-trained knowledge

  Background:
    Given the LLM integration system is available

  Scenario: Enabling LLM for simulation
    Given I create a simulation with LLM enabled
    And I configure the LLM model as "gpt-4o"
    And I add an agent to the simulation
    When I run the simulation
    Then the LLM should be used to generate initial Q-Tables
    And the agent should start with LLM-provided knowledge

  Scenario: LLM configuration with different models
    Given I enable LLM with model "gpt-3.5-turbo"
    When I configure the simulation
    Then the LLM configuration should use the specified model
    And the configuration should be properly stored

  Scenario: Simulation without LLM
    Given I create a simulation without enabling LLM
    And I add an agent to the simulation
    When I run the simulation
    Then the agent should start with default Q-values
    And no LLM API calls should be made

  Scenario: LLM API failure handling
    Given I enable LLM for the simulation
    And the LLM API is unavailable or returns an error
    When I attempt to run the simulation
    Then the simulation should handle the failure gracefully
    And the agent should fall back to default initialization
    And an appropriate warning should be logged

  Scenario: LLM returns invalid Q-Table format
    Given I enable LLM for the simulation
    And the LLM returns malformed JSON
    When I attempt to load the Q-Table
    Then the loading should fail safely
    And the agent should use default Q-values
    And the error should be properly reported

  Scenario: Multiple agents with LLM
    Given I enable LLM for the simulation
    And I add multiple agents with different configurations
    When I run the simulation
    Then each agent should receive appropriate LLM-generated Q-Tables
    And the Q-Tables should be tailored to each agent's environment