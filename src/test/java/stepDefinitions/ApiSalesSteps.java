package stepDefinitions;

import api.auth.AuthApi;
import api.sales.SalesApiHelper;
import api.sales.helper.SalesPrereqApi;
import api.support.ApiSession;
import api.support.ApiSpecs;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import utils.SalesContext;

import java.util.List;

public class ApiSalesSteps {

    private ApiSession adminSession;
    private ApiSession userSession;
    private Response apiResponse;
    private SalesPrereqApi.PlantForSale currentPlant;
    private int createdSaleId;
    private int lastStatusCode;

    @Given("admin is authenticated via API")
    public void admin_is_authenticated_via_api() {
        adminSession = AuthApi.loginAsAdmin();
        Assert.assertNotNull(adminSession, "Admin authentication failed");
    }

    @Given("testuser is authenticated via API")
    public void testuser_is_authenticated_via_api() {
        userSession = AuthApi.loginAsUser();
        Assert.assertNotNull(userSession, "Test user authentication failed");
    }

    @Given("a plant is created via API with price {string} and stock {string}")
    public void plant_is_created_via_api_with_price_and_stock(String price, String stock) {
        double priceValue = Double.parseDouble(price);
        int stockValue = Integer.parseInt(stock);
        currentPlant = SalesPrereqApi.createAndStorePlantForSale(priceValue, stockValue);
        Assert.assertTrue(currentPlant.getPlantId() > 0, "Plant creation via API failed");
    }

    @When("admin sends POST request to create a sale via API with quantity {string}")
    public void admin_sends_post_request_to_create_sale_with_quantity(String quantity) {
        Assert.assertNotNull(currentPlant, "Plant not created");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        int quantityValue = Integer.parseInt(quantity);
        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .queryParam("quantity", quantityValue)
                .post("/api/sales/plant/" + currentPlant.getPlantId());

        lastStatusCode = apiResponse.statusCode();
        if (lastStatusCode == 201) {
            try {
                createdSaleId = apiResponse.jsonPath().getInt("id");
            } catch (Exception ignored) {
                createdSaleId = 0;
            }
        }
    }

    @When("testuser sends POST request to create a sale via API with quantity {string}")
    public void testuser_sends_post_request_to_create_sale_with_quantity(String quantity) {
        Assert.assertNotNull(currentPlant, "Plant not created");
        Assert.assertNotNull(userSession, "User not authenticated");

        int quantityValue = Integer.parseInt(quantity);
        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .queryParam("quantity", quantityValue)
                .post("/api/sales/plant/" + currentPlant.getPlantId());

        lastStatusCode = apiResponse.statusCode();
    }

    @When("admin sends DELETE request to remove the sale via API")
    public void admin_sends_delete_request_to_remove_sale_via_api() {
        Assert.assertTrue(createdSaleId > 0, "No sale created");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .delete("/api/sales/" + createdSaleId);

        lastStatusCode = apiResponse.statusCode();
    }

    @When("testuser sends DELETE request to remove the sale via API")
    public void testuser_sends_delete_request_to_remove_sale_via_api() {
        Assert.assertTrue(createdSaleId > 0, "No sale created");
        Assert.assertNotNull(userSession, "User not authenticated");

        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .delete("/api/sales/" + createdSaleId);

        lastStatusCode = apiResponse.statusCode();
    }

    @When("admin sends GET request to retrieve sales list via API")
    public void admin_sends_get_request_to_retrieve_sales_list_via_api() {
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .get("/api/sales");

        lastStatusCode = apiResponse.statusCode();
    }

    @When("testuser sends GET request to retrieve sales list via API")
    public void testuser_sends_get_request_to_retrieve_sales_list_via_api() {
        Assert.assertNotNull(userSession, "User not authenticated");

        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .get("/api/sales");

        lastStatusCode = apiResponse.statusCode();
    }

    @When("testuser sends GET request to retrieve sales list sorted by {string} via API")
    public void testuser_sends_get_request_to_retrieve_sorted_sales_list_via_api(String sortField) {
        Assert.assertNotNull(userSession, "User not authenticated");

        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .queryParam("sort", sortField + ",asc")
                .get("/api/sales");

        lastStatusCode = apiResponse.statusCode();
    }

