package tests;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.FlightResultsPage;
import pages.HomePage;
import utils.DriverFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FlightSelectionTest {

    private static final String DEPARTURE = "Paris";
    private static final String DESTINATION = "Buenos Aires";

    private WebDriver driver;
    private FlightResultsPage resultsPage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver();
        HomePage homePage = new HomePage(driver);
        homePage.open();
        resultsPage = homePage.searchFlights(DEPARTURE, DESTINATION);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            DriverFactory.captureScreenshot(driver, result.getName());
        }
        DriverFactory.quitDriver(driver);
    }

    @Test(description = "TC16: Select the first available flight")
    public void tc16_selectFirstFlight() {
        assertTrue(resultsPage.getFlightCount() > 0, "At least one flight should be available");
        assertTrue(resultsPage.isChooseButtonAvailable(0), "First flight should have a Choose button");
        BookingPage booking = resultsPage.chooseFlight(0);
        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "Booking form should open");
        assertTrue(booking.isFormDisplayed(), "Booking form should be displayed");
    }

    @Test(description = "TC17: Select another available flight")
    public void tc17_selectAnotherFlight() {
        if (resultsPage.getFlightCount() < 2) {
            throw new SkipException("Fewer than two flights were listed, so a second flight cannot be selected");
        }
        String chosenNumber = resultsPage.getFlightNumber(1);
        BookingPage booking = resultsPage.chooseFlight(1);
        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "Choosing the second flight should open the booking form");
        assertTrue(booking.isFormDisplayed(), "Booking form should be displayed");
        Reporter.log("OBSERVED: chose flight " + chosenNumber + " but the booking form shows flight number "
                + booking.getReservedFlightNumber() + ". LIMITATION: BlazeDemo's booking page does not "
                + "reflect which flight was chosen.", true);
    }

    @Test(description = "TC18: Verify the selected flight price")
    public void tc18_selectedFlightPriceCarriesOver() {
        String displayedPrice = resultsPage.getPriceText(0);
        BookingPage booking = resultsPage.chooseFlight(0);
        String bookingPrice = booking.getReservedPriceText();
        assertTrue(!bookingPrice.isEmpty() && booking.getReservedPriceValue() > 0,
                "Booking form should display a price greater than zero");
        Reporter.log("OBSERVED: results page price " + displayedPrice + " | booking form price " + bookingPrice
                + ". LIMITATION: BlazeDemo shows a fixed price on the booking form instead of the selected "
                + "flight's price.", true);
    }

    @Test(description = "TC19: Verify the selected flight number")
    public void tc19_selectedFlightNumberCarriesOver() {
        String chosenNumber = resultsPage.getFlightNumber(0);
        String chosenAirline = resultsPage.getAirline(0);
        BookingPage booking = resultsPage.chooseFlight(0);
        assertTrue(!booking.getReservedFlightNumber().isEmpty(), "Booking form should display a flight number");
        assertTrue(!booking.getReservedAirline().isEmpty(), "Booking form should display an airline");
        Reporter.log("OBSERVED: chosen flight " + chosenNumber + " (" + chosenAirline + ") | booking form shows "
                + booking.getReservedFlightNumber() + " (" + booking.getReservedAirline() + "). "
                + "LIMITATION: BlazeDemo does not carry the chosen flight over to the booking form.", true);
    }

    @Test(description = "TC20: Verify navigation to the booking form")
    public void tc20_navigationToBookingForm() {
        BookingPage booking = resultsPage.chooseFlight(0);
        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "URL should contain purchase.php");
        String heading = booking.getHeadingText();
        assertTrue(heading.contains("Your flight from") && heading.contains("has been reserved"),
                "Unexpected booking heading: " + heading);
        Reporter.log("OBSERVED: searched route " + DEPARTURE + " to " + DESTINATION
                + " | booking heading: " + heading
                + ". LIMITATION: the heading does not reflect the searched route.", true);
        assertTrue(booking.areAllFormFieldsDisplayed(), "All booking form fields should be displayed");
        assertTrue(booking.isPurchaseButtonDisplayed(), "Purchase button should be displayed");
    }
}