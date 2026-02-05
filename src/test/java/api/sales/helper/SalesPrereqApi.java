package api.sales.helper;

import api.auth.AuthApi;
import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Single entry-point helper for creating Sales UI pre-requisites via APIs.
 *
 * Creates: admin session -> parent category -> child category -> plant under child category.
 *
 * Intended usage: call from Cucumber hooks before Sales scenarios, then read the created plant
 * name/price when driving the UI.
 */
public final class SalesPrereqApi {

    private static final ThreadLocal<PlantForSale> CURRENT_PLANT_FOR_SALE = new ThreadLocal<>();

    private SalesPrereqApi() {
    }

    public static PlantForSale getCurrentPlantForSale() {
        return CURRENT_PLANT_FOR_SALE.get();
    }

    public static void clearCurrentPlantForSale() {
        CURRENT_PLANT_FOR_SALE.remove();
    }

    /**
     * Creates a brand-new plant under a fresh category hierarchy and stores it in a ThreadLocal.
     */
    public static PlantForSale createAndStorePlantForSale(double price, int quantity) {
        ApiSession session = AuthApi.loginAsAdmin();

        String parentName = uniqueName("par", 10);
        Response parentResp = createParentCategory(session, parentName);
        assert2xx(parentResp, "Create parent category");
        int parentCategoryId = parentResp.jsonPath().getInt("id");

        String childName = uniqueName("chi", 10);
        Response childResp = createChildCategory(session, childName, parentCategoryId);
        assert2xx(childResp, "Create child category");
        int childCategoryId = childResp.jsonPath().getInt("id");

        String plantName = uniqueName("plt", 25);
        Response plantResp = createPlant(session, plantName, price, quantity, childCategoryId);
        assert2xx(plantResp, "Create plant");
        int plantId = safeGetInt(plantResp, "id");

        PlantForSale created = new PlantForSale(
                plantId,
                plantName,
                price,
                quantity,
                parentCategoryId,
                parentName,
                childCategoryId,
                childName
        );

        CURRENT_PLANT_FOR_SALE.set(created);
        return created;
    }

    /**
     * Creates the plant if not already created for this thread, otherwise returns the existing one.
     */
    public static PlantForSale ensurePlantForSale(double price, int quantity) {
        PlantForSale existing = CURRENT_PLANT_FOR_SALE.get();
        if (existing != null) {
            return existing;
        }
        return createAndStorePlantForSale(price, quantity);
    }

    /**
     * Ensures at least one sale record exists by creating a small sale for the current plant.
     */
    public static void ensureSaleExistsForCurrentPlant(int saleQuantity) {
        if (saleQuantity <= 0) {
            throw new IllegalArgumentException("saleQuantity must be > 0");
        }

        PlantForSale plant = ensurePlantForSale(150.0, 25);
        ApiSession session = AuthApi.loginAsAdmin();
        Response resp = tryCreateSale(session, plant.getPlantId(), saleQuantity);
        assert2xx(resp, "Create sale");
    }

    /**
     * Creates multiple sales across multiple plants so sorting by Plant/Quantity/Total/Sold At is meaningful.
     */
    public static void ensureSalesExistForSorting() {
        ApiSession session = AuthApi.loginAsAdmin();

        PlantForSale p1 = createPlantForSale(session, 150.0, 50);
        PlantForSale p2 = createPlantForSale(session, 160.0, 50);
        PlantForSale p3 = createPlantForSale(session, 170.0, 50);

        assert2xx(tryCreateSale(session, p1.getPlantId(), 1), "Create sale for plant 1");
        assert2xx(tryCreateSale(session, p2.getPlantId(), 3), "Create sale for plant 2");
        assert2xx(tryCreateSale(session, p3.getPlantId(), 2), "Create sale for plant 3");
    }

    /**
     * Deletes all sales records using admin credentials.
     *
     * Contract used:
     * - GET  /api/sales      -> returns a list (or paged list) of sales with "id"
     * - DELETE /api/sales/{id}
     */
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

    public static final class PlantForSale {
        private final int plantId;
        private final String plantName;
        private final double price;
        private final int quantity;

        private final int parentCategoryId;
        private final String parentCategoryName;
        private final int childCategoryId;
        private final String childCategoryName;

        public PlantForSale(
                int plantId,
                String plantName,
                double price,
                int quantity,
                int parentCategoryId,
                String parentCategoryName,
                int childCategoryId,
                String childCategoryName
        ) {
            this.plantId = plantId;
            this.plantName = plantName;
            this.price = price;
            this.quantity = quantity;
            this.parentCategoryId = parentCategoryId;
            this.parentCategoryName = parentCategoryName;
            this.childCategoryId = childCategoryId;
            this.childCategoryName = childCategoryName;
        }

        public int getPlantId() {
            return plantId;
        }

        public String getPlantName() {
            return plantName;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getParentCategoryId() {
            return parentCategoryId;
        }

        public String getParentCategoryName() {
            return parentCategoryName;
        }

        public int getChildCategoryId() {
            return childCategoryId;
        }

        public String getChildCategoryName() {
            return childCategoryName;
        }
    }

    private static void assert2xx(Response response, String action) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new AssertionError(action + " expected 2xx but got " + status + ". Body: " + response.asString());
        }
    }

    private static int safeGetInt(Response response, String path) {
        try {
            return response.jsonPath().getInt(path);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String uniqueName(String prefix, int maxLen) {
        String suffix = Long.toString(System.nanoTime(), 36).toLowerCase(Locale.ROOT);
        String raw = prefix + suffix;

        if (raw.length() > maxLen) {
            raw = raw.substring(0, maxLen);
        }

        if (raw.length() < 3) {
            raw = (raw + "abc").substring(0, 3);
        }

        return raw;
    }

    private static String categoriesPath() {
        return Optional.ofNullable(ConfigReader.get("api.categories.path"))
                .orElse("/api/categories");
    }

    private static String plantsPath() {
        return Optional.ofNullable(ConfigReader.get("api.plants.path"))
                .orElse("/api/plants");
    }

    private static String salesPath() {
        return Optional.ofNullable(ConfigReader.get("api.sales.path"))
                .orElse("/api/sales");
    }

    private static PlantForSale createPlantForSale(ApiSession session, double price, int quantity) {
        String parentName = uniqueName("par", 10);
        Response parentResp = createParentCategory(session, parentName);
        assert2xx(parentResp, "Create parent category");
        int parentCategoryId = parentResp.jsonPath().getInt("id");

        String childName = uniqueName("chi", 10);
        Response childResp = createChildCategory(session, childName, parentCategoryId);
        assert2xx(childResp, "Create child category");
        int childCategoryId = childResp.jsonPath().getInt("id");

        String plantName = uniqueName("plt", 25);
        Response plantResp = createPlant(session, plantName, price, quantity, childCategoryId);
        assert2xx(plantResp, "Create plant");
        int plantId = safeGetInt(plantResp, "id");

        return new PlantForSale(
                plantId,
                plantName,
                price,
                quantity,
                parentCategoryId,
                parentName,
                childCategoryId,
                childName
        );
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

    private static Response createParentCategory(ApiSession session, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);

        return RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .body(body)
                .post(categoriesPath());
    }

    private static Response createChildCategory(ApiSession session, String name, int parentId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("parent", Map.of("id", parentId));

        return RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .body(body)
                .post(categoriesPath());
    }

    private static Response createPlant(ApiSession session, String name, double price, int quantity, int categoryId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", quantity);

        return RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(session))
                .body(body)
                .post(plantsPath() + "/category/" + categoryId);
    }
}
