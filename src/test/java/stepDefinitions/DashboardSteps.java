package stepDefinitions;

import io.cucumber.java.en.*;
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
}
