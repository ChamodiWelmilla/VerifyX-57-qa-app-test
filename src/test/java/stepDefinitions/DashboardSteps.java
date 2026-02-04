package stepDefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import pages.authentication.LoginPage;
import pages.dashboard.DashboardPage;
import utils.DriverManager;
import utils.ConfigReader;

public class DashboardSteps {

    LoginPage loginPage;
    DashboardPage dashboardPage;

    @Given("user is on the login page")
    public void user_is_on_the_login_page() {
        DriverManager.getDriver().get(
                ConfigReader.get("ui.base.url") + ConfigReader.get("ui.login.path")
        );

        loginPage = new LoginPage(DriverManager.getDriver());
        dashboardPage = new DashboardPage(DriverManager.getDriver());
    }

    @When("user logs in as {string}")
    public void user_logs_in_as(String role) {

        if (role.equalsIgnoreCase("admin")) {
            loginPage.enterUsername(ConfigReader.get("admin.username"));
            loginPage.enterPassword(ConfigReader.get("admin.password"));
        } else if (role.equalsIgnoreCase("user")) {
            loginPage.enterUsername(ConfigReader.get("user.username"));
            loginPage.enterPassword(ConfigReader.get("user.password"));
        } else {
            throw new RuntimeException("Invalid role: " + role);
        }

        loginPage.clickLogin();
    }

    @Then("Dashboard should be displayed")
    public void dashboard_should_be_displayed() {
        Assert.assertTrue(
                dashboardPage.isDashboardDisplayed(),
                "Dashboard is not displayed!"
        );
        System.out.println("Dashboard loaded successfully!");
    }

    @Then("the Dashboard should display summary information for Categories, Plants, and Sales")
    public void verify_dashboard_summary_sections() {
        Assert.assertTrue(
                dashboardPage.areSummarySectionsDisplayed(),
                "Dashboard summary sections for Categories, Plants, or Sales are missing!"
        );
        System.out.println("All summary sections are displayed correctly.");
    }

    @When("user clicks on the Manage Plants button")
    public void user_clicks_on_the_manage_plants_button() {
        dashboardPage.clickManagePlants();
    }

    @Then("user should be redirected to the Plant List page")
    public void user_should_be_redirected_to_the_plant_list_page() {
        String expectedUrlPart = ConfigReader.get("ui.plants.path");
        dashboardPage.waitForPlantListPage(expectedUrlPart);
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(expectedUrlPart));
    }

    @When("user clicks on the Categories menu item")
    public void user_clicks_on_categories() {
        dashboardPage.clickCategories();
    }

    @Then("the Categories menu item should be highlighted as active")
    public void categories_should_be_active() {
        Assert.assertTrue(
                dashboardPage.isCategoriesMenuActive(),
                "Categories menu is not highlighted as active!"
        );
    }

    @When("user clicks on the Plants menu item")
    public void user_clicks_on_plants() {
        dashboardPage.clickPlants();
    }

    @Then("the Plants menu item should be highlighted as active")
    public void plants_should_be_active() {
        Assert.assertTrue(
                dashboardPage.isPlantsMenuActive(),
                "Plants menu is not highlighted as active!"
        );
    }

    @When("user clicks on the Sales menu item")
    public void user_clicks_on_sales() {
        dashboardPage.clickSales();
    }

    @Then("the Sales menu item should be highlighted as active")
    public void sales_should_be_active() {
        Assert.assertTrue(
                dashboardPage.isSalesMenuActive(),
                "Sales menu is not highlighted as active!"
        );
    }

}
