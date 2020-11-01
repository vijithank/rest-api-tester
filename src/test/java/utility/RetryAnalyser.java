package utility;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyser implements IRetryAnalyzer {

    int count = 0;
    final int retryLimit = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (count < retryLimit && !iTestResult.isSuccess()) {
            count++;
            return true;
        }
        return false;
    }
}
