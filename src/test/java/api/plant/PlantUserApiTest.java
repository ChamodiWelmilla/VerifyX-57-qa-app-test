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

import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

public class PlantUserApiTest {

    private ApiSession userSession;
    private int dynamicPlantId;

@BeforeClass
public void setup() {
    userSession = AuthApi.login(
            ConfigReader.get("user.username"),
            ConfigReader.get("user.password")
    );

    // Fetch a valid plant ID 
    Response response = RestAssured.given()
            .spec(ApiSpecs.authedJsonSpec(userSession))
            .get("/api/plants");
    
    // Get the first ID from the list
    dynamicPlantId = response.jsonPath().getInt("[0].id"); 
    
}

    @Test
    public void TC_PLANTAPI_USR_01_userCanGetAllPlants() {
        Response response = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
        .when()
                .get("/api/plants");

        Assert.assertEquals(response.statusCode(), 200, "Users should be able to view all plants");
    }

    @Test
    public void TC_PLANTAPI_USR_02_userCanGetPlantById() {
        // Using a known existing plant ID 
        Response response = RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
        .when()
                .get("/api/plants/"+ dynamicPlantId);

        Assert.assertEquals(response.statusCode(), 200);
    }

    @Test
    public void TC_PLANTAPI_USR_03_userForbiddenToCreatePlant() {
        // Prepare valid data that would normally pass validation
        Map<String, Object> validPlantData = new HashMap<>();
        validPlantData.put("name", "Test Plant");
        validPlantData.put("price", 10.00);
        validPlantData.put("quantity", 10);

        Assert.assertEquals(
            RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .body(validPlantData) 
            .when()
                .post("/api/plants/category/2")
            .then()
                .extract().statusCode(), 403, "Users must be Forbidden (403) from creating plants");
    }

    @Test
    public void TC_PLANTAPI_USR_04_userForbiddenToUpdatePlant() {
        Map<String, Object> validUpdateData = new HashMap<>();
        validUpdateData.put("name", "Update Attempt");
        validUpdateData.put("price", 15.00);
        validUpdateData.put("quantity", 5);

        Assert.assertEquals(
            RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
                .body(validUpdateData) 
            .when()
                .put("/api/plants/"+dynamicPlantId)
            .then()
                .extract().statusCode(), 403, "Users must be Forbidden (403) from updating plants");
    }

    @Test
    public void TC_PLANTAPI_USR_05_userForbiddenToDeletePlant() {
        Assert.assertEquals(
            RestAssured.given()
                .spec(ApiSpecs.authedJsonSpec(userSession))
            .when()
                .delete("/api/plants/"+dynamicPlantId)
            .then()
                .extract().statusCode(), 403, "Users must be Forbidden (403) from deleting plants");
    }
}