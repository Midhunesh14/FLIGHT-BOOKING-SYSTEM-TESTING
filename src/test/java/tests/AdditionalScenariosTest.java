package tests;

import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.FlightResultsPage;
import pages.HomePage;
import utils.DriverFactory;

import java.time.Duration;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Extra test cases TC41 to TC50.
 * Note: BlazeDemo is a demo site. No real airline reservation or payment is made.
 */
public class AdditionalScenariosTest {

    private static final String DEPARTURE = "Paris";
    private static final String DESTINATION = "Buenos Aires";

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

    @Test(description = "TC41: Verify the homepage welcome heading")
    public void tc41_homepageWelcomeHeading() {
        assertTrue(homePage.isHeadingDisplayed(), "Homepage heading should be displayed");
        String heading = homePage.getHeadingText();
        Reporter.log("Homepage heading: " + heading, true);
        assertTrue(heading.contains("Welcome"), "Heading should contain 'Welcome' but was: " + heading);
    }

    @Test(description = "TC42: Search flights from several different departure cities")
    public void tc42_searchFromMultipleDepartureCities() {
        List<String> departures = homePage.getDepartureOptions();
        int limit = Math.min(3, departures.size());
        assertTrue(limit > 0, "Departure dropdown should have options");

        for (int i = 0; i < limit; i++) {
            String city = departures.get(i);
            homePage.open();
            FlightResultsPage results = homePage.searchFlights(city, DESTINATION);
            assertTrue(results.getHeadingText().contains(city),
                    "Results heading should mention " + city + " but was: " + results.getHeadingText());
            assertTrue(results.getFlightCount() > 0, "Flights should be listed from " + city);
        }
    }

    @Test(description = "TC43: Search flights to several different destination cities")
    public void tc43_searchToMultipleDestinationCities() {
        List<String> destinations = homePage.getDestinationOptions();
        int limit = Math.min(3, destinations.size());
        assertTrue(limit > 0, "Destination dropdown should have options");

        for (int i = 0; i < limit; i++) {
            String city = destinations.get(i);
            homePage.open();
            FlightResultsPage results = homePage.searchFlights(DEPARTURE, city);
            assertTrue(results.getHeadingText().contains(city),
                    "Results heading should mention " + city + " but was: " + results.getHeadingText());
            assertTrue(results.getFlightCount() > 0, "Flights should be listed to " + city);
        }
    }

    @Test(description = "TC44: Verify the flight results table column headers")
    public void tc44_resultsTableHeaders() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        List<String> headers = results.getHeaderTexts();
        Reporter.log("Table headers: " + headers, true);
        assertTrue(headers.size() >= 5, "Results table should have at least 5 columns but had " + headers.size());
        String joined = String.join(" | ", headers).toLowerCase();
        assertTrue(joined.contains("airline"), "Headers should include Airline: " + headers);
        assertTrue(joined.contains("price"), "Headers should include Price: " + headers);
    }

    @Test(description = "TC45: Verify airline names are displayed for every flight")
    public void tc45_airlineNamesAreDisplayed() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Flights should be listed");
        for (int i = 0; i < results.getFlightCount(); i++) {
            String airline = results.getAirline(i);
            assertTrue(airline.matches(".*[A-Za-z].*"),
                    "Row " + i + " airline should contain letters but was: '" + airline + "'");
        }
    }

    @Test(description = "TC46: Verify departure and arrival time format")
    public void tc46_departureAndArrivalTimeFormat() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Flights should be listed");
        for (int i = 0; i < results.getFlightCount(); i++) {
            String departs = results.getDepartureTime(i);
            String arrives = results.getArrivalTime(i);
            assertTrue(departs.matches(".*\\d{1,2}:\\d{2}.*"),
                    "Row " + i + " departure time has an unexpected format: '" + departs + "'");
            assertTrue(arrives.matches(".*\\d{1,2}:\\d{2}.*"),
                    "Row " + i + " arrival time has an unexpected format: '" + arrives + "'");
        }
    }

    @Test(description = "TC47: Find and select the cheapest flight")
    public void tc47_selectCheapestFlight() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        int count = results.getFlightCount();
        assertTrue(count > 0, "Flights should be listed");

        int cheapestIndex = 0;
        double cheapest = results.getPriceValue(0);
        double highest = cheapest;
        for (int i = 1; i < count; i++) {
            double price = results.getPriceValue(i);
            if (price < cheapest) {
                cheapest = price;
                cheapestIndex = i;
            }
            if (price > highest) {
                highest = price;
            }
        }
        Reporter.log("Cheapest flight: row " + cheapestIndex + " (" + results.getFlightNumber(cheapestIndex)
                + ") at " + cheapest + " | highest price: " + highest, true);
        assertTrue(cheapest > 0, "Cheapest price should be greater than zero");
        assertTrue(cheapest <= highest, "Cheapest price should not exceed the highest price");

        BookingPage booking = results.chooseFlight(cheapestIndex);
        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "Booking form should open for the cheapest flight");
        assertTrue(booking.isFormDisplayed(), "Booking form should be displayed");
    }

    @Test(description = "TC48: Enter card expiration month and year")
    public void tc48_enterCardExpiration() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        Reporter.log("Default expiration on the form: " + booking.getCardMonth() + "/" + booking.getCardYear(), true);

        booking.enterCardMonth("06");
        booking.enterCardYear("2031");
        assertEquals(booking.getCardMonth(), "06", "Card expiration month was not entered correctly");
        assertEquals(booking.getCardYear(), "2031", "Card expiration year was not entered correctly");
    }

    @Test(description = "TC49: Navigate back from the booking form to the flight results")
    public void tc49_navigateBackFromBookingForm() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        BookingPage booking = results.chooseFlight(0);
        assertTrue(booking.isFormDisplayed(), "Booking form should be displayed before going back");

        driver.navigate().back();
        dismissAlertIfPresent();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("reserve.php"));

        assertTrue(driver.getCurrentUrl().contains("reserve.php"), "Back should return to the flight results URL");
        Reporter.log("OBSERVED: URL after Back: " + driver.getCurrentUrl()
                + " (browsers may show a form-resubmission page for POST results).", true);
    }

    @Test(description = "TC50: Submit a completely empty booking form")
    public void tc50_submitEmptyBookingForm() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);

        booking.enterPassengerName("");
        booking.enterAddress("");
        booking.enterCity("");
        booking.enterState("");
        booking.enterZipCode("");
        booking.enterCardNumber("");
        booking.enterCardMonth("");
        booking.enterCardYear("");
        booking.enterNameOnCard("");
        assertEquals(booking.getPassengerName(), "", "Passenger name should be empty");
        assertEquals(booking.getCardNumber(), "", "Card number should be empty");

        boolean accepted = booking.submitAndCheckAccepted(5);

        if (accepted) {
            Reporter.log("OBSERVED: BlazeDemo accepted a completely empty booking form "
                    + "(no mandatory field validation). LIMITATION of the demo application.", true);
            assertTrue(booking.isConfirmationHeadingDisplayed(),
                    "Observed behaviour: confirmation page opens even with every field empty");
        } else {
            Reporter.log("OBSERVED: BlazeDemo rejected a completely empty booking form.", true);
            assertTrue(booking.isFormDisplayed(),
                    "Observed behaviour: form stays open when all fields are empty");
        }
    }

    private void dismissAlertIfPresent() {
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
            // No alert appeared, which is the normal case.
        }
    }
}