Feature: Plant Management
  Background:
    Given user is on the login page
    
  #adminui1
  @UI @Admin @AdminUI_Test_01
  Scenario: Creation of a new plant successfully with valid data
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "fruit" as name, "25.00" as price, and "15" as quantity
    And selects a valid "subcat1" sub-category and clicks "Save"
    Then the new plant "Jack fruit" appears in the list


# Based on PLANTUI_AD_02
  @UI @Admin @AdminUI_Test_02
  Scenario: Cancel button functionality in the add plant form
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Dummy Data" as name, "10.00" as price, and "5" as quantity
    And the Admin clicks the Cancel button
    Then the system redirects to the Plants page


    # Based on PLANTUI_AD_03
  @UI @Admin @NegativeTest @AdminUI_Test_03
  Scenario: Validation error when the price is negative
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Cactus" as name and "-10" as price
    And clicks the "Save" button
    Then the system displays the validation error "Price must be greater than 0" 

  # Based on TC_PLANTUI_AD_04
  @UI @Admin @NegativeTest @AdminUI_Test_04
  Scenario: Validation error for short Plant Name
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Ab" as name and "15.00" as price
    And clicks the "Save" button
    Then the system displays the validation error "Plant name must be between 3 and 25 characters"

  # Based on TC_PLANTUI_AD_05
  @UI @Admin @AdminUI_Test_05
  Scenario: Edit functionality for an existing plant
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the Edit button for an existing plant
    And updates the Quantity to "50" and saves
    Then the system redirects to the Plants page






# --- USER SCENARIOS (TC_PLANTUI_USR_01 to 05) ---
 # Based on TC_PLANTUI_USR_01 
  @UI @User @UserUI_Test_01
  Scenario: Add Plant button is hidden for normal User
    Given user is on the login page
    When user logs in as "user"
    And Admin is navigated to the Plant List page
    Then the Add Plant button should not be visible


  @UI @User @UserUI_Test_02
  Scenario: Edit and Delete actions are hidden for normal User
    When user logs in as "user"
    And Admin is navigated to the Plant List page
    Then the Edit and Delete actions should be hidden for the user 

# Based on TC_PLANTUI_USR_03
  @UI @User @UserUI_Test_03
    Scenario: Verify Plant List Low Badge
      When user logs in as "user"
      And User navigates to the Plant List page 
      Then Low badge should be displayed for plants with quantity less than five

  @UI @User @UserUI_Test_04
  Scenario: Search by Plant Name
    When user logs in as "user"
    And User is navigated to the Plant List page
    When the user searches for plant name "Aloe"
    Then the list should only display plants matching "Aloe"

  @UI @User @UserUI_Test_05
  Scenario: No plants found empty state
    When user logs in as "user"
    And Admin is navigated to the Plant List page
    When the user searches for plant name "NonExistentPlant"
    Then the system should display "No plants found"


#=================API ==========================


@TC_PLANTAPI_AD_01 @plantAdAPI
Scenario: Creating a plant under a sub-category
    Given I am authenticated as a Plant Admin
    When I send a POST request to create a plant with category 242
    Then the plant API status code should be 201
    And the response should contain a valid plant ID

  @TC_PLANTAPI_AD_02 @plantAdAPI
Scenario: Updating an existing plant's details
    Given I am authenticated as a Plant Admin
    When I send a PUT request to "/api/plants/1" with price 25.0 and quantity 20
    Then the plant API status code should be 200

  @TC_PLANTAPI_AD_03 @plantAdAPI
Scenario: Deleting a plant by ID
    Given I am authenticated as a Plant Admin
    When I send a DELETE request to "/api/plants/7"
    Then the plant API status code should be 204
    And a GET request to "/api/plants/7" should return 404

 @TC_PLANTAPI_AD_04 @plantAdAPI
Scenario: Validation error for Negative Price
    Given I am authenticated as a Plant Admin
    When I send a POST request to "/api/plants/category/2" with a negative price "-10"
    Then the plant API status code should be 400
    And the response message should be "Validation failed"

  @TC_PLANTAPI_AD_05 @plantAdAPI
Scenario: Validation error for Short Plant Name
    Given I am authenticated as a Plant Admin
    When I send a POST request to "/api/plants/category/2" with a short name "Al"
    Then the plant API status code should be 400


 
  @TC_PLANTAPI_USR_01 
Scenario: Retrieving a plant by ID
    # Change this from UI login to API authentication
    Given I am authenticated as a plant testuser 
    When I send a GET request to "/api/plants/8"
    Then the plant API status code should be 200
    And the plant API response should contain the plant name "Golden Pothos"

  @TC_PLANTAPI_USR_02
Scenario: Searching plants with pagination
    Given I am authenticated as a plant testuser
    When I send a GET request to "/api/plants/paged?name=Aloe&page=0&size=10"
    Then the plant API status code should be 200
    And the plant API response should contain a paginated list of plants

@TC_PLANTAPI_USR_03
  Scenario: User is forbidden from updating a Plant
    Given I am authenticated as a plant testuser
    When I send a PUT request to "/api/plants/2"
    Then the plant API status code should be 403

@TC_PLANTAPI_USR_04
  Scenario: User is forbidden from Creating a Plant
    # CHANGE THIS: Use the API authentication step
    Given I am authenticated as a plant testuser
    When I send a POST request to "/api/plants/category/2"
    Then the plant API status code should be 403

@TC_PLANTAPI_USR_05
Scenario: User is forbidden from deleting a Plant
  # Change this from the UI step to the API auth step
  Given I am authenticated as a plant testuser
  When I send a DELETE request to "/api/plants/8"
  Then the plant API status code should be 403