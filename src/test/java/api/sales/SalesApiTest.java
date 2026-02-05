package api.sales;

import api.auth.AuthApi;
import api.sales.helper.SalesPrereqApi;
import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

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

    private static int getPlantQuantity(Response plantResp, int plantId) {
        if (plantId <= 0) {
            return 0;
        }

        try {
            return plantResp.jsonPath().getInt("quantity");
        } catch (Exception ignored) {
        }

        try {
            // Try paged list format (GET /api/plants returns content array)
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
}
