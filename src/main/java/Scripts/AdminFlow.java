package Scripts;
import Read_Write_Files.ReadFromCSV;
import Read_Write_Files.ReadFromXlsFile;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import net.bytebuddy.implementation.bytecode.Throw;
import net.sourceforge.htmlunit.corejs.javascript.EcmaError;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import net.lingala.zip4j.ZipFile;
import org.testng.asserts.SoftAssert;

import javax.script.ScriptContext;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static Functions.ClickElement.*;
import static Functions.Driver.driverAllocation;
import static Functions.ScrollToView.*;
import static Functions.SelectRandomFile.createRandomNum;
import static Functions.SelectRandomFile.selectRandomFileFromList;
import static Functions.UploadFile.uploadByXpathRobo;
import static Read_Write_Files.WriteToCSV.*;
import static Reports.AllureReport.*;
import static Functions.CreateNameByTimestamp.*;

public class AdminFlow {
    static WebDriver driver;
    static WebDriverWait wait;
    SoftAssert softAssert=new SoftAssert();

    @BeforeTest
    public void DriverAllocation() throws IOException {
        FileUtils.cleanDirectory(Paths.get(System.getProperty("user.dir"),"\\downloadFiles").toFile());
        driver = driverAllocation("chrome");  //Allocates the driver
        DelPreviousReport();  //Deletes previous allure report
        wait=new WebDriverWait(driver,30);
    }

    @Step("Opening Safexpay website")
    public void openUrl(String url,String message){
        saveTextLog(message);
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
        waitForElementXpath(driver,"//*[@id=\"js-side-menu-0\"]");
        Thread.sleep(1000);
        Screenshot(driver, "Login Successful");  //Saving Screenshot for allure report
    }


