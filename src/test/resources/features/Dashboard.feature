Feature: Dashboard Access Control

  Scenario Outline: Verify Dashboard access based on user role
    Given user is on the login page
    When user logs in as "<role>"
    Then Dashboard should be displayed

    Examples:
      | role  |
      | admin |
      | user  |