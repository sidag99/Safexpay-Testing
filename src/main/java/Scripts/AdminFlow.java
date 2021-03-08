package Scripts;
import Read_Write_Files.ReadFromCSV;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static Functions.ClickElement.*;
import static Functions.Driver.driverAllocation;
import static Functions.ScrollToView.*;
import static Functions.SelectRandomFile.createRandomNum;
import static Functions.SelectRandomFile.selectRandomFileFromList;
import static Read_Write_Files.WriteToCSV.*;
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
        dataCreateMerchant= new String[11];
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
                dataPricing = csvPricing.ReadLineNumber(j);//Reading data from csv
                createMerchantFormFill(dataBusiness,dataPricing);

                initializeCsvWriter(allSessionsWritePath);
                writeNextLineCsv(dataCreateMerchant);

                initializeCsvWriter(currentSessionWritePath);
                writeNextLineCsv(dataCreateMerchant);

                Thread.sleep(15000);
            }
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
        String testName= "Merchant"+ testNumber;  //Make random name
        sendKeysByXpath(driver,"//*[@ng-model=\"name\"]", testName);  //Enter Merchant
        saveTextLog("New Merchant Name: "+testName);
        dataCreateMerchant[0]=testName;
        Thread.sleep(1000);
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

        Thread.sleep(1000);
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
            Thread.sleep(500);
        }//else dataCreateMerchant[2]="no";

        if(data[2].equalsIgnoreCase("no")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[2]/div/div/label");  //Toggle 3DS
            saveTextLog("3DS Turned OFF");
            dataCreateMerchant[2]="no";
            Thread.sleep(500);
        }else dataCreateMerchant[2]="yes";


        if(data[3].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[2]/div/div/div/div/label");  //Toggle Non-3DS
            saveTextLog("Non-3DS Turned ON");
            dataCreateMerchant[3]="yes";
            Thread.sleep(500);
        }else dataCreateMerchant[3]="no";


        if(data[4].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[1]/div/div/label");  //Toggle Refund API
            saveTextLog("Refund API Turned ON");
            //dataCreateMerchant[5]="yes";
            Thread.sleep(500);
        }//else dataCreateMerchant[5]="no";


        if(data[5].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[20]/div/div/div[2]/div/div/label"); //Toggle Refund Portal
            saveTextLog("Refund Portal Turned ON");
            //dataCreateMerchant[6]="yes";
            Thread.sleep(500);
        }//else dataCreateMerchant[6]="no";


        clickByXpath(driver,"//div//button[@ng-click=\"saveBusinessDetails()\"]");  //Go to Next Page
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        String message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);

    //------------------------Filling User Details--------------------------

        saveTextLog("FILLING USER DETAILS");
        Thread.sleep(3000);
        waitAndClickByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[1]/div/a");  //Clicking Add User
        saveTextLog("Clicked \"add user\"");
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[1]/div/div/div/div/div/label");  //Make admin toggle
        saveTextLog("Make Admin button clicked");
        Thread.sleep(1000);
        String randomName=getRandomString();
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[2]/div[1]/div/input","FName"+randomName);  //First name added
        saveTextLog("First Name Added: FName"+randomName);
        dataCreateMerchant[4]="FName"+randomName;
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[2]/div[2]/div/input","LName"+randomName);  //Last Name added
        saveTextLog("Last Name Added: LName"+randomName);
        dataCreateMerchant[5]="LName"+randomName;
        Thread.sleep(1000);
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
        Thread.sleep(3000);
        dataCreateMerchant[7] = "no";
        dataCreateMerchant[8] = "no";
        dataCreateMerchant[9] = "no";
        dataCreateMerchant[10]="no";
        if(paymentModes[0].equalsIgnoreCase("yes")||paymentModes[1].equalsIgnoreCase("yes")) {
            waitAndClickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[1]/div/div/div/label");
            saveTextLog("Card button clicked");
            Thread.sleep(2000);
            if (paymentModes[0].equalsIgnoreCase("yes")) {
                String keyCyberCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\CybersourcePG_Key.csv";
                ReadFromCSV csv = new ReadFromCSV(keyCyberCsvPath);  //Reading encryption data
                String[] key = csv.ReadLineNumber(1);
                clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[1]/div/div/legend/a");  //Add cybersource pg
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[1]/div/div/legend/a");  //Add cybersource pg
                Thread.sleep(1000);
                sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[2]/div[1]/input", key[0]);  //Add MID
                Thread.sleep(1000);
                sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[2]/div[2]/input", key[1]);  //Add Encryption key
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[1]/div/div/a");  //Select Currency
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select AED
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[2]/div/div/a");  //Select Schema
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select Mastercard
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[3]/div/div/a");  //Select Operating mode
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select International
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[1]/div/div/a");//Select Currency
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select AED
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[2]/div/div/a");//Select Schema
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[2]");//Select Visa
                Thread.sleep(1000);
                clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[3]/div/div/a");//Select Operating mode
                Thread.sleep(1000);
                clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
                Thread.sleep(1000);
                saveTextLog("Cybersource PG Details added");
                dataCreateMerchant[7] = "yes";
                Thread.sleep(1000);
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
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[1]/div/div/legend/a");  //Add MPGS-Fab
                    Thread.sleep(1000);
                    sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[2]/div[1]/input", key[0]);  //Add MID
                    Thread.sleep(1000);
                    sendKeysByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[3]/div[2]/div[2]/input", key[1]);  //Add Encryption key
                    Thread.sleep(1000);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[4]/div[1]/div/div/a");  //Select Currency
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select AED
                    Thread.sleep(1000);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[4]/div[2]/div/div/a");  //Select Schema
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select Mastercard
                    Thread.sleep(1000);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[4]/div[3]/div/div/a");  //Select Operating mode
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");  //Select International
                    Thread.sleep(1000);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[5]/div[1]/div/div/a");//Select Currency
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select AED
                    Thread.sleep(1000);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[5]/div[2]/div/div/a");//Select Schema
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[2]");//Select Visa
                    Thread.sleep(1000);
                    clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[3]/div[5]/div[3]/div/div/a");//Select Operating mode
                    Thread.sleep(1000);
                    clickByXpath(driver, "//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
                    Thread.sleep(1000);
                    if (dataCreateMerchant[3].equalsIgnoreCase("yes") && dataCreateMerchant[2].equalsIgnoreCase("yes")) {
                        dataCreateMerchant[9] = "yes";
                    } else if (dataCreateMerchant[3].equalsIgnoreCase("yes")) {
                        dataCreateMerchant[9] = "yes";
                    } else if (dataCreateMerchant[2].equalsIgnoreCase("yes")) {
                        dataCreateMerchant[8] = "yes";
                    }
                    saveTextLog("MPGS Details added");
                    Thread.sleep(1000);
                }
            }
        }

        if(paymentModes[2].equalsIgnoreCase("yes"))
        {
            String keyMID="sa_store";
            waitAndClickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[1]/div/div/div/label");
            Thread.sleep(2000);
            clickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[2]/div[1]/div/div/legend/a");
            Thread.sleep(1000);
            sendKeysByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[2]/div[2]/div[1]/input",keyMID);
            Thread.sleep(1000);
            sendKeysByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[2]/div[2]/div[2]/input",keyMID);
            Thread.sleep(1000);
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[2]/div/div[2]/div[4]/div[1]/div/div/a");  //Select Currency
            Thread.sleep(1000);
            clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li");  //Select AED
            Thread.sleep(1000);
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[2]/div/div[2]/div[4]/div[2]/div/div/a"); //Select Schema
            Thread.sleep(1000);
            clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li");  //Select NA
            Thread.sleep(1000);
            clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[2]/div/div[2]/div[4]/div[3]/div/div/a");//Select Operating mode
            Thread.sleep(1000);
            clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
            Thread.sleep(1000);
            saveTextLog("Tabby Details Added");
            Thread.sleep(1000);
            dataCreateMerchant[10]="yes";
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
        Thread.sleep(3000);
        scrollToCenterXpath(driver,"//*[@id=\"Velocity\"]/form/div[10]/div/div/button[2]");
        Thread.sleep(2000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[8]/div[1]/div/input",dataVelocity[0]);  //enter Refund Min Transaction Amount
        saveTextLog("Refund Min Transaction Amount: "+dataVelocity[0]);
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[8]/div[2]/div/input",dataVelocity[1]);  //enter Refund Max Transaction Amount
        saveTextLog("Refund Max Transaction Amount: "+dataVelocity[1]);
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[9]/div[1]/div/input",dataVelocity[2]);  //enter Refund Daily Transaction Count
        saveTextLog("Refund Daily Transaction Count: "+dataVelocity[2]);
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[9]/div[2]/div/input",dataVelocity[3]);  //enter Refund Daily Transaction Amount
        saveTextLog("Refund Daily Transaction Amount: "+dataVelocity[3]);
        Thread.sleep(2000);
        clickByXpath(driver,"//div//button[@ng-click=\"saveVelocityDetails();\"]");
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);
    //--------------------------Fill Other Form---------------------
        saveTextLog("FILL OTHER FORM");
        String ReferralDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Referral_URLs.csv";  //path to get details file
        ReadFromCSV csvReferral = new ReadFromCSV(ReferralDetailsCsvPath);  //Reading file

        Thread.sleep(3000);
        String[] dataReferral=csvReferral.ReadLineNumber(1);
        int i=1;
        try {
            while (!dataReferral[0].isEmpty()) {
                waitAndClickByXpath(driver, "//*[@id=\"Others\"]/form/div[1]/div/div/legend/a");
                Thread.sleep(1000);
                sendKeysByXpath(driver, "//*[@id=\"Others\"]/form/div[1]/div/div/div[" + i + "]/div/div[1]/input", dataReferral[0]);
                i++;
                dataReferral = csvReferral.ReadLineNumber(i);
            }
        }catch (Exception e){
            saveTextLog("Referrals Added");
        }
        Thread.sleep(2000);
        clickByXpath(driver,"//div//button[@ng-click=\"saveOtherDetails();\"]");
        saveTextLog("Next Button Clicked");
        waitForElementXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p");
        message=driver.findElement(By.xpath("//*[@id=\"avantgarde\"]/div[1]/div/div/div/div[2]/p")).getText();
        Screenshot(driver,"SnackBar Message: "+message);
        String mpgs;
        if(dataCreateMerchant[8].equalsIgnoreCase("yes")||dataCreateMerchant[9].equalsIgnoreCase("yes"))
        {
            mpgs="yes";
        }
        else mpgs="no";
        String stepName="Merchant Name: "+dataCreateMerchant[0]+" | Integration Type: "+dataCreateMerchant[1]+" | 3DS Enabled: "+dataCreateMerchant[2]+
                " | Non-3DS Enabled: "+dataCreateMerchant[3]+" | Cybersource: "+dataCreateMerchant[7]+" | MPGS: "+mpgs+" | Tabby: "+dataCreateMerchant[10];
        changeStepName(stepName);
    }



    //------------------------Editing merchants created-------------------------------
    @Test(priority=2, description = "Merchant Edit Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Edit Flow")
    public void editMerchant() throws Exception{
        openManageMerchantMaker();
        Thread.sleep(5000);
        deleteContentsOfCsv("Output_Files/Merchant_Authorization_Status_Last_Session.csv");
        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_Last_Session.csv");
        for(int i=1;i<lastRun.SizeOfFile();i++)
        {
            String [] lastData=lastRun.ReadLineNumber(i);
            editMerchant(lastData);
            Thread.sleep(5000);
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

    @Step("Authorize Merchant")
    public void editMerchant(String [] merchantData) throws InterruptedException, IOException {
        waitForElementXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input");
        Thread.sleep(1000);
        driver.findElement(By.xpath("/html/body/div[1]/div/div/div/div[4]/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/input")).clear();
        String temp=merchantData[0];
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
    //-----------------User Creation Module------------------
    //@Test(priority=3, description = "User Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User Creation Flow")
    public void createUser() throws Exception {
        String random_string=getRandomString();
        merchant_userDetails(random_string);
        Thread.sleep(2000);
        aggregate_maker(random_string);
        Thread.sleep(2000);
        aggregate_checker(random_string);
        Thread.sleep(2000);
        //EditUser();
    }
    //------------Entering User Details-----------------
    @Step("Enter Merchant User Details")
    public void merchant_userDetails(String random_strings) throws Exception {
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
        String ReferralDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Referral_URLs.csv";  //path to get details file
        ReadFromCSV csvReferral = new ReadFromCSV(ReferralDetailsCsvPath);  //Reading file
        waitAndClickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[3]/div/select/option[@label=\"merchantdemo\"]");
        Thread.sleep(2000);
        //--------------------------------Enter UserId--------------------------------------------------
        saveTextLog("Entering Merchant User Details");
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input","merch"+random_strings); // UserId
        Merchant_Details_writer[0]="merchant"+random_strings;
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
        deleteContentsOfCsv("Output_Files/Create_User_Detail_last_run.csv");
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv"); // Write Details to File
        writeNextLineCsv(Merchant_Details_writer);
        //--------------------------------Submitting User Merchant Details--------------------------------
        saveTextLog("Submitting Merchant User");
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");
    }

    @Step("Create Aggregate Maker")
    public void aggregate_maker(String random_string) throws InterruptedException, IOException {
        String[] Maker_Details_writer=new String[4];
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[2]"); // Aggregate
        Thread.sleep(2000);
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
    }

    @Step("Create Aggragator Checker")
    public void aggregate_checker(String random_string) throws InterruptedException, IOException {
        String [] Checker_Detail_Writer=new String[4];
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[2]"); // Aggregate
        Thread.sleep(2000);
        clickByXpath(driver,"/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div/a"); // Role Name
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]/div"); // Checker
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
    }

    //------------------------Opening Checker Admin account-------------------------------
    @Test(priority=4, description = "Opening Safexpay website and logging in for checker")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test setup by opening the login page and logging in")
    public void testSetupAdminChecker() throws Exception {
        String path = System.getProperty("user.dir") + "\\Configuration_Files\\Admin_Credentials.csv";  //path to get login details file or credentials file
        ReadFromCSV csv = new ReadFromCSV(path);  //Reading credentials file
        String[] credential = csv.ReadLineNumber(1); //Reads first line containing login id and password
        System.out.println(Arrays.toString(credential));
        openUrl(credential[0]);
        login(credential[2],credential[3]);
    }

    //------------------------Authorizing merchants created-------------------------------
    @Test(priority=5, description = "Checker Authorize Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Merchant Authorization Flow")
    public void checkerAdmin() throws Exception{
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

            authorizeMerchant(lastData, true);
            Thread.sleep(5000);
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
            String [] newData=new String[11];
            newData[0]=merchantData[0];
            newData[1]=merchantData[1];
            newData[2]=merchantData[2];
            newData[3]=merchantData[3];
            newData[4]=merchantData[7];
            newData[5]=merchantData[8];
            newData[6]=merchantData[9];
            newData[7]=merchantData[10];
            newData[8]=id;
            newData[9]=decryptionKey;
            if(authorized)
                newData[10]="Yes";
            else newData[10]="No";
            initializeCsvWriter("Output_Files/Merchant_Authorization_Status_All_Sessions.csv"); // Write Details to File
            writeNextLineCsv(newData);
            initializeCsvWriter("Output_Files/Merchant_Authorization_Status_Last_Session.csv"); // Write Details to File
            writeNextLineCsv(newData);
        }
        else {
            Screenshot(driver,"Merchant name not available on first index");
        }

    }
    //--------------------------------Transaction Simulation----------------
    @Test(priority = 6,description = "Transaction Simulation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("This test is for simulating transactions")
    public void transactionSimulation() throws Exception {
        ReadFromCSV lastRun=new ReadFromCSV(System.getProperty("user.dir") + "\\Output_Files\\Merchant_Authorization_Status_Last_Session.csv");
        for(int i=1;i<lastRun.SizeOfFile();i++)
        {
            String [] lastData=lastRun.ReadLineNumber(i);
            authorizeMerchant(lastData, true);
            Thread.sleep(5000);
        }
    }
}
