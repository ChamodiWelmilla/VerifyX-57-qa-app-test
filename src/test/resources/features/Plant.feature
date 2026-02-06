Feature: Plant Management
  Background:
    Given user is on the login page
    
  # --- ADMIN SCENARIOS---
  #TC_PLANTUI_AD_01
  @UI @AdminUI @AdminUI_Test_01
  Scenario: Creation of a new plant successfully with valid data
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Cinnamon" as name, "25.00" as price, and "15" as quantity
    And selects a valid "subcat1" sub-category and clicks "Save"
    Then the new plant "Cinnamon" appears in the list


  #TC_PLANTUI_AD_02
  @UI @AdminUI @AdminUI_Test_02
  Scenario: Cancel button functionality in the add plant form
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Dummy Data" as name, "10.00" as price, and "5" as quantity
    And the Admin clicks the Cancel button
    Then the system redirects to the Plants page


  #TC_PLANTUI_AD_03 
  @UI @AdminUI @NegativeTest @AdminUI_Test_03
  Scenario: Validation error when the price is negative
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Cactus" as name and "-10" as price
    And clicks the "Save" button
    Then the system displays the validation error "Price must be greater than 0" 

  #TC_PLANTUI_AD_04
  @UI @AdminUI @AdminUI_Test_04
  Scenario: Validation error for short Plant Name
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the "Add Plant" button
    And enters "Ab" as name and "15.00" as price
    And clicks the "Save" button
    Then the system displays the validation error "Plant name must be between 3 and 25 characters"

  #TC_PLANTUI_AD_05
  @UI @AdminUI @AdminUI_Test_05
  Scenario: Edit functionality for an existing plant
    Given Admin is logged in and navigated to the Plant List page
    When the Admin clicks the Edit button for an existing plant
    And updates the Quantity to "50" and saves
    Then the system redirects to the Plants page


  # --- USER SCENARIOS---
  #TC_PLANTUI_USR_01 
    @UI @UserUI @UserUI_Test_01
    Scenario: Add Plant button is hidden for normal User
      Given user is on the login page
      When user logs in as "user"
      And Admin is navigated to the Plant List page
      Then the Add Plant button should not be visible

  #TC_PLANTUI_USR_01
    @UI @UserUI @UserUI_Test_02
    Scenario: Edit and Delete actions are hidden for normal User
      When user logs in as "user"
      And Admin is navigated to the Plant List page
      Then the Edit and Delete actions should be hidden for the user 

  #TC_PLANTUI_USR_03
    @UI @UserUI @UserUI_Test_03
      Scenario: Verify Plant List Low Badge
        When user logs in as "user"
        And User navigates to the Plant List page 
        Then Low badge should be displayed for plants with quantity less than five

  #TC_PLANTUI_USR_04
    @UI @UserUI @UserUI_Test_04
    Scenario: Search by Plant Name
      When user logs in as "user"
      And User is navigated to the Plant List page
      When the user searches for plant name "Aloe"
      Then the list should only display plants matching "Aloe"

  #TC_PLANTUI_USR_05
    @UI @UserUI @UserUI_Test_05
    Scenario: No plants found empty state
      When user logs in as "user"
      And Admin is navigated to the Plant List page
      When the user searches for plant name "NonExistentPlant"
      Then the system should display "No plants found"


