package Functions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static Reports.AllureReport.Screenshot;
import static Reports.AllureReport.saveTextLog;

public class ErrorSuccessMessage {
    public static boolean checkMessage(WebDriver driver, String messageXpath, String cancelXpath){
        try {
            if (driver.findElement(By.xpath(messageXpath)).isDisplayed()) {
                String message=driver.findElement(By.xpath(messageXpath)).getText();
                if(message.toLowerCase().contains("success"))
                    Screenshot(driver, "Success message:   " + message);
                else{
                    Screenshot(driver, "Error message:   " + message);
                }
                Thread.sleep(1000);
                driver.findElement(By.xpath(cancelXpath)).click();
                return true;
            }
        } catch (Exception e3) {
                return false;
        }
        return false;
    }
}
