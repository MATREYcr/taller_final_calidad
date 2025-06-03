package edu.unac.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class InventorySteps {
    private ChromeDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp(){
        System.setProperty("webdriver.chrome.driver",
                System.getProperty("user.dir") +
                        "/src/main/java/edu/unac/drivers/chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary("C:\\yo\\Calidad de software\\functionTesting\\chrome-win64\\chrome-win64\\chrome.exe");
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("file:///C:/yo/Calidad de software/Taller final/taller_final_calidad/Frontend/index.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
    }

    @After
    public void tearDown() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("a device is registered with name Projector, type Multimedia, and location Room101")
    public void a_device_is_registered_with_name_projector_type_multimedia_and_location_room101() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deviceName")));
        WebElement typeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deviceType")));
        WebElement locationInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deviceLocation")));
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("addDeviceBtn")));

        nameInput.sendKeys("Projector");
        typeInput.sendKeys("Multimedia");
        locationInput.sendKeys("Room101");
        addButton.click();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(),'Projector')]")));
    }

    @Given("the device is currently loaned to user Alice")
    public void the_device_is_currently_loaned_to_user_alice() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("devicesTableBody")));

        WebElement userBorrowerInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loanBorrowedBy")));
        WebElement deviceSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loanDeviceSelect")));

        Select loanDeviceDropdown = new Select(deviceSelect);

        deviceSelect.click();
        System.out.println("DEBUG: Se hizo clic en el dropdown de dispositivos para préstamo.");

        wait.until(ExpectedConditions.textToBePresentInElement(deviceSelect, "Projector"));
        System.out.println("DEBUG: Opción 'Projector' detectada en el dropdown.");

        wait.until(ExpectedConditions.elementToBeClickable(deviceSelect));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        userBorrowerInput.sendKeys("Alice");

        loanDeviceDropdown.selectByVisibleText("Projector (Multimedia)");

        WebElement addLoanButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("addLoanBtn")));
        addLoanButton.click();
    }

    @When("the user attempts to delete the device")
    public void the_user_attempts_to_delete_the_device() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("devicesTableBody")));
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//tbody[@id='devicesTableBody']/tr[td[2][text()='Projector']]/td[6]/button[text()='Delete']")));
        deleteButton.click();
    }

    @Then("the device should not be deleted")
    public void the_device_should_not_be_deleted() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean deviceExists = driver.findElements(By.xpath("//td[contains(text(),'Projector')]")).size() > 0;
        Assert.assertTrue(deviceExists, "ERROR: El dispositivo 'Projector' fue eliminado, pero NO debía serlo.");
    }

    @Then("an error message Failed to delete device should be displayed")
    public void an_error_message_failed_to_delete_device_should_be_displayed() {
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deviceMessage")));
        String actualMessage = errorMsg.getText().trim();
        Assert.assertEquals(actualMessage, "Failed to delete device", "ERROR: El mensaje de error no coincide.");
    }
}