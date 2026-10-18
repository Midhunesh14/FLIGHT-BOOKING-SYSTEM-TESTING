package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.BookingPage;
import pages.FlightResultsPage;
import pages.HomePage;
import utils.DriverFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ResultsDataIntegrityTest {

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

    @Test(description = "TC71: Number of Choose buttons equals number of flight rows")
    public void tc71_chooseButtonPerRow() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        int rows = results.getFlightCount();
        int buttons = 0;
        for (int i = 0; i < rows; i++) {
            if (results.isChooseButtonAvailable(i)) {
                buttons++;
            }
        }
        Reporter.log("Rows: " + rows + " | Choose buttons: " + buttons, true);
        assertTrue(rows > 0, "Flights should be listed");
        assertEquals(buttons, rows, "Every flight row should have exactly one working Choose button");
    }

    @Test(description = "TC72: Every Choose button has the label 'Choose This Flight'")
    public void tc72_chooseButtonLabels() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Flights should be listed");
        for (int i = 0; i < results.getFlightCount(); i++) {
            assertEquals(results.getChooseButtonLabel(i), "Choose This Flight", "Row " + i + " button label mismatch");
        }
    }

    @Test(description = "TC73: Number of listed flights is within a sensible range")
    public void tc73_flightCountIsSensible() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        int count = results.getFlightCount();
        Reporter.log("Flights listed: " + count, true);
        assertTrue(count >= 1 && count <= 20, "Flight count should be between 1 and 20 but was " + count);
    }

    @Test(description = "TC74: Minimum, average and maximum flight prices are consistent")
    public void tc74_priceStatisticsAreConsistent() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        int count = results.getFlightCount();
        assertTrue(count > 0, "Flights should be listed");

        double min = Double.MAX_VALUE;
        double max = 0;
        double sum = 0;
        for (int i = 0; i < count; i++) {
            double price = results.getPriceValue(i);
            min = Math.min(min, price);
            max = Math.max(max, price);
            sum += price;
        }
        double average = sum / count;
        Reporter.log("Prices - min: " + min + ", average: " + average + ", max: " + max, true);
        assertTrue(min > 0, "Minimum price should be greater than zero");
        assertTrue(min <= average && average <= max, "Expected min <= average <= max");
    }

    @Test(description = "TC75: Results heading is correct for several different routes")
    public void tc75_headingMatchesSeveralRoutes() {
        String[][] routes = {{"Boston", "Dublin"}, {"Portland", "Berlin"}, {"San Diego", "New York"}};
        for (String[] route : routes) {
            homePage.open();
            FlightResultsPage results = homePage.searchFlights(route[0], route[1]);
            String heading = results.getHeadingText();
            assertTrue(heading.contains(route[0]) && heading.contains(route[1]),
                    "Heading should show " + route[0] + " and " + route[1] + " but was: " + heading);
        }
    }

    @Test(description = "TC76: Results page title belongs to the BlazeDemo site")
    public void tc76_resultsPageTitle() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        String title = results.getPageTitle();
        Reporter.log("Results page title: " + title, true);
        assertTrue(title.toLowerCase().contains("blazedemo"), "Title should contain 'BlazeDemo' but was: " + title);
    }

    @Test(description = "TC77: Refreshing the results page keeps the same route")
    public void tc77_refreshResultsPage() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        String headingBefore = results.getHeadingText();

        driver.navigate().refresh();
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
            // No alert appeared, which is the normal case.
        }

        assertTrue(driver.getCurrentUrl().contains("reserve.php"), "URL should still be reserve.php after refresh");
        assertEquals(results.getHeadingText(), headingBefore, "Heading should be unchanged after refresh");
        assertTrue(results.getFlightCount() > 0, "Flights should still be listed after refresh");
    }

    @Test(description = "TC78: Every price shows the dollar currency symbol")
    public void tc78_pricesStartWithDollarSign() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Flights should be listed");
        for (String price : results.getAllPriceTexts()) {
            assertTrue(price.startsWith("$"), "Price should start with '$' but was: '" + price + "'");
        }
    }

    @Test(description = "TC79: Every flight row has all its data columns")
    public void tc79_everyRowHasAllColumns() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        assertTrue(results.getFlightCount() > 0, "Flights should be listed");
        for (int i = 0; i < results.getFlightCount(); i++) {
            int cells = results.getRows().get(i).findElements(By.tagName("td")).size();
            assertTrue(cells >= 6, "Row " + i + " should have at least 6 columns but had " + cells);
        }
    }

    @Test(description = "TC80: First and last flights both open a complete booking form")
    public void tc80_firstAndLastFlightOpenCompleteForm() {
        FlightResultsPage results = homePage.searchFlights(DEPARTURE, DESTINATION);
        int lastIndex = results.getFlightCount() - 1;
        BookingPage firstBooking = results.chooseFlight(0);
        assertTrue(firstBooking.areAllFormFieldsDisplayed(), "Form for the first flight should be complete");

        homePage.open();
        FlightResultsPage secondResults = homePage.searchFlights(DEPARTURE, DESTINATION);
        BookingPage lastBooking = secondResults.chooseFlight(lastIndex);
        assertTrue(lastBooking.areAllFormFieldsDisplayed(), "Form for the last flight should be complete");
    }
}