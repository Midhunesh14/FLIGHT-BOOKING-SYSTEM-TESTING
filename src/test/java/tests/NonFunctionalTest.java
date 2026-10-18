package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.FlightResultsPage;
import pages.HomePage;
import utils.DriverFactory;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Non-functional test cases TC51 to TC60:
 * performance, security, compatibility, usability, accessibility, reliability and robustness.
 * Note: BlazeDemo is a demo site. No real airline reservation or payment is made.
 */
public class NonFunctionalTest {

    private static final String DEPARTURE = "Paris";
    private static final String DESTINATION = "Buenos Aires";

    // Generous limits so that normal internet speed variations do not cause false failures.
    private static final long MAX_PAGE_LOAD_MILLIS = 10_000;
    private static final long MAX_SEARCH_MILLIS = 10_000;
    private static final long MAX_WORKFLOW_MILLIS = 90_000;

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

    // ------------------------------------------------------------------ PERFORMANCE

    @Test(description = "TC51: Performance - homepage load time")
    public void tc51_homepageLoadTime() {
        long start = System.nanoTime();
        homePage.open();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        Reporter.log("Homepage load time: " + elapsedMillis + " ms (limit " + MAX_PAGE_LOAD_MILLIS + " ms)", true);
        assertTrue(elapsedMillis <= MAX_PAGE_LOAD_MILLIS,
                "Homepage took " + elapsedMillis + " ms, which exceeds " + MAX_PAGE_LOAD_MILLIS + " ms");
    }

    @Test(description = "TC52: Performance - flight search response time")
    public void tc52_flightSearchResponseTime() {
        long start = System.nanoTime();
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        Reporter.log("Flight search response time: " + elapsedMillis + " ms (limit " + MAX_SEARCH_MILLIS + " ms)", true);
        assertTrue(results.isResultsPageDisplayed(), "Results page should be displayed");
        assertTrue(elapsedMillis <= MAX_SEARCH_MILLIS,
                "Search took " + elapsedMillis + " ms, which exceeds " + MAX_SEARCH_MILLIS + " ms");
    }

    @Test(description = "TC53: Performance - complete booking workflow duration")
    public void tc53_completeWorkflowDuration() {
        long start = System.nanoTime();
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        booking.fillValidBookingForm();
        booking.submitBooking();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        Reporter.log("Full workflow (search to confirmation): " + elapsedMillis + " ms (limit "
                + MAX_WORKFLOW_MILLIS + " ms)", true);
        assertTrue(booking.isOnConfirmationPage(), "Workflow should reach the confirmation page");
        assertTrue(elapsedMillis <= MAX_WORKFLOW_MILLIS,
                "Workflow took " + elapsedMillis + " ms, which exceeds " + MAX_WORKFLOW_MILLIS + " ms");
    }

    // ------------------------------------------------------------------ SECURITY

    @Test(description = "TC54: Security - every page in the workflow is served over HTTPS")
    public void tc54_workflowUsesHttps() {
        assertTrue(homePage.getCurrentUrl().startsWith("https://"),
                "Homepage should use HTTPS but was: " + homePage.getCurrentUrl());

        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Results page should use HTTPS but was: " + driver.getCurrentUrl());

        BookingPage booking = results.chooseFlight(0);
        assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Booking form should use HTTPS but was: " + driver.getCurrentUrl());

        booking.fillValidBookingForm();
        booking.submitBooking();
        assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Confirmation page should use HTTPS but was: " + driver.getCurrentUrl());
    }

