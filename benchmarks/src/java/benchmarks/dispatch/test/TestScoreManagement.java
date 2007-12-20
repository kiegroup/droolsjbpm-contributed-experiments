package benchmarks.dispatch.test;

import java.util.Date;

import benchmarks.dispatch.fact.derived.Score;

public class TestScoreManagement extends AbstractDispatchTest {
	public void testScoreExists() throws Exception {
		Couple c = createNeighboringCouple();
		
		c.job.setStartTime(new Date());
		
		c.worker.setTimeAvailable(new Date());
		
		wm.insert(c.job);
		wm.insert(c.worker);
		wm.insert(c.workerPosition);
		
		wm.fireAllRules();	
		
		Score score = getScore(c.job.getJobId(), c.worker.getWorkerId());
		
		assertNotNull("No score computed", score);
	}
}
