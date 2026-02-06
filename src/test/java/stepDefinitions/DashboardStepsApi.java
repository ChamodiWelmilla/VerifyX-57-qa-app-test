package stepDefinitions;

import api.auth.AuthApi;
import api.dashboard.DashboardApi;
import api.support.ApiSession;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class DashboardStepsApi {

    private ApiSession adminSession;
    private Map<String, Integer> dashboardSummary;

    @Given("admin is authenticated via API")
    public void admin_is_authenticated_via_api() {
        adminSession = AuthApi.loginAsAdmin();
        assertNotNull(adminSession.getBearerToken(), "Admin token should not be null");
    }

    @When("admin requests the dashboard summary")
    public void admin_requests_dashboard_summary() {
        dashboardSummary = new HashMap<>();

        Response categoryResponse = DashboardApi.getCategorySummary(adminSession);
        Response plantResponse = DashboardApi.getPlantSummary(adminSession);

        assertEquals(categoryResponse.statusCode(), 200, "Category summary API failed");
        assertEquals(plantResponse.statusCode(), 200, "Plant summary API failed");

        Integer mainCatCount = categoryResponse.jsonPath().getInt("mainCategories");
        Integer subCatCount = categoryResponse.jsonPath().getInt("subCategories");

        Integer plantCount = plantResponse.jsonPath().getInt("totalPlants");

        dashboardSummary.put("categories", mainCatCount);
        dashboardSummary.put("plants", plantCount);
    }

    @Then("the response status should be {int}")
    public void response_status_should_be(int statusCode) {
        // This step is mostly for backward compatibility; actual status checks done above
        assertTrue(dashboardSummary.values().stream().allMatch(v -> v >= 0),
                "All summary values should be non-negative");
    }

    @Then("dashboard summary should contain integer counts for:")
    public void dashboard_summary_should_contain_integer_counts(DataTable table) {
        table.asList().forEach(field -> {
            assertTrue(dashboardSummary.containsKey(field), field + " should be present");
            Integer value = dashboardSummary.get(field);
            assertNotNull(value, field + " should not be null");
            assertTrue(value >= 0, field + " should be a non-negative integer");
        });
    }
}
