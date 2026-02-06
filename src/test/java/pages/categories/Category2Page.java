package pages.categories;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class Category2Page {
    private WebDriver driver;
    private WebDriverWait wait;
    private By addCategoryBtn = By.xpath("//a[contains(normalize-space(), 'Add A Category')]");
    private By categoryNameField = By.id("name"); 
    private By parentCategoryDropdown = By.id("parentId");
    private By saveBtn = By.cssSelector("button[type='submit']"); 
    
    private By editBtn = By.cssSelector(".edit-action");
    private By deleteBtn = By.cssSelector(".delete-action");

    public Category2Page(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isAddButtonVisible() {
    try {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Add A Category')]")
        ));
        return btn.isDisplayed();
    } catch (Exception e) {
        System.out.println("LOG: Button not found within 10 seconds.");
        return false;
    }
}
    
    public void clickAddCategory() { 
        wait.until(ExpectedConditions.elementToBeClickable(addCategoryBtn)).click(); 
    }
    
    public void enterName(String name) { 
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameField));
        field.clear();
        field.sendKeys(name); 
    }
    
    public void selectParent(String parentName) {
    WebElement dropdownElement = wait.until(ExpectedConditions.visibilityOfElementLocated(parentCategoryDropdown));
    Select dropdown = new Select(dropdownElement);
    dropdown.selectByVisibleText(parentName);
    }
public void clickEditForCategory(String categoryName) {
   
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
    String rowXPath = "//tr[td[contains(text(), '" + categoryName + "')]]//a[contains(@href, 'edit')]";
    try {
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(rowXPath)));
        editBtn.click();
    } catch (Exception e) {
        System.out.println("ERROR: Could not find the category '" + categoryName + "' in the table.");
        System.out.println("Current Table Rows: " + driver.findElement(By.tagName("tbody")).getText());
        throw e;
    }
}
    public void clickDeleteForCategory(String categoryName) {
    String xpath = "//tr[contains(.,'" + categoryName + "')]//button[@title='Delete']";
    
    WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    deleteBtn.click();
}

public void handleConfirmationBox() {
    try {
   
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    } catch (Exception e) {
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@class='modal-content']//button[contains(text(),'OK') or contains(text(),'Delete')]")));
        confirmBtn.click();
    }
}
    
public void clickSave() { 
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
    saveBtn.click();
    try {
        wait.until(ExpectedConditions.urlContains("/ui/categories"));
    } catch (Exception e) {
        System.err.println("SAVE FAILED: Still on the same page. Check for validation errors on screen!");
        throw e;
    } 
    }

    public boolean isEditVisible() { 
        return !driver.findElements(By.xpath("//button[@title='Edit']")).isEmpty();
    }
    
    public boolean isDeleteVisible() { 
        return driver.findElements(deleteBtn).size() > 0; 
    }
    
}