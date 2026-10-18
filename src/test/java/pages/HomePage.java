package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HomePage {

    public static final String BASE_URL = "https://blazedemo.com/";

    private final WebDriver driver;
    private final WebDriverWait wait;

    // BlazeDemo's dropdowns have no id, only a name attribute.
    private final By departureDropdown = By.name("fromPort");
    private final By destinationDropdown = By.name("toPort");
    private final By findFlightsButton = By.cssSelector("input.btn.btn-primary");
    private final By mainHeading = By.tagName("h1");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public HomePage open() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(findFlightsButton));
        return this;
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getHeadingText() {
        return driver.findElement(mainHeading).getText().trim();
    }

    public boolean isHeadingDisplayed() {
        return driver.findElement(mainHeading).isDisplayed();
    }

    public boolean isDepartureDropdownDisplayed() {
        return driver.findElement(departureDropdown).isDisplayed();
    }

    public boolean isDestinationDropdownDisplayed() {
        return driver.findElement(destinationDropdown).isDisplayed();
    }

    public boolean isFindFlightsButtonDisplayed() {
        return driver.findElement(findFlightsButton).isDisplayed();
    }

    public boolean isFindFlightsButtonEnabled() {
        return driver.findElement(findFlightsButton).isEnabled();
    }

    public String getFindFlightsButtonLabel() {
        return driver.findElement(findFlightsButton).getAttribute("value");
    }

    public List<String> getDepartureOptions() {
        return getOptionTexts(departureDropdown);
    }

    public List<String> getDestinationOptions() {
        return getOptionTexts(destinationDropdown);
    }

    public void selectDepartureCity(String city) {
        new Select(driver.findElement(departureDropdown)).selectByVisibleText(city);
    }

    public void selectDestinationCity(String city) {
        new Select(driver.findElement(destinationDropdown)).selectByVisibleText(city);
    }

    public String getSelectedDepartureCity() {
        return new Select(driver.findElement(departureDropdown)).getFirstSelectedOption().getText().trim();
    }

    public String getSelectedDestinationCity() {
        return new Select(driver.findElement(destinationDropdown)).getFirstSelectedOption().getText().trim();
    }

    public FlightResultsPage clickFindFlights() {
        driver.findElement(findFlightsButton).click();
        return new FlightResultsPage(driver);
    }

    public FlightResultsPage searchFlights(String departureCity, String destinationCity) {
        selectDepartureCity(departureCity);
        selectDestinationCity(destinationCity);
        return clickFindFlights();
    }

    public boolean areHomepageElementsDisplayed() {
        return isHeadingDisplayed()
                && isDepartureDropdownDisplayed()
                && isDestinationDropdownDisplayed()
                && isFindFlightsButtonDisplayed();
    }

    private List<String> getOptionTexts(By dropdownLocator) {
        List<String> texts = new ArrayList<>();
        List<WebElement> options = new Select(driver.findElement(dropdownLocator)).getOptions();
        for (WebElement option : options) {
            texts.add(option.getText().trim());
        }
        return texts;
    }
}