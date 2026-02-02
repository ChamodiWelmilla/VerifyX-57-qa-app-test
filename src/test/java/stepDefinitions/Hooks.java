package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import utils.DriverManager;
import utils.ConfigReader;

public class Hooks {

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
