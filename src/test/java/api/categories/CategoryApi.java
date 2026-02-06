package api.categories;

import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.response.Response;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class CategoryApi {
    private static final String BASE_PATH = "/api/categories";

    public static Response createCategory(ApiSession session, Map<String, Object> body) {
        return given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .body(body)
                .post(BASE_PATH);
    }

    public static Response getCategories(ApiSession session, int page, int size) {
        return given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .queryParam("page", page)
                .queryParam("size", size)
                .get(BASE_PATH);
    }

    public static Response updateCategory(ApiSession session, int id, Map<String, Object> body) {
        return given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .body(body)
                .put(BASE_PATH + "/" + id);
    }

    public static Response deleteCategory(ApiSession session, int id) {
        return given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .delete(BASE_PATH + "/" + id);
    }
}