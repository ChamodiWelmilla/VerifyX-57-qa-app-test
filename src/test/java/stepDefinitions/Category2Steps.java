package stepDefinitions;

import api.auth.AuthApi;
import api.categories.CategoryApi;
import api.support.ApiSession;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.ConfigReader;
import pages.authentication.LoginPage;
import pages.categories.Category2Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import pages.dashboard.DashboardPage;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;
import utils.DriverManager;
import org.openqa.selenium.WebDriver;
import static org.hamcrest.Matchers.*;
import org.testng.Assert;

public class Category2Steps {
    private ApiSession session;
    private Response response;
    private Category2Page category2Page; 
    private WebDriver driver;
    private LoginPage loginPage;

    @Given("I am authenticated as an Admin")
    public void authAdmin() {
        session = AuthApi.loginAsAdmin();
    }

    @Given("I am authenticated as a testuser")
public void i_am_authenticated_as_a_testuser() {
    
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
    response = CategoryApi.updateCategory(session, id, body);
}

    @When("I send a DELETE request for category id {int}")
public void i_send_a_delete_request_for_category_id(Integer id) {
    response = api.categories.CategoryApi.deleteCategory(session, id);
}

    @When("I request page {int} with size {int}")
    public void getPage(int page, int size) {
        response = CategoryApi.getCategories(session, page, size);
    }

    @Then("the status code should be {int}")
public void checkStatus(int expectedCode) {
    if (response.statusCode() == 500) {
        System.out.println("DEBUG SERVER ERROR: " + response.asPrettyString());
    }
    response.then().statusCode(expectedCode);
}
@Then("the error message should contain {string}")
public void the_error_message_should_contain(String expectedMsg) {
    response.then().assertThat()
            .body(org.hamcrest.Matchers.containsString(expectedMsg));
}


@Given("I am logged in as an Admin")
    public void i_am_logged_in_as_an_admin() {
        setupUIObjects();
        String url = ConfigReader.get("ui.base.url") + ConfigReader.get("ui.login.path");
        driver.get(url);
        loginPage.enterUsername(ConfigReader.get("admin.username"));
        loginPage.enterPassword(ConfigReader.get("admin.password"));
        loginPage.clickLogin();
    }


    @Given("I am logged in as a testuser")
    public void i_am_logged_in_as_a_regular_user() {
        setupUIObjects();
        String url = ConfigReader.get("ui.base.url") + ConfigReader.get("ui.login.path");
        driver.get(url);
        loginPage.enterUsername(ConfigReader.get("user.username"));
        loginPage.enterPassword(ConfigReader.get("user.password"));
        loginPage.clickLogin();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.urlContains("dashboard"));
    }


    @When("I navigate to {string}")
public void i_navigate_to(String path) {
    String targetUrl = ConfigReader.get("ui.base.url") + path;
    
    driver.get(targetUrl);
    
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    
    if (driver.getCurrentUrl().contains("dashboard")) {
        driver.navigate().to(targetUrl);
    }
    
    wait.until(ExpectedConditions.urlContains(path));
    System.out.println("DEBUG: Successfully reached: " + driver.getCurrentUrl());
}

    @Then("the {string} button should be visible")
    public void the_button_should_be_visible(String btnName) {
        Assert.assertTrue(category2Page.isAddButtonVisible(), btnName + " is not visible!");
    }
    @When("I manually type {string} in the browser")
    public void manual_navigation(String path) {
        driver.get(ConfigReader.get("ui.base.url") + path);
    }

    @Then("I should be redirected to a {string} page")
