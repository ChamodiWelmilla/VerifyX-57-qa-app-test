package api.dashboard;

import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.response.Response;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;

public class DashboardApi {

    private static final String BASE_URL = ConfigReader.get("api.base-url");

    // Calls the specific Category summary endpoint
    public static Response getCategorySummary(ApiSession session) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .get(BASE_URL + "/api/categories/summary");
    }

    // Calls the specific Plant summary endpoint
    public static Response getPlantSummary(ApiSession session) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .get(BASE_URL + "/api/plants/summary");
    }
}