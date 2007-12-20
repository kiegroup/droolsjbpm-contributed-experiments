package benchmarks.dispatch.test;

import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.drools.FactHandle;
import org.drools.ObjectFilter;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.WorkingMemory;

import benchmarks.dispatch.DispatchWrapper;
import benchmarks.dispatch.fact.derived.Exclusion;
import benchmarks.dispatch.fact.derived.Info;
import benchmarks.dispatch.fact.derived.Score;
import benchmarks.dispatch.fact.derived.ScoreComponent;
import benchmarks.dispatch.fact.derived.WorkerInfo;
import benchmarks.dispatch.fact.independent.DispatchState;
import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.VehicleSize;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;

public class AbstractDispatchTest extends TestCase {

	protected WorkingMemory wm;
	protected FactHandle dispatchStateFactHandle;
	protected DispatchState ds;

	public void setUp() throws Exception {
		DispatchWrapper db = new DispatchWrapper();
		wm = db.getWM();

		ds = new DispatchState();
		ds.setCurrentTime(new Date());
		dispatchStateFactHandle = wm.insert(ds);
		
		wm.fireAllRules();
	}

	protected void incrementDSTime(int minutes) {
		DispatchState ds = (DispatchState) wm
				.getObject(dispatchStateFactHandle);
		Date newTime = new Date(ds.getCurrentTime().getTime() + 1000 * 60
				* minutes);
		ds.setCurrentTime(newTime);
		wm.update(dispatchStateFactHandle, ds);
	}
	
	protected static class Couple {
		public Worker worker;
		public WorkerPosition workerPosition;
		public Job job;
		
	}
	
	protected Couple createNeighboringCouple(){
		Couple c = new Couple();
		
		Job job = new Job();
		job.setJobId("X");
		job.setStatus(Job.Status.PENDING);
		job.setVehicleSizeRequired(VehicleSize.SMALL);
		job.setStartTime(ds.getCurrentTime());
		job.setLatitude(-99D);
		job.setLongitude(-99D);
		c.job = job;

		Worker w = new Worker();
		w.setWorkerId("X");
		w.setStatus(Worker.Status.WAITING_FOR_JOB);
		w.setVehicleSize(VehicleSize.SMALL);
		c.worker = w;

		WorkerPosition wp = new WorkerPosition();
		wp.setLatitude(-100D);
		wp.setLongitude(-100D);
		wp.setWorkerId("X");
		
		c.workerPosition = wp;
		
		return c;

	}

	protected void assertFactExists(Class factClass, String message) {
		assertTrue(message, hasFact(factClass));
	}

	protected void assertFactAbsent(Class factClass, String message) {
		assertFalse(message, hasFact(factClass));

	}
	
	private boolean hasFact(Class factClass){
		Iterator it = wm.iterateObjects(new TypeFilter(factClass));
		return it.hasNext();
	}
	
	protected WorkerInfo getWorkerInfo(String workerId, WorkerInfo.Type type) {
		QueryResults qrs = wm.getQueryResults("getWorkerInfo", new Object[] {
				workerId, type });
		if (qrs.size() == 0)
			return null;
		QueryResult qr = qrs.get(0);
		if (qr.size() == 0)
			return null;
		return (WorkerInfo) qr.get(0);

	}

	protected Info getInfo(String workerId, String jobId, Info.Type type) {
		QueryResults qrs = wm.getQueryResults("getInfo", new Object[] {
				workerId, jobId, type });
		if (qrs.size() == 0)
			return null;
		QueryResult qr = qrs.get(0);
		if (qr.size() == 0)
			return null;
		return (Info) qr.get(0);

	}
	
	protected Exclusion getExclusion(String workerId, String jobId, Exclusion.Type type) {
		QueryResults qrs = wm.getQueryResults("getExclusion", new Object[] {
				workerId, jobId, type });
		if (qrs.size() == 0)
			return null;
		QueryResult qr = qrs.get(0);
		if (qr.size() == 0)
			return null;
		return (Exclusion) qr.get(0);

	}
	
	protected Score getScore(String workerId, String jobId) {
		QueryResults qrs = wm.getQueryResults("getScore", new Object[] {
				workerId, jobId });
		if (qrs.size() == 0)
			return null;
		QueryResult qr = qrs.get(0);
		if (qr.size() == 0)
			return null;
		return (Score) qr.get(0);

	}
	
	protected ScoreComponent getScoreComponent(String workerId, String jobId, ScoreComponent.Type type) {
		QueryResults qrs = wm.getQueryResults("getScoreComponent", new Object[] {
				workerId, jobId, type });
		if (qrs.size() == 0)
			return null;
		QueryResult qr = qrs.get(0);
		if (qr.size() == 0)
			return null;
		return (ScoreComponent) qr.get(0);

	}

	private static class TypeFilter implements ObjectFilter {

		Class c;

		public TypeFilter(Class c) {
			this.c = c;
		}

		public boolean accept(Object object) {
			return object.getClass().equals(c);
		}

	}
}