public void verify_redirection(String expectedResult) {
    String currentUrl = driver.getCurrentUrl();
    
    if (currentUrl.contains("/edit") || currentUrl.contains("/add")) {
        Assert.fail("SECURITY BUG: Unauthorized user was allowed to access: " + currentUrl);
    }

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    try {
        wait.until(ExpectedConditions.urlContains("403"));
        Assert.assertTrue(driver.getPageSource().contains("Access Denied") || 
                          driver.getPageSource().contains("Forbidden"));
    } catch (Exception e) {
        Assert.fail("User was not redirected to the 403 error page. Current URL: " + currentUrl);
    }
}

    @Then("the edit and delete buttons should not be visible")
    public void verify_actions_hidden() {
        Assert.assertFalse(category2Page.isEditVisible());
        Assert.assertFalse(category2Page.isDeleteVisible());
    }

    private void setupUIObjects() {
        this.driver = DriverManager.getDriver();
        this.category2Page = new Category2Page(driver);
        this.loginPage = new LoginPage(driver);
    }
    @When("I click the {string}")
public void i_click_the(String element) {
    if(element.toLowerCase().contains("add category")) {
        category2Page.clickAddCategory();
    }
}

@When("I enter a valid name {string} without a parent")
public void ii_enter_valid_name(String name) {
    category2Page.enterName(name);
}

@When("I click the {string} button")
public void i_click_the_button(String btnName) {
    if(btnName.equalsIgnoreCase("Save")) {
        category2Page.clickSave();
    }
}

@Then("the category {string} should be shown in the list as a parent category")
public void verify_category_in_list(String categoryName) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    wait.until(ExpectedConditions.urlToBe(ConfigReader.get("ui.base.url") + "/ui/categories"));

    WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[contains(@class, 'alert-success') or contains(text(), 'successfully')]")
    ));
    System.out.println("Success Alert: " + successAlert.getText());

    boolean isFound = wait.until(ExpectedConditions.textToBePresentInElementLocated(
                        By.tagName("table"), categoryName));
    
    Assert.assertTrue(isFound, "FAILURE: Category '" + categoryName + "' not found in the table list!");
}

@When("I enter a valid name {string} and select a parent category")
public void i_enter_name_and_select_parent(String subCatName) {
  
    category2Page.enterName(subCatName);
  
    category2Page.selectParent("UniqueName"); 
}

@Then("the sub-category should be added and shown in the list")
public void the_sub_category_should_be_added_and_shown_in_the_list() {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.urlContains("categories"));
    boolean hasMessage = wait.until(ExpectedConditions.textToBePresentInElementLocated(
        By.tagName("body"), "successfully"));
    
    Assert.assertTrue(hasMessage, "Sub-category creation success message not found!");
}
@When("I click the Edit button for category {string}")
public void i_click_the_edit_button_for_category(String categoryName) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
    
    category2Page.clickEditForCategory(categoryName);
}
@Then("the category {string} should be updated and shown in the list")
public void the_category_should_be_updated_and_shown_in_the_list(String expectedName) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
 
    wait.until(ExpectedConditions.urlContains("categories"));
    boolean isFound = wait.until(ExpectedConditions.textToBePresentInElementLocated(
                        By.tagName("table"), expectedName));
    
    Assert.assertTrue(isFound, "The updated category '" + expectedName + "' was not found in the list!");
}

@When("I update the name to {string}")
public void i_update_the_name_to(String newName) {
   
    category2Page.enterName(newName);
}
@When("I click the Delete button for a category {string}")
public void i_click_the_delete_button_for_a_category(String categoryName) {
    category2Page.clickDeleteForCategory(categoryName);
    category2Page.handleConfirmationBox();
}

@Then("the category should be deleted and removed from the list")
public void the_category_should_be_deleted_and_removed_from_the_list() {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    
    WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[contains(@class, 'alert-success')]")));
    
    Assert.assertTrue(successMsg.getText().toLowerCase().contains("success"));
}
@Then("the {string} should not be visible")
public void the_element_should_not_be_visible(String elementDescription) {
    boolean isVisible = false;

    if (elementDescription.toLowerCase().contains("add category")) {
        isVisible = category2Page.isAddButtonVisible();
    } else if (elementDescription.toLowerCase().contains("edit")) {
        isVisible = category2Page.isEditVisible();
    }
    
    Assert.assertFalse(isVisible, "Security Failure: '" + elementDescription + "' is visible to an unauthorized user!");
}
}