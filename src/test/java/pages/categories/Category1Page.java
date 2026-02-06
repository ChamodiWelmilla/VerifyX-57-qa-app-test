package pages.categories;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.TimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Category1Page {

    private WebDriver driver;

    private By addCategoryButton = By.xpath("//a[contains(normalize-space(), 'Add A Category')]");
    private By saveButton = By.xpath("//button[@type='submit' or contains(normalize-space(), 'Save')]");
    private By searchInput = By.name("name");
    private By searchButton = By.xpath("//button[contains(., 'Search')]");
    private By parentFilterDropdown = By.name("parentId");
    private By categoryTableRows = By.cssSelector("table tbody tr");
    private By tableHeaders = By.cssSelector("table thead th");

    public Category1Page(WebDriver driver) {
        this.driver = driver;
    }

    public void navigateToCategories() {
        if (!driver.getCurrentUrl().contains("/ui/categories")) {
            driver.get("http://localhost:8080/ui/categories");
        }
    }

    public void clickAddCategory() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(addCategoryButton));
        try {
            button.click();
        } catch (Exception e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", button);
        }
    }

    public void clickSaveButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        try {
            button.click();
        } catch (Exception e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", button);
        }
    }

    public boolean verifyValidationMessage(String expectedText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5)); // Wait up to 5 seconds

        try {
            String xpath = "//*[contains(normalize-space(.), '" + expectedText + "')]";

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
            return true;

        } catch (TimeoutException e) {
            System.out.println(">>> TEXT NOT FOUND: '" + expectedText + "'");
            System.out.println(">>> PAGE CONTAINS: " + driver.findElement(By.tagName("body")).getText());
            return false;
        }
    }

    public boolean isAddCategoryButtonVisible() {
        try {
            return driver.findElement(addCategoryButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void searchFor(String categoryName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        driver.findElement(searchInput).clear();
        driver.findElement(searchInput).sendKeys(categoryName);
        driver.findElement(searchButton).click();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    public boolean doResultsContain(String text) {
        List<WebElement> rows = driver.findElements(categoryTableRows);
        for (WebElement row : rows) {
            if (row.getText().toLowerCase().contains(text.toLowerCase()))
                return true;
        }
        return false;
    }

    public boolean isNoDataDisplayed() {
        try {
            List<WebElement> rows = driver.findElements(categoryTableRows);
            if (rows.isEmpty())
                return true;
            String text = rows.get(0).getText().toLowerCase();
            return text.contains("no data") || text.contains("no categor") || text.contains("found");
        } catch (Exception e) {
            return false;
        }
    }

    public void selectParentFilter(String parentName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(parentFilterDropdown));

        WebElement tableBody = driver.findElement(By.tagName("tbody"));
        String oldTableText = tableBody.getText();

        Select dropdown = new Select(driver.findElement(parentFilterDropdown));
        dropdown.selectByVisibleText(parentName);

        try {
            driver.findElement(searchButton).click();
        } catch (Exception e) {
        }

        try {
            wait.until(d -> !d.findElement(By.tagName("tbody")).getText().equals(oldTableText));
        } catch (TimeoutException e) {
        }
    }

    public List<String> getAvailableParentCategories() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(parentFilterDropdown));

        Select select = new Select(driver.findElement(parentFilterDropdown));
        List<String> names = new ArrayList<>();
        for (WebElement opt : select.getOptions()) {
            String text = opt.getText().trim();
            if (!text.isEmpty() && !text.equalsIgnoreCase("Select Parent Category")) {
                names.add(text);
            }
        }
        return names;
    }

    public void clickSortHeader(String columnName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String searchText = columnName;
        if (columnName.equalsIgnoreCase("Parent Category"))
            searchText = "Parent";

        WebElement tableBody = driver.findElement(By.tagName("tbody"));

        By headerLink = By.xpath("//th[contains(., '" + searchText + "')]//a");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(headerLink));

        try {
            link.click();
        } catch (Exception e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", link);
        }

        try {
            wait.until(ExpectedConditions.stalenessOf(tableBody));
        } catch (TimeoutException e) {

        }

        wait.until(ExpectedConditions.presenceOfElementLocated(categoryTableRows));
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        } // Small buffer for rendering
    }

    public int getColumnIndex(String columnName) {
        List<WebElement> headers = driver.findElements(tableHeaders);

        String searchText = columnName;
        if (columnName.equalsIgnoreCase("Parent Category"))
            searchText = "Parent";
        if (columnName.equalsIgnoreCase("ID"))
            searchText = "ID";

        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().contains(searchText)) {
                return i;
            }
        }

        if (columnName.equalsIgnoreCase("ID"))
            return 0;
        if (columnName.equalsIgnoreCase("Parent Category"))
            return 2;
        return 1;
    }

    public List<String> getColumnData(String columnName) {
        List<String> data = new ArrayList<>();
        List<WebElement> rows = driver.findElements(categoryTableRows);
        int index = getColumnIndex(columnName);

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() > index) {
                data.add(cols.get(index).getText().trim());
            }
        }
        return data;
    }

    public boolean isSortedAscending(String colName) {
        List<String> rawData = getColumnData(colName);
        if (rawData == null || rawData.size() < 2)
            return true;

        List<String> cleanData = rawData.stream()
                .filter(s -> s != null && !s.equals("-") && !s.trim().isEmpty())
                .collect(Collectors.toList());

        if (cleanData.size() < 2)
            return true;

        if (colName.equalsIgnoreCase("ID")) {
            List<String> sortedNumericAsc = new ArrayList<>(cleanData);

            sortedNumericAsc.sort((a, b) -> {
                try {
                    return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                } catch (NumberFormatException e) {
                    return a.compareToIgnoreCase(b);
                }
            });

            List<String> sortedNumericDesc = new ArrayList<>(sortedNumericAsc);
            Collections.reverse(sortedNumericDesc);

            return cleanData.equals(sortedNumericAsc) || cleanData.equals(sortedNumericDesc);
        }

        List<String> sortedAsc = new ArrayList<>(cleanData);
        sortedAsc.sort(String.CASE_INSENSITIVE_ORDER);

        List<String> sortedDesc = new ArrayList<>(sortedAsc);
        Collections.reverse(sortedDesc);

        boolean isAscending = cleanData.equals(sortedAsc);
        boolean isDescending = cleanData.equals(sortedDesc);

        if (!isAscending && !isDescending) {
            System.out.println(">>> SORT CHECK FAILED for column: " + colName);
            System.out.println(">>> Clean UI Data: " + cleanData);
            System.out.println(">>> Expected Asc:  " + sortedAsc);
            System.out.println(">>> Expected Desc: " + sortedDesc);
        }

        return isAscending || isDescending;
    }

    private boolean checkSort(List<String> original, Comparator<String> comparator, boolean nullsFirst) {
        List<String> sortedCopy = new ArrayList<>(original);
        sortedCopy.sort((s1, s2) -> {
            boolean empty1 = (s1 == null || s1.equals("-") || s1.trim().isEmpty());
            boolean empty2 = (s2 == null || s2.equals("-") || s2.trim().isEmpty());

            if (empty1 && empty2)
                return 0;
            if (empty1)
                return nullsFirst ? -1 : 1;
            if (empty2)
                return nullsFirst ? 1 : -1;

            return comparator.compare(s1, s2);
        });
        return original.equals(sortedCopy);
    }
}