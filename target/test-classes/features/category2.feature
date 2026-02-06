Feature: Category API Management

  # Admin Tests
  @TC_CAT2_ADMIN_API_01
  Scenario: Add category with valid data
    Given I am authenticated as an Admin
    When I send a POST request with name "testcat3"
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
    When I send a PUT request to update category id 162 to "UniqueName"
    Then the status code should be 200

  @TC_CAT2_ADMIN_API_05
  Scenario: Delete a category
    Given I am authenticated as an Admin
    When I send a DELETE request for category id 159
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
    When I send a PUT request to update category id 156 to "Fail"
    Then the status code should be 403

  @TC_CAT2_USER_API_04
  Scenario: User attempt to delete category
    Given I am authenticated as a testuser
    When I send a DELETE request for category id 153
    Then the status code should be 403

  @TC_CAT2_USER_API_05
  Scenario: Navigate to next page of category list
    Given I am authenticated as a testuser
    When I request page 2 with size 10
    Then the status code should be 200


  # ADMIN UI TEST CASES
  @TC_CAT2_ADMIN_UI_01
  Scenario: View Add Category button as Admin
    Given I am logged in as an Admin
    When I navigate to "/ui/categories"
    Then the "Add Category" button should be visible 

  @TC_CAT2_ADMIN_UI_02
  Scenario: Add a parent category
    Given I am logged in as an Admin
    And I navigate to "/ui/categories"
    When I click the "Add category button"
    And I enter a valid name "testcat9" without a parent
    And I click the "Save" button
    Then the category "testcat9" should be shown in the list as a parent category 

  @TC_CAT2_ADMIN_UI_03
  Scenario: Add a new sub-category
    Given I am logged in as an Admin
    And I navigate to "/ui/categories"
    When I click the "Add category button"
    And I enter a valid name "testcat10" and select a parent category
    And I click the "Save" button
    Then the sub-category should be added and shown in the list 

  @TC_CAT2_ADMIN_UI_04
  Scenario: Edit existing category
    Given I am logged in as an Admin
    And I navigate to "/ui/categories"
    When I click the Edit button for category "testcat9"
    And I update the name to "testcat9e"
    And I click the "Save" button
    Then the category "testcat9e" should be updated and shown in the list 

  @TC_CAT2_ADMIN_UI_05
  Scenario: Delete a category
    Given I am logged in as an Admin
    And I navigate to "/ui/categories"
    When I click the Delete button for a category "testcat9e"
    Then the category should be deleted and removed from the list 

  # USER UI TEST CASES
  @TC_CAT2_USER_UI_01
  Scenario: Add category button visibility for User
    Given I am logged in as a testuser
    When I navigate to "/ui/categories"
    Then the "Add Category button" should not be visible 

  @TC_CAT2_USER_UI_02
  Scenario: User cannot access add category page manually
    Given I am logged in as a testuser
    When I manually type "/ui/categories/add" in the browser
    Then I should be redirected to a "Access Denied" page

  @TC_CAT2_USER_UI_03
  Scenario: User cannot access edit category page manually
    Given I am logged in as a testuser
    When I manually type "/ui/categories/edit/233" in the browser
    Then I should be redirected to a "Access Denied" page

  @TC_CAT2_USER_UI_04
  Scenario: Edit action not available to user
    Given I am logged in as a testuser
    When I navigate to "/ui/categories"
    Then the Edit button should not be visible 

  @TC_CAT2_USER_UI_05
  Scenario: Delete action not available to user
    Given I am logged in as a testuser
    When I navigate to "/ui/categories"
    Then the Delete button should not be visible