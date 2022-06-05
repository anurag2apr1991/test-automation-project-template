package ta.core.testng.listeners;

import ta.core.env.Environment;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

@SuppressWarnings("unused")
public class RetryAnalyzerListener implements IRetryAnalyzer {

    private int counter = 0;
    private static final int RETRY_LIMIT = Environment.getOrDefault("retryOnFail", 0);

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess()) {
            if (counter < RETRY_LIMIT) {
                counter++;
                return true;
            }
        }
        return false;
    }

}
