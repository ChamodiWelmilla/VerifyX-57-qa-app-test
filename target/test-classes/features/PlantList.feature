Feature: PlantList feature
  Background:
    Given user is on the login page

  @plant
  Scenario:Verify Plant List Low Badge

  When user logs in as "admin"
  And admin navigates to the Plant List page
  Then Low badge should be displayed for plants with quantity less than five

  Scenario: Verify Plant List Quantity Sorting

    When user logs in as "user"
    And admin navigates to the Plant List page
    # First click for Ascending
    When user clicks on the Quantity column header
    Then the plant list should be sorted by quantity in ascending order
    # Second click for Descending
    When user clicks on the Quantity column header
    Then the plant list should be sorted by quantity in descending order