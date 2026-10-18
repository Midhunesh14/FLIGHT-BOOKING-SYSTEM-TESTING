package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Booking form page (purchase.php).
 * The confirmation page (confirmation.php) has no separate page class in this project,
 * so its helper methods are at the bottom of this class.
 */
public class BookingPage {

    // Synthetic test data only. 4111111111111111 is a widely published dummy test number, not a real card.
    public static final String TEST_NAME = "Test Passenger";
    public static final String TEST_ADDRESS = "123 Demo Street";
    public static final String TEST_CITY = "Testville";
    public static final String TEST_STATE = "Demoland";
    public static final String TEST_ZIP = "12345";
    public static final String TEST_CARD_NUMBER = "4111111111111111";
    public static final String TEST_CARD_MONTH = "12";
    public static final String TEST_CARD_YEAR = "2030";
    public static final String TEST_NAME_ON_CARD = "Test Passenger";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By passengerNameField = By.id("inputName");
    private final By addressField = By.id("address");
    private final By cityField = By.id("city");
    private final By stateField = By.id("state");
    private final By zipCodeField = By.id("zipCode");
    private final By cardTypeDropdown = By.id("cardType");
    private final By cardNumberField = By.id("creditCardNumber");
    private final By cardMonthField = By.id("creditCardMonth");
    private final By cardYearField = By.id("creditCardYear");
    private final By nameOnCardField = By.id("nameOnCard");
    private final By rememberMeCheckbox = By.id("rememberMe");
    private final By purchaseButton = By.cssSelector("input[type='submit']");
    private final By pageHeading = By.tagName("h2");
    private final By confirmationHeading = By.tagName("h1");

