package ta.core.driver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.events.EventFiringDecorator;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ta.core.testng.listeners.WebDriverEventListener;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@SuppressWarnings("unused")
public class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER_INSTANCE = new ThreadLocal<>();

    private static String getPlatformNameFromCapabilities(Map<String, Object> capabilities) {
        log.info("getting platform name capability");
        var platformName = capabilities.getOrDefault("platformName", "").toString();
        log.info("platform name is <{}>", platformName);
        return platformName;
    }

    @SneakyThrows
    private static URL getURLFromCapabilities(Map<String, Object> capabilities) {
        log.info("getting url from capabilities");
        var url = new URL(capabilities.getOrDefault("hub", "").toString());
        log.info("url is <{}>", url);
        return url;
    }

    private static WebDriver createDecoratedEventFiringDriver(WebDriver driver) {
        log.info("creating driver event listener");
        var decoratedWebDriver = new EventFiringDecorator(new WebDriverEventListener()).decorate(driver);
        log.info("driver event listener created");
        return decoratedWebDriver;
    }

    private static AppiumDriver initAppiumDriver(Map<String, Object> capabilities) {
        log.info("initializing appium driver");
        var url = getURLFromCapabilities(capabilities);
        var platformName = getPlatformNameFromCapabilities(capabilities);
        var desiredCapabilities = new DesiredCapabilities(capabilities);
        if (platformName.equalsIgnoreCase(Platform.ANDROID.name())) {
            var androidDriver = new AndroidDriver(url, desiredCapabilities);
            log.info("appium android driver initialized with capabilities: <{}> and <{}>", url, desiredCapabilities);
            return androidDriver;
        } else if (platformName.equalsIgnoreCase(Platform.IOS.name())) {
            var iosDriver = new IOSDriver(url, desiredCapabilities);
            log.info("appium ios driver initialized with capabilities: <{}> and <{}>", url, desiredCapabilities);
            return iosDriver;
        } else {
            throw new ExceptionInInitializerError("missing platformName capability");
        }
    }

    private static WebDriver initRemoteWebDriver(Map<String, Object> capabilities) {
        if (!getURLFromCapabilities(capabilities).toString().isEmpty()) {
            var url = getURLFromCapabilities(capabilities);
            var remoteWebDriver = new RemoteWebDriver(url, new DesiredCapabilities(capabilities));
            log.info("remote web driver initialized with capabilities: <{}> and <{}>", url, capabilities);
            return createDecoratedEventFiringDriver(remoteWebDriver);
        } else {
            throw new ExceptionInInitializerError("missing hub capability");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static WebDriver initWebDriver(String driverName, Map<String, Object> capabilities) {
        WebDriver driver;
        if (driverName.equalsIgnoreCase("chrome")) {
            WebDriverManager.chromedriver().setup();
            var chromeOptions = new ChromeOptions();
            capabilities.forEach(chromeOptions::setCapability);
            chromeOptions.addArguments((ArrayList) capabilities.getOrDefault("arguments", new ArrayList<>()));
            driver = new ChromeDriver(chromeOptions);
            log.info("chrome driver initialized with options: <{}>", chromeOptions);
            return createDecoratedEventFiringDriver(driver);
        } else if (driverName.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            var firefoxOptions = new FirefoxOptions();
            capabilities.forEach(firefoxOptions::setCapability);
            firefoxOptions.addArguments((ArrayList) capabilities.getOrDefault("arguments", new ArrayList<>()));
            driver = new FirefoxDriver(firefoxOptions);
            log.info("firefox driver initialized with options: <{}>", firefoxOptions);
            return createDecoratedEventFiringDriver(driver);
        } else if (driverName.equalsIgnoreCase("safari")) {
            var safariOptions = new SafariOptions();
            capabilities.forEach(safariOptions::setCapability);
            driver = new SafariDriver(safariOptions);
            log.info("safari driver initialized with options: <{}>", safariOptions);
            return createDecoratedEventFiringDriver(driver);
        } else if (driverName.equalsIgnoreCase("edge")) {
            WebDriverManager.edgedriver().setup();
            var edgeOptions = new EdgeOptions();
            capabilities.forEach(edgeOptions::setCapability);
            driver = new EdgeDriver(edgeOptions);
            log.info("edge driver initialized with options: <{}>", edgeOptions);
            return createDecoratedEventFiringDriver(driver);
        } else {
            throw new ExceptionInInitializerError("missing driver capability");
        }
    }

    public static void setDriver(String driverName, Map<String, Object> capabilities) {
        WebDriver driver = null;
        switch (driverName.toLowerCase()) {
            case "appium": {
                driver = initAppiumDriver(capabilities);
                break;
            }
            case "remote": {
                driver = initRemoteWebDriver(capabilities);
                break;
            }
            case "chrome":
            case "firefox":
            case "safari":
            case "edge": {
                driver = initWebDriver(driverName, capabilities);
                break;
            }
        }
        DRIVER_INSTANCE.set(driver);
    }

    public static WebDriver getDriver() {
        return DRIVER_INSTANCE.get();
    }

    public static WebDriverWait getDriverWait(int timeOutInSeconds) {
        return new WebDriverWait(DRIVER_INSTANCE.get(), Duration.ofSeconds(timeOutInSeconds));
    }

    public static void quitDriver() {
        if (DRIVER_INSTANCE.get() != null) {
            DRIVER_INSTANCE.get().quit();
            DRIVER_INSTANCE.remove();
        }
    }

}
