package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.testng.Assert;
import pages.plants.PlantListPage;
import utils.DriverManager;
import utils.ConfigReader;

import api.auth.AuthApi;
import api.plants.PlantsApi;
import api.support.ApiSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;


public class PlantListSteps {

    PlantListPage plantListPage;
    private ApiSession adminSession;
    private Response response;

    @When("admin navigates to the Plant List page")
    public void admin_navigates_to_plant_list() {
        DriverManager.getDriver().get(
                ConfigReader.get("ui.base.url") + ConfigReader.get("ui.plants.path")
        );
        plantListPage = new PlantListPage(DriverManager.getDriver());
    }

    @Then("Low badge should be displayed for plants with quantity less than five")
    public void verify_low_badge() {
        Assert.assertTrue(
                plantListPage.isLowBadgeDisplayedForLowQuantityPlant(),
                "Low badge is NOT displayed for plants with quantity < 5"
        );
    }

    @When("user clicks on the Quantity column header")
    public void user_clicks_on_quantity_header() {
        plantListPage.clickQuantityHeader();
    }

    @Then("the plant list should be sorted by quantity in ascending order")
    public void verify_ascending_quantity_sorting() {
        // 1. Get the actual numbers from the UI
        List<Integer> actualQuantities = plantListPage.getAllQuantities();

        // 2. Create a copy and sort it using Java
        List<Integer> expectedQuantities = new ArrayList<>(actualQuantities);
        Collections.sort(expectedQuantities);

        // 3. Compare
        Assert.assertEquals(actualQuantities, expectedQuantities,
                "Descending sort failed!");
    }

    @Then("the plant list should be sorted by quantity in descending order")
    public void verify_descending_quantity_sorting() {
        List<Integer> actual = plantListPage.getAllQuantities();
        List<Integer> expected = new ArrayList<>(actual);

        // Sort ascending then reverse to get descending
        Collections.sort(expected);
        Collections.reverse(expected);

        Assert.assertEquals(actual, expected, "Descending sort failed!");
    }

    //api
    @Given("plant admin is authenticated via API")
    public void admin_is_authenticated_via_api() {
        adminSession = AuthApi.loginAsAdmin();
        assertNotNull(adminSession.getBearerToken(), "Admin token should not be null");
    }

    @When("admin requests the full plant list")
    public void admin_requests_the_full_plant_list() {
        response = PlantsApi.getAllPlants(adminSession);
    }

    @Then("the plant response status should be 200")
    public void the_response_status_should_be_200() {
        assertEquals(response.statusCode(), 200, "Unexpected status code");
    }


    @Then("the response should contain a list of plants")
    public void response_should_contain_list_of_plants() {
        assertNotNull(response.jsonPath().getList("$"), "Response body should not be null");
        assertFalse(response.jsonPath().getList("$").isEmpty(),
                "Plant list should not be empty");
    }
    //category
    @When("admin requests plants for category {int}")
    public void admin_requests_plants_for_category(int categoryId) {
        response = PlantsApi.getPlantsByCategory(adminSession, String.valueOf(categoryId));
    }

    @Then("the category plant response status should be 200")
    public void category_plant_response_status_should_be_200() {
        assertEquals(response.statusCode(), 200, "Unexpected status code for category plants");
    }

    @Then("all plants returned should belong to category {int}")
    public void all_plants_belong_to_category(int categoryId) {

        List<Map<String, Object>> plants = response.jsonPath().getList("$");
        if (plants.isEmpty()) {
            System.out.println("No plants found for category " + categoryId);
            return;
        }
        assertFalse(plants.isEmpty(), "Plant list should not be empty");

        for (Map<String, Object> plant : plants) {

            Map<String, Object> category =
                    (Map<String, Object>) plant.get("category");

            assertNotNull(category, "Category should not be null");

            int actualCategoryId =
                    ((Number) category.get("id")).intValue();

            assertEquals(categoryId, actualCategoryId,
                    "Plant does not belong to requested category");
        }
    }

    // price sorting

    @When("admin requests paged plants sorted by {string} ascending")
    public void admin_requests_paged_plants_sorted_by_field_ascending(String field) {
        // Request first page with 3 records sorted by given field
        response = PlantsApi.getPagedPlants(adminSession, 0, 3, field + ",asc");
    }

    @Then("plants should be sorted by {string} ascending")
    public void plants_should_be_sorted_by_field_ascending(String field) {
        List<Map<String, Object>> plants = response.jsonPath().getList("content");

        // Check the list is sorted ascending
        for (int i = 0; i < plants.size() - 1; i++) {
            Number price1 = (Number) plants.get(i).get(field);
            Number price2 = (Number) plants.get(i + 1).get(field);
            assertTrue(price1.doubleValue() <= price2.doubleValue(),
                    "Plants are not sorted ascending by " + field + ": " + price1 + " > " + price2);
        }
    }

    @Then("at least {int} plants are returned")
    public void at_least_n_plants_are_returned(int expectedCount) {
        List<Map<String, Object>> plants = response.jsonPath().getList("content");
        assertTrue(plants.size() >= expectedCount,
                "Expected at least " + expectedCount + " plants but got " + plants.size());
    }


}
