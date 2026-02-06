package api.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import utils.ConfigReader;

import java.util.Optional;

public class ApiSpecs {

    private static String getBaseUrl() {
        return Optional.ofNullable(ConfigReader.get("api.base-url"))
                .orElse("http://localhost:8080");
    }

    public static RequestSpecification baseJsonSpec() {
        RestAssured.useRelaxedHTTPSValidation();
        return new RequestSpecBuilder()
                .setBaseUri(getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    public static RequestSpecification authedJsonSpec(ApiSession session) {
        CookieFilter cookieFilter = session == null ? new CookieFilter() : session.getCookieFilter();
        RequestSpecification spec = baseJsonSpec().filter(cookieFilter);

        if (session != null && session.hasBearerToken()) {
            spec.header("Authorization", "Bearer " + session.getBearerToken());
        }

        return spec;
    }
}