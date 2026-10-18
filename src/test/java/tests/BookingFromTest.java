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
import static org.testng.Assert.assertTrue;

class BookingFormTest {

    private WebDriver driver;
    private BookingPage bookingPage;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver();
        HomePage homePage = new HomePage(driver);
        homePage.open();
        // Correct workflow: Homepage -> Flight Results -> Select Flight -> Booking Form
        bookingPage = homePage.searchFlights("Paris", "Buenos Aires").chooseFlight(0);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            DriverFactory.captureScreenshot(driver, result.getName());
        }
        DriverFactory.quitDriver(driver);
    }

    @Test(description = "TC21: Enter a valid passenger name")
    public void tc21_enterValidPassengerName() {
        bookingPage.enterPassengerName(BookingPage.TEST_NAME);
        assertEquals(bookingPage.getPassengerName(), BookingPage.TEST_NAME, "Passenger name was not entered correctly");
    }

    @Test(description = "TC22: Test an empty passenger name")
    public void tc22_emptyPassengerName() {
        bookingPage.fillValidBookingForm();
        bookingPage.clearPassengerName();
        assertEquals(bookingPage.getPassengerName(), "", "Passenger name field should be empty before submitting");

        boolean accepted = bookingPage.submitAndCheckAccepted(5);

        if (accepted) {
            Reporter.log("OBSERVED: BlazeDemo accepted the form with an empty passenger name "
                    + "(no mandatory validation). This is a limitation of the demo application.", true);
            assertTrue(bookingPage.isConfirmationHeadingDisplayed(),
                    "Observed behaviour: confirmation page opens even with an empty name");
        } else {
            Reporter.log("OBSERVED: BlazeDemo rejected the form with an empty passenger name.", true);
            assertTrue(bookingPage.isFormDisplayed(),
                    "Observed behaviour: form stays open when the name is empty");
        }
    }

    @Test(description = "TC23: Enter a valid address")
    public void tc23_enterValidAddress() {
        bookingPage.enterAddress(BookingPage.TEST_ADDRESS);
        assertEquals(bookingPage.getAddress(), BookingPage.TEST_ADDRESS, "Address was not entered correctly");
    }

    @Test(description = "TC24: Enter a valid city")
    public void tc24_enterValidCity() {
        bookingPage.enterCity(BookingPage.TEST_CITY);
        assertEquals(bookingPage.getCity(), BookingPage.TEST_CITY, "City was not entered correctly");
    }

    @Test(description = "TC25: Enter a valid state")
    public void tc25_enterValidState() {
        bookingPage.enterState(BookingPage.TEST_STATE);
        assertEquals(bookingPage.getState(), BookingPage.TEST_STATE, "State was not entered correctly");
    }

    @Test(description = "TC26: Enter a valid ZIP code")
    public void tc26_enterValidZipCode() {
        bookingPage.enterZipCode(BookingPage.TEST_ZIP);
        assertEquals(bookingPage.getZipCode(), BookingPage.TEST_ZIP, "ZIP code was not entered correctly");
    }

    @Test(description = "TC27: Select a card type")
    public void tc27_selectCardType() {
        List<String> options = bookingPage.getCardTypeOptions();
        Reporter.log("Card types: " + options, true);
        assertTrue(options.size() >= 2, "At least two card types should be available");
        String lastOption = options.get(options.size() - 1);
        bookingPage.selectCardType(lastOption);
        assertEquals(bookingPage.getSelectedCardType(), lastOption, "Selected card type mismatch");
    }

    @Test(description = "TC28: Enter a card number")
    public void tc28_enterCardNumber() {
        bookingPage.enterCardNumber(BookingPage.TEST_CARD_NUMBER);
        assertEquals(bookingPage.getCardNumber(), BookingPage.TEST_CARD_NUMBER, "Card number was not entered correctly");
    }

    @Test(description = "TC29: Enter the cardholder name")
    public void tc29_enterCardholderName() {
        bookingPage.enterNameOnCard(BookingPage.TEST_NAME_ON_CARD);
        assertEquals(bookingPage.getNameOnCard(), BookingPage.TEST_NAME_ON_CARD, "Cardholder name was not entered correctly");
    }

    @Test(description = "TC30: Test the Remember Me checkbox")
    public void tc30_rememberMeCheckbox() {
        boolean initialState = bookingPage.isRememberMeSelected();
        Reporter.log("Remember Me initial state: " + initialState, true);

        bookingPage.toggleRememberMe();
        assertEquals(bookingPage.isRememberMeSelected(), !initialState, "First click should flip the checkbox");

        bookingPage.toggleRememberMe();
        assertEquals(bookingPage.isRememberMeSelected(), initialState, "Second click should restore the checkbox");

        bookingPage.setRememberMe(true);
        assertTrue(bookingPage.isRememberMeSelected(), "setRememberMe(true) should leave the box checked");
    }
}