Feature: Sales

  @ui-sales-view
  Scenario: Verify admin views sales list with correct details
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And user clicks Sell Plant
    And user sells the created plant with quantity "3"
    Then sales list should show correct total price for the created plant with quantity "3"

  @ui-sales-delete
  Scenario: Verify admin deletes a sale
    Given user is on the login page
    When user logs in as "admin"
    Then Dashboard should be displayed
    When user navigates to Sales page
    And admin deletes the last created sale
    Then sales list should not contain the last created sale

  @ui-sales-empty-validation
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

  @ui-sales-invalid-quantity
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

  @ui-sales-exceed-stock
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

  @ui-sales-remaining-stock
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

  @ui-user-sales-list
  Scenario: Verify testuser can view the sales list
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And sales list should display existing sales records

  @ui-user-sales-sort
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
      | Total Price |

  @ui-user-sales-create-forbidden
  Scenario: Verify testuser is forbidden from creating a new sale
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And Sell Plant button should not be displayed for the user
    And user should not be able to delete an existing sale

  @ui-user-sales-delete-forbidden
  Scenario: Verify testuser is forbidden from deleting a sale
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    And sales list should display existing sales records
    And user should not be able to delete an existing sale

  @ui-user-sales-empty
  Scenario: Verify testuser identify when no sales exist
    Given user is on the login page
    When user logs in as "user"
    Then Dashboard should be displayed
    When user navigates to Sales page
    Then Sales page should load successfully
    Then no sales message should be displayed

  @api-sales-create
  Scenario: Verify admin creates a new sale via API successfully
    Given admin is authenticated via API
    And a plant is created via API with price "150.0" and stock "25"
    When admin sends POST request to create a sale via API with quantity "1"
    Then API should return status code 201
    And API response should contain created sale id
    And sales list via API should contain the newly created sale

  @api-sales-delete
  Scenario: Verify admin deletes a sale via API successfully
    Given admin is authenticated via API
    And a plant is created via API with price "150.0" and stock "25"
    And a sale is created via API with quantity "1"
    When admin sends DELETE request to remove the sale via API
    Then API should return status code 200 or 204
    And sales list via API should not contain the deleted sale

  @api-sales-exceed-stock
  Scenario: Verify admin is prevented from creating sale exceeding stock via API
    Given admin is authenticated via API
    And a plant is created via API with price "150.0" and stock "1"
    When admin sends POST request to create a sale via API with quantity "2"
    Then API should return an error status code
    And API response should contain error message about exceeding stock

  @api-sales-update-stock
  Scenario: Verify admin updates remaining stock after creating sale via API
    Given admin is authenticated via API
    And a plant is created via API with price "150.0" and stock "20"
    When admin sends POST request to create a sale via API with quantity "5"
    Then API should return status code 201
    And plant remaining stock should be updated to "15" via API

  @api-sales-restore-stock
  Scenario: Verify admin restores stock after deleting sale via API
    Given admin is authenticated via API
    And a plant is created via API with price "150.0" and stock "20"
    And a sale is created via API with quantity "5"
    When admin sends DELETE request to remove the sale via API
    Then API should return status code 200 or 204
    And plant remaining stock should be restored to "20" via API

  @api-user-sales-list
  Scenario: Verify testuser retrieves sales list via API
    Given testuser is authenticated via API
    When testuser sends GET request to retrieve sales list via API
    Then API should return status code 200
    And API response should contain sales list data

  @api-user-sales-create-forbidden
  Scenario: Verify testuser is forbidden from creating sale via API
    Given testuser is authenticated via API
    And a plant is created via API with price "150.0" and stock "10"
    When testuser sends POST request to create a sale via API with quantity "1"
    Then API should return an error status code (4xx)
    And no sale should be created for the plant

  @api-user-sales-delete-forbidden
  Scenario: Verify testuser is forbidden from deleting sale via API
    Given admin is authenticated via API
    And a plant is created via API with price "150.0" and stock "10"
    And a sale is created via API with quantity "1"
    When testuser is authenticated via API
    And testuser sends DELETE request to remove the sale via API
    Then API should return an error status code (4xx)
    And sale should still exist in sales list

  @api-user-sales-sort
  Scenario Outline: Verify testuser can sort sales list by <SortField> via API
    Given testuser is authenticated via API
    And sales data exists for sorting
    When testuser sends GET request to retrieve sales list sorted by "<SortField>" via API
    Then API should return status code 200
    And sales list should be sorted by "<SortField>" in ascending order

    Examples:
      | SortField   |
      | quantity    |
      | soldAt      |

  @api-user-sales-not-found
  Scenario: Verify testuser cannot retrieve non-existing sale via API
    Given testuser is authenticated via API
    When testuser sends GET request for non-existing sale via API
    Then API should return status code 404
