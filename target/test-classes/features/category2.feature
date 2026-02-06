Feature: Category API Management

  # Admin Tests
  @TC_CAT2_ADMIN_API_01
  Scenario: Add category with valid data
    Given I am authenticated as an Admin
    When I send a POST request with name "Tech"
    Then the status code should be 201

  @TC_CAT2_ADMIN_API_02
  Scenario: Verify API Validation - Name Missing
    Given I am authenticated as an Admin
    When I send a POST request with name ""
    Then the status code should be 400
    And the error message should contain "Category name is mandatory"

  @TC_CAT2_ADMIN_API_03
  Scenario: Add category with invalid length
    Given I am authenticated as an Admin
    When I send a POST request with name "ab"
    Then the status code should be 400
    And the error message should contain "Category name must be between 3 and 10 characters"

  @TC_CAT2_ADMIN_API_04
  Scenario: Edit existing category
    Given I am authenticated as an Admin
    When I send a PUT request to update category id 3 to "UniqueName"
    Then the status code should be 200

  @TC_CAT2_ADMIN_API_05
  Scenario: Delete a category
    Given I am authenticated as an Admin
    When I send a DELETE request for category id 81
    Then the status code should be 204

  # User Tests
  @TC_CAT2_USER_API_01
  Scenario: User attempt to create category
    Given I am authenticated as a testuser
    When I send a POST request with name "veggies"
    Then the status code should be 403

  @TC_CAT2_USER_API_02
  Scenario: Retrieve category list with pagination
    Given I am authenticated as a testuser
    When I request page 1 with size 10
    Then the status code should be 200

  @TC_CAT2_USER_API_03
  Scenario: User attempt to update category
    Given I am authenticated as a testuser
    When I send a PUT request to update category id 81 to "Fail"
    Then the status code should be 403

  @TC_CAT2_USER_API_04
  Scenario: User attempt to delete category
    Given I am authenticated as a testuser
    When I send a DELETE request for category id 81
    Then the status code should be 403

  @TC_CAT2_USER_API_05
  Scenario: Navigate to next page of category list
    Given I am authenticated as a testuser
    When I request page 2 with size 10
    Then the status code should be 200


  