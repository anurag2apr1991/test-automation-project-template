package com.ta.core.reporting;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Allure;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.util.Map;
import java.util.Properties;

@Log4j2
public class ReportingHelper {

    @SuppressWarnings("rawtypes")
    private static String stopRecordingScreen(RemoteWebDriver driver) {
        String output = "";
        if (driver instanceof AndroidDriver) {
            log.debug("stop recording android screen");
            output = ((AndroidDriver) driver).stopRecordingScreen();
        } else if (driver instanceof IOSDriver) {
            log.debug("stop recording ios screen");
            output = ((IOSDriver) driver).stopRecordingScreen();
        }
        log.debug("screen recording stopped");
        return output;
    }

    @SuppressWarnings("rawtypes")
    public static void startRecordingScreen(RemoteWebDriver driver, boolean shouldRecord) {
        if (driver instanceof AndroidDriver && shouldRecord) {
            log.debug("start recording android screen");
            ((AndroidDriver) driver).startRecordingScreen();
        } else if (driver instanceof IOSDriver && shouldRecord) {
            log.debug("start recording ios screen");
            ((IOSDriver) driver).startRecordingScreen();
        }
        log.debug("screen recording started");
    }

    public static void attachVideo(RemoteWebDriver driver, String name, boolean shouldAttach) {
        if (shouldAttach) {
            String output = stopRecordingScreen(driver);
            log.debug("attaching video");
            Allure.addAttachment(name, new ByteArrayInputStream(Base64.decodeBase64(output)));
            log.debug("video attached");
        }
    }

    public static void attachScreenshot(RemoteWebDriver driver, String name, boolean shouldAttach) {
        if (shouldAttach) {
            byte[] screenshotByteArray = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            log.debug("attaching screenshot");
            Allure.addAttachment(name, new ByteArrayInputStream(screenshotByteArray));
            log.debug("screenshot attached");
        }
    }

    @SneakyThrows
    public static void attachEnvironmentInfo(Map<String, Object> capabilities) {
        if (capabilities != null) {
            log.debug("attaching environment info from capabilities \n" + capabilities);
            OutputStream output = new FileOutputStream("build/allure-results/environment.properties");
            Properties properties = new Properties();
            capabilities.forEach((key, value) -> properties.setProperty(key, value.toString()));
            properties.store(output, "Environment info");
            log.debug("environment info attached");
        }
    }

}