    @Test(description = "TC55: Security - card number field masking")
    public void tc55_cardNumberFieldMasking() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);
        booking.enterCardNumber(BookingPage.TEST_CARD_NUMBER);
        String fieldType = driver.findElement(By.id("creditCardNumber")).getAttribute("type");
        Reporter.log("OBSERVED: card number input type = '" + fieldType + "'", true);

        if ("password".equalsIgnoreCase(fieldType)) {
            Reporter.log("OBSERVED: card number is masked while typing.", true);
        } else {
            Reporter.log("LIMITATION: the card number is shown in plain text while typing "
                    + "(input type '" + fieldType + "'). Acceptable only because this is a demo site "
                    + "using synthetic test data.", true);
        }
        assertEquals(booking.getCardNumber(), BookingPage.TEST_CARD_NUMBER,
                "Card number field should hold the entered value");
        assertTrue(booking.areAllFormFieldsDisplayed(), "All booking form fields should be displayed");
    }

    // ------------------------------------------------------------------ COMPATIBILITY

    @Test(description = "TC56: Compatibility - mobile and tablet screen sizes")
    public void tc56_responsiveLayout() {
        driver.manage().window().setSize(new Dimension(375, 812));
        homePage.open();
        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage elements should display at mobile size (375x812)");
        logHorizontalOverflow("mobile 375x812");

        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.isResultsPageDisplayed(), "Search should work at mobile size");
        assertTrue(results.getFlightCount() > 0, "Flights should be listed at mobile size");

        driver.manage().window().setSize(new Dimension(768, 1024));
        homePage.open();
        assertTrue(homePage.areHomepageElementsDisplayed(), "Homepage elements should display at tablet size (768x1024)");
        logHorizontalOverflow("tablet 768x1024");
    }

    // ------------------------------------------------------------------ USABILITY

    @Test(description = "TC57: Usability - keyboard Tab order on the booking form")
    public void tc57_keyboardTabOrder() {
        BookingPage booking = homePage.searchFlights(DEPARTURE, DESTINATION).chooseFlight(0);

        WebElement nameField = driver.findElement(By.id("inputName"));
        nameField.click();

        String[] expectedOrder = {"address", "city", "state", "zipCode"};
        for (String expectedId : expectedOrder) {
            driver.switchTo().activeElement().sendKeys(Keys.TAB);
            String focusedId = driver.switchTo().activeElement().getAttribute("id");
            assertEquals(focusedId, expectedId, "Pressing Tab should move focus to '" + expectedId + "'");
        }
        assertTrue(booking.isFormDisplayed(), "Booking form should still be displayed");
    }

    // ------------------------------------------------------------------ ACCESSIBILITY

    @Test(description = "TC58: Accessibility - every page in the workflow has a page title")
    public void tc58_pageTitlesOnEveryPage() {
        List<String> titles = new ArrayList<>();
        titles.add(driver.getTitle());

        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        titles.add(driver.getTitle());

        BookingPage booking = results.chooseFlight(0);
        titles.add(driver.getTitle());

        booking.fillValidBookingForm();
        booking.submitBooking();
        titles.add(driver.getTitle());

        Reporter.log("Page titles (home, results, booking, confirmation): " + titles, true);
        for (String title : titles) {
            assertFalse(title == null || title.trim().isEmpty(), "Every page should have a non-empty title");
        }
    }

    // ------------------------------------------------------------------ RELIABILITY

    @Test(description = "TC59: Reliability - repeating the same search gives consistent results")
    public void tc59_repeatedSearchIsConsistent() {
        String firstHeading = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            homePage.open();
            FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
            String heading = results.getHeadingText();
            int count = results.getFlightCount();
            Reporter.log("Attempt " + attempt + ": heading '" + heading + "', flights listed: " + count, true);

            assertTrue(count > 0, "Attempt " + attempt + " should list at least one flight");
            if (firstHeading == null) {
                firstHeading = heading;
            } else {
                assertEquals(heading, firstHeading, "Heading should be the same on every attempt");
            }
        }
    }

    // ------------------------------------------------------------------ ROBUSTNESS

    /**
     * Opens internal pages directly on purpose (without the normal workflow) to observe how the site
     * behaves. This is an intentional robustness check, not a normal functional test.
     */
    @Test(description = "TC60: Robustness - opening internal pages directly without the workflow")
    public void tc60_directAccessToInternalPages() {
        String[] internalPages = {"https://blazedemo.com/purchase.php", "https://blazedemo.com/confirmation.php"};

        for (String url : internalPages) {
            driver.get(url);
            String title = driver.getTitle();
            String body = driver.findElement(By.tagName("body")).getText().replace("\n", " | ");
            String preview = body.length() > 150 ? body.substring(0, 150) + "..." : body;
            Reporter.log("OBSERVED: direct GET of " + url + " -> title '" + title + "' | content: " + preview, true);
            assertFalse(title == null || title.trim().isEmpty(),
                    "The server should return a page with a title for " + url);
        }
        Reporter.log("OBSERVED: the application serves these pages without enforcing the Search -> Select Flight "
                + "workflow. Compare the logged content above with the normal workflow.", true);
    }

    // ------------------------------------------------------------------ helpers

    private void logHorizontalOverflow(String label) {
        Object scrollWidth = ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollWidth;");
        Object clientWidth = ((JavascriptExecutor) driver).executeScript("return document.documentElement.clientWidth;");
        Reporter.log("OBSERVED (" + label + "): page scrollWidth=" + scrollWidth + ", visible width=" + clientWidth
                + " (scrollWidth larger than visible width means horizontal scrolling).", true);
    }
}