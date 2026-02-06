package api.plant;

import api.support.ApiSpecs;
import api.support.ApiSession;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class PlantApi {

    public static Response postPlant(ApiSession session, int categoryId, Map<String, Object> body) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .body(body)
                .post("/api/plants/category/" + categoryId);
    }

    public static Response putPlant(ApiSession session, int plantId, Map<String, Object> body) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .body(body)
                .put("/api/plants/" + plantId);
    }

    public static Response getPlant(ApiSession session, String path) {
        return RestAssured.given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .get(path);
    }

    public static Response deletePlant(ApiSession session, int plantId) {
        return given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .delete("/api/plants/" + plantId);
    }
}