    public BookingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("purchase.php"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(passengerNameField));
    }

    // ---------------------------------------------------------------- page information

    public String getHeadingText() {
        return driver.findElement(pageHeading).getText().trim();
    }

    public String getBodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    public String getReservedAirline() {
        return extractFromBody("Airline");
    }

    public String getReservedFlightNumber() {
        return extractFromBody("Flight Number");
    }

    /** Price text as shown on the form page, for example "472.56". */
    public String getReservedPriceText() {
        return extractFromBody("Price");
    }

    public double getReservedPriceValue() {
        return Double.parseDouble(getReservedPriceText().replaceAll("[^0-9.]", ""));
    }

    public boolean isFormDisplayed() {
        List<WebElement> nameFields = driver.findElements(passengerNameField);
        List<WebElement> buttons = driver.findElements(purchaseButton);
        return !nameFields.isEmpty() && nameFields.get(0).isDisplayed()
                && !buttons.isEmpty() && buttons.get(0).isDisplayed();
    }

    public boolean areAllFormFieldsDisplayed() {
        By[] fields = {passengerNameField, addressField, cityField, stateField, zipCodeField,
                cardTypeDropdown, cardNumberField, cardMonthField, cardYearField, nameOnCardField,
                rememberMeCheckbox, purchaseButton};
        for (By field : fields) {
            if (!driver.findElement(field).isDisplayed()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPurchaseButtonDisplayed() {
        return driver.findElement(purchaseButton).isDisplayed();
    }

    public String getPurchaseButtonLabel() {
        return driver.findElement(purchaseButton).getAttribute("value");
    }

    // ---------------------------------------------------------------- entering data

    public void enterPassengerName(String value) {
        setField(passengerNameField, value);
    }

    public void enterAddress(String value) {
        setField(addressField, value);
    }

    public void enterCity(String value) {
        setField(cityField, value);
    }

    public void enterState(String value) {
        setField(stateField, value);
    }

    public void enterZipCode(String value) {
        setField(zipCodeField, value);
    }

    public void enterCardNumber(String value) {
        setField(cardNumberField, value);
    }

    public void enterCardMonth(String value) {
        setField(cardMonthField, value);
    }

    public void enterCardYear(String value) {
        setField(cardYearField, value);
    }

    public void enterNameOnCard(String value) {
        setField(nameOnCardField, value);
    }

    public void clearPassengerName() {
        driver.findElement(passengerNameField).clear();
    }

    public List<String> getCardTypeOptions() {
        List<String> texts = new ArrayList<>();
        for (WebElement option : new Select(driver.findElement(cardTypeDropdown)).getOptions()) {
            texts.add(option.getText().trim());
        }
        return texts;
    }

    public void selectCardType(String visibleText) {
        new Select(driver.findElement(cardTypeDropdown)).selectByVisibleText(visibleText);
    }

    public String getSelectedCardType() {
        return new Select(driver.findElement(cardTypeDropdown)).getFirstSelectedOption().getText().trim();
    }

    /** Fills every field with valid synthetic data. The card type keeps its default selection. */
    public void fillValidBookingForm() {
        enterPassengerName(TEST_NAME);
        enterAddress(TEST_ADDRESS);
        enterCity(TEST_CITY);
        enterState(TEST_STATE);
        enterZipCode(TEST_ZIP);
        enterCardNumber(TEST_CARD_NUMBER);
        enterCardMonth(TEST_CARD_MONTH);
        enterCardYear(TEST_CARD_YEAR);
        enterNameOnCard(TEST_NAME_ON_CARD);
    }

    // ---------------------------------------------------------------- reading values back

    public String getPassengerName() {
        return getFieldValue(passengerNameField);
    }

    public String getAddress() {
        return getFieldValue(addressField);
    }

    public String getCity() {
        return getFieldValue(cityField);
    }

    public String getState() {
        return getFieldValue(stateField);
    }

    public String getZipCode() {
        return getFieldValue(zipCodeField);
    }

    public String getCardNumber() {
        return getFieldValue(cardNumberField);
    }

    public String getCardMonth() {
        return getFieldValue(cardMonthField);
    }

    public String getCardYear() {
        return getFieldValue(cardYearField);
    }

    public String getNameOnCard() {
        return getFieldValue(nameOnCardField);
    }

    // ---------------------------------------------------------------- Remember Me checkbox

    public boolean isRememberMeSelected() {
        return driver.findElement(rememberMeCheckbox).isSelected();
    }

    public void toggleRememberMe() {
        driver.findElement(rememberMeCheckbox).click();
    }

    public void setRememberMe(boolean shouldBeChecked) {
        if (isRememberMeSelected() != shouldBeChecked) {
            toggleRememberMe();
        }
    }

    // ---------------------------------------------------------------- submitting

    /** Clicks "Purchase Flight" and waits until the confirmation page opens. Fails if it does not open. */
    public void submitBooking() {
        driver.findElement(purchaseButton).click();
        wait.until(ExpectedConditions.urlContains("confirmation.php"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationHeading));
    }

    /**
     * Clicks "Purchase Flight" and reports whether the confirmation page opened within the given time.
     * Used for form-behaviour tests where acceptance is not assumed.
     */
    public boolean submitAndCheckAccepted(int secondsToWait) {
        driver.findElement(purchaseButton).click();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(secondsToWait))
                    .until(ExpectedConditions.urlContains("confirmation.php"));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------- confirmation page helpers

    public boolean isOnConfirmationPage() {
        return driver.getCurrentUrl().contains("confirmation.php");
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getConfirmationHeadingText() {
        return driver.findElement(confirmationHeading).getText().trim();
    }

    public boolean isConfirmationHeadingDisplayed() {
        List<WebElement> headings = driver.findElements(confirmationHeading);
        return !headings.isEmpty() && headings.get(0).isDisplayed();
    }

    /**
     * Reads a value from the confirmation table, for example label "Id" or "Amount".
     * Returns an empty string if that row does not exist.
     */
    public String getConfirmationField(String label) {
        By valueCell = By.xpath("//table//tr[normalize-space(*[1])='" + label + "']/*[2]");
        List<WebElement> cells = driver.findElements(valueCell);
        return cells.isEmpty() ? "" : cells.get(0).getText().trim();
    }

    public String getConfirmationTableText() {
        List<WebElement> tables = driver.findElements(By.tagName("table"));
        return tables.isEmpty() ? "" : tables.get(0).getText();
    }

    // ---------------------------------------------------------------- helpers

    private void setField(By locator, String value) {
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(value);
    }

    private String getFieldValue(By locator) {
        return driver.findElement(locator).getAttribute("value");
    }

    /** Finds a line like "Airline: Virgin America" in the page text and returns the part after the colon. */
    private String extractFromBody(String label) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(label) + ":\\s*(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(getBodyText());
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}