    //------------------------Merchant Creation------------------------------
    String[] dataCreateMerchant;
    @Test(priority=0, description = "Merchant Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Creation Flow")
    public void createMerchant() throws Exception {
        boolean failCase=false;
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to maker account");
        login(credential[1],credential[3]);

        dataCreateMerchant= new String[10];
        openCreateMerchant();
        String allSessionsWritePath = System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_All_Sessions.csv";
        String currentSessionWritePath = System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_Last_Session.csv";
        deleteContentsOfCsv(currentSessionWritePath);

        String businessDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Business_Details.csv";
        ReadFromCSV csvBusiness = new ReadFromCSV(businessDetailsCsvPath);
        String[] dataBusiness;

        String pricingDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Pricing_Details.csv";
        ReadFromCSV csvPricing = new ReadFromCSV(pricingDetailsCsvPath);
        String[] dataPricing;
        for(int i=1;i<csvBusiness.SizeOfFile();i++)
        {
            dataBusiness = csvBusiness.ReadLineNumber(i);  //Reading data from csv

            for(int j=1;j<csvPricing.SizeOfFile();j++)
            {
                try {
                    dataPricing = csvPricing.ReadLineNumber(j);//Reading data from csv
                    createMerchantFormFill(dataBusiness, dataPricing);

                    initializeCsvWriter(allSessionsWritePath);
                    writeNextLineCsv(dataCreateMerchant);

                    initializeCsvWriter(currentSessionWritePath);
                    writeNextLineCsv(dataCreateMerchant);

                    waitForElementXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[1]/div[2]/div/input");
                    Thread.sleep(2000);
                }catch (Exception e){
                    driver.navigate().refresh();
                    waitForPageToLoad(driver);
                    waitForElementXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[1]/div[2]/div/input");
                    Thread.sleep(2000);
                    softAssert.fail();
                    failCase=true;
                }
            }
        }
        if(failCase){
            Assert.fail();
        }
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

    //------------------------Filling all forms to create merchant------------------------------
    @Step("FILL MERCHANT CREATION DATA")
    public void createMerchantFormFill(String[] data, String [] paymentModes) throws Exception {
        saveTextLog("FILL BUSINESS DETAILS FORM");
        waitForElementXpath(driver,"//*[@id=\"BusinessDetails\"]/form/div[1]/div[2]/div/input");
        Thread.sleep(1000);
        String testNumber=getTimestampShort();
        String testName="";
        String description="";
        if(data[0].toLowerCase().contains("aggregator")) {
            testName += "Agg Hosted";
            description+="Aggregator Hosted";
        }
        else if(data[0].toLowerCase().contains("js")) {
            testName += "JS Checkout";
            description+="JS Checkout";
        }
        if(paymentModes[0].equalsIgnoreCase("yes")) {
            testName += " - Cyber";
            description+=" - CYBERSOURCEPG";
        }
        if(paymentModes[1].equalsIgnoreCase("yes")) {
            testName += " - MPGS";
            description+=" - MPGS-FAB";
        }
        if(paymentModes[2].equalsIgnoreCase("yes")) {
            testName += " - Tabby";
            description+=" - Tabby";
        }
        if(data[2].equalsIgnoreCase("yes")&&data[3].equalsIgnoreCase("yes")) {
            testName += " -Mix 3Ds";
            description+=" - Both 3DS and Non 3Ds";
        }
        else if (data[2].equalsIgnoreCase("yes")) {
            testName += " - 3Ds";
            description+=" - 3Ds";
        }
        else if(data[3].equalsIgnoreCase("yes")) {
            testName += " - Non3Ds";
            description+=" - Non 3Ds";
        }

        testName+=" - "+testNumber;  //Make Unique name
        sendKeysByXpath(driver,"//*[@ng-model=\"name\"]", testName);  //Enter Merchant
        saveTextLog("New Merchant Name: "+testName);
        dataCreateMerchant[0]=testName;
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[4]/div[1]/div/div/a");  //Clicks Sector drop down
        saveTextLog("Clicked \"Sector\" Dropdown Button");
        Thread.sleep(1000);
        List<WebElement> sectorDD=driver.findElements(By.xpath("//*[@id=\"select2-drop\"]/ul/li"));
        sectorDD.remove(0);

        int selectedSector=createRandomNum(0,sectorDD.size()-1);
        Thread.sleep(1000);
        String sectorName=sectorDD.get(selectedSector).findElement(By.tagName("div")).getText();
        saveTextLog("Selected Sector Name: "+sectorName);
        sectorDD.get(selectedSector).click();  //Selects random item from list

        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[1]/div/div");  //Scroll Country to center of screen
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[1]/div/div");  //Clicks Country Dropdown
        saveTextLog("Clicked Country Dropdown Button");
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[5]/ul/li[2]");  //Selects UAE as Country
        saveTextLog("Selected UAE");
        Thread.sleep(5000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[2]/div/div");  //Select Currency
        saveTextLog("Clicked Currency Dropdown Button");
        scrollToViewXpath(driver,"/html/body/div[6]/ul/li[2]");
        clickByXpath(driver,"/html/body/div[6]/ul/li[2]");  //Select AED
        saveTextLog("Selected AED as currency");
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[9]/div/div/textarea");
        sendKeysByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[9]/div/div/textarea",description);  //Enter Description
        saveTextLog("Description Added: "+description);
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
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[21]/div/div/button");
        Thread.sleep(1000);

        if(data[1].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[1]/div/div/label");  //Toggle EPP
            saveTextLog("EPP Turned ON");
            //dataCreateMerchant[2]="yes";
            Thread.sleep(50);
        }//else dataCreateMerchant[2]="no";

        if(data[2].equalsIgnoreCase("no")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[2]/div/div/label");  //Toggle 3DS
            saveTextLog("3DS Turned OFF");
            dataCreateMerchant[2]="no";
            Thread.sleep(50);
        }else dataCreateMerchant[2]="yes";


        if(data[3].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[2]/div/div/div/div/label");  //Toggle Non-3DS
            saveTextLog("Non-3DS Turned ON");
            dataCreateMerchant[3]="yes";
            Thread.sleep(50);
        }else dataCreateMerchant[3]="no";


        if(data[4].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[1]/div/div/label");  //Toggle Refund API
            saveTextLog("Refund API Turned ON");
            //dataCreateMerchant[5]="yes";
            Thread.sleep(50);
        }//else dataCreateMerchant[5]="no";


        if(data[5].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[2]/div/div/label"); //Toggle Refund Portal
            saveTextLog("Refund Portal Turned ON");
            //dataCreateMerchant[6]="yes";
            Thread.sleep(50);
        }//else dataCreateMerchant[6]="no";


        Thread.sleep(500);
        clickByXpath(driver,"//div//button[@ng-click=\"saveBusinessDetails()\"]");  //Go to Next Page
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        String message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);

    //------------------------Filling User Details--------------------------

        saveTextLog("FILLING USER DETAILS");
        Thread.sleep(1000);
        waitAndClickByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[1]/div/a");  //Clicking Add User
        saveTextLog("Clicked \"add user\"");
        Thread.sleep(1000);
        clickByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[1]/div/div/div/div/div/label");  //Make admin toggle
        saveTextLog("Make Admin button clicked");
        Thread.sleep(500);
        String userName="";
        if(data[0].toLowerCase().contains("aggregator"))
            userName+="AGG";
        else if(data[0].toLowerCase().contains("js"))
            userName+="JS";
        if(paymentModes[0].equalsIgnoreCase("yes"))
            userName+="cyb";
        if(paymentModes[1].equalsIgnoreCase("yes"))
            userName+="mpg";
        if(data[2].equalsIgnoreCase("yes")&&data[3].equalsIgnoreCase("yes"))
            userName+="MixThreeDs";
        else if (data[2].equalsIgnoreCase("yes"))
            userName+="ThreeDs";
        else if(data[3].equalsIgnoreCase("yes"))
            userName+="NonThreeDs";
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[2]/div[1]/div/input",userName);  //First name added
        saveTextLog("First Name Added: "+userName);
        dataCreateMerchant[4]=userName;
        String lastName=getRandomString();
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[2]/div[2]/div/input",lastName);  //Last Name added
        saveTextLog("Last Name Added: "+lastName);
        dataCreateMerchant[5]=lastName;
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[3]/div[1]/div/input","padmawati.taddy@bankfab.com");  //Email Added
        saveTextLog("Email Added: padmawati.taddy@bankfab.com");
        dataCreateMerchant[6]="padmawati.taddy@bankfab.com";
        Thread.sleep(2000);
        clickByXpath(driver,"//div//button[@ng-click=\"saveUserDetails();\"]");
        saveTextLog("Next button clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);

    //------------------------Fill Pricing Information-----------------------
        saveTextLog("FILLING PRICING INFORMATION");
        Thread.sleep(1000);
        dataCreateMerchant[7] = "no";
        dataCreateMerchant[8] = "no";
        dataCreateMerchant[9] = "no";
        if(paymentModes[0].equalsIgnoreCase("yes")||paymentModes[1].equalsIgnoreCase("yes")) {
            waitAndClickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[1]/div/div/div/label");
            saveTextLog("Card button clicked");
            Thread.sleep(1000);
            if (paymentModes[0].equalsIgnoreCase("yes")) {
                String[] key = null;
                if (dataCreateMerchant[3].equalsIgnoreCase("yes")) {
                    String keyCyberCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\CybersourcePG_Non-3DS_Key.csv";
                    ReadFromCSV csv = new ReadFromCSV(keyCyberCsvPath);  //Reading encryption data
                    key = csv.ReadLineNumber(1);
                } else if (dataCreateMerchant[2].equalsIgnoreCase("yes")) {
                    String keyCyberCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\CybersourcePG_3DS_Key.csv";
                    ReadFromCSV csv = new ReadFromCSV(keyCyberCsvPath);  //Reading encryption data
                    key = csv.ReadLineNumber(1);
                }
                if (key != null) {

                    clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[1]/div/div/legend/a");  //Add cybersource pg
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[1]/div/div/legend/a");  //Add cybersource pg
                    Thread.sleep(500);
                    sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[2]/div[1]/input", key[0]);  //Add MID
                    sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[2]/div[2]/input", key[1]);  //Add Encryption key
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[1]/div/div/a");  //Select Currency
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select AED
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[2]/div/div/a");  //Select Schema
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select Mastercard
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[3]/div/div/a");  //Select Operating mode
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select International
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[1]/div/div/a");//Select Currency
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select AED
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[2]/div/div/a");//Select Schema
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[2]");//Select Visa
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[3]/div/div/a");//Select Operating mode
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
                    Thread.sleep(500);
                    saveTextLog("Cybersource PG Details added");
                    dataCreateMerchant[7] = "yes";
                    Thread.sleep(500);
                }
            }

            if (paymentModes[1].equalsIgnoreCase("yes")) {
                String[] key = null;
                if (dataCreateMerchant[3].equalsIgnoreCase("yes")) {
                    String keyCyberCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\MPGS-Fab-Non-3DS_key.csv";
                    ReadFromCSV csv = new ReadFromCSV(keyCyberCsvPath);  //Reading encryption data
                    key = csv.ReadLineNumber(1);
                } else if (dataCreateMerchant[2].equalsIgnoreCase("yes")) {
                    String keyCyberCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\MPGS-Fab-3DS_key.csv";
                    ReadFromCSV csv = new ReadFromCSV(keyCyberCsvPath);  //Reading encryption data
                    key = csv.ReadLineNumber(1);
                }
                if (key != null) {
                    clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[1]/div/div/legend/a");  //Add MPGS-Fab
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[1]/div/div/legend/a");  //Add MPGS-Fab
                    Thread.sleep(500);
                    sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[2]/div[1]/input", key[0]);  //Add MID
                    Thread.sleep(500);
                    sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[2]/div[2]/input", key[1]);  //Add Encryption key
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[4]/div[1]/div/div/a");  //Select Currency
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select AED
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[4]/div[2]/div/div/a");  //Select Schema
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select Mastercard
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[4]/div[3]/div/div/a");  //Select Operating mode
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select International
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[5]/div[1]/div/div/a");//Select Currency
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select AED
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[5]/div[2]/div/div/a");//Select Schema
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[2]");//Select Visa
                    Thread.sleep(500);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[5]/div[3]/div/div/a");//Select Operating mode
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
                    Thread.sleep(500);
                    dataCreateMerchant[8] = "yes";
                    saveTextLog("MPGS Details added");
                    Thread.sleep(500);
                }
            }
        }

        if(paymentModes[2].equalsIgnoreCase("yes"))
        {
            String keyTabbyCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\Tabby_Key.csv";
            ReadFromCSV csv = new ReadFromCSV(keyTabbyCsvPath);  //Reading encryption data
            String[] key = csv.ReadLineNumber(1);
            waitAndClickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[1]/div/div/div/label");
            Thread.sleep(1000);
            clickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[2]/div[1]/div/div/legend/a");
            Thread.sleep(1000);
            sendKeysByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[2]/div[2]/div[1]/input",key[0]);
            Thread.sleep(500);
            sendKeysByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[2]/div[2]/div[2]/input",key[1]);
            Thread.sleep(500);
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[2]/div/div[2]/div[4]/div[1]/div/div/a");  //Select Currency
            Thread.sleep(500);
            clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li");  //Select AED
            Thread.sleep(500);
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[2]/div/div[2]/div[4]/div[2]/div/div/a"); //Select Schema
            Thread.sleep(500);
            clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li");  //Select NA
            Thread.sleep(500);
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[2]/div/div[2]/div[4]/div[3]/div/div/a");//Select Operating mode
            Thread.sleep(500);
            clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
            Thread.sleep(500);
            saveTextLog("Tabby Details Added");
            Thread.sleep(500);
            dataCreateMerchant[9]="yes";
        }

        scrollToCenterXpath(driver,"//*[@id=\"Pricing\"]/form/div[2]/div/div/button[2]");
        clickByXpath(driver,"//div//button[@ng-click=\"savePriceDetails();\"]");
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);

    //----------------------Fill Velocity Form----------------------------
        saveTextLog("FILL VELOCITY FORM");
        String velocityDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Velocity_Details.csv";  //path to get details file
        ReadFromCSV csvVelocity = new ReadFromCSV(velocityDetailsCsvPath);  //Reading file
        String[] dataVelocity = csvVelocity.ReadLineNumber(1);
        Thread.sleep(1000);
        waitForElementXpath(driver,"//*[@id=\"Velocity\"]/form/div[10]/div/div/button[2]");
        scrollToCenterXpath(driver,"//*[@id=\"Velocity\"]/form/div[10]/div/div/button[2]");
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[8]/div[1]/div/input",dataVelocity[0]);  //enter Refund Min Transaction Amount
        saveTextLog("Refund Min Transaction Amount: "+dataVelocity[0]);
        Thread.sleep(500);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[8]/div[2]/div/input",dataVelocity[1]);  //enter Refund Max Transaction Amount
        saveTextLog("Refund Max Transaction Amount: "+dataVelocity[1]);
        Thread.sleep(500);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[9]/div[1]/div/input",dataVelocity[2]);  //enter Refund Daily Transaction Count
        saveTextLog("Refund Daily Transaction Count: "+dataVelocity[2]);
        Thread.sleep(500);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[9]/div[2]/div/input",dataVelocity[3]);  //enter Refund Daily Transaction Amount
        saveTextLog("Refund Daily Transaction Amount: "+dataVelocity[3]);
        Thread.sleep(1000);
        clickByXpath(driver,"//div//button[@ng-click=\"saveVelocityDetails();\"]");
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);
    //--------------------------Fill Other Form---------------------
        saveTextLog("FILL OTHER FORM");
        String ReferralDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Referral_URLs.csv";  //path to get details file
        ReadFromCSV csvReferral = new ReadFromCSV(ReferralDetailsCsvPath);  //Reading file

        Thread.sleep(1000);
        String[] dataReferral=csvReferral.ReadLineNumber(1);
        int i=1;
        try {
            while (!dataReferral[0].isEmpty()) {
                waitAndClickByXpath(driver, "//*[@id=\"Others\"]/form/div[1]/div/div/legend/a");
                Thread.sleep(500);
                sendKeysByXpath(driver, "//*[@id=\"Others\"]/form/div[1]/div/div/div[" + i + "]/div/div[1]/input", dataReferral[0]);
                i++;
                dataReferral = csvReferral.ReadLineNumber(i);
            }
        }catch (Exception e){
            saveTextLog("Referrals Added");
        }
        Thread.sleep(1000);
        clickByXpath(driver,"//div//button[@ng-click=\"saveOtherDetails();\"]");
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);
        String stepName="Merchant Name: "+dataCreateMerchant[0]+" | Integration Type: "+dataCreateMerchant[1]+" | 3DS Enabled: "+dataCreateMerchant[2]+
                " | Non-3DS Enabled: "+dataCreateMerchant[3]+" | Cybersource: "+dataCreateMerchant[7]+" | MPGS: "+dataCreateMerchant[8]+" | Tabby: "+dataCreateMerchant[9];
        changeStepName(stepName);
    }


    //------------------------Editing merchants created-------------------------------
    @Test(priority=1, description = "Merchant Edit Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Edit Flow")
    public void editMerchant() throws Exception{
        boolean testFail=false;
//        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
//        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
//        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
//        System.out.println(Arrays.toString(credential));
//        openUrl(credential[0],"Logging in to maker account");
//        login(credential[1],credential[3]);
        openManageMerchantMaker();
        deleteContentsOfCsv("Output_Files/Merchant_Authorization_Status_Last_Session.csv");
        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_Last_Session.csv");
        for(int i=1;i<lastRun.SizeOfFile();i++)
        {
            try {
                String[] lastData = lastRun.ReadLineNumber(i);
                editMerchant(lastData);
                Thread.sleep(5000);
            }catch (Exception e){
                testFail=true;
                softAssert.fail();
            }
        }
        if (testFail){
            Assert.fail();
        }
    }

    @Step("Opening Manage Merchant")
    public void openManageMerchantMaker() throws InterruptedException {
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-1\"]");  //Clicks Merchant Management
        saveTextLog("Clicked Merchant Management");
        Thread.sleep(1000);
        clickByXpath(driver,"//*[@id=\"js-side-menu-1\"]/ul/li[2]/a");  //Clicks Create Merchant
        saveTextLog("Clicked Manage Merchant");
    }

    @Step("Edit Merchant")
    public void editMerchant(String [] merchantData) throws InterruptedException, IOException {
        waitForElementXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input");
        Thread.sleep(1000);
        driver.findElement(By.xpath("/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input")).clear();
        String temp=merchantData[0];
        String stepname="Editing: "+merchantData[0];
        changeStepName(stepname);
        for(int i=0;i<merchantData[0].length();i++) {
            sendKeysByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input", Character.toString(temp.charAt(i)));
            Thread.sleep(100);
        }
        Thread.sleep(5000);
        if(driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[2]/td[2]")).getText().equalsIgnoreCase(merchantData[0])) {
            clickByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[2]/td[13]/button[2]");
            Thread.sleep(5000);
            scrollToCenterXpath(driver, "//*[@id=\"BusinessDetails\"]/form/div[22]/div/div/button");
            Thread.sleep(2000);

            clickByXpath(driver, "//*[@id=\"BusinessDetails\"]/form/div[20]/div[1]/div/div[1]/div/div/label");  //Toggle EPP
            saveTextLog("EPP Turned ON");
            Thread.sleep(500);

            clickByXpath(driver, "//*[@id=\"BusinessDetails\"]/form/div[21]/div/div/div[1]/div/div/label");  //Toggle Refund API
            saveTextLog("Refund API Turned ON");
            Thread.sleep(500);

            clickByXpath(driver, "//*[@id=\"BusinessDetails\"]/form/div[21]/div/div/div[2]/div/div/label"); //Toggle Refund Portal
            saveTextLog("Refund Portal Turned ON");
            Thread.sleep(500);

            clickByXpath(driver, "//*[@id=\"BusinessDetails\"]/form/div[22]/div/div/button");  //Next Button
            saveTextLog("Next button Clicked");
            waitForElementXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
            String message = driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
            Screenshot(driver, "SnackBar Message: " + message);
            Thread.sleep(3000);
            clickByXpath(driver, "//*[@id=\"heading-action-wrapper\"]/div/button");  //Back To Merchant Management
            saveTextLog("Back To Merchant Management Clicked");
        }else {
            Screenshot(driver,"Merchant name not available on first index");
        }
    }

    //------------------------Authorizing merchants created-------------------------------
    @Test(priority=2, description = "Checker Authorize Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Authorization Flow")
    public void checkerAdmin() throws Exception{
        boolean testFail=false;
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to checker account");
        login(credential[2],credential[3]);

        openManageMerchantChecker();
        Thread.sleep(5000);
        deleteContentsOfCsv("Output_Files/Merchant_Authorization_Status_Last_Session.csv");
        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_Last_Session.csv");
        int randomFileToUnauthorize =createRandomNum(1,lastRun.SizeOfFile()-1);
        for(int i=1;i<lastRun.SizeOfFile();i++)
        {
            String [] lastData=lastRun.ReadLineNumber(i);

            if(i==randomFileToUnauthorize)
            {
                authorizeMerchant(lastData, false);
                Thread.sleep(5000);
                continue;
            }

            try {
                authorizeMerchant(lastData, true);
            }catch (Exception e)
            {
                testFail=true;
                softAssert.fail();
            }
            Thread.sleep(5000);
        }
        if (testFail){
            Assert.fail();
        }
    }

    @Step("Opening Manage Merchant")
    public void openManageMerchantChecker() throws InterruptedException {
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-1\"]");  //Clicks Merchant Management
        saveTextLog("Clicked Merchant Management");
        Thread.sleep(1000);
        clickByXpath(driver,"//*[@id=\"js-side-menu-1\"]/ul/li/a");  //Clicks Create Merchant
        saveTextLog("Clicked Manage Merchant");
    }

    @Step("Authorize Merchant")
    public void authorizeMerchant(String [] merchantData, boolean authorizeRandom) throws InterruptedException, IOException {
        waitForElementXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input");
        Thread.sleep(1000);
        driver.findElement(By.xpath("/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input")).clear();
        Thread.sleep(1000);
        String temp=merchantData[0];
        String stepName="Authorizing: "+merchantData[0];
        changeStepName(stepName);
        for(int i=0;i<merchantData[0].length();i++) {
            sendKeysByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input", Character.toString(temp.charAt(i)));
            Thread.sleep(100);
        }
        Thread.sleep(5000);
        if(driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[2]/td[2]")).getText().equalsIgnoreCase(merchantData[0])){
            clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[2]/td[13]/button[1]");
            Thread.sleep(5000);
            saveTextLog("Authorizing Merchant: "+merchantData[0]);
            String decryptionKey=driver.findElement(By.xpath("/html/body/div[1]/div/div/div/div[3]/div[2]/div/div[1]/form/div[1]/div[2]/div/input")).getAttribute("value");
            saveTextLog("Decryption Key: "+decryptionKey );
            String id=driver.findElement(By.xpath("//*[@id=\"id\"]")).getAttribute("value");
            saveTextLog("Id: "+id);
            Thread.sleep(1000);
            boolean authorized=false;
            if(authorizeRandom) {
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[3]/div[2]/ul/div/li/select");
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[3]/div[2]/ul/div/li/select/option[2]");
                Thread.sleep(2000);

                clickByXpath(driver, "//*[@id=\"resnavtab\"]/div/li/button");

                waitForElementXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
                String message = driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
                Screenshot(driver, "SnackBar Message: " + message);
                Thread.sleep(2000);
                authorized=true;
            }
            clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/button");
            String [] newData=new String[10];
            newData[0]=merchantData[0];
            newData[1]=merchantData[1];
            newData[2]=merchantData[2];
            newData[3]=merchantData[3];
            newData[4]=merchantData[7];
            newData[5]=merchantData[8];
            newData[6]=merchantData[9];
            newData[7]=id;
            newData[8]=decryptionKey;
            if(authorized)
                newData[9]="Yes";
            else newData[9]="No";
            initializeCsvWriter("Output_Files/Merchant_Authorization_Status_All_Sessions.csv"); // Write Details to File
            writeNextLineCsv(newData);
            initializeCsvWriter("Output_Files/Merchant_Authorization_Status_Last_Session.csv"); // Write Details to File
            writeNextLineCsv(newData);
        }
        else {
            Screenshot(driver,"Merchant name not available on first index");
        }

    }


    //-----------------User Creation Module------------------
    @Test(priority=3, description = "User Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User Creation Flow")
    public void createUser() throws Exception {
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to maker account");
        login(credential[1],credential[3]);

        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") + "\\Output_Files\\Merchant_Authorization_Status_Last_Session.csv");
        String random_string;
        deleteContentsOfCsv("Output_Files/Create_User_Detail_last_run.csv");
        try {
            for (int i = 1; i < lastRun.SizeOfFile(); i++) {
                random_string = getRandomString();
                if (lastRun.ReadLineNumber(i)[9].equalsIgnoreCase("yes")) {
                    String mName = lastRun.ReadLineNumber(i)[0];
                    merchant_userDetails(random_string, mName);
                    Thread.sleep(5000);
                }
            }
            Thread.sleep(2000);
            random_string = getRandomString();
            aggregate_maker(random_string);
            Thread.sleep(2000);
            aggregate_checker(random_string);
            Thread.sleep(2000);
            EditUser();
        }catch (Exception e){
            Assert.fail();
        }
    }
    //------------Entering User Details-----------------
    @Step("Enter Merchant User Details")
    public void merchant_userDetails(String random_strings, String merchantName) throws Exception {
        String[] Merchant_Details_writer=new String[4];
        //---------------------Navigate to User Management-----------------------------
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-0\"]"); // User Management
        saveTextLog("Navigate to User Management");
        //---------------------Navigate to Create User---------------------------------
        clickByXpath(driver,"//*[@id=\"js-side-menu-0\"]/ul/li[1]/a");
        saveTextLog("Navigate to Create User");
        Thread.sleep(2000);
        //-------------------------------Merchant Option-------------------------------
        saveTextLog("Creating Merchant User");
        waitAndClickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[3]"); // Merchant Option
        Thread.sleep(2000);
        clickByXpath(driver,"/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/a"); // Role Name
        Thread.sleep(2000);
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]/div"); // Merchant Admin
        Thread.sleep(2000);
        //--------------------------------Selecting Merchant Name---------------------------
        List<WebElement> merchantsList=driver.findElements(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[3]/div/select/option"));
        for(WebElement element : merchantsList){
            if(element.getAttribute("label").equalsIgnoreCase(merchantName))
            {
                element.click();
                saveTextLog("Merchant selected: "+merchantName);
                Thread.sleep(2000);
            }
        }
        Thread.sleep(2000);
        //--------------------------------Enter UserId--------------------------------------------------
        saveTextLog("Entering Merchant User Details");
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input","merch"+random_strings); // UserId
        Merchant_Details_writer[0]="merch"+random_strings;
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[2]/div/div/label");
        //--------------------------------Enter First Name Last Name and Email--------------------------
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[1]/div/input","Fname_"+random_strings); // First Name
        Merchant_Details_writer[1]="Fname_"+random_strings;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[2]/div/input","Lname_"+random_strings); // Last name
        Merchant_Details_writer[2]="Lname_"+random_strings;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[1]/div/input","padmawati.taddy@bankfab.com"); // Email
        Merchant_Details_writer[3]="padmawati.taddy@bankfab.com";
        Thread.sleep(2000);
        initializeCsvWriter("Output_Files/Create_User_Details_Allsessions.csv"); // Write Details to File
        writeNextLineCsv(Merchant_Details_writer);
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv"); // Write Details to File
        writeNextLineCsv(Merchant_Details_writer);
        //--------------------------------Submitting User Merchant Details--------------------------------
        saveTextLog("Submitting Merchant User");
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");
        Thread.sleep(2000);
        String message=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]/p"))).getText();
        saveTextLog(Merchant_Details_writer[0]+" "+message);
        System.out.println(message);
    }
    @Step("Create Aggregate Maker")
    public void aggregate_maker(String random_string) throws InterruptedException, IOException {
        String[] Maker_Details_writer=new String[4];
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[2]"); // Aggregate
        Thread.sleep(3000);
        clickByXpath(driver,"/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/a"); // Role Name
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]/div"); // Maker Option
        Thread.sleep(2000);
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input","maker"+random_string); // UserId
        Maker_Details_writer[0]="maker"+random_string;
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[2]/div/div/label"); // Is Admin Toggle
        //-----------------------Enter First Name Last Name and Email--------------------------
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[1]/div/input","Fmaker_"+random_string); // First Name
        Maker_Details_writer[1]="Fmaker_"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[2]/div/input","Lmaker_"+random_string); // Last Name
        Maker_Details_writer[2]="Lmaker"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[1]/div/input","padmawati.taddy@bankfab.com"); // Email
        Maker_Details_writer[3]="padmawati.taddy@bankfab.com";
        Thread.sleep(2000);
        initializeCsvWriter("Output_Files/Create_User_Details_Allsessions.csv"); // Write to File
        writeNextLineCsv(Maker_Details_writer);
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv"); // Write Details to File
        writeNextLineCsv(Maker_Details_writer);
        //-------------------------------------Submitting User Maker Details-------------------------------------
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");
        String message=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]/p"))).getText();
        saveTextLog(Maker_Details_writer[0]+" "+message);
        System.out.println(message);
    }
    @Step("Create Aggragator Checker")
    public void aggregate_checker(String random_string) throws InterruptedException, IOException {
        String [] Checker_Detail_Writer=new String[4];
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[2]"); // Aggregate
        Thread.sleep(2000);
        clickByXpath(driver,"/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/a"); // Role Name
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[3]/div");
        Thread.sleep(2000);
        String userId="check"+random_string;
        Checker_Detail_Writer[0]=userId;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input",userId); // UserId
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[2]/div/div/label"); // Is Admin Toggle
        //-----------------------Enter First Name Last Name and Email--------------------------
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[1]/div/input","Fchecker_"+random_string); // First Name
        Checker_Detail_Writer[1]="Fchecker"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[2]/div/input","Lchecker_"+random_string); // Last Name
        Checker_Detail_Writer[2]="Lchecker"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[1]/div/input","padmawati.taddy@bankfab.com"); // Email
        Checker_Detail_Writer[3]="padmawati.taddy@bankfab.com";
        Thread.sleep(2000);
        initializeCsvWriter("Output_Files/Create_User_Details_Allsessions.csv"); // Write details to file
        writeNextLineCsv(Checker_Detail_Writer);
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv"); // Write Details to File
        writeNextLineCsv(Checker_Detail_Writer);
        //------------------------Submitting User Checker Details-------------------------------
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");
        String message=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]/p"))).getText();
        saveTextLog(Checker_Detail_Writer[0]+" "+message);
        System.out.println(message);
    }
    @Step("Edit Users")
    public void EditUser() throws Exception {
        ReadFromCSV read_data= new ReadFromCSV("Output_Files/Create_User_Detail_last_run.csv"); // UserIds from file
        String userIds;
        for(int i=1;i<read_data.SizeOfFile();i++)
        {
            userIds= read_data.ReadLineNumber(i)[0];
            Thread.sleep(1000);
            sendKeysByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div/div[2]/div/table/tbody/tr[1]/td[2]/input",userIds); // Search userId
            Thread.sleep(2000);
            clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div[2]/div/table/tbody/tr[2]/td[8]/button[3]/i"); // Delete user
            Alert alert= driver.switchTo().alert(); // alert Delete
            Thread.sleep(2000);
            alert.accept(); // Click on Ok
            String message=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]/p"))).getText();
            saveTextLog(userIds+" "+message);
            System.out.println("Delete");
            Thread.sleep(5000);
            clickByXpath(driver,"/html/body/div[1]/div/div/div[2]/div/table/tbody/tr[2]/td[8]/button[5]"); // Reset Password
            alert= driver.switchTo().alert();
            Thread.sleep(2000);
            alert.accept();
            String message1=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]/p"))).getText();
            saveTextLog(userIds+":"+message1);
            Screenshot(driver,"Reset Password");
            System.out.println("Reset Password");
            Thread.sleep(3000);
            clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div[2]/div/table/tbody/tr[2]/td[8]/button[2]/i"); // Edit
            Thread.sleep(1000);
            sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[2]/div/input","9422222222"); // Enter Phone Number
            clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button"); // Submit
            saveTextLog("Edit user"+" "+userIds);
            System.out.println("Edit");
            Screenshot(driver,"User Edit");
            driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div[2]/div/table/tbody/tr[1]/td[2]/input")).clear();
        }
    }



    //------------------------EPP----------------------
    @Test(priority=4, description = "EPP Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("EPP Flow")
    public void EPPflow() throws Exception {
        boolean testFail=false;
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to Maker account");
        login(credential[1],credential[3]);

        try{
            binUploadFile();
        }catch (Exception e){
            testFail=true;
            softAssert.fail();
        }
        try {

            merchantEPP();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            Screenshot(driver,"");
            saveTextLog("Merchant EPP response is slow");
            testFail=true;
            softAssert.fail(e.getMessage());
        }
        if (testFail){
            Assert.fail();
        }
    }
    @Step("Bin Upload File")
    public void binUploadFile() throws AWTException, InterruptedException {
        clickByXpath(driver,"//*[@id=\"js-side-menu-4\"]"); // EPP navigation
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-4\"]/ul/li[2]"); // Bin Upload File
        String downloadPath = System.getProperty("user.dir") + "\\downloadFiles";
        File directory=new File(downloadPath);
        int initial_size=directory.list().length;
        //sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[4]/div/div/div[1]/div/div/button","Configuration_Files/new24emiBinUpload.xlsx");
        waitAndClickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[4]/div/div/div[3]/div/div/a");
        Thread.sleep(5000);
        if(initial_size==directory.list().length)
        {
            saveTextLog("File not downloaded");
        }
        else{
            saveTextLog("Sample File Downloaded");
        }
        Thread.sleep(1000);
        uploadByXpathRobo(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[4]/div/div/div[1]/div/div/button",System.getProperty("user.dir")+"\\Configuration_Files\\Bin Uploads\\new24emiBinUpload.xlsx");
        Thread.sleep(5000);
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[2]/button");
        try{
            String message=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[2]/p"))).getText();
            saveTextLog(message);
            Screenshot(driver,"Bin Upload File Successful");
        } catch(Exception e)
        {
            Screenshot(driver,"BinUploadFailed");
        }
    }
    @Step("Merchant EPP")
    public void merchantEPP() throws Exception {
        boolean testFail = false;
        try {

            waitAndClickByXpath(driver, "//*[@id=\"js-side-menu-4\"]");
            clickByXpath(driver, "//*[@id=\"js-side-menu-4\"]/ul/li[1]/a");  // Navigate Merchant EPP

            String[] name = new String[]{"FAB", "ENDB", "ADCB"};
            ReadFromCSV r = new ReadFromCSV("Output_Files/Merchant_Authorization_Status_Last_Session.csv");

            for (int i = 0; i < name.length; i++) {
                Thread.sleep(5000);
                scrollToViewXpath(driver, "//*[@id=\"s2id_bankName\"]");
                waitAndClickByXpath(driver, "//*[@id=\"s2id_bankName\"]/a");
                sendKeysByXpath(driver, "//*[@id=\"select2-drop\"]/div/input", name[i]); // Input Bank Name
                driver.findElement(By.xpath("//*[@id=\"select2-drop\"]/div/input")).sendKeys(Keys.ENTER);
                saveTextLog("Bank Name: " + name[i]);
                Thread.sleep(4000);
                //--------------------Enter Merchant Names----------------------------
                for (int j = 1; j < r.SizeOfFile(); j++) {
                    Thread.sleep(1000);
                    System.out.println("File Size " + r.SizeOfFile());
                    System.out.println(r.ReadLineNumber(j)[9]);
                    if (r.ReadLineNumber(j)[9].equalsIgnoreCase("yes")) {   // Check If Merchant is authorised
                        String merchantname = r.ReadLineNumber(j)[0];
                        Thread.sleep(5000);
                        clickWithJavaScriptByXpath(driver, "/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/ul"); // Enter Merchant Name
                        for (int k = 0; k < merchantname.length(); k++) {
                            Thread.sleep(50);
                            sendKeysByXpath(driver, "/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/ul/li/input", String.valueOf(merchantname.charAt(k)));
                        }
                        saveTextLog("Merchant Name " + merchantname);
                        Thread.sleep(1000);
                        driver.findElement(By.xpath("/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/ul/li/input")).sendKeys(Keys.ENTER);
                    }

                    if (j == r.SizeOfFile() - 1) {                                // Enter amount
                        scrollToCenterXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[3]/div/input");
                        String amount = Integer.toString(createRandomNum(100, 200));
                        Thread.sleep(2000);
                        sendKeysByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[3]/div/input", amount);
                        saveTextLog("Amount entered for all Merchants and Bank");
                    }
                }
                Thread.sleep(8000);
                List<WebElement> tenure = driver.findElements(By.xpath("//*[@id=\"noOfMonthsPlan\"]/option")); // Check Tenure
                int index = createRandomNum(1, tenure.size() - 1);
                Thread.sleep(3000);
                tenure.get(index).click();
                saveTextLog("Tenure Month is selected is: " + tenure.get(index).getText());
                String percentageValue = Integer.toString(createRandomNum(1, 5));
                String interestRate = Integer.toString(createRandomNum(5, 15));
                sendKeysByXpath(driver, "//*[@id=\"percValueInput\"]", interestRate); // Enter Interest Rate
                saveTextLog("Interest Rate Entered: " + interestRate);
                Thread.sleep(2000);
                clickByXpath(driver, "//*[@id=\"processingFeeType\"]/option[3]"); // Processing Fee type
                saveTextLog("Processing Fee Type is selected as Percentage");
                Thread.sleep(2000);
                sendKeysByXpath(driver, "//*[@id=\"processingFeeValues\"]", percentageValue); // Percentage Value
                saveTextLog("Percentage Value is entered: " + percentageValue);
                Thread.sleep(2000);
                Screenshot(driver, "");
                clickWithJavaScriptByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/button"); // Add Tenure
                saveTextLog("Tenure is added");
                scrollToViewXpath(driver, "//*[@id=\"heading-action-wrapper\"]/div/h1");
                clickWithJavaScriptByXpath(driver, "//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");
                Thread.sleep(1000);
                Screenshot(driver, "");
                WebElement flag = waitForTwoElementsByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/form/div[1]", "//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]", 30);
                if (flag!=null)
                {
                    if(driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]")).isDisplayed()){
                        saveTextLog("Success Message: "+driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[2]/p")).getText());
                    }
                    if(driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[1]")).isDisplayed()){
                        saveTextLog("Error Message: "+driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/form/div[1]/p")).getText());
                        testFail=true;
                    }
                    Screenshot(driver,"");
                }else{
                    System.out.println("no Message");
                    testFail=true;
                }
                driver.navigate().refresh();
                waitAndClickByXpath(driver, "//*[@id=\"js-side-menu-4\"]");
                Thread.sleep(5000);
                clickByXpath(driver, "//*[@id=\"js-side-menu-4\"]/ul/li[1]/a");  // Navigate Merchant EPP
                Thread.sleep(10000);
            }
            Thread.sleep(2000);
            saveTextLog("Merchant EPP added");
            Thread.sleep(5000);

            /*
        ReadFromCSV r = new ReadFromCSV("Output_Files/Merchant_Authorization_Status_Last_Session.csv");
        waitAndClickByXpath(driver, "//*[@id=\"js-side-menu-4\"]");
        clickByXpath(driver, "//*[@id=\"js-side-menu-4\"]/ul/li[1]/a");
        int i = createRandomNum(1, r.SizeOfFile() - 1);
        Thread.sleep(2000);
        String username = r.ReadLineNumber(i)[0];
        //-------------------Merchant Name in Merchant List---------------------------
        if (r.ReadLineNumber(i)[9].equalsIgnoreCase("yes")) {
            sendKeysByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div[2]/div[2]/div/table/tbody/tr[1]/td[2]/input", username);
        } else {
            i = createRandomNum(1, i - 1);
            username = r.ReadLineNumber(i)[0];
            sendKeysByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div[2]/div[2]/div/table/tbody/tr[1]/td[2]/input", username);
        }
        Thread.sleep(2000);
        waitAndClickByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div[2]/div[2]/div/table/tbody/tr[2]/td[6]/button[3]/i"); // Delete Merchant EPP
        Thread.sleep(2000);
        System.out.println("Click");
        Alert alert = driver.switchTo().alert();
        saveTextLog(alert.getText() + " username: " + username);
        alert.accept();
        Thread.sleep(5000);
        driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div[2]/div[2]/div/table/tbody/tr[1]/td[2]/input")).clear();
        sendKeysByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div[2]/div[2]/div/table/tbody/tr[1]/td[2]/input", username); // Enter Merchant name
        saveTextLog("Merchant name for Edit: " + username);
        Thread.sleep(2000);
        clickWithJavaScriptByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/div[2]/div[2]/div/table/tbody/tr[2]/td[6]/button[2]"); // Merchant EPP Edit
        Thread.sleep(2000);
        List<WebElement> TenureDate;
        TenureDate = driver.findElements(By.xpath("//*[@id=\"tbl_posts_body\"]/tr/td[3]"));
        //----------------------Selecting unique Tenure month in Edit--------------------
        List<String> months = new ArrayList<>();
        for (WebElement w : TenureDate) {
            months.add(w.getText());
            //  System.out.println(months);
        }
        List<String> monthsTenure = Arrays.asList("3", "6", "9", "12", "18", "24", "36", "42", "54");
        // System.out.println("month tenure:"+ monthsTenure);
        int index_tenure;
        while (true) {
            index_tenure = createRandomNum(1, 8);
            if (!months.contains(monthsTenure.get(index_tenure))) {
                Thread.sleep(2000);
                String xpath = "//*[@id=\"noOfMonthsPlan\"]/option[@label=\"" + monthsTenure.get(index_tenure) + "\"]";
                System.out.println(xpath);
                Thread.sleep(4000);
                List<WebElement> tenure1 = driver.findElements(By.xpath("//*[@id=\"noOfMonthsPlan\"]/option"));
                tenure1.get(index_tenure).click();
                break;
            }
        }
        saveTextLog(username + " Edit Tenure Month is selected");
        String percetageValue = Integer.toString(createRandomNum(1, 5));
        String interestRate = Integer.toString(createRandomNum(5, 15));
        sendKeysByXpath(driver, "//*[@id=\"percValueInput\"]", interestRate);
        saveTextLog(username + " Edit Interest Rate is entered");
        Thread.sleep(2000);
        clickByXpath(driver, "//*[@id=\"processingFeeType\"]/option[3]"); // Processing Fee type
        saveTextLog(username + " Edit Processing Fee type is Selected");
        Thread.sleep(2000);
        sendKeysByXpath(driver, "//*[@id=\"processingFeeValues\"]", percetageValue); // Percentage Value
        saveTextLog(username + " Edit Percentage Value is added");
        Thread.sleep(2000);
        Screenshot(driver, "");
        clickWithJavaScriptByXpath(driver, "//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/button");
        saveTextLog(username + " Edit Add Tenure");
        Thread.sleep(3000);
        clickByXpath(driver, "//*[@id=\"heading-action-wrapper\"]/div/div/div[1]/div/select/option[2]");
        saveTextLog(username + " is Active");
        Thread.sleep(2000);
        Screenshot(driver, "");
        clickWithJavaScriptByXpath(driver, "//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");
        saveTextLog(username + " Edit Submit is clicked");
        Screenshot(driver, "");

         */
        }finally {
            if(testFail){
                throw new Exception("EPP Failed");
            }
        }
    }

    //-------------------------Transaction Management------------------
    @Test(priority=5, description = "Transaction Management Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transaction Management")
    public void TransactionManagement() throws Exception {
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to maker account");
        login(credential[1],credential[3]);
        masterBinUpload();
    }
    @Step("Master Bin Upload")
    public void masterBinUpload() throws AWTException, InterruptedException {
        Thread.sleep(7000);
        clickByXpath(driver,"//*[@id=\"js-side-menu-5\"]");
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-5\"]/ul/li");
        //sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[4]/div/div/div[1]/div/div/button","Configuration_Files/new24emiBinUpload.xlsx");
        Thread.sleep(5000);
        String downloadPath = System.getProperty("user.dir") + "\\downloadFiles";
        File directory=new File(downloadPath);
        int initial_size=directory.list().length;
        waitAndClickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[4]/div/div/div[3]/div/div/a");
        Thread.sleep(5000);
        if(initial_size==directory.list().length)
        {
            saveTextLog("File not downloaded");
        }
        else{
            saveTextLog("Sample File Downloaded");
        }
        Thread.sleep(5000);
        uploadByXpathRobo(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[4]/div/div/div[1]/div/div/button",System.getProperty("user.dir")+"\\Configuration_Files\\Bin Uploads\\new24masterBinUpload.xlsx");
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[2]/button");
        try{
            String message=wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/form/div[2]/p"))).getText();
            saveTextLog(message);
            Screenshot(driver,"Master Bin Upload File Successful");
        } catch(Exception e)
        {
            Screenshot(driver,"MasterBinUploadFailed");
        }
    }


    //------------------------Transaction Simulation---------------------
    @Test(priority = 6,description = "Transaction Simulation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test is for simulating transactions")
    public void transactionSimulationNewMerchants() throws Exception {
        boolean testFail=false;
        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") +"/Output_Files/Merchant_Authorization_Status_Last_Session.csv");
        deleteContentsOfCsv("Output_Files/Transactions_Status_Aggregator_Last_Session.csv");
        deleteContentsOfCsv("Output_Files/Transactions_Status_JS_Last_Session.csv");
        for(int i=1;i<lastRun.SizeOfFile();i++)
        {
            String [] lastData=lastRun.ReadLineNumber(i);

            if (lastData[1].equalsIgnoreCase("aggregator hosted")) {
                try {
                    aggregatorHostedSimulator(lastData, "VISA");
                }catch (Exception e){
                    softAssert.fail("Error in transaction");
                    testFail=true;
                    System.out.println(e.getMessage());
                }
                try {
                    aggregatorHostedSimulator(lastData, "Mastercard");
                }catch (Exception e){
                    softAssert.fail("Error in transaction");
                    testFail=true;
                    System.out.println(e.getMessage());
                }
                try {
                    aggregatorHostedSimulator(lastData, "Tabby");
                }catch (Exception e){
                    softAssert.fail("Error in transaction");
                    testFail=true;
                    System.out.println(e.getMessage());
                }
            } else if (lastData[1].equalsIgnoreCase("js checkout")) {
                try {
                    jsCheckoutSimulator(lastData, "VISA");
                }catch (Exception e){
                    softAssert.fail("Error in transaction");
                    testFail=true;
                    System.out.println(e.getMessage());
                }
                try {
                    jsCheckoutSimulator(lastData, "MasterCard");
                }catch (Exception e){
                    softAssert.fail("Error in transaction");
                    testFail=true;
                    System.out.println(e.getMessage());
                }
                try {
                    jsCheckoutSimulator(lastData, "Tabby");
                }catch (Exception e){
                    softAssert.fail("Error in transaction");
                    testFail=true;
                    System.out.println(e.getMessage());
                }
            }

            Thread.sleep(5000);
        }
        if(testFail){
            Assert.fail();
        }
    }

    //------------------------Transaction Simulation---------------------
    @Test(priority = 7,description = "Transaction Simulation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test is for simulating transactions")
    public void transactionSimulationPredefinedMerchants() throws Exception {
        boolean testFail=false;
        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") +"/Configuration_Files/Authorized_Merchants_Scenarios.csv");
        deleteContentsOfCsv("Output_Files/Transactions_Status_Aggregator_Last_Session.csv");
        deleteContentsOfCsv("Output_Files/Transactions_Status_JS_Last_Session.csv");
        for(int i=1;i<lastRun.SizeOfFile();i++)
        {
            String [] lastData=lastRun.ReadLineNumber(i);

                if (lastData[1].equalsIgnoreCase("aggregator hosted")) {
                    try {
                        aggregatorHostedSimulator(lastData, "VISA");
                    }catch (Exception e){
                        softAssert.fail("Error in transaction");
                        testFail=true;
                        System.out.println(e.getMessage());
                    }
                    try {
                        aggregatorHostedSimulator(lastData, "Mastercard");
                    }catch (Exception e){
                        softAssert.fail("Error in transaction");
                        testFail=true;
                        System.out.println(e.getMessage());
                    }
                    try {
                        aggregatorHostedSimulator(lastData, "Tabby");
                    }catch (Exception e){
                        softAssert.fail("Error in transaction");
                        testFail=true;
                        System.out.println(e.getMessage());
                    }
                } else if (lastData[1].equalsIgnoreCase("js checkout")) {
                    try {
                        jsCheckoutSimulator(lastData, "VISA");
                    }catch (Exception e){
                        softAssert.fail("Error in transaction");
                        testFail=true;
                        System.out.println(e.getMessage());
                    }
                    try {
                        jsCheckoutSimulator(lastData, "MasterCard");
                    }catch (Exception e){
                        softAssert.fail("Error in transaction");
                        testFail=true;
                        System.out.println(e.getMessage());
                    }
                    try {
                        jsCheckoutSimulator(lastData, "Tabby");
                    }catch (Exception e){
                        softAssert.fail("Error in transaction");
                        testFail=true;
                        System.out.println(e.getMessage());
                    }
                }

            Thread.sleep(5000);
        }
        if(testFail){
            Assert.fail();
        }
    }

    @Step("Aggregator Hosted Payment Simulator")
    public void aggregatorHostedSimulator(String[] merchantData, String mode) throws Exception {
        String[] orderDetails = new String[11];
        orderDetails[10]="No";
        ReadFromCSV portalInfo=new ReadFromCSV(System.getProperty("user.dir") + "\\Configuration_Files\\Transactions\\Payment Portals\\Aggregator_Hosted.csv");
        String aggregatorPortalUrl=portalInfo.ReadLineNumber(1)[0];
        driver.get(aggregatorPortalUrl);
        waitForElementXpath(driver,"//*[@id=\"me_id\"]");
        clickByXpath(driver,"/html/body/form/div/div[2]/div/div/div/div[1]/div/div/select/option[3]");
        Thread.sleep(500);
        sendKeysByXpath(driver,"//*[@id=\"me_id\"]",merchantData[7]);
        saveTextLog("Merchant Name: "+merchantData[0]);
        saveTextLog("Merchant Id: "+merchantData[7]);
        sendKeysByXpath(driver,"//*[@id=\"me_key\"]",merchantData[8]);
        saveTextLog("Merchant Key: "+merchantData[8]);
        String orderid=driver.findElement(By.xpath("//*[@id=\"order_no\"]")).getAttribute("value");
        saveTextLog("Order number: "+orderid);
        int amount=createRandomNum(100,500);
        driver.findElement(By.xpath("//*[@id=\"amount\"]")).clear();
        sendKeysByXpath(driver,"//*[@id=\"amount\"]",Integer.toString(amount));
        saveTextLog("Amount: "+amount);
        clickByXpath(driver,"/html/body/form/div/div[2]/div/div/div/div[3]/div[1]/div/div[3]/div/div/select/option[5]");
        saveTextLog("Country selected: "+driver.findElement(By.xpath("/html/body/form/div/div[2]/div/div/div/div[3]/div[1]/div/div[3]/div/div/select/option[5]")).getText());
        clickByXpath(driver,"/html/body/form/div/div[2]/div/div/div/div[3]/div[1]/div/div[4]/div/div/select/option[5]");
        saveTextLog("Currency selected: "+driver.findElement(By.xpath("/html/body/form/div/div[2]/div/div/div/div[3]/div[1]/div/div[4]/div/div/select/option[5]")).getText());
        scrollToCenterXpath(driver,"//*[@id=\"cust_name\"]");
        String randomName=getRandomString();
        sendKeysByXpath(driver,"//*[@id=\"cust_name\"]",randomName);
        saveTextLog("Name: "+randomName);
        sendKeysByXpath(driver,"//*[@id=\"email_id\"]",(randomName+"@gmail.com"));
        saveTextLog("Email: "+(randomName+"@gmail.com"));
        String phone=getTimestamp("yyMMddmmss");
        sendKeysByXpath(driver,"//*[@id=\"mobile_no\"]",phone);
        saveTextLog("Phone Number: "+phone);
        scrollToCenterXpath(driver,"//*[@id=\"bill_address\"]");
        sendKeysByXpath(driver,"//*[@id=\"bill_address\"]","Address_"+randomName);
        saveTextLog("Address: "+"Address_"+randomName);
        sendKeysByXpath(driver,"//*[@id=\"bill_city\"]","City_"+randomName);
        saveTextLog("City: "+"City_"+randomName);
        sendKeysByXpath(driver,"//*[@id=\"bill_state\"]","State_"+randomName);
        saveTextLog("State: "+"State_"+randomName);
        sendKeysByXpath(driver,"//*[@id=\"bill_country\"]","Country_"+randomName);
        saveTextLog("Country: "+"Country_"+randomName);
        String zip=getTimestampShort();
        sendKeysByXpath(driver,"//*[@id=\"bill_zip\"]",zip);
        saveTextLog("Zip: "+zip);
        Thread.sleep(2000);
        clickByXpath(driver,"/html/body/form/div/div[2]/div/div/div/div[4]/div/div/div/button");
        saveTextLog("Checkout Button Clicked");
        orderDetails[0]=merchantData[0];
        orderDetails[1]=merchantData[7];
        String stepName="Aggregator Hosted Transaction for: "+merchantData[0]+" | ID: "+merchantData[7];
        try{
            Thread.sleep(2000);
            waitForPageToLoad(driver);
            if(!driver.findElement(By.xpath("//*[@type=\"submit\"]")).isEnabled())
            {
                throw new Exception("Transaction Failed");
            }else if(mode.equalsIgnoreCase("tabby")){
                stepName+=" | Tabby";
                clickByXpath(driver,"//*[@id=\"PL\"]");
                saveTextLog("Pay Later Clicked");
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"payBankNameId\"]");
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"payinst\"]");
                ReadFromCSV tabbyCsv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/Tabby.csv");
                String[] tabbyDetails=tabbyCsv.ReadLineNumber(1);
                ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/Tabby_Key.csv");
                String[] tabbyMID=csv.ReadLineNumber(1);
                orderDetails[9]=tabbyMID[0];
                if(tabbyDetails[0].toLowerCase().contains("success"))
                {
                    stepName+=" Positive Scenario";
                }
                else if(tabbyDetails[0].toLowerCase().contains("reject"))
                {
                    stepName+=" Negative Scenario";
                }
                sendKeysByXpath(driver,"//*[@id=\"lp_emailid\"]",tabbyDetails[0]);
                saveTextLog("Email added: "+tabbyDetails[0]);
                sendKeysByXpath(driver,"//*[@id=\"lp_custName\"]",randomName);
                saveTextLog("Name added: "+randomName);
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"buttonLPInst\"]");
                Thread.sleep(1000);
                try {
                    WebElement iframe = driver.findElement(By.xpath("//*[@src=\"https://checkout.tabby.ai/checkout/\"]"));
                    driver.switchTo().frame(iframe);
                    //Thread.sleep(5000);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder=\"Mobile phone\"]"))).sendKeys(tabbyDetails[1]);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder=\"E-mail\"]"))).clear();
                    sendKeysByXpath(driver,"//*[@placeholder=\"E-mail\"]",tabbyDetails[0]);
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".Button__container--ecb91.FirstScreen__submit--73ed7.Button__primary--54247"))).click();
                    //wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    Thread.sleep(3000);
                    List<WebElement> otpInputBoxes=driver.findElement(By.cssSelector(".styles_react-code-input__CRulA")).findElements(By.tagName("input"));
                    int i=0;
                    for(WebElement e: otpInputBoxes){
                        e.sendKeys(Character.toString(tabbyDetails[2].charAt(i)));
                        i++;
                    }
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"Dropzone__container--5b4cf DropzoneUpload__dropzone--d30cb\"]")));
                    uploadByXpathRobo(driver,"//*[@class=\"Dropzone__container--5b4cf DropzoneUpload__dropzone--d30cb\"]",System.getProperty("user.dir")+"\\"+tabbyDetails[3]);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"Button__container--ecb91 ScanConfirm__callToAction--0a8c1 Button__primary--54247\"]")));
                    Thread.sleep(2000);
                    clickByXpath(driver,"//*[@class=\"Button__container--ecb91 ScanConfirm__callToAction--0a8c1 Button__primary--54247\"]");
                    Thread.sleep(5000);
                    boolean tabbyFail=false;
                    try {
                        if (driver.findElement(By.xpath("//*[@class=\"Rejected__title--6c840\"]")).isDisplayed()) {
                            tabbyFail=true;
                        }
                    }catch (Exception e){
                    }
                    finally {
                        if(tabbyFail){
                            throw new Exception();
                        }
                    }
                }catch (Exception e) {
                    //System.out.println(e.getMessage());
                    try {
                        String failMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"Rejected__title--6c840\"]"))).getText();
                        Screenshot(driver, failMessage);
                        saveTextLog("Tabby Unavailable");
                    } catch (Exception e2) {
                        Screenshot(driver, "Some elements are not Interactable/Available");
                    } finally {
                        throw new Exception("Tabby Unavailable");
                    }
                }

                }
            else {
                Thread.sleep(2000);
                ReadFromCSV cardDetails = null;
                saveTextLog("Card Payment in progress");
                if (merchantData[4].equalsIgnoreCase("yes")) {
                    stepName += " | Cybersource";
                    if(mode.equalsIgnoreCase("visa")) {
                        stepName+=" | VISA";
                        orderDetails[8]="VISA";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/Cybersource_Visa.csv");
                    }
                    else if(mode.equalsIgnoreCase("mastercard")) {
                        stepName+=" | MasterCard";
                        orderDetails[8]="MasterCard";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/Cybersource_MasterCard.csv");
                    }
                    if(merchantData[3].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/CybersourcePG_Non-3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[9]=MID[0];
                    }
                    else if(merchantData[2].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/CybersourcePG_3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[9]=MID[0];
                    }
                } else if (merchantData[5].equalsIgnoreCase("yes")) {
                    stepName += " | MPGS";
                    if(mode.equalsIgnoreCase("visa")) {
                        stepName+=" | VISA";
                        orderDetails[8]="VISA";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/MPGS_Visa.csv");
                    }
                    else if(mode.equalsIgnoreCase("mastercard")) {
                        stepName+=" | MasterCard";
                        orderDetails[8]="MasterCard";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/MPGS_MasterCard.csv");
                    }
                    if(merchantData[3].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/MPGS-Fab-Non-3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[9]=MID[0];
                    }
                    else if(merchantData[2].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/MPGS-Fab-3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[9]=MID[0];
                    }
                }
                List<String[]> cards = new ArrayList<>();
                if (cardDetails != null) {
                    for (int i = 1; i < cardDetails.SizeOfFile(); i++) {
                        String[] temp = cardDetails.ReadLineNumber(i);
                        if (merchantData[2].equalsIgnoreCase(temp[4]) || merchantData[3].equalsIgnoreCase(temp[5])) {
                            cards.add(temp);
                        }
                    }
                    Thread.sleep(2000);
                    String[] selectedCard = cards.get(createRandomNum(0, cards.size() - 1));
                    if(merchantData[2].equalsIgnoreCase(selectedCard[4])){
                        stepName+=" | 3DS";
                    }
                    else if(merchantData[3].equalsIgnoreCase(selectedCard[5])){
                        stepName+=" | Non-3DS";
                    }
                    waitForElementXpathByTime(driver,"//*[@id=\"cdCardNumber\"]",20);
                    sendKeysByXpath(driver, "//*[@id=\"cdCardNumber\"]", selectedCard[0]);
                    saveTextLog("Card Number: " + selectedCard[0]);
                    Thread.sleep(1000);

                    sendKeysByXpath(driver, "//*[@id=\"name\"]", randomName);
                    saveTextLog("Name on Card: " + randomName);
                    Thread.sleep(1000);

                    String month = selectedCard[1];
                    WebElement monthsList = driver.findElement(By.xpath("//*[@id=\"cdExpiryMonth\"]"));
                    monthsList.findElement(By.xpath("//*[@value=\"" + month + "\"]")).click();
                    saveTextLog("Expiry Month: " + month);
                    Thread.sleep(1000);

                    String year = selectedCard[2];
                    WebElement yearList = driver.findElement(By.xpath("//*[@id=\"cdExpYear\"]"));
                    yearList.findElement(By.xpath("//*[@value=\"" + year + "\"]")).click();
                    saveTextLog("Expiry Month: " + year);
                    Thread.sleep(1000);

                    sendKeysByXpath(driver, "//*[@id=\"cdCVV\"]", selectedCard[3]);
                    saveTextLog("CVV: " + selectedCard[3]);
                    Thread.sleep(1000);

                    try{
                        if(driver.findElement(By.xpath("//*[@class=\"emiCheck\"]/label")).isDisplayed())
                        {
                            clickByXpath(driver,"//*[@class=\"emiCheck\"]/label");
                            Screenshot(driver,"EMI Option Clicked");
                            scrollToCenterXpath(driver,"//*[@type=\"submit\"]");
                            stepName+=" | EMI";
                            orderDetails[10]="Yes";
                        }
                        else
                            saveTextLog("EMI not available");
                    }catch (Exception e){
                        saveTextLog("EMI not available");
                    }
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@type=\"submit\"]");
                    Thread.sleep(2000);
                    try {
                        if (merchantData[2].equalsIgnoreCase("yes")) {
                            waitForPageToLoad(driver);
                            if (driver.getCurrentUrl().contains("https://merchantacsstag.cardinalcommerce.com")) {
                                sendKeysByXpath(driver, "//*[@id=\"password\"]", "1234");
                                saveTextLog("Password entered: " + "1234");
                                Thread.sleep(1000);
                                Screenshot(driver,"");
                                clickByXpath(driver, "//*[@value=\"Submit\"]");
                            } else if (driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/checkPay#no-back")) {
                                Screenshot(driver,"");
                                waitForElementToBeStale(driver, "//*[@onclick=\"changeAction('YES')\"]");
                            } else if (driver.getCurrentUrl().contains("https://ap.gateway.mastercard.com/acs/VisaACS") ||
                                    driver.getCurrentUrl().contains("https://ap.gateway.mastercard.com/acs/MastercardACS")) {
                                Screenshot(driver,"");
                                clickByXpath(driver, "//*[@value=\"Submit\"]");
                                Thread.sleep(1000);
                                try{
                                    waitForPageToLoad(driver);
                                    if (driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/captureBayanpayPgResponse#no-back")) {
                                        Screenshot(driver, "");
                                        waitForElementToBeStale(driver, "//*[@onclick=\"changeAction('YES')\"]");
                                    }
                                }catch(Exception e){
                                }
                            }
                        }
                    } catch (Exception e) {
                        Screenshot(driver,"Error in payment: " + e.getMessage());
                    }
                }
            }
        }catch (Exception e){
            if(e.getMessage().equalsIgnoreCase("tabby unavailable"))
            {
                saveTextLog("Transaction Failed for Tabby");
            }
            else if(driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/payment#no-back")){
                Screenshot(driver,driver.findElement(By.xpath("//*[@id=\"resnavtab\"]/div/div")).getText());
                orderid=driver.findElement(By.xpath("//*[@id=\"pg-col\"]/div[1]/div[3]/span[2]")).getText();
                if(merchantData[9].equalsIgnoreCase("no")){
                    saveTextLog("Scenario Successful, Merchant was Not Authorized");
                }
                else {
                    saveTextLog("Some error occurred");
                }
            }else Screenshot(driver,"Some other error occurred");
        }
        finally {

            try {

                waitForPageToLoad(driver);
                if(driver.getCurrentUrl().contains("https://fabpg.safexpay.com/simulator/response?")) {
                    String status="";

                    status=wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"cs-main-body\"]/div/div/div/div[6]/div[1]/div/div"))).getText();
                    Thread.sleep(1000);
                    Screenshot(driver, "Payment response page");

                    try {
                        orderDetails[2] = status;
                        orderDetails[3] = driver.findElement(By.xpath("/html/body/div/div[2]/div/div/div/div/div[4]/div[1]/div/div")).getText();
                        orderDetails[4] = driver.findElement(By.xpath("/html/body/div/div[2]/div/div/div/div/div[5]/div[1]/div/div")).getText();
                        orderDetails[5] = driver.findElement(By.xpath("/html/body/div/div[2]/div/div/div/div/div[5]/div[2]/div/div")).getText();
                        orderDetails[6] = driver.findElement(By.xpath("/html/body/div/div[2]/div/div/div/div/div[6]/div[2]/div/div")).getText();
                        orderDetails[7] = driver.findElement(By.xpath("/html/body/div/div[2]/div/div/div/div/div[4]/div[2]/div/div")).getText();
                        orderDetails[8] = mode;
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    for (int i = 0; i < 7; i++) {
                        System.out.println(orderDetails[i]);
                    }
                    if(status.length()>1)
                        stepName+=" | "+status;
                    stepName+=" | Order: "+orderDetails[1];
                    stepName+=" | Amount: "+orderDetails[7];
                    stepName+=" | Transaction AG REF: "+orderDetails[4];
                    stepName+=" | Transaction PG REF: "+orderDetails[5];
                    stepName+=" | Transaction Date & Time: "+orderDetails[6];
                }
                else {
                    orderDetails[2]="Cancelled";
                    orderDetails[7]=Integer.toString(amount);
                    stepName+=" | Status: Cancelled";
                    stepName+=" | Order: "+orderid;
                    stepName+=" | Amount: "+amount;
                }

            }catch (Exception e)
            {
                Screenshot(driver,"");
            }
            finally {

                initializeCsvWriter("Output_Files/Transactions_Status_Aggregator_All_Session.csv");
                writeNextLineCsv(orderDetails);

                initializeCsvWriter("Output_Files/Transactions_Status_Aggregator_Last_Session.csv");
                writeNextLineCsv(orderDetails);
                changeStepName(stepName);
                if(merchantData[9].equalsIgnoreCase("no")&&orderDetails[2].toLowerCase().contains("success")){
                    throw new Exception("Unverified Merchant Transaction");
                }
                if(merchantData[9].equalsIgnoreCase("yes")&&!orderDetails[2].toLowerCase().contains("success")){
                    throw new Exception("Transaction Failed");
                }
            }
        }
    }

    @Step("JS Checkout Payment Simulator")
    public void jsCheckoutSimulator(String[] merchantData, String mode) throws Exception {
        String[] orderDetails = new String[8];
        orderDetails[7]="No";
        String orderId=null, status=null;
        ReadFromCSV portalInfo = new ReadFromCSV(System.getProperty("user.dir") + "\\Configuration_Files\\Transactions\\Payment Portals\\JS_Checkout.csv");
        String aggregatorPortalUrl = portalInfo.ReadLineNumber(1)[0];
        driver.get(aggregatorPortalUrl);
        waitForElementXpath(driver, "//*[@id=\"meid\"]");
        Thread.sleep(500);
        sendKeysByXpath(driver, "//*[@id=\"meid\"]", merchantData[7]);
        saveTextLog("Merchant Name: "+merchantData[0]);
        saveTextLog("Merchant Id: " + merchantData[7]);
        sendKeysByXpath(driver, "//*[@id=\"key\"]", merchantData[8]);
        saveTextLog("Merchant Key: " + merchantData[8]);
        int amount = createRandomNum(100, 500);
        driver.findElement(By.xpath("//*[@id=\"amount\"]")).clear();
        sendKeysByXpath(driver, "//*[@id=\"amount\"]", Integer.toString(amount));
        saveTextLog("Amount: " + amount);
        String randomName = getRandomString();
        clickByXpath(driver, "//*[@type=\"submit\"]");
        saveTextLog("Submit Button Clicked");
        orderDetails[0] = merchantData[0];
        orderDetails[1] = merchantData[7];
        orderDetails[4]=Integer.toString(amount);
        String stepName = "JS Checkout Transaction for: " + merchantData[0] + " | ID: " + merchantData[7];
        waitForPageToLoad(driver);
        Thread.sleep(2000);
        clickByXpath(driver, "//*[@id=\"buy\"]");
        try {
            Thread.sleep(2000);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"paymentModal\"]/div/div")));
            Thread.sleep(2000);
            if(!driver.findElement(By.xpath("//*[@id=\"paymentModal\"]")).getAttribute("class").contains("_sp-show")) {
                throw new Exception("Transaction Window not Opening");
            }
            if (mode.equalsIgnoreCase("tabby")) {
                orderDetails[5]="Tabby";
                stepName+=" | Tabby";
                clickByXpath(driver,"//*[@id=\"sp-payment-mode-tabby\"]");
                saveTextLog("Pay Later Clicked");
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"sp-footer-btn-tabby\"]");
                Thread.sleep(1000);
                ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/Tabby_Key.csv");
                String[] tabbyMID=csv.ReadLineNumber(1);
                orderDetails[6]=tabbyMID[0];

                ReadFromCSV tabbyCsv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/Tabby.csv");
                String[] tabbyDetails=tabbyCsv.ReadLineNumber(1);
                if(tabbyDetails[0].toLowerCase().contains("success"))
                {
                    stepName+=" Positive Scenario";
                }
                else if(tabbyDetails[0].toLowerCase().contains("reject"))
                {
                    stepName+=" Negative Scenario";
                }
                try {
                    Thread.sleep(4000);
                    if(!driver.findElement(By.xpath("//*[@class=\"_sp-lds-dual-ring-sm\"]")).isEnabled()){
                        throw new Exception("Tabby Unavailable");
                    }
                    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@src=\"https://checkout.tabby.ai/checkout/\"]")));
                    WebElement iframe = driver.findElement(By.xpath("//*[@src=\"https://checkout.tabby.ai/checkout/\"]"));
                    driver.switchTo().frame(iframe);

                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder=\"Mobile phone\"]"))).clear();
                    sendKeysByXpath(driver,"//*[@placeholder=\"Mobile phone\"]",tabbyDetails[1]);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder=\"E-mail\"]"))).clear();
                    sendKeysByXpath(driver,"//*[@placeholder=\"E-mail\"]",tabbyDetails[0]);
                    Thread.sleep(1000);
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".Button__container--ecb91.FirstScreen__submit--73ed7.Button__primary--54247"))).click();

                    Thread.sleep(3000);
                    List<WebElement> otpInputBoxes=driver.findElement(By.cssSelector(".styles_react-code-input__CRulA")).findElements(By.tagName("input"));
                    int i=0;
                    for(WebElement e: otpInputBoxes){
                        e.sendKeys(Character.toString(tabbyDetails[2].charAt(i)));
                        i++;
                    }
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"Dropzone__container--5b4cf DropzoneUpload__dropzone--d30cb\"]")));
                    uploadByXpathRobo(driver,"//*[@class=\"Dropzone__container--5b4cf DropzoneUpload__dropzone--d30cb\"]",System.getProperty("user.dir")+"\\"+tabbyDetails[3]);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"Button__container--ecb91 ScanConfirm__callToAction--0a8c1 Button__primary--54247\"]")));
                    Thread.sleep(2000);
                    clickByXpath(driver,"//*[@class=\"Button__container--ecb91 ScanConfirm__callToAction--0a8c1 Button__primary--54247\"]");
                    Thread.sleep(5000);
                    boolean tabbyFail=false;
                    try {
                        if (driver.findElement(By.xpath("//*[@class=\"Rejected__title--6c840\"]")).isDisplayed()) {
                            tabbyFail=true;
                        }
                    }catch (Exception e){
                    }
                    finally {
                        driver.switchTo().defaultContent();
                        if(tabbyFail){
                            throw new Exception();
                        }
                    }
                }catch (Exception e) {
                    //System.out.println(e.getMessage());
                    try {
                        String failMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class=\"Rejected__title--6c840\"]"))).getText();
                        Screenshot(driver, failMessage);
                        saveTextLog("Tabby Unavailable");
                    } catch (Exception e2) {
                        Screenshot(driver, "Some elements are not Interactable/Available");
                    } finally {
                        throw new Exception("Tabby Unavailable");
                    }
                }

            } else {
                Thread.sleep(2000);
                ReadFromCSV cardDetails = null;
                saveTextLog("Card Payment in progress");
                if (merchantData[4].equalsIgnoreCase("yes")) {
                    stepName += " | Cybersource";
                    if(mode.equalsIgnoreCase("visa")) {
                        stepName+=" | VISA";
                        orderDetails[5]="VISA";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/Cybersource_Visa.csv");
                    }
                    else if(mode.equalsIgnoreCase("mastercard")) {
                        stepName+=" | MasterCard";
                        orderDetails[5]="MasterCard";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/Cybersource_MasterCard.csv");
                    }
                    if(merchantData[3].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/CybersourcePG_Non-3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[6]=MID[0];
                    }
                    else if(merchantData[2].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/CybersourcePG_3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[6]=MID[0];
                    }
                } else if (merchantData[5].equalsIgnoreCase("yes")) {
                    stepName += " | MPGS";
                    if(mode.equalsIgnoreCase("visa")) {
                        stepName+=" | VISA";
                        orderDetails[5]="VISA";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/MPGS_Visa.csv");
                    }
                    else if(mode.equalsIgnoreCase("mastercard")) {
                        stepName+=" | MasterCard";
                        orderDetails[5]="MasterCard";
                        cardDetails = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Transactions/Card Information/MPGS_MasterCard.csv");
                    }
                    if(merchantData[3].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/MPGS-Fab-Non-3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[6]=MID[0];
                    }
                    else if(merchantData[2].equalsIgnoreCase("yes")){
                        ReadFromCSV csv = new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Payment_Modes/MPGS-Fab-3DS_Key.csv");
                        String[] MID = csv.ReadLineNumber(1);
                        orderDetails[6]=MID[0];
                    }
                }
                List<String[]> cards = new ArrayList<>();
                if (cardDetails != null) {
                    for (int i = 1; i < cardDetails.SizeOfFile(); i++) {
                        String[] temp = cardDetails.ReadLineNumber(i);
                        if (merchantData[2].equalsIgnoreCase(temp[4]) || merchantData[3].equalsIgnoreCase(temp[5])) {
                            cards.add(temp);
                        }
                    }
                    Thread.sleep(2000);
                    String[] selectedCard = cards.get(createRandomNum(0, cards.size() - 1));
                    if (merchantData[2].equalsIgnoreCase(selectedCard[4])) {
                        stepName += " | 3DS";
                    } else if (merchantData[3].equalsIgnoreCase(selectedCard[5])) {
                        stepName += " | Non-3DS";
                    }
                    sendKeysByXpath(driver, "//*[@id=\"sp-txt-card-number\"]", selectedCard[0]);
                    saveTextLog("Card Number: " + selectedCard[0]);
                    Thread.sleep(1000);

                    sendKeysByXpath(driver, "//*[@id=\"sp-txt-card-name\"]", randomName);
                    saveTextLog("Name on Card: " + randomName);
                    Thread.sleep(1000);

                    String month = selectedCard[1];
                    WebElement monthsList = driver.findElement(By.xpath("//*[@id=\"sp-txt-card-expiry-month\"]"));
                    monthsList.findElement(By.xpath("//*[@value=\"" + month + "\"]")).click();
                    saveTextLog("Expiry Month: " + month);
                    Thread.sleep(1000);

                    String year = selectedCard[2];
                    WebElement yearList = driver.findElement(By.xpath("//*[@id=\"sp-txt-card-expiry-year\"]"));
                    yearList.findElement(By.xpath("//*[@value=\"" + year + "\"]")).click();
                    saveTextLog("Expiry Month: " + year);
                    Thread.sleep(1000);

                    sendKeysByXpath(driver, "//*[@id=\"sp-txt-card-cvv\"]", selectedCard[3]);
                    saveTextLog("CVV: " + selectedCard[3]);

                    try{
                        if(driver.findElement(By.xpath("//*[@id=\"sp-epp-option-available\"]/following-sibling::span/label")).isDisplayed())
                        {
                            clickByXpath(driver,"//*[@id=\"sp-epp-option-available\"]/following-sibling::span/label");
                            Screenshot(driver,"EMI Option Clicked");
                            scrollToCenterXpath(driver,"//*[@id=\"sp-footer-btn\"]");
                            stepName+=" EMI";
                            orderDetails[7]="Yes";
                        }
                        else
                            saveTextLog("EMI not available");
                    }catch (Exception e){
                        saveTextLog("EMI not available");
                    }
                    Thread.sleep(1000);
                    String mainWindow= driver.getWindowHandle();

                    clickByXpath(driver, "//*[@id=\"sp-footer-btn\"]");
                    Thread.sleep(2000);

                    int maxWait = 20;
//                    while (driver.getWindowHandles().size() > 1 && maxWait > 0) {
//                        Thread.sleep(1000);
//                        maxWait--;
//                    }
                    if(driver.getWindowHandles().size() > 1)
                    {
                       Set<String> handles= driver.getWindowHandles();
                        Iterator ite=handles.iterator();
                        while(ite.hasNext()){

                            try {
                            String popupHandle=ite.next().toString();
                            if(!popupHandle.contains(mainWindow))
                            {
                                    driver.switchTo().window(popupHandle);
                                    waitForPageToLoad(driver);
                                    if (driver.getCurrentUrl().contains("https://merchantacsstag.cardinalcommerce.com")) {
                                        sendKeysByXpath(driver, "//*[@id=\"password\"]", "1234");
                                        saveTextLog("Password entered: " + "1234");
                                        Thread.sleep(1000);
                                        Screenshot(driver, "");
                                        clickByXpath(driver, "//*[@value=\"Submit\"]");
                                        Thread.sleep(4000);
                                        if(driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/jscheckoutPayments")||driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/captureCybersourcePgResponse")){
                                            Screenshot(driver,"Page not found error");
                                            driver.close();
                                            driver.switchTo().window(mainWindow);
                                            throw new Exception("PAGE NOT FOUND");
                                        }
                                        driver.switchTo().window(mainWindow);
                                    }else if (driver.getCurrentUrl().contains("https://ap.gateway.mastercard.com/acs/VisaACS") ||
                                            driver.getCurrentUrl().contains("https://ap.gateway.mastercard.com/acs/MastercardACS")) {
                                        Screenshot(driver, "");
                                        clickByXpath(driver, "//*[@value=\"Submit\"]");
                                        Thread.sleep(1000);
                                        try {
                                            waitForPageToLoad(driver);
                                            if (driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/captureBayanpayPgResponse#no-back")) {
                                                Screenshot(driver, "");
                                                waitForElementToBeStale(driver, "//*[@onclick=\"changeAction('YES')\"]");
                                            }
                                        } catch (Exception e) {
                                        }
                                        Thread.sleep(1000);
                                        driver.switchTo().window(mainWindow);
                                    }
                                    else if(driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/jscheckoutPayments")||driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/captureCybersourcePgResponse")){
                                        Screenshot(driver,"Page not found error");
                                        driver.close();
                                        driver.switchTo().window(mainWindow);
                                        throw new Exception("PAGE NOT FOUND");
                                    }
                                    else if(driver.getCurrentUrl().contains("https://safexpayuat.bankfab.com/agcore/redirectedUrlForError/")){
                                        Screenshot(driver,"Redirecting Error");
                                        driver.close();
                                        driver.switchTo().window(mainWindow);
                                        throw new Exception("PAGE NOT FOUND");
                                    }


                            }
                            }catch (Exception e){
                                if(e.getMessage().equalsIgnoreCase("page not found")){
                                    throw e;
                                }
                            }
                        }

                    }
                    driver.switchTo().window(mainWindow);
                }
            }
        } catch (Exception e) {
            if(e.getMessage().equalsIgnoreCase("tabby unavailable"))
            {
                saveTextLog("Transaction Failed for Tabby");
            }
            else if(e.getMessage().equalsIgnoreCase("transaction window not opening")){
                Screenshot(driver,e.getMessage());
                if(merchantData[9].equalsIgnoreCase("no")){
                    saveTextLog("Scenario successful, Merchant was not authorized");
                }
                else saveTextLog("Some error occurred");
            }
            else if(!e.getMessage().equalsIgnoreCase("page not found"))
                Screenshot(driver, "ERROR IN JS CHECKOUT");
            else
                System.out.println(e.getMessage());
        }finally {
            try {
                try {
                    int maxWait=60;
                    while(!driver.findElement(By.xpath("//*[@id=\"sp-payment-porccessing\"]")).getAttribute("style").equalsIgnoreCase("display: none;")&&maxWait>0)
                    {
                        maxWait--;
                        Thread.sleep(1000);
                    }
                }catch (Exception e){}
                if (driver.findElement(By.xpath("//*[@id=\"sp-success-payment\"]")).isDisplayed()) {
                    status = driver.findElement(By.xpath("//div[@class=\"sp-response-message\"]/h6")).getAttribute("innerText");
                    orderId = driver.findElement(By.xpath("//*[@id=\"sp-success-payment-transaction-id\"]")).getText();
                    stepName+=" | Successful";
                    stepName+=" | Order ID: "+orderId;
                    orderDetails[2]="Successful";
                    orderDetails[3]=orderId;
                } else if (driver.findElement(By.xpath("//*[@id=\"sp-failed-payment\"]")).isDisplayed()) {
                    status = "TRANSACTION FAILED";
                    orderId = driver.findElement(By.xpath("//*[@id=\"sp-failed-payment-transaction-id\"]")).getText();
                    saveTextLog(driver.findElement(By.xpath("//*[@class=\"sp-response-message failed\"]")).getText());
                    stepName+=" | Failed";
                    stepName+=" | Order ID: "+orderId;
                    orderDetails[2]="Failed";
                    orderDetails[3]=orderId;
                }
                else if(merchantData[9].equalsIgnoreCase("no"))
                {
                    orderDetails[2]="Unauthorized";
                    stepName+=" | Unauthorized";
                    status="UNAUTHORIZED TRANSACTION";
                    Screenshot(driver, "Payment Unauthorized");
                }
                else {
                    orderDetails[2]="Cancelled";
                    stepName+=" | Cancelled";
                    status="TRANSACTION CANCELLED";
                    Screenshot(driver, "Payment cancelled as it was taking too long");
                }
                Thread.sleep(2000);
                saveTextLog("Order Status: " + status);
                saveTextLog("Order ID: " + orderId);
            }catch (Exception e){
                Screenshot(driver,"Payment Failed");
            }
            finally {

                initializeCsvWriter("Output_Files/Transactions_Status_JS_All_Session.csv");
                writeNextLineCsv(orderDetails);

                initializeCsvWriter("Output_Files/Transactions_Status_JS_Last_Session.csv");
                writeNextLineCsv(orderDetails);
                changeStepName(stepName);
                if(merchantData[9].equalsIgnoreCase("no")&&orderDetails[2].toLowerCase().contains("success")){
                    throw new Exception("Unverified Merchant Transaction");
                }
                if(merchantData[9].equalsIgnoreCase("yes")&&!orderDetails[2].toLowerCase().contains("success")){
                    throw new Exception("Transaction Failed");
                }
            }
        }
    }

    //-----------------------------Merchant Refund test-------------------
    @Test(priority = 8,description = "Merchant refund test")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Logging into merchant account")
    public void refundSimulation() throws Exception {
        boolean testFail=false;
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\MerchantPortalUrl.csv";  //path to get login details file or credentials file
        ReadFromCSV csvCredentials = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csvCredentials.ReadLineNumber(1); //Reads first line containing login id and password
        String url=credential[0];
        path = System.getProperty("user.dir") + "/Configuration_Files/Created_Merchants_Scenarios.csv";  //path to get login details file or credentials file
        csvCredentials = new ReadFromCSV(path);  //Reading credentials file
        ReadFromCSV velocity=new ReadFromCSV(System.getProperty("user.dir") + "/Configuration_Files/Create_Merchant_Data/Velocity_Details.csv");
        String[] velocityDetails=velocity.ReadLineNumber(1);
        List<String[]> SuccessfulTransactions=new ArrayList<>();
        ReadFromCSV transactionsCsvAgg=new ReadFromCSV(System.getProperty("user.dir") + "\\Output_Files\\Transactions_Status_Aggregator_Last_Session.csv");

        for (int i=1;i<transactionsCsvAgg.SizeOfFile();i++){
            String[] temp=transactionsCsvAgg.ReadLineNumber(i);
            if(temp[2].equalsIgnoreCase("successful")){
                SuccessfulTransactions.add(temp);
            }
        }
        ReadFromCSV transactionsCsvJs=new ReadFromCSV(System.getProperty("user.dir") + "/Output_Files/Transactions_Status_JS_Last_Session.csv");
        for (int i=1;i<transactionsCsvJs.SizeOfFile();i++){
            String[] temp=transactionsCsvJs.ReadLineNumber(i);
            if(temp[2].equalsIgnoreCase("successful")){
                SuccessfulTransactions.add(temp);
            }
        }
        //int randomTransaction=createRandomNum(1,SuccessfulTransactions.size()-1);
        path = System.getProperty("user.dir") + "/Configuration_Files/Created_Merchants_Scenarios.csv";  //path to get login details file or credentials file
        ReadFromCSV authorizedMerchants = new ReadFromCSV(path);  //Reading authorized merchants file
        String [] authMerch;

        for(String[] transactionDetails: SuccessfulTransactions) {

            for (int i = 1; i < csvCredentials.SizeOfFile(); i++) {

                credential = csvCredentials.ReadLineNumber(i);
                authMerch=authorizedMerchants.ReadLineNumber(i);
                if(authMerch[9].equalsIgnoreCase("yes")) {
                    if (credential[0].equals(transactionDetails[0])) {

                        openUrl(url,"Logging in to Merchant account: "+credential[10]);
                        waitForPageToLoad(driver);
                        login(credential[10], credential[11]);
                        Thread.sleep(1000);
                        clickByXpath(driver, "//*[@id=\"js-side-menu-0\"]");
                        saveTextLog("MIS Button Clicked");
                        Thread.sleep(500);
                        driver.findElement(By.xpath("//*[@id=\"js-side-menu-0\"]")).findElement(By.xpath("//*[@href=\"#transactionMIS\"]")).click();
                        saveTextLog("Transaction MIS Clicked");
                        try {
                            refundTransaction(velocityDetails, transactionDetails);
                        } catch (Exception e) {
                            saveTextLog(e.getMessage());
                            softAssert.fail();
                            testFail = true;
                        }
                        break;
                    } else credential = null;
                }
            }
        }
        if (testFail){
            Assert.fail();
        }
    }
    @Step("Refund Transaction")
    public void refundTransaction(String[] velocityDetails,String[] transactionDetails) throws Exception {
        waitForElementXpathByTime(driver,"//*[@id=\"viewData\"]",5);
        Thread.sleep(2000);

        driver.findElement(By.xpath("//*[@id=\"viewData\"]")).findElement(By.xpath("thead/tr/th[3]/div/input")).sendKeys(transactionDetails[3]);
        Thread.sleep(5000);

        List<WebElement> firstRow=driver.findElements(By.xpath("//*[@id=\"viewData\"]/tbody/tr/td"));

        boolean testFail=false;
        boolean refundPass=false;
        try {
            //-------------------Checking if there are any successful transactions------------------
            if (firstRow.size() > 12) {
                try {
                    int amount = Integer.parseInt(firstRow.get(8).getText());
                    saveTextLog("Initial transaction amount: " + amount);
                    hoverByElement(driver, firstRow.get(12).findElement(By.xpath("div/button")));
                    Thread.sleep(1000);
                    firstRow.get(12).findElement(By.xpath("div/div/a[3]")).click();
                    //----------------------Scenario- Refund amount less than minimum limit-------------------
                    saveTextLog("Refunding amount less than minimum limit");
                    Thread.sleep(3000);
                    String amountString = driver.findElement(By.xpath("//*[@id=\"refund\"]/div/div/div[3]/div[1]/div/div/div/div/div[2]/p/b")).getText();
                    String[] tempArr = amountString.split(": ");
                    amount = Integer.parseInt(tempArr[tempArr.length - 1]);
                    saveTextLog("Current Leftover Amount= " + amount);
                    int belowMinLimit = createRandomNum(1, Integer.parseInt(velocityDetails[0]) - 1);
                    driver.findElement(By.xpath("//*[@ng-model=\"refundAmount\"]")).clear();
                    sendKeysByXpath(driver, "//*[@ng-model=\"refundAmount\"]", String.valueOf(belowMinLimit));
                    saveTextLog("Input Amount: " + belowMinLimit);
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"refundClose\"]");
                    Thread.sleep(2000);
                    WebElement messageElement = waitForTwoElementsByXpath(driver, "//*[@id=\"refundSuccess\"]/div/div/div[2]/div/div/div", "//*[@id=\"refund\"]/div/div/div[2]", 20);
                    if (messageElement != null) {
                        String message = messageElement.getText();
                        Screenshot(driver, "Message: " + message);
                        if (messageElement.getAttribute("class").contains("alert-success")) {
                            saveTextLog("Refund Below Limit Successful");
                            testFail = true;
                        }
                    }
                    Thread.sleep(2000);
                    List<WebElement> closeBtn = driver.findElements(By.xpath("//*[@class=\"close\"]"));
                    for (WebElement btn : closeBtn) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            break;
                        }
                    }

                    //----------------------Scenario- Refund amount More than Maximum limit-------------------
                    Thread.sleep(2000);
                    firstRow = driver.findElements(By.xpath("//*[@id=\"viewData\"]/tbody/tr/td"));
                    hoverByElement(driver, firstRow.get(12).findElement(By.xpath("div/button")));
                    Thread.sleep(1000);
                    firstRow.get(12).findElement(By.xpath("div/div/a[3]")).click();

                    saveTextLog("Refunding amount more than max limit");
                    Thread.sleep(3000);
                    int temp = 0;
                    if (transactionDetails.length > 6)
                        temp = (int) Float.parseFloat(transactionDetails[7]);
                    else temp = (int) Float.parseFloat(transactionDetails[4]);
                    int overMaxLimit = createRandomNum(Integer.parseInt(velocityDetails[1]) + 1, temp);
                    driver.findElement(By.xpath("//*[@ng-model=\"refundAmount\"]")).clear();
                    sendKeysByXpath(driver, "//*[@ng-model=\"refundAmount\"]", String.valueOf(overMaxLimit));
                    saveTextLog("Input Amount: " + overMaxLimit);
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"refundClose\"]");
                    Thread.sleep(2000);
                    messageElement = waitForTwoElementsByXpath(driver, "//*[@id=\"refundSuccess\"]/div/div/div[2]/div/div/div", "//*[@id=\"refund\"]/div/div/div[2]", 20);
                    if (messageElement != null) {
                        String message = messageElement.getText();
                        Screenshot(driver, "Message: " + message);
                        if (messageElement.getAttribute("class").contains("alert-success")) {
                            saveTextLog("Refund Over Limit Successful");
                            testFail = true;
                        }
                    }
                    Thread.sleep(2000);
                    closeBtn = driver.findElements(By.xpath("//*[@class=\"close\"]"));
                    for (WebElement btn : closeBtn) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            break;
                        }
                    }
                    //----------------------Scenario- Refund amount within limit-------------------
                    Thread.sleep(2000);
                    firstRow = driver.findElements(By.xpath("//*[@id=\"viewData\"]/tbody/tr/td"));
                    hoverByElement(driver, firstRow.get(12).findElement(By.xpath("div/button")));
                    Thread.sleep(1000);
                    firstRow.get(12).findElement(By.xpath("div/div/a[3]")).click();

                    saveTextLog("Refunding within refund limits");
                    Thread.sleep(3000);
                    int inLimit = createRandomNum(Integer.parseInt(velocityDetails[0]), Integer.parseInt(velocityDetails[1]));
                    driver.findElement(By.xpath("//*[@ng-model=\"refundAmount\"]")).clear();
                    Thread.sleep(500);
                    sendKeysByXpath(driver, "//*[@ng-model=\"refundAmount\"]", String.valueOf(inLimit));
                    saveTextLog("Input Amount: " + inLimit);
                    Thread.sleep(500);
                    clickByXpath(driver, "//*[@id=\"refundClose\"]");
                    Thread.sleep(2000);
                    messageElement = waitForTwoElementsByXpath(driver, "//*[@id=\"refundSuccess\"]/div/div/div[2]/div/div/div", "//*[@id=\"refund\"]/div/div/div[2]", 20);
                    if (messageElement != null) {
                        String message = messageElement.getText();
                        Screenshot(driver, "Message: " + message);
                        if (message.toLowerCase().contains("success")) {
                            refundPass=true;
                            amount = amount - inLimit;
                            saveTextLog("Leftover amount= " + amount + "\nAmount refunded= " + inLimit);
                        } else {
                            saveTextLog("Amount not refunded");
                        }
                        if (messageElement.getAttribute("class").contains("alert-danger")) {
                            saveTextLog("Refund Within Limit Not Successful");
                            testFail = true;
                        }
                    }
                    Thread.sleep(2000);
                    closeBtn = driver.findElements(By.xpath("//*[@class=\"close\"]"));
                    for (WebElement btn : closeBtn) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            break;
                        }
                    }
                    //----------------------Scenario- Full refund-------------------
                    Thread.sleep(2000);
                    firstRow = driver.findElements(By.xpath("//*[@id=\"viewData\"]/tbody/tr/td"));
                    hoverByElement(driver, firstRow.get(12).findElement(By.xpath("div/button")));
                    Thread.sleep(1000);
                    firstRow.get(12).findElement(By.xpath("div/div/a[3]")).click();

                    saveTextLog("Refunding full amount");
                    Thread.sleep(3000);
                    clickByXpath(driver, "//*[@id=\"refund\"]/div/div/div[3]/div[3]/div/div[1]/label");
                    Thread.sleep(2000);
                    clickByXpath(driver, "//*[@id=\"refundClose\"]");
                    Thread.sleep(1000);
                    messageElement = waitForTwoElementsByXpath(driver, "//*[@id=\"refundSuccess\"]/div/div/div[2]/div/div/div", "//*[@id=\"refund\"]/div/div/div[2]", 20);
                    if (messageElement != null) {
                        String message = messageElement.getText();
                        Screenshot(driver, "Message: " + message);
                        if (message.toLowerCase().contains("success")) {
                            refundPass=true;
                            amount = 0;
                            saveTextLog("Leftover amount= " + amount + "\nAmount refunded= " + inLimit);
                        } else {
                            saveTextLog("Amount not refunded");
                        }
                    }
                    Thread.sleep(2000);
                    closeBtn = driver.findElements(By.xpath("//*[@class=\"close\"]"));
                    for (WebElement btn : closeBtn) {
                        if (btn.isDisplayed()) {
                            btn.click();
                            break;
                        }
                    }
                } catch (Exception e) {
                    if (driver.findElement(By.xpath("//*[@id=\"refund\"]/div/div/div[2]")).getAttribute("style").equalsIgnoreCase("display: block;")) {
                        String message = driver.findElement(By.xpath("//*[@id=\"refund\"]/div/div/div[2]/p")).getText();
                        Screenshot(driver, message);
                    }
                    throw new Exception("Refunds Failed");
                }

                //------------------Viewing refunds-------------------
                if(refundPass) {
                    Thread.sleep(5000);
                    firstRow = driver.findElements(By.xpath("//*[@id=\"viewData\"]/tbody/tr/td"));
                    hoverByElement(driver, firstRow.get(12).findElement(By.xpath("div/button")));
                    Thread.sleep(1000);
                    firstRow.get(12).findElement(By.xpath("div/div/a[1]")).click();
                    try {
                        waitForElementXpathByTime(driver, "//*[@id=\"refundDetailsId\"]", 20);
                        Thread.sleep(1000);
                        clickByXpath(driver, "//*[@id=\"refundDetailsId\"]");
                        if (driver.findElements(By.xpath("//*[@ng-show=\"refundDetailsAct\"]/tr")).size() > 1) {
                            Screenshot(driver, "Refunds Listed");
                        } else Screenshot(driver, "No refunds available");
                    } catch (Exception e) {
                        Screenshot(driver, "Refunds not available for this transaction");
                    }
                }else {
                    saveTextLog("No refunds were successful");
                }
            }
        }finally {
            if(testFail){
                throw new Exception("Refund failed");
            }
        }
    }



    //-------------------------MIS------------------
    @Test(priority=9, description = "MIS")
    @Severity(SeverityLevel.CRITICAL)
    @Description("MIS")
    public void TransactionMIS() throws Exception {
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to Maker Account");
        login(credential[1],credential[3]);
        try {
            transactionMIS();
        }catch (Exception e){
            Assert.fail();
        }
    }
    @Step("Transaction MIS Download")
    public void transactionMIS() throws Exception {
        boolean testFail=false;
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-2\"]");
        clickWithJavaScriptByXpath(driver,"//*[@id=\"js-side-menu-2\"]/ul/li"); // Navigate to Transaction MIS Download
        saveTextLog("Transaction MIS Download");
        Thread.sleep(2000);
        waitAndClickByXpath(driver,"//*[@id=\"formatdrdw\"]");
        //------------------------Check for file downloaded or not--------------------------------
        String downloadPath = System.getProperty("user.dir") + "\\downloadFiles";
        File directory=new File(downloadPath);
        int initial_size=directory.list().length;
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/div/ul/li[1]/a/i"); // Download Excl
        Thread.sleep(5000);
        Boolean flag=false;
        if(initial_size==directory.list().length)
        {
            saveTextLog("Excel File not Downloaded");
        }
        else{
            saveTextLog("Excel File Downloaded");
            Screenshot(driver,"");
            flag=true;
        }
        String downloadPathcsv = System.getProperty("user.dir") + "\\downloadFiles";
        File directorycsv=new File(downloadPathcsv);
        int initial_sizecsv=directorycsv.list().length;
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"formatdrdw\"]");
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/div/ul/li[2]/a/i"); // Download CSV
        Thread.sleep(5000);
        if(initial_sizecsv==directorycsv.list().length)
        {
            saveTextLog("CSV File not Downloaded");
        }
        else{
            saveTextLog("CSV File Downloaded");
            Screenshot(driver,"");
        }
        Thread.sleep(4000);
        ReadFromXlsFile readerxls = new ReadFromXlsFile();
        if(flag) {
            //---------------------Date Check in downloaded Transaction File-------------------------
            StringBuilder datecurrent = new StringBuilder(getTimestamp("d-M-yyyy"));
            readerxls.ReadXls("downloadFiles/TransactionMis_" + datecurrent + ".xls");
            int j = 1;
            //---------Traverse Transaction MIS Excl File-----------------------
            saveTextLog("Transaction MIS OrderNumber Check");
            String path;
            int columnNumber;
            String TransactionName=null;
            for(int i=0;i<2;i++){
                if(i==0)
                {
                    path="Output_Files/Transactions_Status_JS_Last_Session.csv";
                    columnNumber=7;
                    TransactionName="JS Checkout";
                }
                else{
                    path="Output_Files/Transactions_Status_Aggregator_Last_Session.csv";
                    columnNumber=10;
                    TransactionName="Aggregate";
                }
                ReadFromCSV readTransaction=new ReadFromCSV(path);
                String orderNumberTransaction=null;
                for(int k=1;k<readTransaction.SizeOfFile()-1;k++)
                {
                    if(readTransaction.ReadLineNumber(k)[2].equalsIgnoreCase("Successful"))
                    {
                        if(readTransaction.ReadLineNumber(k)[columnNumber].equalsIgnoreCase("yes")) {
                            orderNumberTransaction = readTransaction.ReadLineNumber(k)[3];
                            break;
                        }
                    }
                }
                while (true) {
                    try {
                        String orderNumber=readerxls.ReadCellXls(0, j, 5);
                        String EPPStatus=readerxls.ReadCellXls(0,j,24);
                        if(orderNumber.contains(orderNumberTransaction) && EPPStatus.contains("SUCCESS"))
                        {
                            saveTextLog("Transaction Order Number with EPP "+ orderNumberTransaction+" Exists "+TransactionName);
                            break;
                        }
                        else if(orderNumber.contains(orderNumberTransaction)){
                            testFail=true;
                        }
                        else{
                            j++;
                        }
                    } catch (Exception e) {
                        if (e.getMessage().contains("because the return value of \"org.apache.poi.hssf.usermodel.HSSFSheet.getRow(int)\" is null")) {
                            saveTextLog("Transaction MIS Excl File Traversed No Transaction "+TransactionName);
                        }
                        else if (e.getMessage().contains("\"java.lang.CharSequence.toString()\" because \"s\" is null"))
                        {
                            saveTextLog("No EPP Transaction in "+TransactionName);
                        }
                        else {
                            System.out.println(e.getMessage());
                            saveTextLog("Error in Transaction MIS "+TransactionName); // fail
                        }
                        break;
                    }
                    finally {
                        if(testFail){
                            throw new Exception("EPP Success not found");
                        }
                    }
                }
            }
        }
    }


    //----------------------Refund-----------------------
    @Test(priority=10, description = "Refund MIS")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Refund")
    public void refundMIS() throws Exception {
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0],"Logging in to Maker Account");
        login(credential[1],credential[3]);

        refundMISDownload();
    }
    @Step("Refund MIS")
    public void refundMISDownload() throws Exception {
        boolean testFail=false;
        clickByXpath(driver,"//*[@id=\"js-side-menu-3\"]");
        clickByXpath(driver,"//*[@id=\"js-side-menu-3\"]/ul/li/a"); // Refund MIS navigation
        List<WebElement> merchant;
        merchant=wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"mid\"]/option"))); // Merchant MID Processor
        for(WebElement w:merchant)
        {
            if(w.getText().equalsIgnoreCase("fab_safexpay1"))
            {
                w.click();
                break;
            }
        }
        //------------------------Check for file downloaded or not--------------------------------
        String downloadPath = System.getProperty("user.dir") + "\\downloadFiles";
        File directory=new File(downloadPath);
        int initial_size=directory.list().length;
        Thread.sleep(4000);
        waitAndClickByXpath(driver,"//*[@id=\"downloadID2\"]/a");
        Thread.sleep(5000);
        Boolean flag=false;
        if(initial_size==directory.list().length)
        {
            saveTextLog("Refund MIS not downloaded"); // fail
            testFail=true;
        }
        else{
            saveTextLog("Refund MIS Downloaded");
            Screenshot(driver,"");
            flag=true;
        }
        if(flag) {
            File download = new File("downloadFiles");
            File[] files = download.listFiles();
            String path = null;
            String filename = null;
            for (File f : files) {
                if (f.getName().contains("Refund")) {
                    path = f.getPath();
                    // filename=f.getName();
                }
            }
            ZipFile zip = new ZipFile(path);
            zip.extractAll("downloadFiles/");
            // String [] RefundCSVname=filename.split(".");
            // ReadFromCSV csvreader= new ReadFromCSV("downloadFiles/"+RefundCSVname[0]+".csv");
        }
        if(testFail){
            throw new Exception("Refund MIS not downloaded");
        }
    }



}
