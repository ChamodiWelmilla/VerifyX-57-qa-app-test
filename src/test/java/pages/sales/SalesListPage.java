package pages.sales;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SalesListPage {

    private final WebDriverWait wait;

    @FindBy(xpath = "//h3[contains(normalize-space(.),'Sales')]")
    private WebElement salesHeading;

    @FindBy(css = "a[href='/ui/sales/new']")
    private WebElement sellPlantLink;

    @FindBy(css = "table.table")
    private WebElement salesTable;

    public SalesListPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void waitForPage() {
        wait.until(ExpectedConditions.visibilityOf(salesHeading));
    }

    public void clickSellPlant() {
        wait.until(ExpectedConditions.elementToBeClickable(sellPlantLink));
        sellPlantLink.click();
    }

    public SaleRow findSaleRow(String plantName, int quantity) {
        wait.until(ExpectedConditions.visibilityOf(salesTable));

        List<WebElement> rows = salesTable.findElements(By.cssSelector("tbody tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            if (cells.size() < 3) {
                continue;
            }

            String plantCell = cells.get(0).getText() == null ? "" : cells.get(0).getText().trim();
            String qtyCell = cells.get(1).getText() == null ? "" : cells.get(1).getText().trim();

            if (plantCell.equalsIgnoreCase(plantName) && qtyCell.equals(String.valueOf(quantity))) {
                String totalCell = cells.get(2).getText() == null ? "" : cells.get(2).getText().trim();
                return new SaleRow(plantCell, qtyCell, totalCell);
            }
        }

        return null;
    }

    public static final class SaleRow {
        private final String plant;
        private final String quantity;
        private final String totalPrice;

        public SaleRow(String plant, String quantity, String totalPrice) {
            this.plant = plant;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }

        public String getPlant() {
            return plant;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getTotalPrice() {
            return totalPrice;
        }
    }
}
