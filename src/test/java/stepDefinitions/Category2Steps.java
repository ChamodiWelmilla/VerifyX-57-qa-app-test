package stepDefinitions;

import api.auth.AuthApi;
import api.categories.CategoryApi;
import api.support.ApiSession;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.*;

public class Category2Steps {
    private ApiSession session;
    private Response response;

    @Given("I am authenticated as an Admin")
    public void authAdmin() {
        session = AuthApi.loginAsAdmin();
    }

    @Given("I am authenticated as a testuser")
public void i_am_authenticated_as_a_testuser() {
    // This calls your Auth helper to get a non-admin session
    // Make sure AuthApi.loginAsUser() or similar is defined in your project
    this.session = api.auth.AuthApi.loginAsUser();
}

    @When("I send a POST request with name {string}")
    public void postCategory(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        response = CategoryApi.createCategory(session, body);
    }

    @When("I send a PUT request to update category id {int} to {string}")
public void i_send_a_put_request_to_update_id_to(Integer id, String name) {
    Map<String, Object> body = new HashMap<>();
    body.put("name", name);
    // This calls your CategoryApi helper to perform the update
    response = CategoryApi.updateCategory(session, id, body);
}

    @When("I send a DELETE request for category id {int}")
public void i_send_a_delete_request_for_category_id(Integer id) {
    // This calls your CategoryApi helper to perform the deletion
    response = api.categories.CategoryApi.deleteCategory(session, id);
}

    @When("I request page {int} with size {int}")
    public void getPage(int page, int size) {
        response = CategoryApi.getCategories(session, page, size);
    }

    @Then("the status code should be {int}")
public void checkStatus(int expectedCode) {
    if (response.statusCode() == 500) {
        // This will print the error message from the developer to your console
        System.out.println("DEBUG SERVER ERROR: " + response.asPrettyString());
    }
    response.then().statusCode(expectedCode);
}
@Then("the error message should contain {string}")
public void the_error_message_should_contain(String expectedMsg) {
    // This logic works for both Test 2 and Test 3
    response.then().assertThat()
            .body(org.hamcrest.Matchers.containsString(expectedMsg));
}

}