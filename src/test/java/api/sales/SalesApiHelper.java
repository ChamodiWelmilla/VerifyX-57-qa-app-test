package api.sales;

import api.auth.AuthApi;
import api.sales.helper.SalesPrereqApi;
import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.ConfigReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public final class SalesApiHelper {

    private SalesApiHelper() {
    }

    public static void ensureSaleExistsForCurrentPlant(int saleQuantity) {
        if (saleQuantity <= 0) {
            throw new IllegalArgumentException("saleQuantity must be > 0");
        }

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.ensurePlantForSale(150.0, 25);
        ApiSession session = AuthApi.loginAsAdmin();
        Response resp = tryCreateSale(session, plant.getPlantId(), saleQuantity);
        assert2xx(resp, "Create sale");
    }

    public static void ensureSalesExistForSorting() {
        ApiSession session = AuthApi.loginAsAdmin();

        SalesPrereqApi.PlantForSale p1 = SalesPrereqApi.createPlantForSale(150.0, 50);
        SalesPrereqApi.PlantForSale p2 = SalesPrereqApi.createPlantForSale(160.0, 50);
        SalesPrereqApi.PlantForSale p3 = SalesPrereqApi.createPlantForSale(170.0, 50);

        assert2xx(tryCreateSale(session, p1.getPlantId(), 1), "Create sale for plant 1");
        assert2xx(tryCreateSale(session, p2.getPlantId(), 3), "Create sale for plant 2");
        assert2xx(tryCreateSale(session, p3.getPlantId(), 2), "Create sale for plant 3");
    }

    public static void deleteAllSalesAsAdmin() {
        ApiSession session = AuthApi.loginAsAdmin();

        Response listResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .get(salesPath());

        assert2xx(listResp, "List sales");

        List<Integer> ids = new ArrayList<>();
        try {
            List<Integer> contentIds = listResp.jsonPath().getList("content.id", Integer.class);
            if (contentIds != null) {
                ids.addAll(contentIds);
            }
        } catch (Exception ignored) {
        }

        if (ids.isEmpty()) {
            try {
                List<Integer> flatIds = listResp.jsonPath().getList("id", Integer.class);
                if (flatIds != null) {
                    ids.addAll(flatIds);
                }
            } catch (Exception ignored) {
            }
        }

        if (ids.isEmpty()) {
            return;
        }

        List<Integer> uniqueIds = ids.stream()
                .filter(i -> i != null && i > 0)
                .distinct()
                .collect(Collectors.toList());

        for (Integer id : uniqueIds) {
            String path = salesPath() + "/" + id;
            Response delResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(session))
                    .delete(path);

            int status = delResp.statusCode();
            if (status < 200 || status >= 300) {
                throw new AssertionError("Delete sale id=" + id + " expected 2xx but got " + status + ". Body: " + safeBody(delResp));
            }
        }
    }

    private static void assert2xx(Response response, String action) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new AssertionError(action + " expected 2xx but got " + status + ". Body: " + response.asString());
        }
    }

    private static String salesPath() {
        return Optional.ofNullable(ConfigReader.get("api.sales.path"))
                .orElse("/api/sales");
    }

    private static Response tryCreateSale(ApiSession session, int plantId, int quantity) {
        List<String> errors = new ArrayList<>();

        try {
            String path = salesPath() + "/plant/" + plantId;
            Response resp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(session))
                    .queryParam("quantity", quantity)
                    .post(path);
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp;
            }
            errors.add("POST " + path + " ?quantity=" + quantity + " -> " + resp.statusCode() + ": " + safeBody(resp));
        } catch (Exception e) {
            errors.add("POST " + salesPath() + "/plant/{id}?quantity=... threw " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("plantId", plantId);
            body.put("quantity", quantity);
            Response resp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(session))
                    .body(body)
                    .post(salesPath());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp;
            }
            errors.add("POST " + salesPath() + " body={plantId,quantity} -> " + resp.statusCode() + ": " + safeBody(resp));
        } catch (Exception e) {
            errors.add("POST " + salesPath() + " body={plantId,quantity} threw " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("plant", Map.of("id", plantId));
            body.put("quantity", quantity);
            Response resp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(session))
                    .body(body)
                    .post(salesPath());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp;
            }
            errors.add("POST " + salesPath() + " body={plant:{id},quantity} -> " + resp.statusCode() + ": " + safeBody(resp));
        } catch (Exception e) {
            errors.add("POST " + salesPath() + " body={plant:{id},quantity} threw " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("quantity", quantity);
            String path = salesPath() + "/plant/" + plantId;
            Response resp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(session))
                    .post(path);
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return resp;
            }
            errors.add("POST " + path + " body=<none> -> " + resp.statusCode() + ": " + safeBody(resp));
        } catch (Exception e) {
            errors.add("POST " + salesPath() + "/plant/{id} body={quantity} threw " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        throw new AssertionError("Unable to create sale via API. Attempts:\n- " + String.join("\n- ", errors));
    }

    private static String safeBody(Response resp) {
        try {
            String body = resp == null ? null : resp.asString();
            if (body == null) {
                return "<null>";
            }
            body = body.replaceAll("\\s+", " ").trim();
            if (body.length() > 300) {
                return body.substring(0, 300) + "...";
            }
            return body;
        } catch (Exception ignored) {
            return "<unreadable>";
        }
    }
}
