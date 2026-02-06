package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import api.sales.SalesApiHelper;
import api.sales.helper.SalesPrereqApi;
import utils.DriverManager;
import utils.ConfigReader;

public class Hooks {

    @Before
    public void launchApp() {
        DriverManager.getDriver()
                .get(ConfigReader.get("ui.base.url"));
    }

    @Before("@ui-sales-view or @ui-sales-delete or @ui-sales-empty-validation or @ui-sales-invalid-quantity or @ui-sales-exceed-stock or @ui-sales-remaining-stock")
    public void createSalesPrerequisites() {
        SalesPrereqApi.ensurePlantForSale(150.0, 25);
    }

    @Before("@ui-user-sales-list")
    public void ensureAtLeastOneSaleRecordExists() {
        SalesApiHelper.ensureSaleExistsForCurrentPlant(1);
    }

    @Before("@ui-user-sales-sort")
    public void ensureSalesForSortingExist() {
        SalesApiHelper.ensureSalesExistForSorting();
    }

    @Before("@ui-user-sales-empty")
    public void ensureNoSalesExist() {
        SalesApiHelper.deleteAllSalesAsAdmin();
    }

    @After("@ui-sales-view or @ui-sales-delete or @ui-sales-empty-validation or @ui-sales-invalid-quantity or @ui-sales-exceed-stock or @ui-sales-remaining-stock")
    public void clearSalesPrerequisites() {
        SalesPrereqApi.clearCurrentPlantForSale();
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
