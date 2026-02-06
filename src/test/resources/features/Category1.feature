Feature: Category Management


  # UI ADMIN TEST CASES

  @UI @Admin @TestUI001
  Scenario: UI_CAT1_ADM_001 View category list as admin
    Given I am logged in as "admin" with password "admin123"
    When I navigate to the Category List page
    Then the "Add Category" button should be visible

  @UI @Admin @TestUI002
  Scenario: UI_CAT1_ADM_002 Search category by name
    Given I am logged in as "admin" with password "admin123"
    And I navigate to the Category List page
    When I search for category "Cactus"
    Then I should see "Cactus" in the category list results

  @UI @Admin @TestUI003
  Scenario: UI_CAT1_ADM_003 Filter categories by parent category
    Given I am logged in as "admin" with password "admin123"
    And I navigate to the Category List page
    When I filter by parent category "Random"
    Then I should see categories related to "Random"

  @UI @Admin @TestUI004
  Scenario: UI_CAT1_ADM_004 Sort categories by name
    Given I am logged in as "admin" with password "admin123"
    And I navigate to the Category List page
    When I click the sort header for "Name"
    Then the category list should be sorted by "Name"

  @UI @Admin @TestUI005
  Scenario: UI_CAT1_ADM_005 Verify validation for empty Category Name
    Given I am logged in as "admin" with password "admin123"
    And I navigate to the Category List page
    When I click the "Add Category" button
    And I click the Save button without entering a name
    Then I should see the validation message "Category name is required"


  # UI USER TEST CASES

  @UI @User @TestUI006
  Scenario: UI_CAT1_USR_006 Search category by name (User)
    Given I am logged in as "testuser" with password "test123"
    And I navigate to the Category List page
    When I search for category "Fern"
    Then I should see "Fern" in the category list results

  @UI @User @TestUI007
  Scenario: UI_CAT1_USR_007 Sort categories by ID
    Given I am logged in as "testuser" with password "test123"
    And I navigate to the Category List page
    When I click the sort header for "ID"
    Then the category list should be sorted by "ID"

  @UI @User @TestUI008
  Scenario: UI_CAT1_USR_008 Sort categories by Name (User)
    Given I am logged in as "testuser" with password "test123"
    And I navigate to the Category List page
    When I click the sort header for "Name"
    Then the category list should be sorted by "Name"

  @UI @User @TestUI009
  Scenario: UI_CAT1_USR_009 Sort categories by Parent category
    Given I am logged in as "testuser" with password "test123"
    And I navigate to the Category List page
    When I click the sort header for "Parent Category"
    Then the category list should be sorted by "Parent Category"

  @UI @User @TestUI010
  Scenario: UI_CAT1_USR_010 No categories found message
    Given I am logged in as "testuser" with password "test123"
    And I navigate to the Category List page
    When I search for category "XYZ123_NONSENSE"
    Then I should see the message "No category found"


  # API ADMIN TEST CASES

  @API @Admin @TestAPI001
  Scenario: API_CAT1_ADM_001 Admin can retrieve all categories with pagination
    Given I have a valid API token for "admin"
    When I send a GET request to "/api/categories"
    Then the response status code should be 200
    And the response should contain pagination metadata

  @API @Admin @TestAPI002
  Scenario: API_CAT1_ADM_002 Search categories by name returns filtered results
    Given I have a valid API token for "admin"
    When I send a GET request to "/api/categories?search=Cactus"
    Then the response status code should be 200
    And the response body should contain "Cactus"

  @API @Admin @TestAPI003
  Scenario: API_CAT1_ADM_003 Filter by parent category returns correct data
    Given I have a valid API token for "admin"
    When I send a GET request to "/api/categories?parentId=5"
    Then the response status code should be 200
    And all items in response should have parentId 5

  @API @Admin @TestAPI004
  Scenario: API_CAT1_ADM_004 Sorting works correctly (API)
    Given I have a valid API token for "admin"
    When I send a GET request to "/api/categories?sortBy=name&order=asc"
    Then the response status code should be 200
    And the response body should be sorted by "name"

  @API @Admin @TestAPI005
  Scenario: API_CAT1_ADM_005 Admin can get category details by valid ID
    Given I have a valid API token for "admin"
    And a valid category exists in the system
    When I send a GET request to retrieve that category
    Then the response status code should be 200
    And the response body should contain the details of the requested category

 
  # API USER TEST CASES 

  @API @User @TestAPI006
  Scenario: API_CAT1_USR_006 User can search categories by Name
    Given I have a valid API token for "user"
    When I send a GET request to "/api/categories?search=Fern"
    Then the response status code should be 200
    And the response body should contain "Fern"

  @API @User @TestAPI007
  Scenario: API_CAT1_USR_007 User can filter categories by parent category
    Given I have a valid API token for "user"
    When I send a GET request to "/api/categories?parentId=5"
    Then the response status code should be 200
    And all items in response should have parentId 5

  @API @User @TestAPI008
  Scenario: API_CAT1_USR_008 User can sort categories by ID
    Given I have a valid API token for "user"
    When I send a GET request to "/api/categories?sortBy=id"
    Then the response status code should be 200
    And the response body should be sorted by "id"

  @API @User @TestAPI009
  Scenario: API_CAT1_USR_009 User can sort categories by Name
    Given I have a valid API token for "user"
    When I send a GET request to "/api/categories?sortBy=name"
    Then the response status code should be 200
    And the response body should be sorted by "name"

  @API @User @TestAPI010
  Scenario: API_CAT1_USR_010 User can sort categories by Parent category
    Given I have a valid API token for "user"
    When I send a GET request to "/api/categories?sortBy=parentCategory"
    Then the response status code should be 200
    And the response body should be sorted by "parentCategory"