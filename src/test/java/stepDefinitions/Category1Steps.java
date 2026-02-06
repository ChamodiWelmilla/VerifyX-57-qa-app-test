package stepDefinitions;

import io.cucumber.java.en.*;
import org.testng.Assert;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;
import utils.DriverManager;
import io.restassured.response.Response;
import io.restassured.RestAssured;

import pages.categories.Category1Page;
import pages.authentication.LoginPage;

import api.categories.Category1Api;
import api.support.ApiSession;
import api.auth.AuthApi;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class Category1Steps {

    WebDriver driver;
    Category1Page Category1Page;
    LoginPage loginPage;

    private ApiSession apiSession;
    private Response apiResponse;
    private String apiToken;

    private String selectedParentCategory;
    private String searchedCategoryName;
    private int selectedCategoryId;

    public Category1Steps() {

    }

    private void initUI() {
        if (this.driver == null) {
            try {
                this.driver = DriverManager.getDriver();
                this.Category1Page = new Category1Page(driver);
                this.loginPage = new LoginPage(driver);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize UI driver", e);
            }
        }
    }

    // UI STEPS

    @Given("I am logged in as {string} with password {string}")
    public void i_login_ui(String username, String password) {
        initUI();
        String baseUrl = ConfigReader.get("ui.base.url");
        String loginPath = ConfigReader.get("ui.login.path");

        if (loginPath == null)
            loginPath = "/ui/login";
        String fullUrl = baseUrl + loginPath;

        if (!driver.getCurrentUrl().contains("login")) {
            driver.get(fullUrl);
        }

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
    }

    @When("I navigate to the Category List page")
    public void nav_to_categories() {
        initUI();
        Category1Page.navigateToCategories();
    }

    @Then("the {string} button should be visible")
    public void btn_visible(String btnName) {
        initUI();
        if (btnName.equals("Add Category")) {
            Assert.assertTrue(Category1Page.isAddCategoryButtonVisible(),
                    "Admin should see Add Category button!");
        }
    }

    @When("I search for category {string}")
    public void i_search_for(String name) {
        initUI();

        if (name.equalsIgnoreCase("Random")) {
            List<String> availableNames = Category1Page.getColumnData("Name");

            if (availableNames.isEmpty()) {
                throw new RuntimeException("Cannot run Search Test: The category list is empty!");
            }

            int randomIndex = (int) (Math.random() * availableNames.size());
            searchedCategoryName = availableNames.get(randomIndex);

            System.out.println("DEBUG: Randomly selected category to search: " + searchedCategoryName);
        } else {
            searchedCategoryName = name;
        }

        Category1Page.searchFor(searchedCategoryName);
    }

    @Then("I should see {string} in the category list results")
    public void verify_search_result(String name) {
        initUI();
        String expectedName = name.equalsIgnoreCase("Random") ? searchedCategoryName : name;
        Assert.assertTrue(Category1Page.doResultsContain(expectedName),
                "Search result not found. Expected: " + expectedName);
    }

    @Then("I should see the message {string}")
    public void i_should_see_the_message(String message) {
        boolean isDisplayed = Category1Page.verifyValidationMessage(message);

        Assert.assertTrue(isDisplayed, "FAILED: The message '" + message + "' was not found on the page.");
    }

    @When("I filter by parent category {string}")
    public void i_filter_by(String parent) {
        initUI();

        if (parent.equalsIgnoreCase("Random")) {
            List<String> options = Category1Page.getAvailableParentCategories();

            if (options.isEmpty()) {
                throw new RuntimeException("No parent categories found in dropdown to filter by!");
            }

            int randomIndex = (int) (Math.random() * options.size());
            selectedParentCategory = options.get(randomIndex);

            System.out.println("DEBUG: Randomly selected parent: " + selectedParentCategory);
        } else {
            selectedParentCategory = parent;
        }

        Category1Page.selectParentFilter(selectedParentCategory);
    }

    @Then("I should see categories related to {string}")
    public void verify_filter_result(String parent) {
        initUI();
        String expectedParent = parent.equalsIgnoreCase("Random") ? selectedParentCategory : parent;

        boolean hasData = Category1Page.doResultsContain(expectedParent);
        boolean isEmpty = Category1Page.isNoDataDisplayed();

        Assert.assertTrue(hasData || isEmpty,
                "Filter verification failed! Table did not contain '" + expectedParent + "' and was not empty.");
    }

    @When("I click the sort header for {string}")
    public void i_click_sort(String col) {
        initUI();
        Category1Page.clickSortHeader(col);

        for (int i = 0; i < 5; i++) {
            if (Category1Page.isSortedAscending(col)) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        Category1Page.clickSortHeader(col);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    @Then("the category list should be sorted by {string}")
    public void verify_ui_sort(String col) {
        initUI();
        Assert.assertTrue(Category1Page.isSortedAscending(col),
                "UI List is not sorted by " + col);
    }

    @When("I click the {string} button")
    public void i_click_the_button(String buttonName) {
        initUI();
        if (buttonName.equalsIgnoreCase("Add Category")) {
            Category1Page.clickAddCategory();
        } else if (buttonName.equalsIgnoreCase("Save")) {
            Category1Page.clickSaveButton();
        } else {
            throw new IllegalArgumentException("Button not defined: " + buttonName);
        }
    }

    @When("I click the Save button without entering a name")
    public void i_click_save_no_name() {
        initUI();
        Category1Page.clickSaveButton();
    }

    @Then("I should see the validation message {string}")
    public void i_should_see_the_validation_message(String expectedMessage) {
        boolean isDisplayed = Category1Page.verifyValidationMessage(expectedMessage);
        Assert.assertTrue(isDisplayed,
                "FAILED: Expected validation message '" + expectedMessage + "' was not visible on the UI.");
    }

    // API STEPS

    @Given("I have a valid API token for {string}")
    public void set_api_token(String role) {
        if (role.equalsIgnoreCase("admin")) {
            this.apiSession = AuthApi.loginAsAdmin();
        } else {
            this.apiSession = AuthApi.loginAsUser();
        }
        Assert.assertNotNull(apiSession, "Failed to create API session");

        String username = role.equalsIgnoreCase("admin") ? "admin" : "testuser";
        String password = role.equalsIgnoreCase("admin") ? "admin123" : "test123";

        Response authResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }")
                .post("http://localhost:8080/api/auth/login");

        this.apiToken = authResponse.jsonPath().getString("token");

    }

    @When("I send a GET request to {string}")
    public void send_get_request(String endpointUrl) {
        Map<String, Object> params = new HashMap<>();
        String[] parts = endpointUrl.split("\\?");

        if (parts.length > 1) {
            String[] queryPairs = parts[1].split("&");
            for (String pair : queryPairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        if (endpointUrl.matches(".*/api/categories/\\d+")) {
            String idStr = endpointUrl.replaceAll("\\D+", "");
            apiResponse = Category1Api.getCategoryById(apiSession, Integer.parseInt(idStr));
        } else {
            apiResponse = Category1Api.getCategories(apiSession, params);
        }
    }

    @Then("the response status code should be {int}")
    public void verify_status(int code) {
        apiResponse.then().statusCode(code);
    }

    @Then("the response should contain pagination metadata")
    public void verify_pagination() {
        Assert.assertNotNull(apiResponse.jsonPath().get("totalPages"));
        Assert.assertNotNull(apiResponse.jsonPath().get("totalElements"));
    }

    @Then("the response body should contain {string}")
    public void verify_body_content(String text) {
        String body = apiResponse.getBody().asString();
        Assert.assertTrue(body.contains(text), "API response missing: " + text);
    }

    @Then("all items in response should have parentId {int}")
    public void verify_parent_id(int pid) {
        List<Integer> ids = apiResponse.jsonPath().getList("content.parentCategory.id");
        if (ids != null) {
            for (Integer id : ids)
                Assert.assertEquals(id, (Integer) pid);
        }
    }

    @Then("the response body should be sorted by {string}")
    public void verify_api_sort(String field) {
        String jsonPath = "content." + field;
        if (field.equals("parentCategory"))
            jsonPath = "content.parentCategory.name";

        List<String> actual = apiResponse.jsonPath().getList(jsonPath, String.class);
        if (actual != null && actual.size() > 1) {
            List<String> expected = new ArrayList<>(actual);
            Collections.sort(expected);
            Assert.assertEquals(actual, expected, "API response not sorted by " + field);
        }
    }

    @Given("a valid category exists in the system")
    public void a_valid_category_exists_in_the_system() {
        Response listResponse = RestAssured
                .given()
                .header("Authorization", "Bearer " + apiToken)
                .get("http://localhost:8080/api/categories");

        List<Integer> ids = listResponse.jsonPath().getList("id");

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No categories found in the system to test!");
        }
        selectedCategoryId = ids.get(new Random().nextInt(ids.size()));
        System.out.println(">>> Selected Random Category ID for Testing: " + selectedCategoryId);
    }

    @When("I send a GET request to retrieve that category")
    public void i_send_a_get_request_to_retrieve_that_category() {
        String endpoint = "/api/categories/" + selectedCategoryId;

        apiResponse = RestAssured
                .given()
                .header("Authorization", "Bearer " + apiToken)
                .get("http://localhost:8080" + endpoint);
    }

    @Then("the response body should contain the details of the requested category")
    public void the_response_body_should_contain_the_details_of_the_requested_category() {
        int actualId = apiResponse.jsonPath().getInt("id");
        Assert.assertEquals(actualId, selectedCategoryId, "Returned Category ID does not match requested ID!");

        Assert.assertNotNull(apiResponse.jsonPath().getString("name"), "Category name is missing!");
    }
}