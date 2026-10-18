package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates and closes Chrome WebDriver instances.
 * Selenium Manager (built into Selenium 4.6+) downloads the matching ChromeDriver automatically.
 */
public final class DriverFactory {

    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(5);

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allowed-origins=*");

        // Optional: run without a visible window using  mvn clean test -Dheadless=true
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        return driver;
    }

    /** Closes the browser safely. Does nothing if the driver is null or already closed. */
    public static void quitDriver(WebDriver driver) {
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } catch (Exception ignored) {
            // Browser already closed or crashed; nothing more to clean up.
        }
    }

    /** Saves a PNG screenshot to target/screenshots/. Returns the file path, or null if it failed. */
    public static Path captureScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            return null;
        }
        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path folder = Paths.get("target", "screenshots");
            Files.createDirectories(folder);
            Path destination = folder.resolve(testName + "_" + time + ".png");
            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}