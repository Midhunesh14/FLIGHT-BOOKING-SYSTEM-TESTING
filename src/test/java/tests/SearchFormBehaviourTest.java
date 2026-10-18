package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.FlightResultsPage;
import pages.HomePage;
import utils.DriverFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SearchFormBehaviourTest {

    private WebDriver driver;
    private HomePage homePage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver();
        homePage = new HomePage(driver);
        homePage.open();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            DriverFactory.captureScreenshot(driver, result.getName());
        }
        DriverFactory.quitDriver(driver);
    }

    @Test(description = "TC61: Default dropdown selections on page load")
    public void tc61_defaultSelectionsOnLoad() {
        List<String> departures = homePage.getDepartureOptions();
        List<String> destinations = homePage.getDestinationOptions();
        Reporter.log("Default departure: " + homePage.getSelectedDepartureCity()
                + " | default destination: " + homePage.getSelectedDestinationCity(), true);
        assertEquals(homePage.getSelectedDepartureCity(), departures.get(0),
                "The first departure option should be selected by default");
        assertEquals(homePage.getSelectedDestinationCity(), destinations.get(0),
                "The first destination option should be selected by default");
    }

    @Test(description = "TC62: Departure dropdown contains no duplicate cities")
    public void tc62_departureOptionsHaveNoDuplicates() {
        List<String> departures = homePage.getDepartureOptions();
        assertEquals(new HashSet<>(departures).size(), departures.size(),
                "Departure options should be unique: " + departures);
    }

    @Test(description = "TC63: Destination dropdown contains no duplicate cities")
    public void tc63_destinationOptionsHaveNoDuplicates() {
        List<String> destinations = homePage.getDestinationOptions();
        assertEquals(new HashSet<>(destinations).size(), destinations.size(),
                "Destination options should be unique: " + destinations);
    }

    @Test(description = "TC64: Dropdown options are unchanged after a page refresh")
    public void tc64_optionsConsistentAfterRefresh() {
        List<String> departuresBefore = homePage.getDepartureOptions();
        List<String> destinationsBefore = homePage.getDestinationOptions();

        driver.navigate().refresh();

        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage elements should be displayed after refresh");
        assertEquals(homePage.getDepartureOptions(), departuresBefore, "Departure options changed after refresh");
        assertEquals(homePage.getDestinationOptions(), destinationsBefore, "Destination options changed after refresh");
    }

    @Test(description = "TC65: Search using the last destination option in the list")
    public void tc65_searchWithLastDestinationOption() {
        List<String> destinations = homePage.getDestinationOptions();
        String lastDestination = destinations.get(destinations.size() - 1);
        FlightResultsPage results = homePage.searchFlights("Paris", lastDestination);
        Reporter.log("Last destination option: " + lastDestination + " | heading: " + results.getHeadingText(), true);
        assertTrue(results.getHeadingText().contains(lastDestination),
                "Results heading should mention " + lastDestination + " but was: " + results.getHeadingText());
        assertTrue(results.getFlightCount() > 0, "Flights should be listed to " + lastDestination);
    }

    @Test(description = "TC66: Homepage remains usable after going Back from the results page")
    public void tc66_homepageUsableAfterBackFromResults() {
        homePage.selectDepartureCity("Boston");
        homePage.selectDestinationCity("London");
        homePage.clickFindFlights();

        driver.navigate().back();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.name("fromPort")));

        Reporter.log("OBSERVED: after Back, departure = " + homePage.getSelectedDepartureCity()
                + ", destination = " + homePage.getSelectedDestinationCity()
                + " (the browser may or may not restore earlier selections).", true);
        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage elements should be displayed after Back");
        assertTrue(homePage.getCurrentUrl().contains("blazedemo.com"), "Browser should be back on the homepage");
    }

    @Test(description = "TC67: No dropdown option has a blank label")
    public void tc67_noBlankDropdownOptions() {
        for (String city : homePage.getDepartureOptions()) {
            assertFalse(city.isEmpty(), "Departure dropdown should not contain a blank option");
        }
        for (String city : homePage.getDestinationOptions()) {
            assertFalse(city.isEmpty(), "Destination dropdown should not contain a blank option");
        }
    }

    @Test(description = "TC68: Every departure city can be selected and read back")
    public void tc68_everyDepartureCityIsSelectable() {
        for (String city : homePage.getDepartureOptions()) {
            homePage.selectDepartureCity(city);
            assertEquals(homePage.getSelectedDepartureCity(), city, "Selecting " + city + " did not stick");
        }
    }

    @Test(description = "TC69: Changing the selection several times keeps the last choice")
    public void tc69_lastSelectionWins() {
        homePage.selectDepartureCity("Boston");
        homePage.selectDepartureCity("Portland");
        homePage.selectDestinationCity("Rome");
        homePage.selectDestinationCity("Dublin");

        assertEquals(homePage.getSelectedDepartureCity(), "Portland", "Last departure choice should be kept");
        assertEquals(homePage.getSelectedDestinationCity(), "Dublin", "Last destination choice should be kept");

        FlightResultsPage results = homePage.clickFindFlights();
        assertTrue(results.getHeadingText().contains("Portland") && results.getHeadingText().contains("Dublin"),
                "Search should use the last choices: " + results.getHeadingText());
    }

    @Test(description = "TC70: Dropdown option order is unchanged after making a selection")
    public void tc70_optionOrderUnchangedAfterSelection() {
        List<String> before = homePage.getDepartureOptions();
        homePage.selectDepartureCity(before.get(before.size() - 1));
        assertEquals(homePage.getDepartureOptions(), before, "Selecting an option should not reorder the list");
    }
}