package tests;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.FlightResultsPage;
import pages.HomePage;
import utils.DriverFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FlightSearchTest {

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

    @Test(description = "TC06: Search for flights using valid cities")
    public void tc06_searchWithValidCities() {
        FlightResultsPage results = homePage.searchFlights("Paris", "Buenos Aires");
        assertTrue(results.isResultsPageDisplayed(), "Results page should be displayed");
        assertTrue(driver.getCurrentUrl().contains("reserve.php"), "URL should contain reserve.php");
    }

    @Test(description = "TC07: Select a different departure city")
    public void tc07_selectDifferentDepartureCity() {
        homePage.selectDepartureCity("Boston");
        assertEquals(homePage.getSelectedDepartureCity(), "Boston", "Departure city should be Boston");
        FlightResultsPage results = homePage.clickFindFlights();
        assertTrue(results.getHeadingText().contains("Boston"),
                "Results heading should mention Boston: " + results.getHeadingText());
    }

    @Test(description = "TC08: Select a different destination city")
    public void tc08_selectDifferentDestinationCity() {
        homePage.selectDestinationCity("London");
        assertEquals(homePage.getSelectedDestinationCity(), "London", "Destination city should be London");
        FlightResultsPage results = homePage.clickFindFlights();
        assertTrue(results.getHeadingText().contains("London"),
                "Results heading should mention London: " + results.getHeadingText());
    }

    @Test(description = "TC09: Verify the selected flight route")
    public void tc09_selectedRouteAppearsInResults() {
        FlightResultsPage results = homePage.searchFlights("Philadelphia", "Rome");
        String heading = results.getHeadingText();
        assertTrue(heading.startsWith("Flights from"), "Heading should start with 'Flights from': " + heading);
        assertTrue(heading.contains("Philadelphia"), "Heading should contain Philadelphia: " + heading);
        assertTrue(heading.contains("Rome"), "Heading should contain Rome: " + heading);
    }

    @Test(description = "TC10: Verify the search results heading")
    public void tc10_resultsHeadingFormat() {
        FlightResultsPage results = homePage.searchFlights("Paris", "Buenos Aires");
        String heading = results.getHeadingText();
        Reporter.log("Results heading: " + heading, true);
        assertTrue(heading.startsWith("Flights from"), "Unexpected heading: " + heading);
        assertTrue(heading.contains(" to "), "Heading should contain ' to ': " + heading);
    }

    @Test(description = "TC11: Verify that flight results are displayed")
    public void tc11_flightResultsAreDisplayed() {
        FlightResultsPage results = homePage.searchFlights("Paris", "Buenos Aires");
        int count = results.getFlightCount();
        Reporter.log("Flights found: " + count, true);
        assertTrue(count > 0, "At least one flight should be listed");
        for (int i = 0; i < count; i++) {
            assertTrue(results.isChooseButtonAvailable(i), "Row " + i + " should have a Choose button");
        }
    }

    @Test(description = "TC12: Verify flight prices")
    public void tc12_flightPricesAreValid() {
        FlightResultsPage results = homePage.searchFlights("Paris", "Buenos Aires");
        assertTrue(results.getFlightCount() > 0, "Flights should be listed");
        for (int i = 0; i < results.getFlightCount(); i++) {
            String price = results.getPriceText(i);
            assertTrue(price.matches("^\\$\\d{1,3}(,\\d{3})*(\\.\\d{1,2})?$"),
                    "Row " + i + " price has an unexpected format: " + price);
            assertTrue(results.getPriceValue(i) > 0, "Row " + i + " price should be greater than zero");
        }
    }

    @Test(description = "TC13: Verify flight numbers")
    public void tc13_flightNumbersAreValid() {
        FlightResultsPage results = homePage.searchFlights("Paris", "Buenos Aires");
        List<String> numbers = results.getAllFlightNumbers();
        Reporter.log("Flight numbers: " + numbers, true);
        assertFalse(numbers.isEmpty(), "Flight numbers should be listed");
        for (String number : numbers) {
            assertTrue(number.matches("\\d+"), "Flight number should be numeric but was: '" + number + "'");
        }
    }

    @Test(description = "TC14: Verify departure and destination information")
    public void tc14_departureAndDestinationInformation() {
        FlightResultsPage results = homePage.searchFlights("Boston", "London");
        String heading = results.getHeadingText();
        assertTrue(heading.contains("Boston") && heading.contains("London"),
                "Heading should show both cities: " + heading);
        Reporter.log("Table headers: " + results.getHeaderTexts(), true);
        for (int i = 0; i < results.getFlightCount(); i++) {
            assertFalse(results.getDepartureTime(i).isEmpty(), "Row " + i + " departure time is empty");
            assertFalse(results.getArrivalTime(i).isEmpty(), "Row " + i + " arrival time is empty");
            assertFalse(results.getAirline(i).isEmpty(), "Row " + i + " airline is empty");
        }
    }

    @Test(description = "TC15: Test selecting the same departure and destination city")
    public void tc15_sameDepartureAndDestinationCity() {
        Set<String> commonCities = new HashSet<>(homePage.getDepartureOptions());
        commonCities.retainAll(homePage.getDestinationOptions());

        if (commonCities.isEmpty()) {
            Reporter.log("LIMITATION: BlazeDemo's departure and destination lists share no city, "
                    + "so the same city cannot be chosen on both sides. "
                    + "Identical-city validation cannot be tested through the UI.", true);
            assertTrue(commonCities.isEmpty(), "Departure and destination lists are expected to be disjoint");
            return;
        }

        String sameCity = commonCities.iterator().next();
        homePage.selectDepartureCity(sameCity);
        homePage.selectDestinationCity(sameCity);
        FlightResultsPage results = homePage.clickFindFlights();
        Reporter.log("OBSERVED: the application accepted the same city '" + sameCity
                + "' for departure and destination. Heading: " + results.getHeadingText(), true);
        assertTrue(results.isResultsPageDisplayed(), "Observed behaviour: results page opens for identical cities");
    }
}