package pages.plants;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class PlantListPage {

    WebDriver driver;
    WebDriverWait wait;

    // Use a CSS selector for the table rows as per Lecture 13 standards
    @FindBy(css = "table tbody tr")
    List<WebElement> plantRows;

    @FindBy(css = "table thead tr th:nth-child(4) > a")
    WebElement quantityHeader;

    @FindBy(css = "table tbody tr td:nth-child(4)")
    List<WebElement> quantityCells;


    public PlantListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public boolean isLowBadgeDisplayedForLowQuantityPlant() {
        // Wait for elements to be present
        wait.until(ExpectedConditions.visibilityOfAllElements(plantRows));

        for (WebElement row : plantRows) {
            // Use CSS for the quantity cell
            WebElement quantityCell = row.findElement(By.cssSelector("td:nth-child(4)"));

            // Scroll to the row (Good practice for long tables!)
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", quantityCell);
            String cellText = quantityCell.getText().trim();
            String numberOnly = cellText.split(" ")[0];
            int quantity = Integer.parseInt(numberOnly);

            if (quantity < 5) {
                // Using a CSS selector for the badge class is often faster than XPath
                WebElement lowBadge = row.findElement(By.cssSelector("td:nth-child(4) span.badge.bg-danger.ms-2"));
                return lowBadge.isDisplayed() && lowBadge.getText().contains("Low");
            }
        }
        return false;
    }

    public void clickQuantityHeader() {
        wait.until(ExpectedConditions.elementToBeClickable(quantityHeader));
        quantityHeader.click();
    }

    public List<Integer> getAllQuantities() {
        wait.until(ExpectedConditions.visibilityOfAllElements(quantityCells));
        return quantityCells.stream()
                .map(e -> {
                    // This regex replaces everything that IS NOT a digit with nothing
                    String textOnlyNumbers = e.getText().replaceAll("[^0-9]", "").trim();
                    return Integer.parseInt(textOnlyNumbers);
                })
                .collect(Collectors.toList());
    }
}
