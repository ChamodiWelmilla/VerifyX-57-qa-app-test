package api.plants;

import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.response.Response;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;

public class PlantsApi {

    private static final String BASE_URL = ConfigReader.get("api.base-url");
    private static final String PLANTS_PATH = "/api/plants";

    public static Response getAllPlants(ApiSession session) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .get(BASE_URL + "/api/plants");
    }

    public static Response getPlantsByCategory(ApiSession session, String categoryId) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .queryParam("category", categoryId)
                .get(BASE_URL + PLANTS_PATH + "/category/" + categoryId);
    }

    public static Response getPagedPlants(ApiSession session, int page, int size, String sortField) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sortField)
                .get(BASE_URL + PLANTS_PATH + "/paged");
    }
}
