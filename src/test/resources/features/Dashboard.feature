Feature: Dashboard Access Control
  Background:
    Given user is on the login page

  Scenario Outline: Verify Dashboard access based on user role
    When user logs in as "<role>"
    Then Dashboard should be displayed

    Examples:
      | role  |
      | admin |
      | user  |
  Scenario Outline: Verify Dashboard summary display
    When user logs in as "<role>"
    Then the Dashboard should display summary information for Categories, Plants, and Sales

    Examples:
      | role  |
      | admin |
      | user  |

  Scenario Outline: Verify "Manage Plants" Button Navigation
    When user logs in as "<role>"
    And user clicks on the Manage Plants button
    Then user should be redirected to the Plant List page

    Examples:
      | role  |
      | admin |
      | user  |

  Scenario Outline: Verify Categories Menu Highlighting
    When user logs in as "<role>"
    And user clicks on the Categories menu item
    Then the Categories menu item should be highlighted as active
    Examples:
      | role  |
      | admin |
      | user  |

  Scenario Outline: Verify  Plants Menu Highlighting
    When user logs in as "<role>"
    When user clicks on the Plants menu item
    Then the Plants menu item should be highlighted as active

    Examples:
      | role  |
      | admin |
      | user  |

  Scenario Outline: Verify Sales Navigation Menu Highlighting
    When user logs in as "<role>"
    And user clicks on the Sales menu item
    Then the Sales menu item should be highlighted as active

    Examples:
      | role  |
      | admin |
      | user  |
