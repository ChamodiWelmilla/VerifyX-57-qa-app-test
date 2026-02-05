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
        SalesPrereqApi.ensurePlantForSale(150.0, 25);
    }

    @Before("@sales-record")
    public void ensureAtLeastOneSaleRecordExists() {
        SalesPrereqApi.ensureSaleExistsForCurrentPlant(1);
    }

    @Before("@sales-sort")
    public void ensureSalesForSortingExist() {
        SalesPrereqApi.ensureSalesExistForSorting();
    }

    @Before("@sales-empty")
    public void ensureNoSalesExist() {
        SalesPrereqApi.deleteAllSalesAsAdmin();
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
