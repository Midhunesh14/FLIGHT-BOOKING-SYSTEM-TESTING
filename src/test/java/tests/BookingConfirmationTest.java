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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Note: BlazeDemo is a demo site. No real airline reservation or payment is made.
 */
public class BookingConfirmationTest {

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

    /** Homepage -> Flight Results -> Select Flight -> Booking Form (not yet submitted). */
    private BookingPage openBookingForm() {
        return homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
    }

    /** Full workflow, submitting the booking exactly once. Returns the page positioned on the confirmation page. */
    private BookingPage reachConfirmationPage() {
        BookingPage booking = openBookingForm();
        booking.fillValidBookingForm();
        booking.submitBooking();
        return booking;
    }

    @Test(description = "TC31: Submit a completed booking form")
    public void tc31_submitCompletedBookingForm() {
        BookingPage booking = openBookingForm();
        booking.fillValidBookingForm();
        booking.setRememberMe(false);
        assertTrue(booking.isPurchaseButtonDisplayed(), "Purchase button should be available");
        booking.submitBooking();
        assertTrue(booking.isOnConfirmationPage(), "Submitting a completed form should open confirmation.php");
    }

    @Test(description = "TC32: Verify the confirmation page heading")
    public void tc32_confirmationPageHeading() {
        BookingPage booking = reachConfirmationPage();
        assertTrue(booking.isConfirmationHeadingDisplayed(), "Confirmation heading should be displayed");
        String heading = booking.getConfirmationHeadingText();
        Reporter.log("Confirmation heading: " + heading, true);
        assertTrue(heading.contains("Thank you for your purchase"), "Unexpected heading: " + heading);
    }

    @Test(description = "TC33: Verify the confirmation message")
    public void tc33_confirmationMessage() {
        BookingPage booking = reachConfirmationPage();
        assertTrue(booking.getPageTitle().toLowerCase().contains("confirmation"),
                "Page title should mention confirmation: " + booking.getPageTitle());
        String status = booking.getConfirmationField("Status");
        Reporter.log("Confirmation status: " + status, true);
        assertFalse(status.isEmpty(), "The confirmation table should show a Status value");
    }

    @Test(description = "TC34: Verify the booking ID if provided")
    public void tc34_bookingIdIsProvided() {
        BookingPage booking = reachConfirmationPage();
        String bookingId = booking.getConfirmationField("Id");
        Reporter.log("Booking Id: " + bookingId, true);
        assertFalse(bookingId.isEmpty(), "The confirmation table should show an Id value");
    }

    @Test(description = "TC35: Verify the confirmed flight details")
    public void tc35_confirmedFlightDetails() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        String chosenNumber = results.getFlightNumber(0);
        String chosenAirline = results.getAirline(0);
        BookingPage booking = results.chooseFlight(0);

        String formNumber = booking.getReservedFlightNumber();
        String formAirline = booking.getReservedAirline();
        assertFalse(formNumber.isEmpty(), "Booking form should display a flight number");
        assertFalse(formAirline.isEmpty(), "Booking form should display an airline");
        Reporter.log("OBSERVED: chosen flight " + chosenNumber + " (" + chosenAirline + ") | booking form shows "
                + formNumber + " (" + formAirline + "). LIMITATION: BlazeDemo does not carry the chosen "
                + "flight over to the booking form.", true);

        booking.fillValidBookingForm();
        booking.submitBooking();

        Reporter.log("OBSERVED: confirmation table: " + booking.getConfirmationTableText().replace("\n", " | "), true);
        assertTrue(booking.isOnConfirmationPage(), "Booking should reach the confirmation page");
    }


    @Test(description = "TC36: Verify the passenger name")
    public void tc36_passengerName() {
        BookingPage booking = openBookingForm();
        booking.fillValidBookingForm();
        assertEquals(booking.getPassengerName(), BookingPage.TEST_NAME, "Passenger name should be entered before submit");
        booking.submitBooking();

        boolean nameShown = booking.getBodyText().contains(BookingPage.TEST_NAME);
        Reporter.log("OBSERVED: confirmation page displays the passenger name: " + nameShown
                + " (BlazeDemo's confirmation table lists booking, payment and date fields only).", true);
        assertTrue(booking.isOnConfirmationPage(), "Booking should reach the confirmation page");
    }

    @Test(description = "TC37: Verify the booking price")
    public void tc37_bookingPrice() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        double flightPrice = booking.getReservedPriceValue();
        assertTrue(flightPrice > 0, "Flight price on the booking form should be greater than zero");

        booking.fillValidBookingForm();
        booking.submitBooking();

        String amount = booking.getConfirmationField("Amount");
        Reporter.log("Flight price on form: " + flightPrice + " | Amount on confirmation: " + amount, true);
        assertTrue(amount.matches(".*\\d.*"), "Confirmation Amount should contain a number but was: '" + amount + "'");
    }

    @Test(description = "TC38: Test refreshing the confirmation page")
    public void tc38_refreshConfirmationPage() {
        BookingPage booking = reachConfirmationPage();
        String idBefore = booking.getConfirmationField("Id");

        driver.navigate().refresh();
        dismissAlertIfPresent();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("confirmation.php"));

        assertTrue(booking.isOnConfirmationPage(), "Page should still be confirmation.php after refresh");
        assertTrue(booking.isConfirmationHeadingDisplayed(), "Confirmation heading should still be displayed after refresh");
        String idAfter = booking.getConfirmationField("Id");
        Reporter.log("Id before refresh: " + idBefore + " | Id after refresh: " + idAfter, true);
    }

    @Test(description = "TC39: Test navigating back from the confirmation page")
    public void tc39_navigateBackFromConfirmationPage() {
        BookingPage booking = reachConfirmationPage();
        driver.navigate().back();
        dismissAlertIfPresent();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("purchase.php"));

        assertFalse(booking.isOnConfirmationPage(), "Browser should have left the confirmation page");
        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "Back should return to the booking form URL");
        Reporter.log("OBSERVED: booking form visible again after Back: " + booking.isFormDisplayed()
                + " (browsers may show a form-resubmission page for POST results).", true);
    }

    @Test(description = "TC40: Verify the complete booking workflow")
    public void tc40_completeBookingWorkflow() {
        assertTrue(homePage.areHomepageElementsDisplayed(), "Step 1: homepage should be displayed");

        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Step 2: flights should be listed");

        String chosenNumber = results.getFlightNumber(0);
        BookingPage booking = results.chooseFlight(0);
        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "Step 3: booking form should open");
        assertFalse(booking.getReservedFlightNumber().isEmpty(), "Step 3: booking form should show a flight number");
        Reporter.log("OBSERVED: chosen flight " + chosenNumber + " | booking form flight "
                + booking.getReservedFlightNumber() + " (not carried over by BlazeDemo)", true);

        booking.fillValidBookingForm();
        assertEquals(booking.getPassengerName(), BookingPage.TEST_NAME, "Step 4: form should hold the entered name");

        booking.submitBooking();
        assertTrue(booking.isOnConfirmationPage(), "Step 5: confirmation page should open");
        assertTrue(booking.getConfirmationHeadingText().contains("Thank you for your purchase"),
                "Step 5: confirmation heading mismatch");
        assertFalse(booking.getConfirmationField("Id").isEmpty(), "Step 5: booking Id should be shown");
    }

    private void dismissAlertIfPresent() {
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
            // No alert appeared, which is the normal case.
        }
    }
}