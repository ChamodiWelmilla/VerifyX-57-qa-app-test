package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.DriverManager;
import utils.ConfigReader;

public class Hooks {

    private boolean isUiTest = false;

    @Before
    public void launchApp() {
        DriverManager.getDriver()
                .get(ConfigReader.get("ui.base.url"));
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
