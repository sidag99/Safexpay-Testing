package Scripts;
import Functions.ScrollToView;
import Read_Write_Files.ReadFromCSV;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static Functions.ClickElement.*;
import static Functions.Driver.driverAllocation;
import static Functions.ScrollToView.scrollToViewElementXpath;
import static Functions.ScrollToView.scrollToViewXpath;
import static Functions.SelectRandomFile.selectRandomFileFromList;
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

    //------------------------Merchant Creation------------------------------
    String[] dataCreateMerchant;
    @Test(priority=1, description = "Merchant Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Creation Flow")
    public void createMerchant() throws Exception {
        dataCreateMerchant= new String[15];
        openCreateMerchant();
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Business_Details.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] data = csv.ReadLineNumber(1);
        fillBusinessDetailsForm(data);
    }
    //------------------------Create Merchant Clicked------------------------------
    @Step("Opening Create Merchant")
    public void openCreateMerchant() throws InterruptedException {
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-1\"]");
        Thread.sleep(1000);
        clickByXpath(driver,"//*[@id=\"js-side-menu-1\"]/ul/li[1]/a");
    }

    //------------------------Filling Business details form------------------------------
    private String testNumber;
    @Step("Fill Business Details Form")
    public void fillBusinessDetailsForm(String[] data) throws InterruptedException {
        waitForElementXpath(driver,"//*[@id=\"BusinessDetails\"]/form/div[1]/div[2]/div/input");
        Thread.sleep(1000);
        testNumber =getTimestampShort();
        String testName= "test_"+ testNumber;
        sendKeysByXpath(driver,"//*[@ng-model=\"name\"]", testName);
        dataCreateMerchant[0]=testName;
        Thread.sleep(200);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[4]/div[1]/div/div/a");
        Thread.sleep(500);
        int selectedSector=selectRandomFileFromList(driver,"/html/body/div[4]/ul/li");
        if(selectedSector!=-1)
            clickByXpath(driver,"/html/body/div[4]/ul/li["+selectedSector+"]");
        Thread.sleep(200);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[1]/div/div");
        Thread.sleep(200);
        clickByXpath(driver,"/html/body/div[5]/ul/li[2]");
        Thread.sleep(2000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[2]/div/div");
//        Thread.sleep(500);
//        scrollToViewElementXpath(driver,"/html/body/div[5]/div/input");
//        clickByXpath(driver,"/html/body/div[5]/div/input");
//        sendKeysByXpath(driver,"/html/body/div[5]/div/input","AED");
//        driver.findElement(By.xpath("/html/body/div[5]/div/input")).sendKeys(Keys.ENTER);
        scrollToViewXpath(driver,"/html/body/div[6]/ul/li[2]");
        clickByXpath(driver,"/html/body/div[6]/ul/li[2]");
        Thread.sleep(500);
        String description="Description_"+ testNumber;
        sendKeysByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[9]/div/div/textarea",description);
        Thread.sleep(1000);
        scrollToViewXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[14]/div[2]/div/div");
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[14]/div[2]/div/div");
        Thread.sleep(500);
        if(data[0].equalsIgnoreCase("Aggregator Hosted")){
            scrollToViewXpath(driver,"/html/body/div[7]/ul/li[2]");
            clickByXpath(driver,"/html/body/div[7]/ul/li[2]");
        }
        else if(data[0].equalsIgnoreCase("JS Checkout")){
            clickByXpath(driver,"/html/body/div[7]/ul/li[5]");
        }
        Thread.sleep(500);
        if(data[1].equalsIgnoreCase("yes")&&!driver.findElement(By.id("checkbox_eppOnCardsFlag")).getAttribute("checked").equals("checked"))
        {
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[1]/div/div/label");
        }
        Thread.sleep(500);
        if(data[2].equalsIgnoreCase("yes")&&!driver.findElement(By.id("checkbox_threeDS")).getAttribute("checked").equals("checked"))
        {
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[2]/div/div/label");
        }
        Thread.sleep(500);
        if(data[3].equalsIgnoreCase("yes")&&!driver.findElement(By.id("checkbox_nonThreeDS")).getAttribute("checked").equals("checked"))
        {
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[2]/div/div/div/div/label");
        }
        Thread.sleep(500);
        if(data[4].equalsIgnoreCase("yes")&&!driver.findElement(By.id("checkbox_isRefundApiFlag")).getAttribute("checked").equals("checked"))
        {
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[1]/div/div/label");
        }
        Thread.sleep(500);
        if(data[5].equalsIgnoreCase("yes")&&!driver.findElement(By.id("checkbox_isRefundPortalFlag")).getAttribute("checked").equals("checked"))
        {
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[2]/div/div/label");
        }
        Thread.sleep(500);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[21]/div/div/button");
    }







}
