Feature: Dashboard Functionality
                               # --- UI SCENARIOS---

  Background:
    Given user is on the login page

  @ui @dashboard1
  Scenario Outline: Verify Dashboard access based on user role
    When user logs in as "<role>"
    Then Dashboard should be displayed
    Examples:
      | role  |
      | admin |
      | user  |

  @ui @dashboard2
  Scenario Outline: Verify Dashboard summary display
    When user logs in as "<role>"
    Then the Dashboard should display summary information for Categories, Plants, and Sales
    Examples:
      | role  |
      | admin |
      | user  |

  @ui @dashboard3
  Scenario Outline: Verify "Manage Plants" Button Navigation
    When user logs in as "<role>"
    And user clicks on the Manage Plants button
    Then user should be redirected to the Plant List page
    Examples:
      | role  |
      | admin |
      | user  |

  @ui @dashboard4
  Scenario Outline: Verify Categories Menu Highlighting
    When user logs in as "<role>"
    And user clicks on the Categories menu item
    Then the Categories menu item should be highlighted as active
    Examples:
      | role  |
      | admin |
      | user  |

  @ui @dashboard5
  Scenario Outline: Verify  Plants Menu Highlighting
    When user logs in as "<role>"
    When user clicks on the Plants menu item
    Then the Plants menu item should be highlighted as active

    Examples:
      | role  |
      | admin |
      | user  |

  @ui @dashboard6
  Scenario Outline: Verify Sales Navigation Menu Highlighting
    When user logs in as "<role>"
    And user clicks on the Sales menu item
    Then the Sales menu item should be highlighted as active

    Examples:
      | role  |
      | admin |
      | user  |

                                 # --- API SCENARIOS---

  @api @dashboard1
  Scenario Outline: TC_DASHBOARD_001 Verify Dashboard Summary for different users
    Given "<user>" is authenticated
    When "<user>" requests the dashboard summary
    Then the response status should be 200
    And dashboard summary should contain integer counts for:
      | categories |
      | plants     |

    Examples:
      | user      |
      | admin     |
      | nonadmin  |
