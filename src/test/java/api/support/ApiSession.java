package api.support;

import io.restassured.filter.cookie.CookieFilter;

/**
 * Holds auth state (token and/or cookies) for API calls.
 */
public class ApiSession {

    private final CookieFilter cookieFilter;
    private final String bearerToken;

    private ApiSession(CookieFilter cookieFilter, String bearerToken) {
        this.cookieFilter = cookieFilter;
        this.bearerToken = bearerToken;
    }

    public static ApiSession unauthenticated() {
        return new ApiSession(new CookieFilter(), null);
    }

    public static ApiSession authenticated(CookieFilter cookieFilter, String bearerToken) {
        return new ApiSession(cookieFilter == null ? new CookieFilter() : cookieFilter, bearerToken);
    }

    public CookieFilter getCookieFilter() {
        return cookieFilter;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public boolean hasBearerToken() {
        return bearerToken != null && !bearerToken.isBlank();
    }
}
