package tests;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import utils.DriverFactory;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class HomePageTest {

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

    @Test(description = "TC01: Verify that the homepage opens")
    public void tc01_homepageOpens() {
        assertTrue(homePage.getCurrentUrl().contains("blazedemo.com"),
                "Browser should be on blazedemo.com but was on: " + homePage.getCurrentUrl());
        assertTrue(homePage.areHomepageElementsDisplayed(),
                "Heading, both dropdowns and the Find Flights button should be displayed");
        Reporter.log("Homepage heading: " + homePage.getHeadingText(), true);
    }

    @Test(description = "TC02: Verify the page title")
    public void tc02_pageTitleIsBlazeDemo() {
        assertEquals(homePage.getTitle(), "BlazeDemo", "Homepage title mismatch");
    }

    @Test(description = "TC03: Verify the departure city dropdown")
    public void tc03_departureDropdownIsPopulated() {
        assertTrue(homePage.isDepartureDropdownDisplayed(), "Departure dropdown should be displayed");
        List<String> options = homePage.getDepartureOptions();
        Reporter.log("Departure options: " + options, true);
        assertFalse(options.isEmpty(), "Departure dropdown should have options");
        assertTrue(options.contains("Paris"), "Departure options should include Paris");
        assertTrue(options.contains("Boston"), "Departure options should include Boston");
        assertTrue(options.contains("Philadelphia"), "Departure options should include Philadelphia");
    }

    @Test(description = "TC04: Verify the destination city dropdown")
    public void tc04_destinationDropdownIsPopulated() {
        assertTrue(homePage.isDestinationDropdownDisplayed(), "Destination dropdown should be displayed");
        List<String> options = homePage.getDestinationOptions();
        Reporter.log("Destination options: " + options, true);
        assertFalse(options.isEmpty(), "Destination dropdown should have options");
        assertTrue(options.contains("Buenos Aires"), "Destination options should include Buenos Aires");
        assertTrue(options.contains("London"), "Destination options should include London");
        assertTrue(options.contains("Rome"), "Destination options should include Rome");
    }

    @Test(description = "TC05: Verify the Find Flights button")
    public void tc05_findFlightsButtonIsAvailable() {
        assertTrue(homePage.isFindFlightsButtonDisplayed(), "Find Flights button should be displayed");
        assertTrue(homePage.isFindFlightsButtonEnabled(), "Find Flights button should be enabled");
        assertEquals(homePage.getFindFlightsButtonLabel(), "Find Flights", "Button label mismatch");
    }
}