package Scripts;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;

public class Login {
    static WebDriver driver;
    static WebDriverWait wait;
    @BeforeTest
    public void DriverAllocation(){
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\Drivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait=new WebDriverWait(driver, 100);
    }
    @Test(priority = 0)
    public void Url(){
        
        driver.get("https://safexpayuat.bankfab.com/agadmin/fabIndex.jsp");
        wait=new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName")));
        System.out.println("Url opened");
    }

    @Test(priority = 1)
    public void login(){
        String username = "fab_makr";
        String password = "Test@123";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName")));
        WebElement userName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName")));
        userName.click();
        userName.clear();
        userName.sendKeys(username);
        WebElement passWord = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("passWord")));
        passWord.click();
        passWord.clear();
        passWord.sendKeys(password);
        passWord.sendKeys(Keys.RETURN);
    }
}
