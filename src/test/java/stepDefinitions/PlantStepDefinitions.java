package stepDefinitions;

import io.cucumber.java.en.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.plant.PlantPage;
import pages.authentication.LoginPage;
import utils.DriverManager;
import utils.ConfigReader;
import java.time.Duration;
import org.openqa.selenium.TimeoutException;

public class PlantStepDefinitions {

    PlantPage plantPage;
    LoginPage loginPage;

    @Given("Admin is logged in and navigated to the Plant List page")
    public void admin_is_logged_in_and_navigated_to_the_plant_list_page() {
        DriverManager.getDriver().get(ConfigReader.get("ui.base.url") + ConfigReader.get("ui.login.path"));
        loginPage = new LoginPage(DriverManager.getDriver());
        loginPage.enterUsername(ConfigReader.get("admin.username"));
        loginPage.enterPassword(ConfigReader.get("admin.password"));
        loginPage.clickLogin();

        DriverManager.getDriver().get(ConfigReader.get("ui.base.url") + "/ui/plants");
        plantPage = new PlantPage(DriverManager.getDriver());
    }

    @When("the Admin clicks the {string} button")
    public void the_admin_clicks_the_button(String buttonName) {
        if (buttonName.equalsIgnoreCase("Add Plant")) {
            plantPage.clickAddPlant();
        }
    }

    @Then("the system redirects to the Plants page")
    public void the_system_redirects_to_the_plants_page() {
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/ui/plants"), 
            "Redirection failed! Current URL: " + currentUrl);
    }

    @When("enters {string} as name, {string} as price, and {string} as quantity")
    public void enters_as_name_as_price_and_as_quantity(String name, String price, String qty) {
        plantPage.fillPlantForm(name, "subcat1", price, qty);
    }

    @When("selects a valid {string} sub-category and clicks {string}")
    public void selects_a_valid_sub_category_and_clicks(String cat, String btn) {
        plantPage.clickSave();
    }

    @Then("the new plant {string} appears in the list")
    public void the_new_plant_appears_in_the_list(String plantName) {
        Assert.assertTrue(DriverManager.getDriver().getPageSource().contains(plantName));
    }

    @When("the Admin clicks the Cancel button")
    public void clicks_cancel() {
        plantPage.clickCancel();
    }

    @When("the Admin clicks the Edit button for an existing plant")
    public void click_edit() {
        plantPage.clickEditFirstPlant(); 
    }

    @When("updates the Quantity to {string} and saves")
    public void update_quantity(String qty) {
        plantPage.clickSave(); 
    }

    @Then("the Add Plant button should not be visible")
    public void verify_add_button_hidden() {
        Assert.assertFalse(plantPage.isAddPlantButtonVisible(), "Add button is visible to a non-admin!");
    }


    // PLANTUI_AD_03 & PLANTUI_AD_04: Validates specific field errors 
    @When("enters {string} as name and {string} as price")
    public void enters_as_name_and_as_price(String name, String price) {
        plantPage.fillPlantForm(name, "subcat1", price, "10"); 
    }

    @When("clicks the {string} button")
    public void clicks_the_button(String buttonName) {
        if (buttonName.equalsIgnoreCase("Save")) {
            plantPage.clickSave();
        }
    }

    @Then("the system displays the validation error {string}")
    public void the_system_displays_the_validation_error(String expectedError) {
        String actualError = plantPage.getValidationErrorMessage();
        Assert.assertEquals(actualError, expectedError, "Validation message mismatch!");
    }

    @And("Admin is navigated to the Plant List page")
    public void admin_is_navigated_to_the_plant_list_page() {
        // Build the URL using your ConfigReader pattern
        DriverManager.getDriver().get(ConfigReader.get("ui.base.url") + "/ui/plants");
        
        // Initialize the page object if it hasn't been already
        if (plantPage == null) {
            plantPage = new PlantPage(DriverManager.getDriver());
        }
    }


    //user ui test

    // NEWLY ADDED: This matches "And User navigates to the Plant List page"
    @And("User navigates to the Plant List page")
    public void user_navigates_to_plant_list_page() {
        DriverManager.getDriver().get(
                ConfigReader.get("ui.base.url") + "/ui/plants"
        );
        plantPage = new PlantPage(DriverManager.getDriver());
    }

    // NEWLY ADDED: This matches "And User is navigated to the Plant List page"
    @And("User is navigated to the Plant List page")
    public void user_is_navigated_to_plant_list() {
        user_navigates_to_plant_list_page();
    }

    // NEWLY ADDED: Corrected login step to match "When User logs in as {string}"
    @When("User logs in as {string}")
    public void user_logs_in_as_role(String role) {
        // You can reuse the existing logic from DashboardSteps if needed
        loginPage = new LoginPage(DriverManager.getDriver());
        if (role.equalsIgnoreCase("admin")) {
            loginPage.enterUsername(ConfigReader.get("admin.username"));
            loginPage.enterPassword(ConfigReader.get("admin.password"));
        } else {
            loginPage.enterUsername(ConfigReader.get("user.username"));
            loginPage.enterPassword(ConfigReader.get("user.password"));
        }
        loginPage.clickLogin();
    }


    // TC_PLANTUI_USR_02
    @Then("the Edit and Delete actions should be hidden for the user")
    public void verify_actions_hidden() {
        boolean actionsVisible = plantPage.areEditDeleteButtonsVisible();
        Assert.assertFalse(actionsVisible, "Edit/Delete buttons are visible to a standard User!"); 
    }

    

    @When("user navigates to the Plant List page")
    public void user_navigates_to_plant_list() {
        DriverManager.getDriver().get(
                ConfigReader.get("ui.base.url") + ConfigReader.get("ui.plants.path")
        );
        plantPage = new PlantPage(DriverManager.getDriver());
    }

    @Then("Low badge should be displayed for plants with quantity less than five")
    public void verify_low_badge() {
        // FIXED: Using instance variable 'plantPage' instead of class name
        Assert.assertTrue(plantPage.isLowBadgeDisplayedForLowQuantityPlant());
    }






    // TC_PLANTUI_USR_04
    @When("the user searches for plant name {string}")
    public void search_for_plant(String name) {
        plantPage.searchPlant(name); 
    }

    @Then("the list should only display plants matching {string}")
    public void verify_search_results(String name) {
        Assert.assertTrue(DriverManager.getDriver().getPageSource().contains(name), "Search result not found!"); 
    }

    // TC_PLANTUI_USR_05
   @Then("the system should display {string}")
public void verify_empty_state(String expectedMessage) {
    WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(5));
    
    // Wait until the element is actually present in the DOM
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(
        By.xpath("//td[contains(text(), '" + expectedMessage + "')]")
    ));
    
    Assert.assertEquals(element.getText().trim(), expectedMessage);
}

}