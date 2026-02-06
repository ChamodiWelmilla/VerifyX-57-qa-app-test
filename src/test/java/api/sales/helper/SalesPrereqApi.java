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

/**
 * Single entry-point helper for creating Sales UI pre-requisites via APIs.
 *
 * Creates: admin session -> parent category -> child category -> plant under child category.
 * 
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
     * Creates a brand-new plant under a fresh category.
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
     * Creates a new plant under a category.
     */
    public static PlantForSale createPlantForSale(double price, int quantity) {
        ApiSession session = AuthApi.loginAsAdmin();
        return createPlantForSale(session, price, quantity);
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
