package benchmarks.dispatch.test;

import java.util.Date;

import org.drools.FactHandle;

import benchmarks.dispatch.fact.derived.JobExclusion;
import benchmarks.dispatch.fact.independent.Job;

public class TestEligibilityExclusions extends AbstractDispatchTest {
	public void testJobExclusion() throws Exception {
		Job j = new Job();
		j.setJobId("X");
		j.setStatus(Job.Status.PENDING);
		// Five days ahead
		j
				.setStartTime(new Date(new Date().getTime() + 1000 * 60 * 60
						* 24 * 5));
		FactHandle jobHandle = wm.insert(j);

		wm.fireAllRules();

		// Should have a JobExclusion
		assertFactExists(JobExclusion.class, "Job Exclusion should exist");

		j.setStatus(Job.Status.DISPATCHED);
		// Should override exiting job since equals/hashtable based on jobId
		wm.update(jobHandle, j);

		wm.fireAllRules();

		// Should not have a job exclusion
		assertFactAbsent(JobExclusion.class,
				"Job Exclusion should have been retracted");
	}
}
