package benchmarks.dispatch.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({TestLogicalTopTier.class, TestEligibilityExclusions.class, TestDerivations.class, TestWorkerEligibility.class, TestScoring.class, TestScoreManagement.class})
public class DispatchTestSuite {

	public static Test suite() {
		return new JUnit4TestAdapter(AllTests.class);
		}
}
