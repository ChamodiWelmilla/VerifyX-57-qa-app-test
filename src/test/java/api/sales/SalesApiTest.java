package api.sales;

import api.auth.AuthApi;
import api.sales.helper.SalesPrereqApi;
import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * API tests for Sales management.
 */
public class SalesApiTest {

    private static final boolean CLEANUP_SALES = Boolean.parseBoolean(
            System.getProperty("api.cleanup.sales", "true")
    );

    @Test
    public void verifyAdminCreatesNewSaleSuccessfully() {
        ApiSession adminSession = AuthApi.loginAsAdmin();

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, 25);
        Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

        int createdSaleId = 0;
        Response createSaleResp = null;

        try {
            createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .queryParam("quantity", 1)
                    .post("/api/sales/plant/" + plant.getPlantId());

            Assert.assertEquals(
                    createSaleResp.statusCode(),
                    201,
                    "Expected status code 201. Body: " + safeBody(createSaleResp)
            );

            createdSaleId = tryGetInt(createSaleResp, "id");
            Assert.assertTrue(createdSaleId > 0, "Expected created sale to include a valid id. Body: " + safeBody(createSaleResp));

            System.out.println("Created sale id=" + createdSaleId + " for plantId=" + plant.getPlantId());

            Response listResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/sales");

            Assert.assertTrue(
                    listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                    "Expected sales list to return 2xx. Got " + listResp.statusCode() + ". Body: " + safeBody(listResp)
            );

            Assert.assertTrue(
                    salesListContainsId(listResp, createdSaleId),
                    "Expected sales list to contain created id=" + createdSaleId + ". Body: " + safeBody(listResp)
            );

        } finally {
            if (CLEANUP_SALES && createdSaleId > 0) {
                Response delResp = RestAssured.given()
                        .spec(ApiSpecs.authedJsonSpec(adminSession))
                        .delete("/api/sales/" + createdSaleId);

                int status = delResp.statusCode();
                if (status < 200 || status >= 300) {
                    System.err.println("WARN: cleanup failed deleting sale id=" + createdSaleId + ". Status=" + status + ", body=" + safeBody(delResp));
                }
            }

            SalesPrereqApi.clearCurrentPlantForSale();
        }
    }

    @Test
    public void verifyAdminDeletesSaleSuccessfully() {
        ApiSession adminSession = AuthApi.loginAsAdmin();

        int saleId = Integer.parseInt(System.getProperty("api.saleId", "0"));

        if (saleId <= 0) {
            SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, 25);
            Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

            Response createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .queryParam("quantity", 1)
                    .post("/api/sales/plant/" + plant.getPlantId());

            Assert.assertEquals(
                    createSaleResp.statusCode(),
                    201,
                    "Expected sale creation status code 201. Body: " + safeBody(createSaleResp)
            );

            saleId = tryGetInt(createSaleResp, "id");
            Assert.assertTrue(saleId > 0, "Expected created sale to include a valid id. Body: " + safeBody(createSaleResp));
        }

        System.out.println("Deleting sale id=" + saleId);

        Response deleteResp = RestAssured.given()
            .spec(ApiSpecs.authedJsonSpec(adminSession))
            .delete("/api/sales/" + saleId);

        int deleteStatus = deleteResp.statusCode();

        Assert.assertTrue(
            deleteStatus == 200 || deleteStatus == 204,
            "Expected delete status code 200 or 204 but got " + deleteStatus + ". Body: " + safeBody(deleteResp)
        );

        Response listResp = RestAssured.given()
            .spec(ApiSpecs.authedJsonSpec(adminSession))
            .get("/api/sales");

        Assert.assertTrue(
            listResp.statusCode() >= 200 && listResp.statusCode() < 300,
            "Expected sales list to return 2xx. Got " + listResp.statusCode() + ". Body: " + safeBody(listResp)
        );

        Assert.assertFalse(
            salesListContainsId(listResp, saleId),
            "Expected deleted sale id=" + saleId + " to be absent from sales list. Body: " + safeBody(listResp)
        );

        SalesPrereqApi.clearCurrentPlantForSale();
    }

    @Test
    public void verifyAdminIsPreventedFromCreatingSaleExceedingRemainingStock() {
        ApiSession adminSession = AuthApi.loginAsAdmin();

        int remainingStock = 1;
        int requestedQuantity = remainingStock + 1;

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, remainingStock);
        Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

        try {
            Response createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .queryParam("quantity", requestedQuantity)
                    .post("/api/sales/plant/" + plant.getPlantId());

            int status = createSaleResp.statusCode();
            Assert.assertTrue(
                    status >= 400 && status < 500,
                    "Expected a 4xx client error (e.g., 400) when sale quantity exceeds stock. Got " + status + ". Body: " + safeBody(createSaleResp)
            );

            Response listResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/sales");

            Assert.assertTrue(
                    listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                    "Expected sales list to return 2xx. Got " + listResp.statusCode() + ". Body: " + safeBody(listResp)
            );

            Assert.assertFalse(
                    salesListContainsPlantId(listResp, plant.getPlantId()),
                    "Expected no sale record to be created for plantId=" + plant.getPlantId() + " when quantity exceeds stock. Body: " + safeBody(listResp)
            );
        } finally {
            SalesPrereqApi.clearCurrentPlantForSale();
        }
    }

    @Test
    public void verifyAdminUpdatesRemainingStockAfterCreatingSale() {
        ApiSession adminSession = AuthApi.loginAsAdmin();

        int initialStock = 20;
        int saleQuantity = 5;

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, initialStock);
        Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

        try {
            Response createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .queryParam("quantity", saleQuantity)
                    .post("/api/sales/plant/" + plant.getPlantId());

            Assert.assertEquals(
                    createSaleResp.statusCode(),
                    201,
                    "Expected sale creation to return 201. Body: " + safeBody(createSaleResp)
            );

            int createdSaleId = tryGetInt(createSaleResp, "id");
            Assert.assertTrue(createdSaleId > 0, "Expected created sale to include a valid id. Body: " + safeBody(createSaleResp));

            System.out.println("Created sale id=" + createdSaleId + " with quantity=" + saleQuantity + " for plantId=" + plant.getPlantId());

            Response getPlantResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/plants/" + plant.getPlantId());

            if (getPlantResp.statusCode() == 404) {
                getPlantResp = RestAssured.given()
                        .spec(ApiSpecs.authedJsonSpec(adminSession))
                        .get("/api/plants");
            }

            Assert.assertTrue(
                    getPlantResp.statusCode() >= 200 && getPlantResp.statusCode() < 300,
                    "Expected plant fetch to return 2xx. Got " + getPlantResp.statusCode() + ". Body: " + safeBody(getPlantResp)
            );

            int remainingStock = getPlantQuantity(getPlantResp, plant.getPlantId());
            int expectedRemainingStock = initialStock - saleQuantity;

            Assert.assertEquals(
                    remainingStock,
                    expectedRemainingStock,
                    "Expected remaining stock to be " + expectedRemainingStock + " after selling " + saleQuantity + " units. Got " + remainingStock
            );

            System.out.println("Plant stock correctly updated: " + initialStock + " - " + saleQuantity + " = " + remainingStock);

        } finally {
            SalesPrereqApi.clearCurrentPlantForSale();
        }
    }

    @Test
    public void verifyAdminUpdatesRemainingStockAfterDeletingSale() {
        ApiSession adminSession = AuthApi.loginAsAdmin();

        int initialStock = 20;
        int saleQuantity = 5;

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, initialStock);
        Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

        try {
            Response createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .queryParam("quantity", saleQuantity)
                    .post("/api/sales/plant/" + plant.getPlantId());

            Assert.assertEquals(
                    createSaleResp.statusCode(),
                    201,
                    "Expected sale creation to return 201. Body: " + safeBody(createSaleResp)
            );

            int createdSaleId = tryGetInt(createSaleResp, "id");
            Assert.assertTrue(createdSaleId > 0, "Expected created sale to include a valid id. Body: " + safeBody(createSaleResp));

            System.out.println("Created sale id=" + createdSaleId + " with quantity=" + saleQuantity + " for plantId=" + plant.getPlantId());

            Response getPlantAfterSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/plants/" + plant.getPlantId());

            if (getPlantAfterSaleResp.statusCode() == 404) {
                getPlantAfterSaleResp = RestAssured.given()
                        .spec(ApiSpecs.authedJsonSpec(adminSession))
                        .get("/api/plants");
            }

            Assert.assertTrue(
                    getPlantAfterSaleResp.statusCode() >= 200 && getPlantAfterSaleResp.statusCode() < 300,
                    "Expected plant fetch to return 2xx. Got " + getPlantAfterSaleResp.statusCode() + ". Body: " + safeBody(getPlantAfterSaleResp)
            );

            int stockAfterSale = getPlantQuantity(getPlantAfterSaleResp, plant.getPlantId());
            int expectedStockAfterSale = initialStock - saleQuantity;

            Assert.assertEquals(
                    stockAfterSale,
                    expectedStockAfterSale,
                    "Expected stock to be " + expectedStockAfterSale + " after creating sale with quantity " + saleQuantity + ". Got " + stockAfterSale
            );

            System.out.println("Stock after sale creation: " + initialStock + " - " + saleQuantity + " = " + stockAfterSale);

            Response deleteResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .delete("/api/sales/" + createdSaleId);

            int deleteStatus = deleteResp.statusCode();
            Assert.assertTrue(
                    deleteStatus == 200 || deleteStatus == 204,
                    "Expected delete status code 200 or 204 but got " + deleteStatus + ". Body: " + safeBody(deleteResp)
            );

            System.out.println("Deleted sale id=" + createdSaleId);

            Response getPlantAfterDeleteResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/plants/" + plant.getPlantId());

            if (getPlantAfterDeleteResp.statusCode() == 404) {
                getPlantAfterDeleteResp = RestAssured.given()
                        .spec(ApiSpecs.authedJsonSpec(adminSession))
                        .get("/api/plants");
            }

            Assert.assertTrue(
                    getPlantAfterDeleteResp.statusCode() >= 200 && getPlantAfterDeleteResp.statusCode() < 300,
                    "Expected plant fetch to return 2xx. Got " + getPlantAfterDeleteResp.statusCode() + ". Body: " + safeBody(getPlantAfterDeleteResp)
            );

            int stockAfterDelete = getPlantQuantity(getPlantAfterDeleteResp, plant.getPlantId());

            Assert.assertEquals(
                    stockAfterDelete,
                    initialStock,
                    "Expected remaining stock to be restored to " + initialStock + " after deleting sale. Got " + stockAfterDelete
            );

            System.out.println("Plant stock correctly restored after deletion: " + stockAfterDelete + " (initial stock)");

        } finally {
            SalesPrereqApi.clearCurrentPlantForSale();
        }
    }

    @Test
    public void verifyTestUserRetrievesSalesList() {
        ApiSession userSession = AuthApi.loginAsUser();

        Response salesListResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .get("/api/sales");

        Assert.assertEquals(
                salesListResp.statusCode(),
                200,
                "Expected sales list to return status code 200. Got " + salesListResp.statusCode() + ". Body: " + safeBody(salesListResp)
        );

        try {
            List<Integer> saleIds = salesListResp.jsonPath().getList("id", Integer.class);
            if (saleIds == null) {
                saleIds = salesListResp.jsonPath().getList("content.id", Integer.class);
            }

            Assert.assertNotNull(
                    saleIds,
                    "Expected sales list response to contain sales records. Body: " + safeBody(salesListResp)
            );

            System.out.println("Test user successfully retrieved sales list with " + saleIds.size() + " records");

        } catch (Exception e) {
            Assert.fail("Expected sales list to be properly formatted. Error: " + e.getMessage() + ". Body: " + safeBody(salesListResp));
        }
    }

        @Test
        public void verifyTestUserIsForbiddenFromCreatingSale() {
            SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, 10);
            Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

            try {
                ApiSession userSession = AuthApi.loginAsUser();

                Response createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(userSession))
                    .queryParam("quantity", 1)
                    .post("/api/sales/plant/" + plant.getPlantId());

                int status = createSaleResp.statusCode();
                Assert.assertTrue(
                    status >= 400 && status < 500,
                    "Expected 4xx client error when test user creates a sale. Got " + status + ". Body: " + safeBody(createSaleResp)
                );

                Response listResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(userSession))
                    .get("/api/sales");

                Assert.assertTrue(
                    listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                    "Expected sales list to return 2xx. Got " + listResp.statusCode() + ". Body: " + safeBody(listResp)
                );

                Assert.assertFalse(
                    salesListContainsPlantId(listResp, plant.getPlantId()),
                    "Expected no sale to be created for plantId=" + plant.getPlantId() + ". Body: " + safeBody(listResp)
                );
            } finally {
                SalesPrereqApi.clearCurrentPlantForSale();
            }
        }

            @Test
            public void verifyTestUserIsForbiddenFromDeletingSale() {
            ApiSession adminSession = AuthApi.loginAsAdmin();

            SalesPrereqApi.PlantForSale plant = SalesPrereqApi.createAndStorePlantForSale(150.0, 10);
            Assert.assertTrue(plant.getPlantId() > 0, "Expected created plant to have a valid id");

            int createdSaleId = 0;

            try {
                Response createSaleResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .queryParam("quantity", 1)
                    .post("/api/sales/plant/" + plant.getPlantId());

                Assert.assertEquals(
                    createSaleResp.statusCode(),
                    201,
                    "Expected sale creation status code 201. Body: " + safeBody(createSaleResp)
                );

                createdSaleId = tryGetInt(createSaleResp, "id");
                Assert.assertTrue(createdSaleId > 0, "Expected created sale to include a valid id. Body: " + safeBody(createSaleResp));

                ApiSession userSession = AuthApi.loginAsUser();

                Response deleteResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(userSession))
                    .delete("/api/sales/" + createdSaleId);

                int status = deleteResp.statusCode();
                Assert.assertTrue(
                    status >= 400 && status < 500,
                    "Expected 4xx client error when test user deletes a sale. Got " + status + ". Body: " + safeBody(deleteResp)
                );

                Response listResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .get("/api/sales");

                Assert.assertTrue(
                    listResp.statusCode() >= 200 && listResp.statusCode() < 300,
                    "Expected sales list to return 2xx. Got " + listResp.statusCode() + ". Body: " + safeBody(listResp)
                );

                Assert.assertTrue(
                    salesListContainsId(listResp, createdSaleId),
                    "Expected sale id=" + createdSaleId + " to remain after forbidden delete. Body: " + safeBody(listResp)
                );
            } finally {
                if (createdSaleId > 0) {
                Response delResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(adminSession))
                    .delete("/api/sales/" + createdSaleId);

                int status = delResp.statusCode();
                if (status < 200 || status >= 300) {
                    System.err.println("WARN: cleanup failed deleting sale id=" + createdSaleId + ". Status=" + status + ", body=" + safeBody(delResp));
                }
                }

                SalesPrereqApi.clearCurrentPlantForSale();
            }
            }

        @DataProvider(name = "salesSortFields")
        public Object[][] salesSortFields() {
            return new Object[][]{
                    {"plant.name", "Plant"},
                    {"totalPrice", "Total Price"},
                    {"quantity", "Quantity"},
                    {"soldAt", "Sold At"}
            };
        }

        @Test(dataProvider = "salesSortFields")
        public void verifyTestUserRetrievesSalesListSortedBy(String sortField, String label) {
            SalesPrereqApi.ensureSalesExistForSorting();

            ApiSession userSession = AuthApi.loginAsUser();

            Response listResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(userSession))
                    .queryParam("sort", sortField + ",asc")
                    .get("/api/sales");

            Assert.assertEquals(
                    listResp.statusCode(),
                    200,
                    "Expected sales list to return status code 200. Got " + listResp.statusCode() + ". Body: " + safeBody(listResp)
            );

            switch (sortField) {
                case "plant.name":
                    assertSortedStrings(listResp, sortField, label);
                    break;
                case "quantity":
                    assertSortedIntegers(listResp, sortField, label);
                    break;
                case "totalPrice":
                    assertSortedDoubles(listResp, sortField, label);
                    break;
                case "soldAt":
                    assertSortedInstants(listResp, sortField, label);
                    break;
                default:
                    Assert.fail("Unsupported sort field: " + sortField);
            }
        }

        @Test
        public void verifyTestUserCannotRetrieveNonExistingSale() {
            ApiSession userSession = AuthApi.loginAsUser();

            Response listResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(userSession))
                    .get("/api/sales");

            Assert.assertEquals(
                    listResp.statusCode(),
                    200,
                    "Expected sales list to return status code 200. Body: " + safeBody(listResp)
            );

            List<Integer> existingIds = new ArrayList<>();
            try {
                List<Integer> pagedIds = listResp.jsonPath().getList("content.id", Integer.class);
                if (pagedIds != null) {
                    existingIds.addAll(pagedIds);
                }
            } catch (Exception ignored) {
            }

            if (existingIds.isEmpty()) {
                try {
                    List<Integer> flatIds = listResp.jsonPath().getList("id", Integer.class);
                    if (flatIds != null) {
                        existingIds.addAll(flatIds);
                    }
                } catch (Exception ignored) {
                }
            }

            int nonExistingId = generateNonExistingId(existingIds);

            Response detailResp = RestAssured.given()
                    .spec(ApiSpecs.authedJsonSpec(userSession))
                    .get("/api/sales/" + nonExistingId);

            Assert.assertEquals(
                    detailResp.statusCode(),
                    404,
                    "Expected status code 404 when retrieving non-existing sale id=" + nonExistingId + ". Got " + detailResp.statusCode() + ". Body: " + safeBody(detailResp)
            );
        }

    private static boolean salesListContainsId(Response listResp, int expectedId) {
        if (expectedId <= 0) {
            return false;
        }

        try {
            List<Integer> paged = listResp.jsonPath().getList("content.id", Integer.class);
            if (paged != null && paged.contains(expectedId)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            List<Integer> flat = listResp.jsonPath().getList("id", Integer.class);
            return flat != null && flat.contains(expectedId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean salesListContainsPlantId(Response listResp, int plantId) {
        if (plantId <= 0) {
            return false;
        }

        try {
            List<Integer> paged = listResp.jsonPath().getList("content.plant.id", Integer.class);
            if (paged != null && paged.contains(plantId)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            List<Integer> flat = listResp.jsonPath().getList("plant.id", Integer.class);
            return flat != null && flat.contains(plantId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void assertSortedStrings(Response listResp, String fieldPath, String label) {
        List<String> values = getSalesStringValues(listResp, fieldPath);

        Comparator<String> comparator = Comparator.comparing(SalesApiTest::safeLower);
        Assert.assertTrue(
                isSorted(values, comparator),
                "Expected sales list to be sorted by " + label + " (asc). Values: " + values
        );
    }

    private static void assertSortedIntegers(Response listResp, String fieldPath, String label) {
        List<Integer> values = getSalesIntegerValues(listResp, fieldPath);

        Comparator<Integer> comparator = Comparator.nullsFirst(Integer::compareTo);
        Assert.assertTrue(
                isSorted(values, comparator),
                "Expected sales list to be sorted by " + label + " (asc). Values: " + values
        );
    }

    private static void assertSortedDoubles(Response listResp, String fieldPath, String label) {
        List<Double> values = getSalesDoubleValues(listResp, fieldPath);

        Comparator<Double> comparator = Comparator.nullsFirst(Double::compareTo);
        Assert.assertTrue(
                isSorted(values, comparator),
                "Expected sales list to be sorted by " + label + " (asc). Values: " + values
        );
    }

    private static void assertSortedInstants(Response listResp, String fieldPath, String label) {
        List<String> rawValues = getSalesStringValues(listResp, fieldPath);

        List<Instant> values = new ArrayList<>();
        for (String raw : rawValues) {
            if (raw == null || raw.isBlank()) {
                values.add(Instant.MIN);
            } else {
                try {
                    values.add(Instant.parse(raw));
                } catch (Exception e) {
                    Assert.fail("Expected 'soldAt' to be ISO-8601 timestamp but got: " + raw + ". Body: " + safeBody(listResp));
                }
            }
        }

        Comparator<Instant> comparator = Comparator.nullsFirst(Instant::compareTo);
        Assert.assertTrue(
                isSorted(values, comparator),
                "Expected sales list to be sorted by " + label + " (asc). Values: " + rawValues
        );
    }

    private static List<String> getSalesStringValues(Response listResp, String fieldPath) {
        List<String> values = tryGetList(listResp, "content." + fieldPath, String.class);
        if (values == null) {
            values = tryGetList(listResp, fieldPath, String.class);
        }
        return values == null ? new ArrayList<>() : values;
    }

    private static List<Integer> getSalesIntegerValues(Response listResp, String fieldPath) {
        List<Number> values = tryGetList(listResp, "content." + fieldPath, Number.class);
        if (values == null) {
            values = tryGetList(listResp, fieldPath, Number.class);
        }

        if (values == null) {
            return new ArrayList<>();
        }

        List<Integer> result = new ArrayList<>();
        for (Number value : values) {
            result.add(value == null ? null : value.intValue());
        }
        return result;
    }

    private static List<Double> getSalesDoubleValues(Response listResp, String fieldPath) {
        List<Number> values = tryGetList(listResp, "content." + fieldPath, Number.class);
        if (values == null) {
            values = tryGetList(listResp, fieldPath, Number.class);
        }

        if (values == null) {
            return new ArrayList<>();
        }

        List<Double> result = new ArrayList<>();
        for (Number value : values) {
            result.add(value == null ? null : value.doubleValue());
        }
        return result;
    }

    private static <T> List<T> tryGetList(Response listResp, String path, Class<T> type) {
        String[] candidates = new String[]{
                path,
                "$." + path,
                "[*]." + path,
                "$[*]." + path
        };

        for (String candidate : candidates) {
            try {
                List<T> values = listResp.jsonPath().getList(candidate, type);
                if (values != null) {
                    return values;
                }
            } catch (Exception ignored) {
            }
        }

        for (String candidate : candidates) {
            try {
                List<T> values = JsonPath.from(listResp.asString()).getList(candidate, type);
                if (values != null) {
                    return values;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private static <T> boolean isSorted(List<T> values, Comparator<T> comparator) {
        if (values == null || values.size() < 2) {
            return true;
        }
        for (int i = 1; i < values.size(); i++) {
            T prev = values.get(i - 1);
            T curr = values.get(i);
            if (comparator.compare(prev, curr) > 0) {
                return false;
            }
        }
        return true;
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private static int getPlantQuantity(Response plantResp, int plantId) {
        if (plantId <= 0) {
            return 0;
        }

        try {
            return plantResp.jsonPath().getInt("quantity");
        } catch (Exception ignored) {
        }

        try {
            List<Integer> ids = plantResp.jsonPath().getList("content.id", Integer.class);
            List<Integer> quantities = plantResp.jsonPath().getList("content.quantity", Integer.class);
            if (ids != null && quantities != null) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) != null && ids.get(i) == plantId && quantities.get(i) != null) {
                        return quantities.get(i);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            List<Integer> ids = plantResp.jsonPath().getList("id", Integer.class);
            List<Integer> quantities = plantResp.jsonPath().getList("quantity", Integer.class);
            if (ids != null && quantities != null) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) != null && ids.get(i) == plantId && quantities.get(i) != null) {
                        return quantities.get(i);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return 0;
    }

    private static int tryGetInt(Response response, String path) {
        try {
            JsonPath json = response.jsonPath();
            return json.getInt(path);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String safeBody(Response resp) {
        try {
            String body = resp == null ? null : resp.asString();
            if (body == null) {
                return "<null>";
            }
            body = body.replaceAll("\\s+", " ").trim();
            if (body.length() > 500) {
                return body.substring(0, 500) + "...";
            }
            return body;
        } catch (Exception ignored) {
            return "<unreadable>";
        }
    }

    private static int generateNonExistingId(List<Integer> existingIds) {
        if (existingIds == null || existingIds.isEmpty()) {
            return 99999;
        }

        int maxId = existingIds.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        int candidate = maxId + 1;
        if (!existingIds.contains(candidate)) {
            return candidate;
        }

        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 100; i++) {
            candidate = 10000 + rand.nextInt(90000);
            if (!existingIds.contains(candidate)) {
                return candidate;
            }
        }

        return 999999;
    }
}
