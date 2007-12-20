package benchmarks.dispatch.test;

import java.util.Date;

import org.drools.FactHandle;

import benchmarks.dispatch.fact.derived.ScoreComponent;

public class TestScoring extends AbstractDispatchTest {
	public void testDefaultScore() throws Exception {
		Couple c = createNeighboringCouple();
		
		c.job.setStartTime(new Date());
		
		c.worker.setTimeAvailable(new Date());
		
		wm.insert(c.job);
		wm.insert(c.worker);
		wm.insert(c.workerPosition);
		
		wm.fireAllRules();	
		
		ScoreComponent sc = getScoreComponent(c.job.getJobId(), c.worker.getWorkerId(),
				ScoreComponent.Type.DEFAULT);

		assertTrue("Default score component not present", sc != null);
	}
	
	public void testLateToJobScore() throws Exception {
		Couple c = createNeighboringCouple();
		
		c.job.setStartTime(new Date());
		
		c.worker.setTimeAvailable(new Date());
		
		wm.insert(c.job);
		wm.insert(c.worker);
		wm.insert(c.workerPosition);
		
		wm.fireAllRules();
		
		ScoreComponent sc = getScoreComponent(c.job.getJobId(), c.worker.getWorkerId(),
				ScoreComponent.Type.LATE_TO_JOB);

		assertTrue("Score component not present", sc != null);
	}
}
