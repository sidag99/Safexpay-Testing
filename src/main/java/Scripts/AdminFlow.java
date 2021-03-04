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
import static Functions.ScrollToView.*;
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
        String businessDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Business_Details.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(businessDetailsCsvPath);  //Reading credentials file
        String[] data = csv.ReadLineNumber(1);  //Reading first line of data from csv
        fillBusinessDetailsForm(data);

    }
    //------------------------Create Merchant Clicked------------------------------
    @Step("Opening Create Merchant")
    public void openCreateMerchant() throws InterruptedException {
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-1\"]");  //Clicks Merchant Management
        saveTextLog("Clicked Merchant Management");
        Thread.sleep(1000);
        clickByXpath(driver,"//*[@id=\"js-side-menu-1\"]/ul/li[1]/a");  //Clicks Create Merchant
        saveTextLog("Clicked Create Merchant");
    }

    //------------------------Filling Business details form------------------------------
    private String testNumber;
    @Step("Fill Business Details Form")
    public void fillBusinessDetailsForm(String[] data) throws InterruptedException {
        waitForElementXpath(driver,"//*[@id=\"BusinessDetails\"]/form/div[1]/div[2]/div/input");
        Thread.sleep(1000);
        testNumber =getTimestampShort();
        String testName= "Merchant_"+ testNumber;  //Make random name
        sendKeysByXpath(driver,"//*[@ng-model=\"name\"]", testName);  //Enter Merchant
        saveTextLog("New Merchant Name: "+testName);
        dataCreateMerchant[0]=testName;
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[4]/div[1]/div/div/a");  //Clicks Sector drop down
        saveTextLog("Clicked \"Sector\" Dropdown Button");
        Thread.sleep(1000);
        int selectedSector=selectRandomFileFromList(driver,"/html/body/div[4]/ul/li");
        if(selectedSector!=-1)
        {
            saveTextLog("Selected Sector Name: "+driver.findElement(By.xpath("/html/body/div[4]/ul/li["+(selectedSector+2)+"]/div")).getText());
            clickByXpath(driver,"/html/body/div[4]/ul/li["+(selectedSector+2)+"]");   //Selects random item from list
        }
        Thread.sleep(1000);
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[1]/div/div");  //Scroll Country to center of screen
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[1]/div/div");  //Clicks Country Dropdown
        saveTextLog("Clicked Country Dropdown Button");
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[5]/ul/li[2]");  //Selects UAE as Country
        saveTextLog("Selected UAE");
        Thread.sleep(2000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[2]/div/div");  //Select Currency
        saveTextLog("Clicked Currency Dropdown Button");
        scrollToViewXpath(driver,"/html/body/div[6]/ul/li[2]");
        clickByXpath(driver,"/html/body/div[6]/ul/li[2]");  //Select AED
        saveTextLog("Selected AED as currency");
        Thread.sleep(1000);
        String description="Description_"+ testNumber;
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[9]/div/div/textarea");
        sendKeysByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[9]/div/div/textarea",description);  //Enter Description
        saveTextLog("Description Added: "+description);
        Thread.sleep(1000);
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[14]/div[2]/div/div");
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[14]/div[2]/div/div");    //Select Integration Type
        saveTextLog("Select Integration Type Dropdown clicked");
        Thread.sleep(1000);
        if(data[0].equalsIgnoreCase("Aggregator Hosted")){
            scrollToViewXpath(driver,"/html/body/div[7]/ul/li[2]");
            clickByXpath(driver,"/html/body/div[7]/ul/li[2]");  //Select Aggregator hosted
            saveTextLog("Aggregator Hosted Selected");
            dataCreateMerchant[1]="Aggregator Hosted";
        }
        else if(data[0].equalsIgnoreCase("JS Checkout")){
            scrollToViewXpath(driver,"/html/body/div[7]/ul/li[5]");
            clickByXpath(driver,"/html/body/div[7]/ul/li[5]");  //Select JS Checkout
            saveTextLog("JS Checkout Selected");
            dataCreateMerchant[1]="JS Checkout";
        }
        Thread.sleep(500);
        String mccCode=getTimestamp("mmss");
        sendKeysByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[15]/div[2]/div/input",mccCode);
        saveTextLog("MCC Code: "+mccCode);
        //Enter random MCC Code
        Thread.sleep(1000);
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[21]/div/div/button");
        Thread.sleep(1000);

        if(data[1].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[1]/div/div/label");  //Toggle EPP
            saveTextLog("EPP Turned ON");
            //dataCreateMerchant[2]="yes";
        }//else dataCreateMerchant[2]="no";
        Thread.sleep(500);

        if(data[2].equalsIgnoreCase("no")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[2]/div/div/label");  //Toggle 3DS
            saveTextLog("3DS Turned OFF");
            //dataCreateMerchant[3]="yes";
        }//else dataCreateMerchant[3]="no";

        Thread.sleep(500);

        if(data[3].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[2]/div/div/div/div/label");  //Toggle Non-3DS
            saveTextLog("Non-3DS Turned ON");
            //dataCreateMerchant[4]="yes";
        }//else dataCreateMerchant[4]="no";

        Thread.sleep(500);

        if(data[4].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[1]/div/div/label");  //Toggle Refund API
            saveTextLog("Refund API Turned ON");
            //dataCreateMerchant[5]="yes";
        }//else dataCreateMerchant[5]="no";

        Thread.sleep(500);

        if(data[5].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[2]/div/div/label"); //Toggle Refund Portal
            saveTextLog("Refund Portal Turned ON");
            //dataCreateMerchant[6]="yes";
        }//else dataCreateMerchant[6]="no";

        Thread.sleep(500);

        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[21]/div/div/button");  //Go to Next Page
        saveTextLog("Next Button Clicked");
    }







}
