package com.ta.core.testng.listeners;

import com.ta.core.driver.DriverFactory;
import com.ta.core.reporting.allure.ReportingHelper;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import lombok.extern.log4j.Log4j2;
import org.testng.*;
import org.testng.annotations.ITestAnnotation;
import org.testng.reporters.XMLReporter;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.mapper.ObjectMapperType.GSON;


@Log4j2
public class TestListener extends XMLReporter implements IReporter, ITestListener, ISuiteListener, IAnnotationTransformer {

    private Map<String, Object> capabilities;

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzerListener.class);
    }

    @Override
    public void onStart(ISuite suite) {
        log.debug(suite.getName() + " execution started");
        super.getConfig().setGenerateTestResultAttributes(true);
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig(GSON));
        RestAssured.filters(new AllureRestAssured());
        if (log.isDebugEnabled()) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info(result.getMethod().getMethodName() + " STARTED");
        ReportingHelper.startRecordingScreen(DriverFactory.getDriver(), false);
        if (DriverFactory.getDriver() != null) {
            capabilities = DriverFactory.getDriver().getCapabilities().asMap();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ReportingHelper.attachScreenshot(DriverFactory.getDriver(), "screenshot-" + UUID.randomUUID(), false);
        ReportingHelper.attachVideo(DriverFactory.getDriver(), "video-" + UUID.randomUUID(), false);
        log.info(result.getMethod().getMethodName() + " PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ReportingHelper.attachScreenshot(DriverFactory.getDriver(), "screenshot-" + UUID.randomUUID(), true);
        ReportingHelper.attachVideo(DriverFactory.getDriver(), "video-" + UUID.randomUUID(), false);
        log.info(result.getMethod().getMethodName() + " FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.info(result.getMethod().getMethodName() + " SKIPPED");
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        super.generateReport(xmlSuites, suites, outputDirectory);
    }

    @Override
    public void onFinish(ISuite suite) {
        ReportingHelper.attachEnvironmentInfo(capabilities);
        log.debug(suite.getName() + " execution finished");
    }

}
