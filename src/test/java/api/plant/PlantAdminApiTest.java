package api.plant;

import api.auth.AuthApi;
import api.support.ApiSession;
import api.support.ApiSpecs;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class PlantAdminApiTest {

    private ApiSession adminSession;
    private int createdPlantId;

    @BeforeClass
    public void setup() {
        // Authenticate as Admin 
        adminSession = AuthApi.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );
    }

    @Test(priority = 1)
    public void TC_PLANTAPI_AD_01_createPlantSuccessfully() {
        String plantName = "new plant" + System.currentTimeMillis();
        
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("name", plantName);
        plantData.put("price", 25.00);
        plantData.put("quantity", 15);

        int categoryId = 2;

        Response response = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .body(plantData)
        .when()
                .post("/api/plants/category/" + categoryId);

        Assert.assertEquals(response.statusCode(), 201, "Expected 201 Created. Body: " + response.asString());
        
        createdPlantId = response.jsonPath().getInt("id");
        Assert.assertTrue(createdPlantId > 0, "Expected a valid ID in the response");
        Assert.assertEquals(response.jsonPath().getString("name"), plantName);
    }

    @Test(priority = 2, dependsOnMethods = "TC_PLANTAPI_AD_01_createPlantSuccessfully")
    public void TC_PLANTAPI_AD_02_updatePlantDetails() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Updated Plant Name");
        updateData.put("price", 35.50);
        updateData.put("quantity", 50);

        Response response = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .body(updateData)
        .when()
                .put("/api/plants/" + createdPlantId);

        Assert.assertEquals(response.statusCode(), 200, "Expected 200 OK. Body: " + response.asString());
        Assert.assertEquals(response.jsonPath().getFloat("price"), 35.50f);
        Assert.assertEquals(response.jsonPath().getInt("quantity"), 50);
    }

    @Test(priority = 3)
    public void TC_PLANTAPI_AD_04_verifyNegativePriceValidation() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("name", "Negative Price Plant");
        invalidData.put("price", -5.00);
        invalidData.put("quantity", 10);

        Response response = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .body(invalidData)
        .when()
                .post("/api/plants/category/2");

        // Price must be greater than 0
        Assert.assertEquals(response.statusCode(), 400, "Expected 400 Bad Request for negative price");
        Assert.assertTrue(response.asString().contains("Price must be greater than 0"), 
                "Error message should indicate price validation failure");
    }

    @Test(priority = 4)
    public void TC_PLANTAPI_AD_05_verifyShortNameValidation() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("name", "Pl"); // Only 2 characters
        invalidData.put("price", 10.00);
        invalidData.put("quantity", 10);

        Response response = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
                .body(invalidData)
        .when()
                .post("/api/plants/category/2");

        // Plant name must be between 3 and 25 characters
        Assert.assertEquals(response.statusCode(), 400, "Expected 400 Bad Request for short name");
        Assert.assertTrue(response.asString().contains("between 3 and 25 characters"), 
                "Error message should indicate name length validation failure");
    }

    @Test(priority = 5, dependsOnMethods = "TC_PLANTAPI_AD_01_createPlantSuccessfully")
    public void TC_PLANTAPI_AD_03_deletePlantSuccessfully() {
        Response deleteResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
        .when()
                .delete("/api/plants/" + createdPlantId);

        int status = deleteResp.statusCode();
        Assert.assertTrue(status == 200 || status == 204, "Expected 200 or 204. Got: " + status);

        // Verify the plant is deleted
        Response checkResp = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(adminSession))
        .when()
                .get("/api/plants/" + createdPlantId);

        Assert.assertEquals(checkResp.statusCode(), 404, "Plant should return 404 after deletion");
    }
}