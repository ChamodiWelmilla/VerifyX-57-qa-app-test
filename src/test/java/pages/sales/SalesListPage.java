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
import java.util.stream.Collectors;

public class SalesListPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//h3[contains(normalize-space(.),'Sales')]")
    private WebElement salesHeading;

    @FindBy(css = "a[href='/ui/sales/new']")
    private WebElement sellPlantLink;

    @FindBy(css = "table.table")
    private WebElement salesTable;

    public SalesListPage(WebDriver driver) {
        this.driver = driver;
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

    public boolean isSellPlantButtonVisible() {
        // For non-admin users, the element might not exist at all.
        try {
            return sellPlantLink != null && sellPlantLink.isDisplayed();
        } catch (Exception ignored) {
            // Element not present/attached; treat as not visible.
        }

        // Fallback: query DOM directly in case PageFactory couldn't bind it.
        try {
            return !driver.findElements(By.cssSelector("a[href='/ui/sales/new']")).isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean hasAnyDeleteAction() {
        try {
            wait.until(ExpectedConditions.visibilityOf(salesTable));
        } catch (Exception ignored) {
            // allow best-effort
        }

        try {
            return !salesTable.findElements(By.cssSelector("form button.btn-outline-danger")).isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    public String getNoSalesMessageTextIfPresent() {
        // Common patterns: message in a <div class="alert"> or a <td> empty-table cell.
        String xpath = "//*[not(self::script or self::style)][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no sales')]";

        try {
            List<WebElement> els = driver.findElements(By.xpath(xpath));
            for (WebElement el : els) {
                try {
                    if (!el.isDisplayed()) {
                        continue;
                    }
                    String text = el.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        return text.trim();
                    }
                } catch (Exception ignored) {
                    // Ignore stale / detached elements while scanning
                }
            }
        } catch (Exception ignored) {
            // If xpath lookup fails, treat as not present.
        }

        return null;
    }

    public int getSaleRowCount() {
        wait.until(ExpectedConditions.visibilityOf(salesTable));
        return salesTable.findElements(By.cssSelector("tbody tr")).size();
    }

    public void clickColumnHeaderToSort(String headerName) {
        wait.until(ExpectedConditions.visibilityOf(salesTable));
        WebElement header = findHeaderElement(headerName);

        // Some tables attach the sort handler to a nested element.
        WebElement clickable = header;
        try {
            clickable = header.findElement(By.cssSelector("button, a, [role='button']"));
        } catch (Exception ignored) {
            // fall back to the <th> itself
        }

        wait.until(ExpectedConditions.elementToBeClickable(clickable));
        clickable.click();
    }

    public List<String> getColumnValues(String headerName) {
        wait.until(ExpectedConditions.visibilityOf(salesTable));

        int idx = getHeaderIndex(headerName);
        List<WebElement> rows = salesTable.findElements(By.cssSelector("tbody tr"));

        return rows.stream()
                .map(r -> {
                    List<WebElement> cells = r.findElements(By.cssSelector("td"));
                    if (idx < 0 || idx >= cells.size()) {
                        return "";
                    }
                    String t = cells.get(idx).getText();
                    return t == null ? "" : t.trim();
                })
                .collect(Collectors.toList());
    }

    private int getHeaderIndex(String headerName) {
        List<WebElement> headers = salesTable.findElements(By.cssSelector("thead th"));
        for (int i = 0; i < headers.size(); i++) {
            String text = headers.get(i).getText();
            if (headerMatches(text, headerName)) {
                return i;
            }
        }
        throw new AssertionError("Column header not found: " + headerName);
    }

    private WebElement findHeaderElement(String headerName) {
        List<WebElement> headers = salesTable.findElements(By.cssSelector("thead th"));
        for (WebElement th : headers) {
            String text = th.getText();
            if (headerMatches(text, headerName)) {
                return th;
            }
        }
        throw new AssertionError("Column header not found: " + headerName);
    }

    private boolean headerMatches(String headerText, String expectedHeader) {
        if (headerText == null || expectedHeader == null) {
            return false;
        }
        String a = normalizeHeader(headerText);
        String b = normalizeHeader(expectedHeader);
        return !b.isEmpty() && (a.equals(b) || a.contains(b));
    }

    private String normalizeHeader(String s) {
        return s.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
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

    public WebElement findSaleRowElement(String plantName, int quantity) {
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
                return row;
            }
        }

        return null;
    }

    public boolean isSalePresent(String plantName, int quantity) {
        return findSaleRowElement(plantName, quantity) != null;
    }

    public void clickDeleteForSale(String plantName, int quantity) {
        WebElement row = findSaleRowElement(plantName, quantity);
        if (row == null) {
            throw new AssertionError("Sale row not found to delete. plant=" + plantName + " qty=" + quantity);
        }

        WebElement deleteButton = row.findElement(By.cssSelector("form button.btn-outline-danger"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
        deleteButton.click();
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
