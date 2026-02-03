Feature: Sales

  @sales
  Scenario: Verify admin views sales list with correct details
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And user clicks Sell Plant
    And user sells the created plant with quantity "3"
    Then sales list should show correct total price for the created plant with quantity "3"
