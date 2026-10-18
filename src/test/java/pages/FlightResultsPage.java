package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Flight results page (reserve.php).
 * Table columns: 0 = Choose button, 1 = Flight #, 2 = Airline, 3 = Departs, 4 = Arrives, 5 = Price.
 */
public class FlightResultsPage {

    private static final int COL_FLIGHT_NUMBER = 1;
    private static final int COL_AIRLINE = 2;
    private static final int COL_DEPARTS = 3;
    private static final int COL_ARRIVES = 4;
    private static final int COL_PRICE = 5;

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By resultsHeading = By.tagName("h3");
    private final By resultsTable = By.cssSelector("table.table");
    private final By dataRows = By.xpath("//table[contains(@class,'table')]//tr[td]");
    private final By headerCells = By.xpath("//table[contains(@class,'table')]//tr/th");
    private final By chooseButtonInRow = By.xpath(".//input[@type='submit']");

    public FlightResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("reserve.php"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(resultsHeading));
    }

    public boolean isResultsPageDisplayed() {
        return driver.getCurrentUrl().contains("reserve.php")
                && driver.findElement(resultsTable).isDisplayed();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getHeadingText() {
        return driver.findElement(resultsHeading).getText().trim();
    }

    public List<WebElement> getRows() {
        return driver.findElements(dataRows);
    }

    public int getFlightCount() {
        return getRows().size();
    }

    public List<String> getHeaderTexts() {
        List<String> headers = new ArrayList<>();
        for (WebElement header : driver.findElements(headerCells)) {
            headers.add(header.getText().trim());
        }
        return headers;
    }

    public String getFlightNumber(int rowIndex) {
        return getCellText(rowIndex, COL_FLIGHT_NUMBER);
    }

    public String getAirline(int rowIndex) {
        return getCellText(rowIndex, COL_AIRLINE);
    }

    public String getDepartureTime(int rowIndex) {
        return getCellText(rowIndex, COL_DEPARTS);
    }

    public String getArrivalTime(int rowIndex) {
        return getCellText(rowIndex, COL_ARRIVES);
    }

    /** Price exactly as displayed, for example "$472.56". */
    public String getPriceText(int rowIndex) {
        return getCellText(rowIndex, COL_PRICE);
    }

    /** Price as a number, for example 472.56. */
    public double getPriceValue(int rowIndex) {
        return Double.parseDouble(getPriceText(rowIndex).replaceAll("[^0-9.]", ""));
    }

    public List<String> getAllFlightNumbers() {
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < getFlightCount(); i++) {
            numbers.add(getFlightNumber(i));
        }
        return numbers;
    }

    public List<String> getAllPriceTexts() {
        List<String> prices = new ArrayList<>();
        for (int i = 0; i < getFlightCount(); i++) {
            prices.add(getPriceText(i));
        }
        return prices;
    }

    public boolean isChooseButtonAvailable(int rowIndex) {
        List<WebElement> buttons = getRows().get(rowIndex).findElements(chooseButtonInRow);
        return !buttons.isEmpty() && buttons.get(0).isDisplayed() && buttons.get(0).isEnabled();
    }

    public String getChooseButtonLabel(int rowIndex) {
        return getRows().get(rowIndex).findElement(chooseButtonInRow).getAttribute("value");
    }

    /** Clicks "Choose This Flight" on the given row (0 = first flight) and returns the booking form page. */
    public BookingPage chooseFlight(int rowIndex) {
        getRows().get(rowIndex).findElement(chooseButtonInRow).click();
        return new BookingPage(driver);
    }

    private String getCellText(int rowIndex, int columnIndex) {
        List<WebElement> cells = getRows().get(rowIndex).findElements(By.tagName("td"));
        return cells.get(columnIndex).getText().trim();
    }
}