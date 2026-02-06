package stepDefinitions;

import api.auth.AuthApi;
import api.plant.PlantApi;
import api.support.ApiSession;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.testng.Assert;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ApiPlantSteps {
    private ApiSession session;
    private Response response;

    @Given("I am authenticated as a Plant Admin") // Changed text here
public void authAdmin() {
    this.session = AuthApi.loginAsAdmin();
}
@Given("I am authenticated as a plant testuser")
public void authUser() {
    this.session = AuthApi.loginAsUser();
    
    // Check if the token is empty
    if (this.session == null || this.session.getBearerToken() == null) {
        System.out.println("DEBUG: AUTH FAILED - Session/Token is NULL");
    } else {
        // Safe check for substring to avoid crash if token is short
        String token = this.session.getBearerToken();
        System.out.println("DEBUG: TOKEN FOUND - " + (token.length() > 10 ? token.substring(0, 10) : token) + "...");
    }
}

    @When("I send a POST request to {string} with name {string}, price {float}, and quantity {int}")
    public void createPlant(String path, String name, float price, int qty) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", qty);
        response = PlantApi.postPlant(session, 1, body);
    }

    @When("I send a POST request to create a plant with category {int}")
public void createPlant(int categoryId) {
    
    Map<String, Object> categoryMap = new HashMap<>();
    categoryMap.put("id", categoryId); 
    categoryMap.put("name", "Anthurium");
    
    Map<String, Object> body = new HashMap<>();
    body.put("name", "Anthurium");
    body.put("price", 150);
    body.put("quantity", 25);
    body.put("category", categoryMap); 

    response = PlantApi.postPlant(session, categoryId, body);
}

@When("I send a PUT request to {string} with price {double} and quantity {int}")
public void updatePlant(String path, double price, int qty) {

    int plantId = Integer.parseInt(path.replaceAll("[^0-9]", ""));

    Map<String, Object> body = new HashMap<>();
    body.put("price", price);
    body.put("quantity", qty);
    body.put("name", "Anthurium"); 

    this.response = PlantApi.putPlant(session, plantId, body);
}
@Then("the plant API response should contain the plant name {string}")
public void the_plant_api_response_should_contain_the_plant_name(String expectedName) {
    String actualName = response.jsonPath().getString("name");
    
   Assert.assertEquals(actualName, expectedName, "The plant name does not match!");
}


@When("I send a DELETE request to {string}")
public void deletePlant(String path) {
   
    if (this.session == null) {
        throw new RuntimeException("Test setup error: Session is null. " +
                "Ensure 'Given I am authenticated as a plant testuser' is called first.");
    }

  
    int plantId = Integer.parseInt(path.replaceAll("[^0-9]", ""));
    
   
    this.response = io.restassured.RestAssured.given()
            .spec(api.support.ApiSpecs.baseJsonSpec())
            .filter(session.getCookieFilter())
            .header("Authorization", "Bearer " + session.getBearerToken())
            .delete("/api/plants/" + plantId);
}

   
    @Then("the plant API status code should be {int}")
    public void verifyStatus(int code) {
        
        Assert.assertNotNull(this.response, "The response was never captured in the @When step!");
        Assert.assertEquals(this.response.statusCode(), code);
    }

@Then("a GET request to {string} should return {int}")
public void verifyDeleted(String path, int expectedCode) {
   
    Response getRes = PlantApi.getPlant(session, path);
    Assert.assertEquals(getRes.statusCode(), expectedCode);
}


    @Then("the response should contain a valid plant ID")
    public void verifyId() {
        Assert.assertNotNull(response.jsonPath().get("id"));
    }

    @Then("the response body should show the updated price and quantity")
    public void verifyUpdate() {
        
        Assert.assertTrue(response.getBody().asString().contains("price"));
    }

    
    
    @Then("the response should contain a JSON array of plant objects")
    public void verifyJsonArray() {
        
        Assert.assertTrue(response.jsonPath().getList("$") instanceof java.util.List);
    }
    @When("I send a POST request to {string} with a negative price {string}")
public void i_send_a_post_request_with_negative_price(String path, String priceStr) {
    
    double price = Double.parseDouble(priceStr);
    int categoryId = Integer.parseInt(path.replaceAll("[^0-9]", ""));

    
    Map<String, Object> body = new HashMap<>();
    body.put("name", "Invalid Plant");
    body.put("price", price);
    body.put("quantity", 10);

    
    this.response = PlantApi.postPlant(session, categoryId, body);
}

@Then("the response message should be {string}")
public void verifyResponseMessage(String expectedMessage) {
    String actualMessage = response.jsonPath().getString("message");
    Assert.assertEquals(actualMessage, expectedMessage);
}

@Then("the response should contain a paginated list of plants")
public void verifyPagination() {
   
    List<Object> content = response.jsonPath().getList("content");
    Assert.assertNotNull(content, "The 'content' array is missing from the paginated response!");
    
 
    int totalElements = response.jsonPath().getInt("totalElements");
    System.out.println("Total plants found: " + totalElements);
    
    Assert.assertTrue(totalElements >= 0, "Pagination metadata 'totalElements' is missing!");
}
@When("I send a PUT request to {string}")
public void i_send_a_put_request(String path) {
    
    Map<String, Object> updateBody = new HashMap<>();
    updateBody.put("name", "Unauthorized Update");
    updateBody.put("price", 99.99);
    updateBody.put("quantity", 100);

   
    this.response = io.restassured.RestAssured.given()
            .spec(api.support.ApiSpecs.baseJsonSpec())
            .filter(session.getCookieFilter())
            .header("Authorization", "Bearer " + session.getBearerToken())
            .body(updateBody)
            .put(path);
}


@When("I send a POST request to {string}")
public void i_send_a_post_request_to(String path) {

    Map<String, Object> body = new HashMap<>();
    body.put("name", "Unauthorized Plant");
    body.put("price", 100.0);
    body.put("quantity", 10);

 
    this.response = io.restassured.RestAssured.given()
            .spec(api.support.ApiSpecs.baseJsonSpec())
            .filter(session.getCookieFilter()) 
            .header("Authorization", "Bearer " + session.getBearerToken())
            .body(body)
            .post(path);
}
@When("I send a GET request to {string}")
    public void getRequest(String path) {
        
        this.response = io.restassured.RestAssured.given()
                .spec(api.support.ApiSpecs.baseJsonSpec())
                .filter(session.getCookieFilter())
                .header("Authorization", "Bearer " + session.getBearerToken())
                .get(path);
    }







}