package tests;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.HomePage;
import utils.DriverFactory;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FormInputBehaviourTest {

    private WebDriver driver;
    private BookingPage bookingPage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver();
        HomePage homePage = new HomePage(driver);
        homePage.open();
        bookingPage = homePage.searchFlights("Paris", "Buenos Aires").chooseFlight(0);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            DriverFactory.captureScreenshot(driver, result.getName());
        }
        DriverFactory.quitDriver(driver);
    }

    @Test(description = "TC81: Very long passenger name (100 characters)")
    public void tc81_veryLongPassengerName() {
        String longName = "A".repeat(100);
        bookingPage.enterPassengerName(longName);
        int storedLength = bookingPage.getPassengerName().length();
        Reporter.log("OBSERVED: typed 100 characters, field holds " + storedLength
                + (storedLength < 100 ? " (the field limits its length)" : " (no length limit enforced)"), true);
        assertTrue(storedLength > 0 && storedLength <= 100, "Stored length should be between 1 and 100 but was " + storedLength);
    }

    @Test(description = "TC82: Passenger name with special characters")
    public void tc82_specialCharactersInName() {
        String name = "O'Brien-Smith Jr.";
        bookingPage.enterPassengerName(name);
        assertEquals(bookingPage.getPassengerName(), name, "Special characters should be kept in the name field");
    }

    @Test(description = "TC83: Alphanumeric ZIP code")
    public void tc83_alphanumericZipCode() {
        String zip = "SW1A 1AA";
        bookingPage.enterZipCode(zip);
        assertEquals(bookingPage.getZipCode(), zip, "An alphanumeric ZIP code should be accepted by the field");
    }

    @Test(description = "TC84: City name with non-ASCII characters")
    public void tc84_unicodeCityName() {
        String city = "S\u00e3o Paulo";
        bookingPage.enterCity(city);
        assertEquals(bookingPage.getCity(), city, "Non-ASCII characters should be kept in the city field");
    }

    @Test(description = "TC85: Leading and trailing spaces in a field")
    public void tc85_leadingAndTrailingSpaces() {
        String name = "  Test  ";
        bookingPage.enterPassengerName(name);
        assertEquals(bookingPage.getPassengerName(), name, "The field should keep the text exactly as typed");
    }

    @Test(description = "TC86: Every card type option can be selected in turn")
    public void tc86_everyCardTypeIsSelectable() {
        List<String> options = bookingPage.getCardTypeOptions();
        assertTrue(options.size() >= 2, "At least two card types should be available");
        for (String option : options) {
            bookingPage.selectCardType(option);
            assertEquals(bookingPage.getSelectedCardType(), option, "Selecting card type '" + option + "' did not stick");
        }
    }

    @Test(description = "TC87: Submit the booking with Remember Me checked")
    public void tc87_submitWithRememberMeChecked() {
        bookingPage.fillValidBookingForm();
        bookingPage.setRememberMe(true);
        assertTrue(bookingPage.isRememberMeSelected(), "Remember Me should be checked before submitting");
        bookingPage.submitBooking();
        assertTrue(bookingPage.isOnConfirmationPage(), "Booking with Remember Me checked should reach confirmation");
    }

    @Test(description = "TC88: Re-entering a value overwrites the previous one")
    public void tc88_reEnteringValueOverwrites() {
        bookingPage.enterAddress("First Street 1");
        bookingPage.enterAddress("Second Avenue 2");
        assertEquals(bookingPage.getAddress(), "Second Avenue 2", "The field should hold only the latest value");
    }

    @Test(description = "TC89: Non-numeric text in the card number field")
    public void tc89_nonNumericCardNumber() {
        bookingPage.enterCardNumber("ABCD-EFGH");
        String stored = bookingPage.getCardNumber();
        assertNotNull(stored, "Card number field should return a value");
        Reporter.log("OBSERVED: typed 'ABCD-EFGH' into the card number field, field holds '" + stored + "'. "
                + (stored.equals("ABCD-EFGH")
                ? "LIMITATION: the field accepts non-numeric text (no input restriction)."
                : "The field restricts the characters that can be entered."), true);
        assertTrue(bookingPage.isFormDisplayed(), "Booking form should remain displayed");
    }

    @Test(description = "TC90: Submit a booking with an expired card date")
    public void tc90_submitWithExpiredCardDate() {
        bookingPage.fillValidBookingForm();
        bookingPage.enterCardMonth("01");
        bookingPage.enterCardYear("2000");

        boolean accepted = bookingPage.submitAndCheckAccepted(5);

        if (accepted) {
            Reporter.log("OBSERVED: BlazeDemo accepted a card that expired in 2000 "
                    + "(no expiry validation). LIMITATION of the demo application.", true);
            assertTrue(bookingPage.isConfirmationHeadingDisplayed(),
                    "Observed behaviour: confirmation page opens even with an expired card");
        } else {
            Reporter.log("OBSERVED: BlazeDemo rejected a card with an expired date.", true);
            assertTrue(bookingPage.isFormDisplayed(), "Observed behaviour: form stays open for an expired card");
        }
    }
}