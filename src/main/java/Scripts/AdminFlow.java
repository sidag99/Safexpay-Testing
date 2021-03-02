package Scripts;
import ReadFiles.ReadFromCSV;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static Functions.ClickElement.*;
import static Functions.Driver.driverAllocation;
import static Reports.AllureReport.*;
import static Functions.CreateNameByTimestamp.*;

public class AdminFlow {
    static WebDriver driver;
    static WebDriverWait wait;

    @BeforeTest
    public void DriverAllocation() throws IOException {
        driver = driverAllocation("chrome");  //Allocates the driver
        DelPreviousReport();  //Deletes previous allure report
        wait=new WebDriverWait(driver,30);
    }

    @Test(priority=0, description = "Opening Safexpay website and logging in")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test setup by opening the login page and logging in")
    public void testSetupAdminMaker() throws Exception {
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0]);
        login(credential[1],credential[3]);
    }

    @Step("Opening Safexpay website")
    public void openUrl(String url){
        String URL= url;
        driver.get(URL);  //Opening the link
        saveTextLog("Opening URL: "+URL);  //Saving log for allure report
    }

    @Step("Logging in")
    public void login(String username, String password) throws InterruptedException {

        saveTextLog("Username: " + username);
        saveTextLog("Password: " + password);

        sendKeysByXpath(driver, "//*[@id=\"userName\"]", username);  //Typing username into the box
        sendKeysByXpath(driver, "//*[@id=\"passWord\"]", password);  //Typing password into the box
        System.out.println("Demo");
        clickByXpath(driver, "//*[@type=\"submit\"]");
        saveTextLog("Submit button clicked");
        waitForElementXpath(driver,"//*[@id=\"js-side-menu-1\"]");
        Thread.sleep(1000);
        Screenshot(driver, "Login Successful");  //Saving Screenshot for allure report
    }

    @Test(priority=1, description = "Merchant Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Creation Flow")
    public void createMerchant() throws InterruptedException {
        openCreateMerchant();
    }

    @Step("Opening Create Merchant")
    public void openCreateMerchant() throws InterruptedException {
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-1\"]");
        Thread.sleep(1000);
        clickByXpath(driver,"//*[@id=\"js-side-menu-1\"]/ul/li[1]/a");
    }

    @Step("Fill Business Details Form")
    public void fillBusinessDetailsForm() throws InterruptedException {
        waitForElementXpath(driver,"//*[@id=\"BusinessDetails\"]/form/div[1]/div[2]/div/input");
        Thread.sleep(1000);
        String testName= "test_"+getTimestamp();
        sendKeysByXpath(driver,"//*[@ng-model=\"name\"]", testName);
    }

    @Test(priority=2, description = "User Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User Creation Flow")
    public void createUser(){
     clickByXpath(driver,"//*[@id=\"js-side-menu-1\"]");
     clickByXpath(driver,"//*[@id=\"js-side-menu-1\");

    }






}
