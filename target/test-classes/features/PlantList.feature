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
    When user clicks on the Quantity column header
    Then the plant list should be sorted by quantity in ascending order
    When user clicks on the Quantity column header
    Then the plant list should be sorted by quantity in descending order

  @api @plant1
  Scenario Outline: Verify User Full Plant List Retrieval
    Given plant "<user>" is authenticated via API
    When "<user>" requests the full plant list
    Then the plant response status should be 200
    And the response should contain a list of plants
    Examples:
      | user      |
      | admin     |
      | non admin |

  @api @plant2
  Scenario Outline: TC_ADMIN_API_PLANT_002 Verify Category-Specific Plant Count
    Given plant "<user>" is authenticated via API
    When "<user>" requests plants for category 1
    Then the plant response status should be 200
    And all plants returned should belong to category 1
    Examples:
      | user      |
      | admin     |
      | non admin |

  @api @plant3
  Scenario Outline: Verify Plant Price Sorting for different users
    Given plant "<user>" is authenticated via API
    When "<user>" requests paged plants sorted by "price" ascending
    Then the plant response status should be 200
    And plants should be sorted by "price" ascending
    And at least 3 plants are returned

    Examples:
      | user      |
      | admin     |
      | non admin  |


  @api @plant4
 Scenario Outline: TC_ADMIN_API_PLANT_003 Verify Plant List Sorting by Quantity
    Given plant "<user>" is authenticated via API
    When "<user>" requests paged plants sorted by "quantity" ascending
    Then the plant response status should be 200
    And plants should be sorted by "quantity" ascending
    And at least 3 plants are returned

    Examples:
      | user      |
      | admin     |
      | non admin  |
