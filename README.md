# Flight Booking System Automation Tests

## Project overview

This repository is a Java UI-automation test project for the public [BlazeDemo flight-booking demo](https://blazedemo.com/). It uses Selenium WebDriver to exercise the demo's flight search, flight selection, booking form, confirmation page, navigation, and selected non-functional behaviors.

This is **not** a production flight-booking application or a real booking/payment system. The suite uses synthetic test data and interacts with a third-party demo website, so test results can depend on network and site availability. Do not enter real personal or payment information.

## Technology and prerequisites

- Java 17 or later
- Apache Maven
- Google Chrome installed
- Internet access to reach BlazeDemo

The project uses Selenium Java 4.25.0, TestNG 7.10.2, and Maven Surefire 3.2.5. Selenium Manager (included with Selenium) resolves the matching ChromeDriver when required.

## Project structure

```text
pom.xml                             Maven dependencies, Java level, and test runner
testing.xml                         TestNG suite and test class configuration
src/test/java/pages/                Page objects for home, results, and booking/confirmation
src/test/java/tests/                100 numbered UI test cases (TC01–TC100)
src/test/java/utils/DriverFactory.java
                                    Chrome setup, teardown, and failure screenshots
target/surefire-reports/            Generated test reports (after running Maven)
```

The page objects centralize browser interactions; the TestNG classes contain the test scenarios. `BookingPage` also has helper methods for reading the confirmation page.

## Run the tests

Run the full TestNG suite from the repository root:

```bash
mvn clean test
```

Run Chrome in headless mode:

```bash
mvn clean test -Dheadless=true
```

The `headless` property is optional; the driver is visible by default. Maven runs the classes listed in `testing.xml`. Each test creates and closes its own Chrome session. When a test fails, the teardown attempts to save a screenshot under `target/screenshots/`.

## Test reports

After a run, inspect:

- `target/surefire-reports/index.html` — Surefire HTML report
- `target/surefire-reports/testng-results.xml` — TestNG results and reporter output
- `target/surefire-reports/TestSuite.txt` — concise suite summary

The report already present in this repository records **100 tests run, 100 passed, 0 failed, 0 errors, and 0 skipped** (about 7 minutes 45 seconds). This is a snapshot from an earlier run; rerun the suite for current results. A `PASS` in the catalog means the automation assertion passed against the demo's behavior. It does not imply that every observed behavior is desirable or production-ready.

## Known demo-site limitations observed by the tests

- BlazeDemo accepts empty booking forms and expired card dates in the recorded run; required-field and expiry validation should not be assumed.
- Booking/confirmation pages do not consistently carry forward the selected flight, route, price, or passenger name. Several tests only assert that the corresponding page and non-empty values are present.
- The card-number form field is plain text while typing in the observed run, although the confirmation page masks the card number.
- The responsive-layout test records horizontal overflow observations; it does not assert that overflow is absent.
- The departure and destination city lists have no shared city, so TC15 cannot exercise an identical-city search.
- Internal purchase/confirmation pages can be opened directly in the observed run.

## Test case catalog

`Input` shows the data/action used by the corresponding automated test. `Output` describes the assertion or observed result, not an independently guaranteed product requirement. The status column reflects the existing Surefire report noted above.

| tc_id | description | input | output | status |
|---|---|---|---|---|
| TC01 | Verify that the homepage opens | Open `https://blazedemo.com/` | BlazeDemo URL and homepage controls are displayed | PASS |
| TC02 | Verify the page title | Load homepage | Title is `BlazeDemo` | PASS |
| TC03 | Verify the departure city dropdown | Inspect departure options | Dropdown populated with cities including Paris, Boston, Philadelphia | PASS |
| TC04 | Verify the destination city dropdown | Inspect destination options | Dropdown populated with cities including Buenos Aires, London, Rome | PASS |
| TC05 | Verify the Find Flights button | Inspect homepage submit button | Visible, enabled, and labeled `Find Flights` | PASS |
| TC06 | Search for flights using valid cities | Paris to Buenos Aires | Results page opens at `reserve.php` | PASS |
| TC07 | Select a different departure city | Boston to default destination | Boston remains selected and appears in results heading | PASS |
| TC08 | Select a different destination city | Default departure to London | London remains selected and appears in results heading | PASS |
| TC09 | Verify the selected flight route | Philadelphia to Rome | Results heading names both route cities | PASS |
| TC10 | Verify the search results heading | Paris to Buenos Aires | Heading starts `Flights from` and contains ` to ` | PASS |
| TC11 | Verify that flight results are displayed | Paris to Buenos Aires | One or more flight rows appear; each row has a Choose button | PASS |
| TC12 | Verify flight prices | Paris to Buenos Aires | Every price matches dollar/decimal format and is greater than zero | PASS |
| TC13 | Verify flight numbers | Paris to Buenos Aires | Flight-number list is non-empty and each value is numeric | PASS |
| TC14 | Verify departure and destination information | Boston to London | Route is shown; each row has airline and departure/arrival values | PASS |
| TC15 | Test selecting the same departure and destination city | Inspect city dropdown overlap | Dropdowns share no city; same-city validation cannot be tested in the UI | PASS |
| TC16 | Select the first available flight | Search Paris to Buenos Aires; choose row 1 | Booking form opens at `purchase.php` | PASS |
| TC17 | Select another available flight | Choose second result row | Booking form opens; booking flight number may not match selected row | PASS |
| TC18 | Verify the selected flight price | Choose first result row | Booking form has a positive price; recorded result page/form prices differed | PASS |
| TC19 | Verify the selected flight number | Choose first result row | Booking form contains flight number and airline; not necessarily selected flight | PASS |
| TC20 | Verify navigation to the booking form | Choose first flight | Booking heading, form fields, and Purchase button are displayed | PASS |
| TC21 | Enter a valid passenger name | `Test Passenger` | Name field reads back the entered value | PASS |
| TC22 | Test an empty passenger name | Valid form data with passenger name cleared | Recorded run accepted the empty name and showed confirmation | PASS |
| TC23 | Enter a valid address | `123 Demo Street` | Address field reads back the entered value | PASS |
| TC24 | Enter a valid city | `Testville` | City field reads back the entered value | PASS |
| TC25 | Enter a valid state | `Demoland` | State field reads back the entered value | PASS |
| TC26 | Enter a valid ZIP code | `12345` | ZIP field reads back the entered value | PASS |
| TC27 | Select a card type | Select last listed card type | Selected card type matches the choice | PASS |
| TC28 | Enter a card number | Synthetic test number `4111111111111111` | Card-number field reads back the test value | PASS |
| TC29 | Enter the cardholder name | `Test Passenger` | Name-on-card field reads back the entered value | PASS |
| TC30 | Test the Remember Me checkbox | Toggle twice, then set checked | Toggles invert and restore state; final state is checked | PASS |
| TC31 | Submit a completed booking form | Valid synthetic booking details; Remember Me off | Confirmation page opens | PASS |
| TC32 | Verify the confirmation page heading | Submit a valid booking | Heading contains `Thank you for your purchase` | PASS |
| TC33 | Verify the confirmation message | Submit a valid booking | Confirmation title mentions confirmation and Status is present | PASS |
| TC34 | Verify the booking ID if provided | Submit a valid booking | Confirmation table contains a non-empty Id | PASS |
| TC35 | Verify the confirmed flight details | Choose first flight and submit valid details | Booking/confirmation displays non-empty flight details; selection may not carry over | PASS |
| TC36 | Verify the passenger name | Submit valid passenger details | Confirmation page opens; passenger name was not shown in recorded confirmation | PASS |
| TC37 | Verify the booking price | Submit valid booking | Booking price is positive and confirmation Amount contains a number; values may differ | PASS |
| TC38 | Test refreshing the confirmation page | Submit and refresh confirmation | Confirmation page and heading remain; booking Id may change | PASS |
| TC39 | Test navigating back from the confirmation page | Submit booking, then browser Back | Browser returns to the booking form; resubmission prompt may appear | PASS |
| TC40 | Verify the complete booking workflow | Search, choose flight, enter valid details, submit | End-to-end flow reaches confirmation | PASS |
| TC41 | Verify the homepage welcome heading | Open homepage | Visible heading contains `Welcome` | PASS |
| TC42 | Search flights from several different departure cities | First three departure options to Buenos Aires | Each search shows the chosen departure and flight results | PASS |
| TC43 | Search flights to several different destination cities | Paris to first three destination options | Each search shows the chosen destination and flight results | PASS |
| TC44 | Verify the flight results table column headers | Paris to Buenos Aires | Results table has at least five headers including Airline and Price | PASS |
| TC45 | Verify airline names are displayed for every flight | Paris to Buenos Aires | Each result has an airline name containing letters | PASS |
| TC46 | Verify departure and arrival time format | Paris to Buenos Aires | Each row's departure and arrival includes an hour/minute time | PASS |
| TC47 | Find and select the cheapest flight | Search and choose minimum-priced result | Cheapest price is positive and its booking form opens | PASS |
| TC48 | Enter card expiration month and year | Month `06`, year `2031` | Both expiration fields retain the entered values | PASS |
| TC49 | Navigate back from the booking form to the flight results | Choose a flight, then Back | Browser returns to the `reserve.php` results URL | PASS |
| TC50 | Submit a completely empty booking form | Clear all booking fields and submit | Recorded run accepted the empty form and opened confirmation | PASS |
| TC51 | Performance - homepage load time | Load homepage | Completes within 10,000 ms; recorded 481 ms | PASS |
| TC52 | Performance - flight search response time | Search Paris to Buenos Aires | Results appear within 10,000 ms; recorded 750 ms | PASS |
| TC53 | Performance - complete booking workflow duration | Search, choose, fill, and submit | Confirmation within 90,000 ms; recorded 3,065 ms | PASS |
| TC54 | Security - every page in the workflow is served over HTTPS | Visit home, results, booking, confirmation | All workflow URLs start with `https://` | PASS |
| TC55 | Security - card number field masking | Type synthetic card number | Field retains value; recorded field type was plain `text` (not masked) | PASS |
| TC56 | Compatibility - mobile and tablet screen sizes | View/search at 375x812 and 768x1024 | Homepage and search remain usable; horizontal overflow was logged | PASS |
| TC57 | Usability - keyboard Tab order on the booking form | Focus name field and press Tab through fields | Focus moves through the expected form field order | PASS |
| TC58 | Accessibility - every page in the workflow has a page title | Visit each workflow page | Each page has a non-empty title | PASS |
| TC59 | Reliability - repeating the same search gives consistent results | Repeat Paris to Buenos Aires search three times | Each attempt returns flights and the same heading | PASS |
| TC60 | Robustness - opening internal pages directly without the workflow | Directly request purchase.php and confirmation.php | Both pages return a non-empty title; direct access is allowed in observed run | PASS |
| TC61 | Default dropdown selections on page load | Open homepage | First departure and destination options are selected | PASS |
| TC62 | Departure dropdown contains no duplicate cities | Read departure options | All departure labels are unique | PASS |
| TC63 | Destination dropdown contains no duplicate cities | Read destination options | All destination labels are unique | PASS |
| TC64 | Dropdown options are unchanged after a page refresh | Capture options, refresh homepage | Homepage and both option lists remain available and unchanged | PASS |
| TC65 | Search using the last destination option in the list | Paris to last destination option (Cairo) | Heading mentions destination and flight results appear | PASS |
| TC66 | Homepage remains usable after going Back from the results page | Search Boston to London, then Back | Homepage controls display and homepage URL is restored | PASS |
| TC67 | No dropdown option has a blank label | Inspect all city options | No departure or destination label is empty | PASS |
| TC68 | Every departure city can be selected and read back | Select each departure option in turn | Each selected value reads back correctly | PASS |
| TC69 | Changing the selection several times keeps the last choice | Change departure to Portland and destination to Dublin | Final values and results heading use Portland to Dublin | PASS |
| TC70 | Dropdown option order is unchanged after making a selection | Select last departure option | Departure options remain in their original order | PASS |
| TC71 | Number of Choose buttons equals number of flight rows | Search Paris to Buenos Aires | Every flight row has a working Choose button | PASS |
| TC72 | Every Choose button has the label 'Choose This Flight' | Inspect all result rows | Every button label is `Choose This Flight` | PASS |
| TC73 | Number of listed flights is within a sensible range | Search Paris to Buenos Aires | Flight count is between 1 and 20; recorded count was 5 | PASS |
| TC74 | Minimum, average and maximum flight prices are consistent | Read all result prices | Prices are positive and minimum ≤ average ≤ maximum | PASS |
| TC75 | Results heading is correct for several different routes | Boston-Dublin, Portland-Berlin, San Diego-New York | Each heading contains both selected cities | PASS |
| TC76 | Results page title belongs to the BlazeDemo site | Search Paris to Buenos Aires | Results title contains `BlazeDemo` | PASS |
| TC77 | Refreshing the results page keeps the same route | Capture route heading, refresh results | URL remains `reserve.php`, heading is unchanged, flights remain | PASS |
| TC78 | Every price shows the dollar currency symbol | Search and inspect all prices | Every displayed price starts with `$` | PASS |
| TC79 | Every flight row has all its data columns | Search Paris to Buenos Aires | Each result row contains at least six cells | PASS |
| TC80 | First and last flights both open a complete booking form | Choose first and last result on separate searches | Both choices open forms with all expected fields | PASS |
| TC81 | Very long passenger name (100 characters) | Enter a 100-character name | Field retained all 100 characters in recorded run | PASS |
| TC82 | Passenger name with special characters | `O'Brien-Smith Jr.` | Name field preserves punctuation and spaces | PASS |
| TC83 | Alphanumeric ZIP code | `SW1A 1AA` | ZIP field preserves letters, digits, and space | PASS |
| TC84 | City name with non-ASCII characters | `São Paulo` | City field preserves the Unicode value | PASS |
| TC85 | Leading and trailing spaces in a field | Passenger name `  Test  ` | Field preserves leading/trailing spaces | PASS |
| TC86 | Every card type option can be selected in turn | Select each listed card type | Each selected type reads back correctly | PASS |
| TC87 | Submit the booking with Remember Me checked | Valid form and Remember Me checked | Booking reaches confirmation | PASS |
| TC88 | Re-entering a value overwrites the previous one | Enter `First Street 1`, then `Second Avenue 2` | Address field contains only the latest value | PASS |
| TC89 | Non-numeric text in the card number field | Enter `ABCD-EFGH` | Form remains displayed; recorded field accepts the non-numeric text | PASS |
| TC90 | Submit a booking with an expired card date | Valid form, expiration `01/2000` | Recorded run accepted expired date and opened confirmation | PASS |
| TC91 | Destination of the week link opens another page | Click destination-of-the-week link | URL changes from homepage and opened page has a title | PASS |
| TC92 | Going Back from the results page returns to the homepage | Search Paris to Buenos Aires, then Back | Homepage controls reappear and results URL is left | PASS |
| TC93 | Two bookings in the same browser session | Complete Paris-Buenos Aires and Boston-London bookings | Both workflows reach confirmation | PASS |
| TC94 | Confirmation page does not show the full card number | Submit booking with synthetic test number | Confirmation reached; recorded card number was masked | PASS |
| TC95 | Forward navigation returns to the booking form | Choose flight, Back, then Forward | Forward navigation returns to `purchase.php` | PASS |
| TC96 | Search flow works with the page zoomed to 200 percent | Set 200% zoom, restore 100%, then search | Homepage remains visible and search returns flights | PASS |
| TC97 | Confirmation table lists several information rows | Submit valid booking | Confirmation table contains at least three rows | PASS |
| TC98 | Booking Id is a single value without spaces | Submit valid booking | Confirmation Id is non-empty and contains no spaces | PASS |
| TC99 | End-to-end booking with an alternate data set | Boston-London; alternate passenger/address/card data | Alternate booking reaches confirmation with thank-you heading | PASS |
| TC100 | A new search after a completed booking starts cleanly | Complete booking, then search San Diego to Cairo | Homepage reloads and new route results appear | PASS |

