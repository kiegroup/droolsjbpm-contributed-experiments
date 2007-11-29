package benchmarks.dispatch.test;

import java.util.Date;
import java.util.Iterator;

import org.drools.FactHandle;
import org.drools.ObjectFilter;

import benchmarks.dispatch.fact.derived.JobExclusion;
import benchmarks.dispatch.fact.derived.MaxRadius;
import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;

public class TestLogicalTopTier extends AbstractDispatchTest {
	

	public void testJobLifecycle() throws Exception {
		Job j = new Job();
		j.setJobId("X");
		j.setStatus(Job.Status.PENDING);
		//Five days ahead
		j.setStartTime(new Date());
		FactHandle jobHandle = wm.insert(j);
		
		wm.fireAllRules();
		
		assertFactExists(Job.class, "Job should not have been retracted");
		
		//Should have a MaxRadius
		assertFactExists(MaxRadius.class, "Should have a MaxRadius");
	
		j.setStatus(Job.Status.DISPATCHED);
		wm.update(jobHandle, j);
		wm.fireAllRules();
		
		
		assertFactAbsent(Job.class, "Job should have been retracted");
		assertFactAbsent(MaxRadius.class, "Max Radius should have been retracted");
		
		//This test should have cleaned up itself
	}
	
	public void testWorkerLifecycle() throws Exception {
		Worker w = new Worker();
		w.setWorkerId("X");
		w.setStatus(Worker.Status.WAITING_FOR_JOB);
		
		WorkerPosition wp = new WorkerPosition();
		wp.setLatitude(-100D);
		wp.setLongitude(-100D);
		wp.setWorkerId("X");
		
		FactHandle workerFactHandle = wm.insert(w);
		
		wm.fireAllRules();
		
		assertFactExists(Worker.class, "Worker should not have been retracted");
		
		FactHandle positionFactHandle = wm.insert(wp);
		
		wm.fireAllRules();
		
		assertFactExists(WorkerPosition.class, "Worker position should not have been retracted");
		
		w.setStatus(Worker.Status.INACTIVE);
		
		wm.update(workerFactHandle, w);
		
		wm.fireAllRules();
		
		assertFactAbsent(Worker.class, "Worker should have been retracted");
		
		assertFactAbsent(WorkerPosition.class, "Worker position should have been retracted");
		
	}
	
	
}
