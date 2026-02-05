package pages.sales;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellPlantPage {

    private static final By SELL_BUTTON_BY = By.cssSelector("button.btn.btn-primary");

    private final WebDriver driver;
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
        this.driver = driver;
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
        } catch (Exception ignored) {
            // Fallback to texts.
        }

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
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(SELL_BUTTON_BY));
        if (!isSellButtonEnabled()) {
            return;
        }

        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
    }

    public boolean isSellButtonEnabled() {
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(SELL_BUTTON_BY));

        String disabledAttr = button.getAttribute("disabled");
        if (disabledAttr != null) {
            return false;
        }

        String ariaDisabled = button.getAttribute("aria-disabled");
        if (ariaDisabled != null && ariaDisabled.equalsIgnoreCase("true")) {
            return false;
        }

        String classes = button.getAttribute("class");
        if (classes != null && classes.toLowerCase().contains("disabled")) {
            return false;
        }

        return button.isEnabled();
    }

    public boolean isSellButtonDisabled() {
        return !isSellButtonEnabled();
    }

    public void clearQuantity() {
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        quantityInput.clear();

        try {
            quantityInput.sendKeys(Keys.TAB);
        } catch (Exception ignored) {
            // ignore if element doesn't accept keys
        }
    }

    public String getQuantityValue() {
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        return quantityInput.getAttribute("value");
    }

    public String getSelectedPlantText() {
        wait.until(ExpectedConditions.visibilityOf(plantSelect));
        try {
            Select select = new Select(plantSelect);
            WebElement option = select.getFirstSelectedOption();
            return option == null ? null : option.getText();
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getSelectedPlantValue() {
        wait.until(ExpectedConditions.visibilityOf(plantSelect));
        try {
            Select select = new Select(plantSelect);
            WebElement option = select.getFirstSelectedOption();
            return option == null ? null : option.getAttribute("value");
        } catch (Exception ignored) {
            return null;
        }
    }

    public String debugSellFormState() {
        String visibleWarning = null;
        try {
            visibleWarning = getVisibleWarningText();
        } catch (Exception ignored) {
        }

        String browserValidation = null;
        boolean quantityValid = true;
        try {
            quantityValid = isQuantityValid();
            browserValidation = getQuantityValidationMessage();
        } catch (Exception ignored) {
        }

        WebElement button = null;
        try {
            button = driver.findElement(SELL_BUTTON_BY);
        } catch (Exception ignored) {
        }

        String disabledAttr = button == null ? null : button.getAttribute("disabled");
        String ariaDisabled = button == null ? null : button.getAttribute("aria-disabled");
        String classes = button == null ? null : button.getAttribute("class");
        Boolean seleniumEnabled = null;
        try {
            seleniumEnabled = button != null && button.isEnabled();
        } catch (Exception ignored) {
        }

        return "Sell form state: "
                + "sellEnabled=" + seleniumEnabled
                + ", disabledAttr=" + disabledAttr
                + ", ariaDisabled=" + ariaDisabled
                + ", classes=" + classes
                + ", selectedPlantText=" + getSelectedPlantText()
                + ", selectedPlantValue=" + getSelectedPlantValue()
                + ", quantityValue=" + getQuantityValue()
                + ", quantityValid=" + quantityValid
                + ", browserValidation=" + browserValidation
                + ", visibleWarning=" + visibleWarning;
    }

    public Integer getRemainingStock() {
        try {
            String selectedText = getSelectedPlantText();
            Integer fromSelected = parseStockFromPlantOptionText(selectedText);
            if (fromSelected != null) {
                return fromSelected;
            }
        } catch (Exception ignored) {
        }

        By[] directSelectors = new By[] {
                By.id("remainingStock"),
                By.id("remainingQuantity"),
                By.id("availableStock"),
                By.id("availableQuantity"),
                By.cssSelector("[data-testid='remaining-stock']"),
                By.cssSelector("[data-testid='remaining-quantity']"),
                By.cssSelector("[data-testid='available-stock']"),
                By.cssSelector("[data-testid='available-quantity']")
        };

        for (By by : directSelectors) {
            Integer parsed = parseIntFromElementIfVisible(by);
            if (parsed != null) {
                return parsed;
            }
        }

        List<WebElement> keywordEls = driver.findElements(By.xpath(
                "//*[not(self::script or self::style)][contains(translate(normalize-space(.)," +
                        "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'remain')" +
                        " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'available')" +
                        " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'stock')" +
                        " or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'quantity')]"
        ));

        for (WebElement el : keywordEls) {
            try {
                if (!el.isDisplayed()) {
                    continue;
                }
                if (isInHeaderOrNav(el)) {
                    continue;
                }

                String text = safeElementTextOrValue(el);
                if (text == null || text.trim().isEmpty()) {
                    continue;
                }

                String lower = text.toLowerCase();
                boolean relevant = lower.contains("remain") || lower.contains("available") || lower.contains("stock") || lower.contains("quantity");
                if (!relevant) {
                    continue;
                }

                Integer value = parseMostLikelyStockNumber(text);
                if (value != null) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private Integer parseStockFromPlantOptionText(String optionText) {
        if (optionText == null) {
            return null;
        }

        Matcher m = Pattern.compile("(?i)\\bstock\\b\\s*[:=]\\s*(\\d+)").matcher(optionText);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }

    private Integer parseIntFromElementIfVisible(By by) {
        try {
            List<WebElement> els = driver.findElements(by);
            if (els.isEmpty()) {
                return null;
            }
            WebElement el = els.get(0);
            if (!el.isDisplayed() || isInHeaderOrNav(el)) {
                return null;
            }
            String text = safeElementTextOrValue(el);
            return parseMostLikelyStockNumber(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safeElementTextOrValue(WebElement el) {
        try {
            String tag = el.getTagName();
            if (tag != null && tag.trim().equalsIgnoreCase("input")) {
                return el.getAttribute("value");
            }
        } catch (Exception ignored) {
        }
        try {
            return el.getText();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseMostLikelyStockNumber(String text) {
        if (text == null) {
            return null;
        }

        Pattern p = Pattern.compile("(?i)(remain(?:ing)?|available|stock|quantity)[^0-9-]*(-?\\d+)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(2));
            } catch (Exception ignored) {
            }
        }

        String lower = text.toLowerCase();
        if (!(lower.contains("remain") || lower.contains("available") || lower.contains("stock") || lower.contains("quantity"))) {
            return null;
        }

        Pattern nums = Pattern.compile("-?\\d+");
        Matcher nm = nums.matcher(text);
        Integer found = null;
        int count = 0;
        while (nm.find()) {
            count++;
            try {
                found = Integer.parseInt(nm.group());
            } catch (Exception ignored) {
                return null;
            }
            if (count > 1) {
                return null;
            }
        }
        return count == 1 ? found : null;
    }

    public boolean isQuantityValid() {
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        return (Boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].checkValidity();",
                quantityInput
        );
    }

    public String getQuantityValidationMessage() {
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        Object msg = ((JavascriptExecutor) driver).executeScript(
                "arguments[0].checkValidity(); return arguments[0].validationMessage;",
                quantityInput
        );
        return msg == null ? null : String.valueOf(msg);
    }


    public String getVisibleWarningText() {
        List<WebElement> candidates = driver.findElements(By.cssSelector(
                ".invalid-feedback,.invalid-tooltip,.alert,[role='alert'],.toast,.toast-body,"
                        + ".swal2-title,.swal2-html-container,.error,.mat-error,.MuiAlert-message,"
                        + ".MuiFormHelperText-root,.help-block,.field-validation-error"
        ));

        for (WebElement el : candidates) {
            try {
                if (!el.isDisplayed()) {
                    continue;
                }

                if (isInHeaderOrNav(el)) {
                    continue;
                }

                String tag = el.getTagName();
                if (tag != null) {
                    String t = tag.trim().toLowerCase();
                    if (t.equals("a") || t.equals("button") || t.equals("input") || t.equals("select")) {
                        continue;
                    }
                }

                String text = el.getText();
                if (text != null && !text.trim().isEmpty()) {
                    String trimmed = text.trim();
                    if (!trimmed.equalsIgnoreCase("logout")) {
                        return trimmed;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean isInHeaderOrNav(WebElement el) {
        try {
            return !el.findElements(By.xpath("ancestor::header|ancestor::nav|ancestor::*[@role='navigation']")).isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Attempts to select a placeholder/empty option (e.g., "Select plant") if present.
     * Returns true if a placeholder option was selected; otherwise false.
     */
    public boolean selectPlaceholderPlantIfPresent() {
        wait.until(ExpectedConditions.visibilityOf(plantSelect));
        Select select = new Select(plantSelect);

        for (WebElement option : select.getOptions()) {
            String value = option.getAttribute("value");
            String text = option.getText() == null ? "" : option.getText().trim();

            boolean valueEmpty = value == null || value.trim().isEmpty();
            boolean looksLikePlaceholder = text.isEmpty()
                    || text.toLowerCase().contains("select")
                    || text.toLowerCase().contains("choose")
                    || text.toLowerCase().contains("--");

            if (valueEmpty && looksLikePlaceholder) {
                select.selectByVisibleText(option.getText());
                return true;
            }
        }

        return false;
    }
}
