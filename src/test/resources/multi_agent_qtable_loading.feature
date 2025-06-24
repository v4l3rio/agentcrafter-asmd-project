Feature: Multi-Agent Q-Table loading
  As a developer using multiple agents
  I want to load their Q-tables from JSON
  So that valid agents get Q-values and invalid ones fall back to defaults

  Background:
    Given two learner agents

  Scenario: Partial corruption fallback
    Given a multi-agent JSON with one valid and one invalid table
    When I load the multi-agent Q-tables
    Then the first agent should load its Q-table successfully
    And the second agent should fall back to default Q-values

  Scenario: Complete corruption detection
    Given a completely invalid multi-agent JSON
    When I load the multi-agent Q-tables
    Then both agents should report failures