package pages.plant;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PlantPage {
    private WebDriver driver;
    private WebDriverWait wait;


    // Existing Locators
    private By addPlantLink = By.xpath("//a[@href='/ui/plants/add']"); 
    private By nameField = By.id("name"); 
    private By categoryDropdown = By.id("categoryId"); 
    private By priceField = By.id("price"); 
    private By quantityField = By.id("quantity"); 
    private By cancelBtn = By.xpath("//a[text()='Cancel']"); 
    private By editBtn = By.xpath("//a[contains(@href, '/ui/plants/edit')]"); 
    private By deleteBtn = By.xpath("//button[@title='Delete']"); 
    private By lowStockBadge = By.xpath("//span[contains(@class, 'badge') and text()='Low']");    
    private By addPlantBtn = By.xpath("//a[@href='/ui/plants/add']"); 
    private By saveBtn = By.xpath("//button[text()='Save']"); 
    private By searchField = By.name("name");
    private By searchBtn = By.xpath("//button[text()='Search']");
    private By plantTableRows = By.xpath("//table/tbody/tr"); 
    private By actionButtons = By.xpath("//table/tbody/tr[1]/td[last()]//a | //table/tbody/tr[1]/td[last()]//button");
    private By emptyStateMessage = By.xpath("//td[contains(text(), 'No plants found')]");       private By errorMessage = By.xpath("//form//div[@class='text-danger']");

    @FindBy(css = "table tbody tr")
    List<WebElement> plantRows;
    
    public PlantPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // 3. PAGE FACTORY: Ensures @FindBy elements are usable
        PageFactory.initElements(driver, this);
    }

    public void clickCancel() {
        driver.findElement(cancelBtn).click(); 
    }

   public void clickEditFirstPlant() {
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(editBtn));
    try {
        element.click();
    } catch (Exception e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}

    public boolean isAddPlantButtonVisible() {
        return driver.findElements(addPlantBtn).size() > 0; 
    }

    public boolean isLowStockBadgeVisible() {
        return driver.findElement(lowStockBadge).isDisplayed();
    }

    public void clickAddPlant() {
    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(addPlantLink));
    try {
        element.click();
    } catch (Exception e) {
        // Fallback: If an alert banner is blocking the click
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}

    public void fillPlantForm(String name, String cat, String price, String qty) {
        driver.findElement(nameField).clear();
        driver.findElement(nameField).sendKeys(name);
        new Select(driver.findElement(categoryDropdown)).selectByVisibleText(cat);
        driver.findElement(priceField).clear();
        driver.findElement(priceField).sendKeys(price);
        driver.findElement(quantityField).clear();
        driver.findElement(quantityField).sendKeys(qty);
    }

    public void clickSave() {
    WebElement element = driver.findElement(saveBtn);
    try {
        element.click();
    } catch (Exception e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}

    public String getValidationErrorMessage() {
        // Returns the text of the first validation error found in the form 
        return driver.findElement(errorMessage).getText();
    }


    //user ui test
    public void searchPlant(String name) {
        driver.findElement(searchField).clear();
        driver.findElement(searchField).sendKeys(name);
        driver.findElement(searchBtn).click();
    }

    public boolean areEditDeleteButtonsVisible() {
        return driver.findElements(actionButtons).size() > 0; 
    }



public boolean isLowBadgeDisplayedForLowQuantityPlant() {
    try {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        // Target a span or div containing the text "Low" based on your SRS 
        WebElement lowBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//span[contains(@class, 'badge') and contains(text(), 'Low')]")
        ));
        return lowBadge.isDisplayed();
    } catch (org.openqa.selenium.TimeoutException e) {
        return false;
    }
}

    public String getEmptyStateText() {
        return driver.findElement(emptyStateMessage).getText(); 
    }
    // Inside PlantPage.java
public boolean isPlantInList(String plantName) {
    searchPlant(plantName); // Filter the list first
    List<WebElement> rows = driver.findElements(By.xpath("//table/tbody/tr"));
    if (rows.size() > 0 && !rows.get(0).getText().contains("No plants found")) {
        return rows.get(0).getText().contains(plantName);
    }
    return false;
}


    
}