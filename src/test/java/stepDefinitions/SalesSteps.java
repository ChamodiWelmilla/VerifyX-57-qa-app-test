package stepDefinitions;

import api.sales.helper.SalesPrereqApi;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.sales.SalesListPage;
import pages.sales.SellPlantPage;
import utils.ConfigReader;
import utils.DriverManager;
import utils.SalesContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SalesSteps {

    private SalesListPage salesListPage;
    private SellPlantPage sellPlantPage;
    private int initialSalesRowCount;

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

    @And("user records current sales count")
    public void user_records_current_sales_count() {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");
        initialSalesRowCount = salesListPage.getSaleRowCount();
    }

    @And("user keeps required fields empty")
    public void user_keeps_required_fields_empty() {
        sellPlantPage.selectPlaceholderPlantIfPresent();
        sellPlantPage.clearQuantity();
    }

    @And("user sells the created plant with quantity {string}")
    public void user_sells_the_created_plant_with_quantity(String qty) {
        int quantity = Integer.parseInt(qty);

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        sellPlantPage.selectPlantByName(plant.getPlantName());
        sellPlantPage.setQuantity(quantity);
        sellPlantPage.clickSell();

        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();
    }

    @And("user selects the created plant")
    public void user_selects_the_created_plant() {
        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        sellPlantPage.selectPlantByName(plant.getPlantName());
    }

    @And("user leaves quantity empty")
    public void user_leaves_quantity_empty() {
        sellPlantPage.clearQuantity();
    }

    @And("user enters quantity {string}")
    public void user_enters_quantity(String qty) {
        int quantity = Integer.parseInt(qty);
        sellPlantPage.setQuantity(quantity);
    }

    @And("user enters quantity exceeding remaining stock")
    public void user_enters_quantity_exceeding_remaining_stock() {
        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        int exceedingQuantity = plant.getQuantity() + 1;
        sellPlantPage.setQuantity(exceedingQuantity);
    }

    @When("user clicks Sell")
    public void user_clicks_sell() {
        sellPlantPage.clickSell();
    }

    @Then("no new sale should be created")
    public void no_new_sale_should_be_created() {
        user_navigates_to_sales_page();

        int currentCount = salesListPage.getSaleRowCount();
        Assert.assertEquals(currentCount, initialSalesRowCount,
            "Expected no new sale to be created, but row count changed.");

        user_clicks_sell_plant();
    }

    @Then("user should be redirected to Sales page")
    public void user_should_be_redirected_to_sales_page() {
        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();
        Assert.assertTrue(DriverManager.getDriver().getCurrentUrl().contains("/ui/sales"),
                "Expected to be on Sales page, but URL was: " + DriverManager.getDriver().getCurrentUrl());
    }

    @Then("Sales page should load successfully")
    public void sales_page_should_load_successfully() {
        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();
        Assert.assertTrue(
                DriverManager.getDriver().getCurrentUrl().contains("/ui/sales"),
                "Expected Sales page URL to contain /ui/sales but was: " + DriverManager.getDriver().getCurrentUrl()
        );
    }

    @Then("sales list should display existing sales records")
    public void sales_list_should_display_existing_sales_records() {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");
        int count = salesListPage.getSaleRowCount();
        Assert.assertTrue(count > 0, "Expected at least one sales record in the list, but found: " + count);
    }

    @Then("no sales message should be displayed")
    public void no_sales_message_should_be_displayed() {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");

        int count = salesListPage.getSaleRowCount();
        String msg = salesListPage.getNoSalesMessageTextIfPresent();
        Assert.assertNotNull(msg, "Expected a 'no sales' message, but none was found.");

        String lower = msg.toLowerCase(Locale.ROOT);
        Assert.assertTrue(
                lower.contains("no sales") && (lower.contains("found") || lower.contains("yet") || lower.contains("have")),
            "Unexpected no-sales message text: " + msg + " (rowCount=" + count + ")"
        );
    }

    @Then("Sell Plant button should not be displayed for the user")
    public void sell_plant_button_should_not_be_displayed_for_the_user() {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");
        Assert.assertFalse(
                salesListPage.isSellPlantButtonVisible(),
                "Expected Sell Plant button/link to be hidden for normal user, but it was visible."
        );
    }

    @Then("user should not be able to delete an existing sale")
    public void user_should_not_be_able_to_delete_an_existing_sale() {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");
        Assert.assertFalse(
                salesListPage.hasAnyDeleteAction(),
                "Expected no Delete action for normal user on Sales list, but delete controls were found."
        );
    }

    @And("admin deletes the created plant sale with quantity {string}")
    public void admin_deletes_the_created_plant_sale_with_quantity(String qty) {
        int quantity = Integer.parseInt(qty);

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();
        salesListPage.clickDeleteForSale(plant.getPlantName(), quantity);

        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(5));
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            DriverManager.getDriver().switchTo().alert().accept();
        } catch (Exception ignored) {
        }

        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();

        WebDriverWait removedWait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));
        removedWait.until(d -> !salesListPage.isSalePresent(plant.getPlantName(), quantity));

        SalesContext.clearLastSale();
    }

    @Then("Sell button should be enabled")
    public void sell_button_should_be_enabled() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(3));
        boolean enabled = wait.until(d -> sellPlantPage.isSellButtonEnabled());
        Assert.assertTrue(enabled, "Expected Sell button to be enabled.");
    }

    @Then("Sell button should be disabled")
    public void sell_button_should_be_disabled() {
        Assert.assertNotNull(sellPlantPage, "Sell Plant page is not initialized.");

        boolean disabled;
        try {
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(2));
            disabled = wait.until(d -> sellPlantPage.isSellButtonDisabled());
        } catch (TimeoutException ignored) {
            disabled = sellPlantPage.isSellButtonDisabled();
        }

        Assert.assertTrue(
                disabled,
                "Expected Sell button to be disabled. " + sellPlantPage.debugSellFormState()
        );
    }

    @Then("Sell button should be disabled or quantity warning should be displayed")
    public void sell_button_should_be_disabled_or_quantity_warning_should_be_displayed() {
        Assert.assertNotNull(sellPlantPage, "Sell Plant page is not initialized.");

        boolean disabled = sellPlantPage.isSellButtonDisabled();

        boolean quantityValid = sellPlantPage.isQuantityValid();
        String browserValidation = sellPlantPage.getQuantityValidationMessage();
        boolean hasBrowserValidation = !quantityValid
                && browserValidation != null
                && !browserValidation.trim().isEmpty();

        String visibleWarning = sellPlantPage.getVisibleWarningText();
        boolean hasVisibleWarning = visibleWarning != null && !visibleWarning.trim().isEmpty();

        Assert.assertTrue(
                disabled || hasBrowserValidation || hasVisibleWarning,
                "Expected Sell to be disabled or a quantity warning to be shown. "
                        + "sellDisabled=" + disabled
                        + ", quantityValid=" + quantityValid
                        + ", browserValidation=" + browserValidation
                        + ", visibleWarning=" + visibleWarning
        );
    }

    @Then("remaining stock should match the created plant quantity")
    public void remaining_stock_should_match_created_plant_quantity() {
        Assert.assertNotNull(sellPlantPage, "Sell Plant page is not initialized.");

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        int expected = plant.getQuantity();

        Integer actual = readRemainingStockEventually(plant.getPlantName(), expected, Duration.ofSeconds(20));

        Assert.assertNotNull(actual, "Could not read remaining stock from UI. " + sellPlantPage.debugSellFormState());
        Assert.assertEquals(actual.intValue(), expected,
                "Remaining stock mismatch. " + sellPlantPage.debugSellFormState());
    }

    @Then("remaining stock should be reduced by {int} from created plant quantity")
    public void remaining_stock_should_be_reduced_by_from_created_plant_quantity(int soldQty) {
        Assert.assertNotNull(sellPlantPage, "Sell Plant page is not initialized.");

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        int expected = plant.getQuantity() - soldQty;
        Assert.assertTrue(expected >= 0, "Expected remaining stock must be non-negative. plantQty=" + plant.getQuantity() + ", soldQty=" + soldQty);

        Integer actual = readRemainingStockEventually(plant.getPlantName(), expected, Duration.ofSeconds(15));

        Assert.assertNotNull(actual, "Could not read remaining stock from UI. " + sellPlantPage.debugSellFormState());
        Assert.assertEquals(actual.intValue(), expected,
                "Remaining stock mismatch. " + sellPlantPage.debugSellFormState());
    }

    private Integer readRemainingStockEventually(String plantName, int expected, Duration timeout) {
        long deadlineMs = System.currentTimeMillis() + timeout.toMillis();
        Integer last = null;
        int reloads = 0;

        while (System.currentTimeMillis() < deadlineMs) {
            try {
                last = sellPlantPage.getRemainingStock();
                if (last != null && last == expected) {
                    return last;
                }
            } catch (Exception ignored) {
                // ignore and retry with reload
            }

            try {
                DriverManager.getDriver().get(ConfigReader.get("ui.base.url") + "/ui/sales/new");
                sellPlantPage = new SellPlantPage(DriverManager.getDriver());
                sellPlantPage.waitForPage();
                sellPlantPage.selectPlantByName(plantName);
                reloads++;
            } catch (Exception ignored) {
                // ignore  and attempt again
            }

            if (reloads >= 6) {
                break;
            }
        }

        return last;
    }

    @Then("sale should be blocked due to exceeding remaining stock")
    public void sale_should_be_blocked_due_to_exceeding_remaining_stock() {
        Assert.assertNotNull(sellPlantPage, "Sell Plant page is not initialized.");

        boolean disabled = false;
        try {
            disabled = sellPlantPage.isSellButtonDisabled();
        } catch (Exception ignored) {
        }

        String visibleWarning = null;
        try {
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(2));
            visibleWarning = wait.until(d -> {
                try {
                    return sellPlantPage.getVisibleWarningText();
                } catch (Exception ignored) {
                    return null;
                }
            });
        } catch (Exception ignored) {
            // ignore and warning might not appear or page might navigate
        }

        boolean hasStockWarning = visibleWarning != null
                && visibleWarning.matches("(?is).*\\b(exceed|exceeds|exceeded|remaining|stock|available|quantity)\\b.*");

        if (!(disabled || hasStockWarning)) {
            System.out.println(
                "No explicit stock warning detected after clicking Sell. "
                    + "Proceeding to validate via sales row count. "
                    + "sellDisabled=" + disabled
                    + ", visibleWarning=" + visibleWarning
            );
        }
    }

    @Then("sales list should show correct total price for the created plant with quantity {string}")
    public void sales_list_should_show_correct_total_price_for_created_plant_with_quantity(String qty) {
        int quantity = Integer.parseInt(qty);

        SalesPrereqApi.PlantForSale plant = SalesPrereqApi.getCurrentPlantForSale();
        Assert.assertNotNull(plant, "Sales prerequisite plant was not created. Ensure scenario has @sales tag.");

        double expectedTotal = plant.getPrice() * quantity;
        String expectedTotalText = String.format(Locale.US, "%.2f", expectedTotal);

        DriverManager.getDriver().get(ConfigReader.get("ui.base.url") + "/ui/sales");
        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();

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

        SalesContext.setLastSale(plant.getPlantName(), quantity);
    }

    @And("admin deletes the last created sale")
    public void admin_deletes_the_last_created_sale() {
        SalesContext.LastSale lastSale = SalesContext.getLastSale();
        Assert.assertNotNull(lastSale,
                "No previous sale stored. Ensure the 'views sales list' scenario runs before this delete scenario.");

        String plantName = lastSale.getPlantName();
        int quantity = lastSale.getQuantity();

        salesListPage.clickDeleteForSale(plantName, quantity);

        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(5));
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            DriverManager.getDriver().switchTo().alert().accept();
        } catch (Exception ignored) {
        }

        salesListPage = new SalesListPage(DriverManager.getDriver());
        salesListPage.waitForPage();

        WebDriverWait removedWait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));
        removedWait.until(d -> !salesListPage.isSalePresent(plantName, quantity));
    }

    @Then("sales list should not contain the last created sale")
    public void sales_list_should_not_contain_the_last_created_sale() {
        SalesContext.LastSale lastSale = SalesContext.getLastSale();
        Assert.assertNotNull(lastSale,
                "No previous sale stored. Ensure the 'views sales list' scenario runs before this delete scenario.");

        String plantName = lastSale.getPlantName();
        int quantity = lastSale.getQuantity();

        SalesListPage.SaleRow row = salesListPage.findSaleRow(plantName, quantity);
        Assert.assertNull(row, "Sale row still present after delete. plant=" + plantName + " qty=" + quantity);

        SalesContext.clearLastSale();
    }

    @When("user sorts sales list by {string}")
    public void user_sorts_sales_list_by(String column) {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");

        Comparator<String> comparator = comparatorForColumn(column);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(6));

        boolean sorted = false;
        for (int i = 0; i < 2 && !sorted; i++) {
            salesListPage.clickColumnHeaderToSort(column);
            try {
                sorted = wait.until(d -> {
                    List<String> v = salesListPage.getColumnValues(column);
                    return isSorted(v, comparator) || isSorted(v, comparator.reversed());
                });
            } catch (Exception ignored) {
                // try again
            }
        }
    }

    @Then("sales list should be sorted by {string}")
    public void sales_list_should_be_sorted_by(String column) {
        Assert.assertNotNull(salesListPage, "Sales list page is not initialized.");

        List<String> values = salesListPage.getColumnValues(column);
        Assert.assertTrue(values.size() >= 2,
                "Expected at least 2 rows to validate sorting, but found: " + values.size());

        Comparator<String> comparator = comparatorForColumn(column);

        boolean asc = isSorted(values, comparator);
        boolean desc = isSorted(values, comparator.reversed());

        Assert.assertTrue(asc || desc,
                "Expected column to be sorted ascending or descending after clicking header. "
                        + "column=" + column + ", values=" + values);
    }

    private static double parseMoney(String text) {
        String cleaned = text.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isBlank()) {
            throw new AssertionError("Could not parse money value from: " + text);
        }
        return Double.parseDouble(cleaned);
    }

    private static Comparator<String> comparatorForColumn(String column) {
        String c = column == null ? "" : column.trim().toLowerCase(Locale.ROOT);
        if ("plant".equals(c)) {
            return Comparator.comparing(SalesSteps::safeLower, Comparator.naturalOrder());
        }
        if ("quantity".equals(c)) {
            return Comparator.comparingInt(SalesSteps::parseIntLenient);
        }
        if ("total price".equals(c) || "total".equals(c)) {
            return Comparator.comparingDouble(SalesSteps::parseMoneyOrZero);
        }
        if ("sold at".equals(c) || "soldat".equals(c)) {
            return Comparator.comparingLong(SalesSteps::parseSoldAtEpochOrZero);
        }
        return Comparator.comparing(SalesSteps::safeLower, Comparator.naturalOrder());
    }

    private static boolean isSorted(List<String> values, Comparator<String> comparator) {
        for (int i = 1; i < values.size(); i++) {
            String prev = values.get(i - 1);
            String cur = values.get(i);
            if (comparator.compare(prev, cur) > 0) {
                return false;
            }
        }
        return true;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static int parseIntLenient(String text) {
        if (text == null) {
            return 0;
        }
        String cleaned = text.replaceAll("[^0-9\\-]", "").trim();
        if (cleaned.isBlank() || cleaned.equals("-")) {
            return 0;
        }
        return Integer.parseInt(cleaned);
    }

    private static double parseMoneyOrZero(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        return parseMoney(text);
    }

    private static long parseSoldAtEpochOrZero(String text) {
        if (text == null || text.isBlank()) {
            return 0L;
        }

        String trimmed = text.trim();
        List<DateTimeFormatter> formats = new ArrayList<>();
        formats.add(DateTimeFormatter.ISO_DATE_TIME);
        formats.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        formats.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        formats.add(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
        formats.add(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
        formats.add(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        formats.add(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        for (DateTimeFormatter f : formats) {
            try {
                LocalDateTime dt = LocalDateTime.parse(trimmed, f);
                return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }

        return trimmed.toLowerCase(Locale.ROOT).hashCode();
    }
}
