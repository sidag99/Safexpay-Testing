package Scripts;
import Functions.ScrollToView;
import Read_Write_Files.ReadFromCSV;
import Read_Write_Files.WriteToCSV;
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
        String businessDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Business_Details.csv";  //path to get login details file or credentials file
        ReadFromCSV csvBusiness = new ReadFromCSV(businessDetailsCsvPath);  //Reading credentials file
        String[] dataBusiness = csvBusiness.ReadLineNumber(1);  //Reading first line of data from csv
        String pricingDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Pricing_Details.csv";  //path to get login details file or credentials file
        ReadFromCSV csvPricing = new ReadFromCSV(pricingDetailsCsvPath);  //Reading credentials file
        String[] dataPricing = csvPricing.ReadLineNumber(1);
        fillBusinessDetailsForm(dataBusiness);
        fillUserDetailsForm();
        fillPricingForm(dataPricing);
        fillVelocityForm();
        fillOtherForm();
        String allSessionsWritePath = System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_All_Sessions.csv";
        initializeCsvWriter(allSessionsWritePath);
        writeNextLineCsv(dataCreateMerchant);
        String currentSessionWritePath = System.getProperty("user.dir") + "\\Output_Files\\Create_Merchant_Last_Session.csv";
        deleteContentsOfCsv(currentSessionWritePath);
        initializeCsvWriter(currentSessionWritePath);
        writeNextLineCsv(dataCreateMerchant);
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
    @Step("Fill Business Details Form")
    public void fillBusinessDetailsForm(String[] data) throws InterruptedException {
        waitForElementXpath(driver,"//*[@id=\"BusinessDetails\"]/form/div[1]/div[2]/div/input");
        Thread.sleep(1000);
        String testNumber=getTimestampShort();
        String testName= "Merchant_"+ testNumber;  //Make random name
        sendKeysByXpath(driver,"//*[@ng-model=\"name\"]", testName);  //Enter Merchant
        saveTextLog("New Merchant Name: "+testName);
        dataCreateMerchant[0]=testName;
        Thread.sleep(1000);
        clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[4]/div[1]/div/div/a");  //Clicks Sector drop down
        saveTextLog("Clicked \"Sector\" Dropdown Button");
        Thread.sleep(1000);
        int selectedSector=selectRandomFileFromList(driver,"/html/body/div[4]/ul/li");
        if(selectedSector>1)
        {
            saveTextLog("Selected Sector Name: "+driver.findElement(By.xpath("/html/body/div[4]/ul/li["+selectedSector+"]/div")).getText());
            clickByXpath(driver,"/html/body/div[4]/ul/li["+selectedSector+"]");   //Selects random item from list
        }
        Thread.sleep(1000);
        scrollToCenterXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[6]/div[1]/div/div");  //Scroll Country to center of screen
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
        }//else dataCreateMerchant[2]="no";
        Thread.sleep(500);

        if(data[2].equalsIgnoreCase("no")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[1]/div/div[2]/div/div/label");  //Toggle 3DS
            saveTextLog("3DS Turned OFF");
            dataCreateMerchant[2]="yes";
        }else dataCreateMerchant[2]="no";

        Thread.sleep(500);

        if(data[3].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "/html/body/div[1]/div/div/div/div[4]/div/div[1]/form/div[19]/div[2]/div/div/div/div/label");  //Toggle Non-3DS
            saveTextLog("Non-3DS Turned ON");
            dataCreateMerchant[3]="yes";
        }else dataCreateMerchant[3]="no";

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

    //------------------------Filling User Details--------------------------
    @Step("Fill User Details Form")
    public void fillUserDetailsForm() throws InterruptedException {
        Thread.sleep(3000);
        clickByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[1]/div/a");  //Clicking Add User
        saveTextLog("Clicked \"add user\"");
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[1]/div/div/div/div/div/label");  //Make admin toggle
        saveTextLog("Make Admin button clicked");
        Thread.sleep(1000);
        String randomName=getRandomString();
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[2]/div[1]/div/input","FName_"+randomName);  //First name added
        saveTextLog("First Name Added: FName_"+randomName);
        dataCreateMerchant[4]="FName_"+randomName;
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[2]/div[2]/div/input","LName_"+randomName);  //Last Name added
        saveTextLog("Last Name Added: LName_"+randomName);
        dataCreateMerchant[5]="LName_"+randomName;
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"UserDetails\"]/form/div[2]/div[3]/div[1]/div/input","padmawati.taddy@bankfab.com");  //Email Added
        saveTextLog("Email Added: padmawati.taddy@bankfab.com");
        dataCreateMerchant[6]="padmawati.taddy@bankfab.com";
        Thread.sleep(2000);
        clickByXpath(driver,"//div//button[@ng-click=\"saveUserDetails();\"]");
        saveTextLog("Next button clicked");
    }

    @Step("Fill Pricing Form")
    public void fillPricingForm(String paymentModes[]) throws Exception {
        Thread.sleep(3000);
        if(paymentModes[0].equalsIgnoreCase("yes")||paymentModes[1].equalsIgnoreCase("yes")) {
            clickByXpath(driver, "//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[1]/div/div/div/label");
            saveTextLog("Card button clicked");
            Thread.sleep(2000);
            if(paymentModes[0].equalsIgnoreCase("yes"))
            {
                String keyCyberCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Payment_Modes\\CybersourcePG_Key.csv";
                ReadFromCSV csv = new ReadFromCSV(keyCyberCsvPath);  //Reading credentials file
                String[] key = csv.ReadLineNumber(1);
                clickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[1]/div/div/legend/a");  //Add cybersource pg
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[1]/div/div/legend/a");  //Add cybersource pg
                Thread.sleep(1000);
                sendKeysByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[2]/div[1]/input",key[0]);  //Add MID
                Thread.sleep(1000);
                sendKeysByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[1]/div/div[2]/div[2]/div[2]/input",key[1]);  //Add Encryption key
                Thread.sleep(1000);
                clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[1]/div/div/a");  //Select Currency
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");  //Select AED
                Thread.sleep(1000);
                clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[2]/div/div/a");  //Select Schema
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");  //Select Mastercard
                Thread.sleep(1000);
                clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[4]/div[3]/div/div/a");  //Select Operating mode
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");  //Select International
                Thread.sleep(1000);
                clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[1]/div/div/a");//Select Currency
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");//Select AED
                Thread.sleep(1000);
                clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[2]/div/div/a");//Select Schema
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]");//Select Visa
                Thread.sleep(1000);
                clickByXpath(driver,"/html/body/div[1]/div/div/div/div[4]/div/div[3]/form/div[1]/div[1]/div/div[2]/div[5]/div[3]/div/div/a");//Select Operating mode
                Thread.sleep(1000);
                clickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[1]");//Select International
                Thread.sleep(1000);
                saveTextLog("Cybersource PG Details added");
                dataCreateMerchant[7]="yes";
                Thread.sleep(1000);
            }
        }
        dataCreateMerchant[8]="no";
        dataCreateMerchant[9]="no";
        if(paymentModes[2].equalsIgnoreCase("yes"))
        {
            String keyMID="sa_store";
            clickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[1]/div[2]/div/div[1]/div/div/div/label");
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
        clickByXpath(driver,"//*[@id=\"Pricing\"]/form/div[2]/div/div/button[2]");
    }

    @Step("Fill Velocity Form")
    public void fillVelocityForm() throws Exception {
        String velocityDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Velocity_Details.csv";  //path to get details file
        ReadFromCSV csvVelocity = new ReadFromCSV(velocityDetailsCsvPath);  //Reading file
        String[] dataVelocity = csvVelocity.ReadLineNumber(1);
        Thread.sleep(3000);
        scrollToCenterXpath(driver,"//*[@id=\"Velocity\"]/form/div[10]/div/div/button[2]");
        Thread.sleep(2000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[8]/div[1]/div/input",dataVelocity[0]);
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[8]/div[2]/div/input",dataVelocity[1]);
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[9]/div[1]/div/input",dataVelocity[2]);
        Thread.sleep(1000);
        sendKeysByXpath(driver,"//*[@id=\"Velocity\"]/form/div[9]/div[2]/div/input",dataVelocity[3]);
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"Velocity\"]/form/div[10]/div/div/button[2]");
    }

    @Step("Fill Other Form")
    public void fillOtherForm() throws Exception
    {
        String ReferralDetailsCsvPath = System.getProperty("user.dir") + "\\Configuration_Files\\Create_Merchant_Data\\Referral_URLs.csv";  //path to get details file
        ReadFromCSV csvReferral = new ReadFromCSV(ReferralDetailsCsvPath);  //Reading file

        Thread.sleep(3000);
        String[] dataReferral=csvReferral.ReadLineNumber(1);
        int i=1;
        try {
            while (!dataReferral[0].isEmpty()) {
                clickByXpath(driver, "//*[@id=\"Others\"]/form/div[1]/div/div/legend/a");
                Thread.sleep(1000);
                sendKeysByXpath(driver, "//*[@id=\"Others\"]/form/div[1]/div/div/div[" + i + "]/div/div[1]/input", dataReferral[0]);
                i++;
                dataReferral = csvReferral.ReadLineNumber(i);
            }
        }catch (Exception e){
            saveTextLog("Referrals Added");
        }
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"Others\"]/form/div[2]/div/div/button[2]");
    }

    //-----------------User Creation Module------------------
    //@Test(priority=2, description = "User Creation Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User Creation Flow")
    public void createUser() throws InterruptedException, IOException {
        String random_string=getRandomString();
        merchant_userDetails(random_string);
        Thread.sleep(2000);
        aggregate_maker(random_string);
        Thread.sleep(2000);
        aggregate_checker(random_string);
    }
    //------------Entering User Details-----------------
    @Step("Enter Merchant User Details")
    public void merchant_userDetails(String random_strings) throws InterruptedException, IOException {
        String[] Merchant_Details_writer=new String[4];
        //---------------------Navigate to User Management-----------------------------
        waitAndClickByXpath(driver,"//*[@id=\"js-side-menu-0\"]");
        saveTextLog("Navigate to User Management");
        //---------------------Navigate to Create User---------------------------------
        clickByXpath(driver,"//*[@id=\"js-side-menu-0\"]/ul/li[1]/a");
        saveTextLog("Navigate to Create User");
        Thread.sleep(2000);
        //-------------------------------Merchant Option-------------------------------
        saveTextLog("Creating Merchant User");
        waitAndClickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[3]");
        Thread.sleep(2000);
        waitAndClickByXpath(driver,"/html/body/div[1]/div/form/div[4]/div[2]/div[1]/div[2]/div/div");
        Thread.sleep(2000);
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]/div");
        Thread.sleep(2000);
        //--------------------------------Selecting Merchant Name---------------------------
        waitAndClickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[3]/div/select/option[@label=\"merchantdemo\"]");
        Thread.sleep(2000);
        //--------------------------------Enter UserId--------------------------------------------------
        saveTextLog("Entering Merchant User Details");
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input","merch"+random_strings);
        Merchant_Details_writer[0]="merchant"+random_strings;
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[2]/div/div/label");
        //--------------------------------Enter First Name Last Name and Email--------------------------
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[1]/div/input","Fname_"+random_strings);
        Merchant_Details_writer[1]="Fname_"+random_strings;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[2]/div/input","Lname_"+random_strings);
        Merchant_Details_writer[2]="Lname_"+random_strings;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[1]/div/input","email_"+random_strings+"@gmail.com");
        Merchant_Details_writer[3]="email_"+random_strings;
        Thread.sleep(2000);
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv");
        WriteToCSV.writeNextLineCsv(Merchant_Details_writer);
        //--------------------------------Submitting User Merchant Details--------------------------------
        saveTextLog("Submitting Merchant User");
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");

    }
    @Step("Create Aggragator Maker")
    public void aggregate_maker(String random_string) throws InterruptedException, IOException {
        String[] Maker_Details_writer=new String[4];
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[2]");
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"s2id_autogen1\"]/a");
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]/div");
        Thread.sleep(2000);
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input","maker"+random_string);
        Maker_Details_writer[0]="maker"+random_string;
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[2]/div/div/label");
        //-----------------------Enter First Name Last Name and Email--------------------------
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[1]/div/input","Fmaker_"+random_string);
        Maker_Details_writer[1]="Fmaker_"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[2]/div/input","Lmaker_"+random_string);
        Maker_Details_writer[2]="Lmaker"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[1]/div/input","maker_"+random_string+"@gmail.com");
        Maker_Details_writer[3]="maker"+random_string;
        Thread.sleep(2000);
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv");
        WriteToCSV.writeNextLineCsv(Maker_Details_writer);
        //-------------------------------------Submitting User Maker Details-------------------------------------
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");

    }
    @Step("Create Aggragator Checker")
    public void aggregate_checker(String random_string) throws InterruptedException, IOException {
        String [] Checker_Detail_Writer=new String[4];
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[1]/div[1]/div/select/option[2]");
        Thread.sleep(2000);
        clickByXpath(driver,"//*[@id=\"s2id_autogen1\"]/a");
        waitAndClickByXpath(driver,"//*[@id=\"select2-drop\"]/ul/li[2]/div");
        Thread.sleep(2000);
        String userId="check"+random_string;
        Checker_Detail_Writer[0]=userId;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[1]/div/input",userId);
        clickByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[2]/div[2]/div/div/label");
        //-----------------------Enter First Name Last Name and Email--------------------------
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[1]/div/input","Fchecker_"+random_string);
        Checker_Detail_Writer[1]="Fchecker"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[3]/div[2]/div/input","Lchecker_"+random_string);
        Checker_Detail_Writer[2]="Lchecker"+random_string;
        sendKeysByXpath(driver,"//*[@id=\"avantgarde\"]/div[1]/div/form/div[4]/div[2]/div[4]/div[1]/div/input","check_"+random_string+"@gmail.com");
        Checker_Detail_Writer[3]="check_"+random_string;
        Thread.sleep(2000);
        initializeCsvWriter("Output_Files/Create_User_Detail_last_run.csv");
        WriteToCSV.writeNextLineCsv(Checker_Detail_Writer);
        //------------------------Submitting User Checker Details-------------------------------
        clickByXpath(driver,"//*[@id=\"heading-action-wrapper\"]/div/div/div[4]/button");

    }



}
