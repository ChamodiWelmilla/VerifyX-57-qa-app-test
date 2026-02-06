package api.categories;

import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.response.Response;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class Category1Api {

    private static final String BASE_PATH = "/api/categories";

    public static Response getCategories(ApiSession session, Map<String, Object> queryParams) {
        return given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .queryParams(queryParams)
                .get(BASE_PATH);
    }

    public static Response getCategoryById(ApiSession session, int id) {
        return given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .get(BASE_PATH + "/" + id);
    }
}