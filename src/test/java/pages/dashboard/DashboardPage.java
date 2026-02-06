package pages.dashboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;

public class DashboardPage {

    WebDriver driver;
    WebDriverWait wait;

    @FindBy(xpath = "//h3[text()='Dashboard']")
    WebElement dashboardHeading;

    @FindBy(xpath = "//a[contains(text(),'Categories')]/ancestor::div[contains(@class,'card')]//*[contains(@class,'fw-bold')]")
    WebElement categoriesSummary;

    @FindBy(xpath = "//a[contains(text(),'Plants')]/ancestor::div[contains(@class,'card')]//*[contains(@class,'fw-bold')]")
    WebElement plantsSummary;

    @FindBy(xpath = "//a[contains(text(),'Sales')]/ancestor::div[contains(@class,'card')]//*[contains(@class,'fw-bold')]")
    WebElement salesSummary;

    @FindBy(xpath = "//a[normalize-space()='Manage Plants']")
    WebElement managePlantsButton;

    @FindBy(css = "a[href*='categories']")
    WebElement categoriesMenu;

    @FindBy(css = "a[href*='plants']")
    WebElement plantsMenu;

    @FindBy(css = "a[href*='sales']")
    WebElement salesMenu;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public boolean isDashboardDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(dashboardHeading));
        return dashboardHeading.isDisplayed();
    }
<<<<<<< HEAD
}
=======

    public boolean areSummarySectionsDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(categoriesSummary));
        return categoriesSummary.isDisplayed() &&
                plantsSummary.isDisplayed() &&
                salesSummary.isDisplayed();
    }

    public void clickManagePlants() {
        wait.until(ExpectedConditions.elementToBeClickable(managePlantsButton));
        managePlantsButton.click();
    }

    public void waitForPlantListPage(String urlPart) {
        wait.until(ExpectedConditions.urlContains(urlPart));
    }


    public void clickCategories() {
        wait.until(ExpectedConditions.elementToBeClickable(categoriesMenu));
        categoriesMenu.click();
    }

    public void clickPlants() {
        wait.until(ExpectedConditions.elementToBeClickable(plantsMenu));
        plantsMenu.click();
    }

    public void clickSales() {
        wait.until(ExpectedConditions.elementToBeClickable(salesMenu));
        salesMenu.click();
    }


    public boolean isCategoriesMenuActive() {
        wait.until(ExpectedConditions.urlContains(ConfigReader.get("ui.plants.path")));
        return categoriesMenu.getAttribute("class").contains("active");
    }

    public boolean isPlantsMenuActive() {
        wait.until(ExpectedConditions.urlContains(ConfigReader.get("ui.plants.path")));
        return plantsMenu.getAttribute("class").contains("active");
    }

    public boolean isSalesMenuActive() {
        wait.until(ExpectedConditions.urlContains(ConfigReader.get("ui.sales.path")));
        return salesMenu.getAttribute("class").contains("active");
    }


}
>>>>>>> 852213ac17ea62b91da461f3e26088dbcbbb0727
