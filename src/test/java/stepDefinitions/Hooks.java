package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import api.sales.helper.SalesPrereqApi;
import utils.DriverManager;
import utils.ConfigReader;

public class Hooks {

    @Before
    public void launchApp() {
        DriverManager.getDriver()
                .get(ConfigReader.get("ui.base.url"));
    }

    @Before("@sales")
    public void createSalesPrerequisites() {
        // Creates a sellable plant via APIs and stores it for the scenario thread.
        SalesPrereqApi.ensurePlantForSale(150.0, 25);
    }

    @After("@sales")
    public void clearSalesPrerequisites() {
        SalesPrereqApi.clearCurrentPlantForSale();
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
