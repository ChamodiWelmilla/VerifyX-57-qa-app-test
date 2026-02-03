package pages.sales;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SellPlantPage {

    private final WebDriverWait wait;

    @FindBy(xpath = "//h3[contains(normalize-space(.),'Sell Plant')]")
    private WebElement sellPlantHeading;

    @FindBy(id = "plantId")
    private WebElement plantSelect;

    @FindBy(id = "quantity")
    private WebElement quantityInput;

    @FindBy(css = "button.btn.btn-primary")
    private WebElement sellButton;

    public SellPlantPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void waitForPage() {
        wait.until(ExpectedConditions.visibilityOf(sellPlantHeading));
        wait.until(ExpectedConditions.visibilityOf(plantSelect));
    }

    public void selectPlantByName(String plantName) {
        wait.until(ExpectedConditions.elementToBeClickable(plantSelect));
        Select select = new Select(plantSelect);

        try {
            select.selectByVisibleText(plantName);
            return;
        } catch (Exception ignored) {}

        for (WebElement option : select.getOptions()) {
            String text = option.getText();
            if (text != null && text.trim().equalsIgnoreCase(plantName)) {
                select.selectByVisibleText(option.getText());
                return;
            }
            if (text != null && text.toLowerCase().contains(plantName.toLowerCase())) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }

        throw new AssertionError("Plant option not found in dropdown: " + plantName);
    }

    public void setQuantity(int quantity) {
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(quantity));
    }

    public void clickSell() {
        wait.until(ExpectedConditions.elementToBeClickable(sellButton));
        sellButton.click();
    }
}
