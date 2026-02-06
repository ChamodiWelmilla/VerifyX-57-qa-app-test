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

  @sales-delete
  Scenario: Verify admin deletes a sale
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And admin deletes the last created sale
    Then sales list should not contain the last created sale

  @sales
  Scenario: Verify admin is prevented from creating a new sale with empty values
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And user clicks Sell Plant
    And user keeps required fields empty
    Then Sell button should be disabled
    And user selects the created plant
    And user leaves quantity empty
    Then Sell button should be disabled
    And user enters quantity "1"
    Then Sell button should be enabled

  @sales
  Scenario: Verify admin is prevented from creating a new sale with invalid data
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And user records current sales count
    And user clicks Sell Plant
    And user selects the created plant
    And user enters quantity "0"
    Then Sell button should be disabled or quantity warning should be displayed
    When user clicks Sell
    Then no new sale should be created

  @sales
  Scenario: Verify admin is prevented from creating a new sale that exceeds the remaining quantity
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And user records current sales count
    And user clicks Sell Plant
    And user selects the created plant
    And user enters quantity exceeding remaining stock
    When user clicks Sell
    Then sale should be blocked due to exceeding remaining stock
    And no new sale should be created

  @sales
  @remaining-stock
  Scenario: Verify admin sees correct remaining stock reflecting deletions
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And user clicks Sell Plant
    And user selects the created plant
    Then remaining stock should match the created plant quantity
    And user enters quantity "3"
    When user clicks Sell
    Then user should be redirected to Sales page
    When user navigates to Sales page
    And user clicks Sell Plant
    And user selects the created plant
    Then remaining stock should be reduced by 3 from created plant quantity
    When user navigates to Sales page
    And admin deletes the created plant sale with quantity "3"
    And user clicks Sell Plant
    And user selects the created plant
    Then remaining stock should match the created plant quantity

  @sales-record
  @user-sales-list
  Scenario: Verify testuser can view the sales list
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And sales list should display existing sales records

  @sales-sort
  @user-sales-sort
  Scenario Outline: Verify testuser can sort the sales list by <Column>
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And sales list should display existing sales records
    When user sorts sales list by "<Column>"
    Then sales list should be sorted by "<Column>"

    Examples:
      | Column      |
      | Plant       |
      | Quantity    |
      | Total Price |
      | Sold At     |

  @sales-record
  @user-sales-forbidden
  Scenario: Verify testuser is forbidden from creating a new sale
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And Sell Plant button should not be displayed for the user
    And user should not be able to delete an existing sale

  @sales-record
  @user-sales-delete-forbidden
  Scenario: Verify testuser is forbidden from deleting a sale
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And sales list should display existing sales records
    And user should not be able to delete an existing sale

  @sales-empty
  @user-sales-empty
  Scenario: Verify testuser identify when no sales exist
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    Then no sales message should be displayed
