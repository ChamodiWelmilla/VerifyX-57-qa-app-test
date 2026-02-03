package stepDefinitions;

import api.sales.helper.SalesPrereqApi;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.sales.SalesListPage;
import pages.sales.SellPlantPage;
import utils.ConfigReader;
import utils.DriverManager;

import java.time.Duration;
import java.util.Locale;

public class SalesSteps {

    private SalesListPage salesListPage;
    private SellPlantPage sellPlantPage;

    @When("user navigates to Sales page")
    public void user_navigates_to_sales_page() {
        try {
            DriverManager.getDriver().findElement(By.cssSelector("a[href='/ui/sales']")).click();
        } catch (Exception ignored) {
            DriverManager.getDriver().get(ConfigReader.get("ui.base.url") + "/ui/sales");
        }

        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();
    }

    @And("user clicks Sell Plant")
    public void user_clicks_sell_plant() {
        salesListPage.clickSellPlant();
        sellPlantPage = new SellPlantPage(DriverManager.getDriver());
        sellPlantPage.waitForPage();
    }

    @And("user sells the created plant with quantity {string}")
    public void user_sells_the_created_plant_with_quantity(String qty) {
        int quantity = Integer.parseInt(qty);

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        sellPlantPage.selectPlantByName(plant.getPlantName());
        sellPlantPage.setQuantity(quantity);
        sellPlantPage.clickSell();

        // After selling, UI should navigate back to sales list.
        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();
    }

    @Then("sales list should show correct total price for the created plant with quantity {string}")
    public void sales_list_should_show_correct_total_price_for_created_plant_with_quantity(String qty) {
        int quantity = Integer.parseInt(qty);

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        double expectedTotal = plant.getPrice() * quantity;
        String expectedTotalText = String.format(Locale.US, "%.2f", expectedTotal);

        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));
        wait.until(d -> salesListPage.findSaleRow(plant.getPlantName(), quantity) != null);

        SalesListPage.SaleRow row = salesListPage.findSaleRow(plant.getPlantName(), quantity);
        Assert.assertNotNull(row, "Expected sale row not found for plant=" + plant.getPlantName() + " qty=" + quantity);

        String total = row.getTotalPrice();
        Assert.assertNotNull(total);

        if (!total.contains(expectedTotalText)) {
            double actual = parseMoney(total);
            Assert.assertEquals(actual, expectedTotal, 0.01, "Total price mismatch. UI text: " + total);
        }
    }

    private static double parseMoney(String text) {
        String cleaned = text.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isBlank()) {
            throw new AssertionError("Could not parse money value from: " + text);
        }
        return Double.parseDouble(cleaned);
    }
}