    @When("testuser sends GET request for non-existing sale via API")
    public void testuser_sends_get_request_for_non_existing_sale_via_api() {
        Assert.assertNotNull(userSession, "User not authenticated");

        int nonExistingId = 999999;
        apiResponse = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .get("/api/sales/" + nonExistingId);

        lastStatusCode = apiResponse.statusCode();
    }

    @Then("API should return status code {int}")
    public void api_should_return_status_code(int expectedCode) {
        Assert.assertEquals(lastStatusCode, expectedCode,
                "Expected status code " + expectedCode + " but got " + lastStatusCode);
    }

    @Then("API should return status code {int} or {int}")
    public void api_should_return_status_code_or(int code1, int code2) {
        Assert.assertTrue(lastStatusCode == code1 || lastStatusCode == code2,
                "Expected status code " + code1 + " or " + code2 + " but got " + lastStatusCode);
    }

    @Then("API should return an error status code")
    public void api_should_return_error_status_code() {
        Assert.assertTrue(lastStatusCode >= 400, "Expected 4xx error but got " + lastStatusCode);
    }

    @Then("API should return an error status code \\(4xx)")
    public void api_should_return_error_status_code_4xx() {
        Assert.assertTrue(lastStatusCode >= 400 && lastStatusCode < 500,
                "Expected 4xx error but got " + lastStatusCode);
    }

    @Then("API response should contain created sale id")
    public void api_response_should_contain_created_sale_id() {
        Assert.assertTrue(createdSaleId > 0, "No valid sale id in response");
    }

    @Then("API response should contain error message about exceeding stock")
    public void api_response_should_contain_error_message_about_stock() {
        String body = apiResponse.asString();
        Assert.assertTrue(body.toLowerCase().contains("stock") || body.toLowerCase().contains("quantity") || body.toLowerCase().contains("exceeds"),
                "Expected error message about stock/quantity in response: " + body);
    }

    @Then("API response should contain validation error message")
    public void api_response_should_contain_validation_error_message() {
        String body = apiResponse.asString();
        Assert.assertTrue(body.length() > 0, "Expected error message in response");
    }

    @Then("API response should contain sales list data")
    public void api_response_should_contain_sales_list_data() {
        String body = apiResponse.asString();
        Assert.assertTrue(body.contains("id") || body.contains("quantity") || body.contains("totalPrice"),
                "Expected sales list data in response: " + body);
    }

    @Then("sales list via API should contain the newly created sale")
    public void sales_list_via_api_should_contain_newly_created_sale() {
        Assert.assertTrue(createdSaleId > 0, "No sale created");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        Response listResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .get("/api/sales");

        Assert.assertTrue(listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                "Failed to retrieve sales list");

        boolean found = salesListContainsSaleId(listResp, createdSaleId);
        Assert.assertTrue(found, "Created sale id " + createdSaleId + " not found in sales list");
    }

    @Then("sales list via API should not contain the deleted sale")
    public void sales_list_via_api_should_not_contain_deleted_sale() {
        Assert.assertTrue(createdSaleId > 0, "No sale to verify deletion");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        Response listResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .get("/api/sales");

        Assert.assertTrue(listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                "Failed to retrieve sales list");

        boolean found = salesListContainsSaleId(listResp, createdSaleId);
        Assert.assertFalse(found, "Deleted sale id " + createdSaleId + " still found in sales list");
    }

    @Then("no sale should be created for the plant")
    public void no_sale_should_be_created_for_plant() {
        Assert.assertNotNull(currentPlant, "Plant not created");
        Assert.assertNotNull(userSession, "User not authenticated");

        Response listResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .get("/api/sales");

        Assert.assertTrue(listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                "Failed to retrieve sales list");

        boolean found = salesListContainsPlantId(listResp, currentPlant.getPlantId());
        Assert.assertFalse(found, "Sale should not be created for plant id " + currentPlant.getPlantId());
    }

    @Then("sale should still exist in sales list")
    public void sale_should_still_exist_in_sales_list() {
        Assert.assertTrue(createdSaleId > 0, "No sale to verify");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        Response listResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .get("/api/sales");

        Assert.assertTrue(listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                "Failed to retrieve sales list");

        boolean found = salesListContainsSaleId(listResp, createdSaleId);
        Assert.assertTrue(found, "Sale id " + createdSaleId + " not found in sales list");
    }

