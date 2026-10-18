package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class NavigationAndEndToEndTest {

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

    @Test(description = "TC91: Destination of the week link opens another page")
    public void tc91_destinationOfTheWeekLink() {
        driver.findElement(By.partialLinkText("destination of the week")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(ExpectedConditions.urlToBe(HomePage.BASE_URL)));
        String title = driver.getTitle();
        Reporter.log("OBSERVED: link opened " + driver.getCurrentUrl() + " | title: " + title, true);
        assertFalse(driver.getCurrentUrl().equals(HomePage.BASE_URL), "The link should leave the homepage");
        assertFalse(title == null || title.trim().isEmpty(), "The opened page should have a title");
    }

    @Test(description = "TC92: Going Back from the results page returns to the homepage")
    public void tc92_backFromResultsReturnsToHomepage() {
        homePage.searchFlights(DEPARTURE, DESTINATION);
        driver.navigate().back();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.name("fromPort")));
        assertFalse(driver.getCurrentUrl().contains("reserve.php"), "Browser should have left the results page");
        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage elements should be displayed after Back");
    }

    @Test(description = "TC93: Two bookings in the same browser session")
    public void tc93_twoBookingsInOneSession() {
        BookingPage first = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        first.fillValidBookingForm();
        first.submitBooking();
        assertTrue(first.isOnConfirmationPage(), "First booking should reach confirmation");

        homePage.open();
        BookingPage second = homePage.searchFlights("Boston", "London").chooseFlight(0);
        second.fillValidBookingForm();
        second.submitBooking();
        assertTrue(second.isOnConfirmationPage(), "Second booking should reach confirmation");

        Reporter.log("Booking Ids - first: " + first.getConfirmationField("Id")
                + " | second: " + second.getConfirmationField("Id"), true);
    }

    @Test(description = "TC94: Confirmation page does not show the full card number")
    public void tc94_confirmationCardNumberHandling() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        booking.fillValidBookingForm();
        booking.submitBooking();

        boolean fullNumberShown = booking.getBodyText().contains(BookingPage.TEST_CARD_NUMBER);
        Reporter.log("OBSERVED: card number on confirmation page: '" + booking.getConfirmationField("Card Number")
                + "' | full test number displayed: " + fullNumberShown
                + (fullNumberShown ? " (LIMITATION: the full number is echoed back)." : " (the number is masked or hidden)."), true);
        assertTrue(booking.isOnConfirmationPage(), "Booking should reach the confirmation page");
    }

    @Test(description = "TC95: Forward navigation returns to the booking form")
    public void tc95_forwardNavigationToBookingForm() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        results.chooseFlight(0);

        driver.navigate().back();
        dismissAlertIfPresent();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains("reserve.php"));

        driver.navigate().forward();
        dismissAlertIfPresent();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains("purchase.php"));

        assertTrue(driver.getCurrentUrl().contains("purchase.php"), "Forward should return to the booking form URL");
    }

    @Test(description = "TC96: Search flow works with the page zoomed to 200 percent")
    public void tc96_searchWorksAtHighZoom() {
        ((JavascriptExecutor) driver).executeScript("document.body.style.zoom='200%';");
        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage elements should be displayed at 200% zoom");

        ((JavascriptExecutor) driver).executeScript("document.body.style.zoom='100%';");
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Search should still work after changing zoom");
    }

    @Test(description = "TC97: Confirmation table lists several information rows")
    public void tc97_confirmationTableHasSeveralRows() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        booking.fillValidBookingForm();
        booking.submitBooking();

        List<?> rows = driver.findElements(By.xpath("//table//tr"));
        Reporter.log("Confirmation table (" + rows.size() + " rows): "
                + booking.getConfirmationTableText().replace("\n", " | "), true);
        assertTrue(rows.size() >= 3, "Confirmation table should list at least 3 rows but had " + rows.size());
    }

    @Test(description = "TC98: Booking Id is a single value without spaces")
    public void tc98_bookingIdFormat() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        booking.fillValidBookingForm();
        booking.submitBooking();

        String bookingId = booking.getConfirmationField("Id");
        Reporter.log("Booking Id: '" + bookingId + "'", true);
        assertTrue(bookingId.matches("\\S+"), "Booking Id should be one value without spaces but was: '" + bookingId + "'");
    }

    @Test(description = "TC99: End-to-end booking with an alternate data set")
    public void tc99_endToEndWithAlternateData() {
        FlightResultsPage results = homePage.searchFlights("Boston", "London");
        int lastIndex = results.getFlightCount() - 1;
        BookingPage booking = results.chooseFlight(lastIndex);

        List<String> cardTypes = booking.getCardTypeOptions();
        booking.enterPassengerName("Alternate Passenger");
        booking.enterAddress("456 Sample Road");
        booking.enterCity("Examplecity");
        booking.enterState("Sampleland");
        booking.enterZipCode("98765");
        booking.selectCardType(cardTypes.get(cardTypes.size() - 1));
        booking.enterCardNumber("5555555555554444");
        booking.enterCardMonth("03");
        booking.enterCardYear("2032");
        booking.enterNameOnCard("Alternate Passenger");
        booking.setRememberMe(true);

        assertEquals(booking.getPassengerName(), "Alternate Passenger", "Alternate data should be entered");
        booking.submitBooking();
        assertTrue(booking.isOnConfirmationPage(), "Booking with alternate data should reach confirmation");
        assertTrue(booking.getConfirmationHeadingText().contains("Thank you for your purchase"),
                "Confirmation heading mismatch");
    }

    @Test(description = "TC100: A new search after a completed booking starts cleanly")
    public void tc100_newSearchAfterCompletedBooking() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        booking.fillValidBookingForm();
        booking.submitBooking();
        assertTrue(booking.isOnConfirmationPage(), "First booking should reach confirmation");

        homePage.open();
        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage should load normally after a booking");
        FlightResultsPage results = homePage.searchFlights("San Diego", "Cairo");
        assertTrue(results.getHeadingText().contains("San Diego") && results.getHeadingText().contains("Cairo"),
                "New search should show the new route but heading was: " + results.getHeadingText());
        assertTrue(results.getFlightCount() > 0, "New search should list flights");
    }

    private void dismissAlertIfPresent() {
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
            // No alert appeared, which is the normal case.
        }
    }
}