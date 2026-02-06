package api.auth;

import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import utils.ConfigReader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;

public class AuthApi {

    private static String getLoginPath() {
        return Optional.ofNullable(ConfigReader.get("api.auth.login.path"))
                .orElse("/api/auth/login");
    }

    public static ApiSession login(String username, String password) {
        CookieFilter cookieFilter = new CookieFilter();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", password);

        Response response = given()
                .spec(ApiSpecs.baseJsonSpec())
                .filter(cookieFilter)
                .body(body)
                .post(getLoginPath());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new AssertionError("Login failed. Status: " + response.statusCode() + ", body: " + response.asString());
        }

        String token = tryExtractToken(response);
        return ApiSession.authenticated(cookieFilter, token);
    }

    public static ApiSession loginAsAdmin() {
        return login(
                Optional.ofNullable(ConfigReader.get("admin.username")).orElse("admin"),
                Optional.ofNullable(ConfigReader.get("admin.password")).orElse("admin123")
        );
    }
    public static ApiSession loginAsUser() {
        return login(
                Optional.ofNullable(ConfigReader.get("user.username")).orElse("user"),
                Optional.ofNullable(ConfigReader.get("user.password")).orElse("user123")
        );
    }


    public static ApiSession loginAsUser() {
        return login(
                Optional.ofNullable(ConfigReader.get("user.username")).orElse("user"),
                Optional.ofNullable(ConfigReader.get("user.password")).orElse("user123")
        );
    }

    private static String tryExtractToken(Response response) {
        String contentType = response.getContentType() == null ? "" : response.getContentType();
        if (!contentType.toLowerCase().contains("json")) {
            return null;
        }

        JsonPath json = response.jsonPath();
        for (String key : new String[]{"token", "accessToken", "access_token", "jwt", "idToken", "id_token"}) {
            try {
                String value = json.getString(key);
                if (value != null && !value.isBlank() && !"null".equalsIgnoreCase(value)) {
                    return value;
                }
            } catch (Exception ignored) {
                // ignore and try next key
            }
        }
        return null;
    }
}