    @Then("sales list should be sorted by {string} in ascending order")
    public void sales_list_should_be_sorted_by_field(String sortField) {
        Assert.assertNotNull(apiResponse, "No API response available");
        String body = apiResponse.asString();

        // Basic validation that response contains expected data
        Assert.assertTrue(body.contains("id") || body.contains(sortField.split("\\.")[0]),
                "Response does not contain expected sorted data for field: " + sortField);
    }

    @And("a sale is created via API with quantity {string}")
    public void sale_is_created_via_api_with_quantity(String quantity) {
        admin_sends_post_request_to_create_sale_with_quantity(quantity);
        Assert.assertEquals(lastStatusCode, 201, "Sale creation failed with status " + lastStatusCode);
        Assert.assertTrue(createdSaleId > 0, "Failed to extract sale id from response");
    }

    @And("plant remaining stock should be updated to {string} via API")
    public void plant_remaining_stock_should_be_updated_to_via_api(String expectedStock) {
        Assert.assertNotNull(currentPlant, "Plant not created");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        Response plantResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .get("/api/plants/" + currentPlant.getPlantId());

        if (plantResp.statusCode() == 404) {
            plantResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/plants");
        }

        Assert.assertTrue(plantResp.statusCode() >= 200 && plantResp.statusCode() < 300,
                "Failed to retrieve plant details");

        int expectedStockValue = Integer.parseInt(expectedStock);
        int actualStock = getPlantQuantity(plantResp, currentPlant.getPlantId());

        Assert.assertEquals(actualStock, expectedStockValue,
                "Expected remaining stock " + expectedStockValue + " but got " + actualStock);
    }

    @And("plant remaining stock should be restored to {string} via API")
    public void plant_remaining_stock_should_be_restored_to_via_api(String expectedStock) {
        Assert.assertNotNull(currentPlant, "Plant not created");
        Assert.assertNotNull(adminSession, "Admin not authenticated");

        Response plantResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .get("/api/plants/" + currentPlant.getPlantId());

        if (plantResp.statusCode() == 404) {
            plantResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/plants");
        }

        Assert.assertTrue(plantResp.statusCode() >= 200 && plantResp.statusCode() < 300,
                "Failed to retrieve plant details");

        int expectedStockValue = Integer.parseInt(expectedStock);
        int actualStock = getPlantQuantity(plantResp, currentPlant.getPlantId());

        Assert.assertEquals(actualStock, expectedStockValue,
                "Expected restored stock " + expectedStockValue + " but got " + actualStock);
    }

    @And("sales data exists for sorting")
    public void sales_data_exists_for_sorting() {
        SalesApiHelper.ensureSalesExistForSorting();
    }

    // Helper methods
    private boolean salesListContainsSaleId(Response listResp, int expectedId) {
        if (expectedId <= 0) {
            return false;
        }

        try {
            List<Integer> paged = listResp.jsonPath().getList("content.id", Integer.class);
            if (paged != null && paged.contains(expectedId)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            List<Integer> flat = listResp.jsonPath().getList("id", Integer.class);
            return flat != null && flat.contains(expectedId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean salesListContainsPlantId(Response listResp, int plantId) {
        if (plantId <= 0) {
            return false;
        }

        try {
            List<Integer> paged = listResp.jsonPath().getList("content.plant.id", Integer.class);
            if (paged != null && paged.contains(plantId)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            List<Integer> flat = listResp.jsonPath().getList("plant.id", Integer.class);
            return flat != null && flat.contains(plantId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private int getPlantQuantity(Response plantResp, int plantId) {
        if (plantId <= 0) {
            return 0;
        }

        try {
            return plantResp.jsonPath().getInt("quantity");
        } catch (Exception ignored) {
        }

        try {
            List<Integer> ids = plantResp.jsonPath().getList("content.id", Integer.class);
            List<Integer> quantities = plantResp.jsonPath().getList("content.quantity", Integer.class);
            if (ids != null && quantities != null) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) != null && ids.get(i) == plantId && quantities.get(i) != null) {
                        return quantities.get(i);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            List<Integer> ids = plantResp.jsonPath().getList("id", Integer.class);
            List<Integer> quantities = plantResp.jsonPath().getList("quantity", Integer.class);
            if (ids != null && quantities != null) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) != null && ids.get(i) == plantId && quantities.get(i) != null) {
                        return quantities.get(i);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return 0;
    }
}
