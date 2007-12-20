package benchmarks.dispatch.test;

import java.util.Date;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;

import benchmarks.dispatch.fact.derived.DateWorkerInfo;
import benchmarks.dispatch.fact.derived.DecimalInfo;
import benchmarks.dispatch.fact.derived.Info;
import benchmarks.dispatch.fact.derived.PositionWorkerInfo;
import benchmarks.dispatch.fact.derived.WorkerInfo;
import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.VehicleSize;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;
import benchmarks.dispatch.function.Helper;

public class TestDerivations extends AbstractDispatchTest {
	public void testTimeAvailableWhenNotOnJob() throws Exception {
		Worker w = new Worker();
		w.setWorkerId("X");
		w.setStatus(Worker.Status.WAITING_FOR_JOB);

		FactHandle workerFactHandle = wm.insert(w);

		wm.fireAllRules();

		WorkerInfo wi = getWorkerInfo("X",
				WorkerInfo.Type.TIME_AVAILABLE_FOR_DISPATCH);

		assertNotNull(wi);

		DateWorkerInfo dwi = (DateWorkerInfo) wi;

		assertTrue(dwi.getValue().equals(ds.getCurrentTime()));

		// Position
		WorkerPosition wp = new WorkerPosition();
		wp.setLatitude(-100D);
		wp.setLongitude(-100D);
		wp.setWorkerId("X");
		wm.insert(wp);

		wm.fireAllRules();

		PositionWorkerInfo pwi = (PositionWorkerInfo) getWorkerInfo("X",
				WorkerInfo.Type.POSITION_AVAILABLE_FOR_DISPATCH);

		assertNotNull(pwi);
		assertTrue(pwi.getLatitude().equals(-100D));
		assertTrue(pwi.getLongitude().equals(-100D));

		// Clean up
		wm.retract(workerFactHandle);
		// Position removed automatically
		wm.fireAllRules();
	}

	public void testTimeAvailableWhenOnJob() throws Exception {
		Worker w = new Worker();
		w.setWorkerId("X");
		w.setCurrentJobLatitude(-50D);
		w.setCurrentJobLongitude(-50D);
		Date avail = new Date(ds.getCurrentTime().getTime() + 1000 * 60 * 60
				* 3);
		w.setTimeAvailable(avail);
		w.setStatus(Worker.Status.DISPATCHED_FOR_JOB);

		FactHandle workerFactHandle = wm.insert(w);

		wm.fireAllRules();

		WorkerInfo wi = getWorkerInfo("X",
				WorkerInfo.Type.TIME_AVAILABLE_FOR_DISPATCH);

		assertNotNull(wi);

		DateWorkerInfo dwi = (DateWorkerInfo) wi;

		assertTrue(dwi.getValue().equals(avail));

		// Position
		WorkerPosition wp = new WorkerPosition();
		wp.setLatitude(-100D);
		wp.setLongitude(-100D);
		wp.setWorkerId("X");
		wm.insert(wp);

		wm.fireAllRules();

		PositionWorkerInfo pwi = (PositionWorkerInfo) getWorkerInfo("X",
				WorkerInfo.Type.POSITION_AVAILABLE_FOR_DISPATCH);

		assertNotNull(pwi);
		assertTrue(pwi.getLatitude().equals(-50D));
		assertTrue(pwi.getLongitude().equals(-50D));

		// Clean up
		wm.retract(workerFactHandle);
		wm.fireAllRules();
	}

	public void testDistanceTimeMinutesLate() throws Exception {
		Job job = new Job();
		job.setJobId("X");
		job.setStatus(Job.Status.PENDING);
		job.setVehicleSizeRequired(VehicleSize.LARGE);
		job.setStartTime(ds.getCurrentTime());
		job.setLatitude(-99D);
		job.setLongitude(-99D);

		FactHandle jobHandle = wm.insert(job);

		Worker w = new Worker();
		w.setWorkerId("X");
		w.setStatus(Worker.Status.WAITING_FOR_JOB);
		w.setVehicleSize(VehicleSize.EXTRA_LARGE);

		FactHandle workerFactHandle = wm.insert(w);

		// Position
		WorkerPosition wp = new WorkerPosition();
		wp.setLatitude(-100D);
		wp.setLongitude(-100D);
		wp.setWorkerId("X");
		wm.insert(wp);

		wm.fireAllRules();

		DecimalInfo distToJob = (DecimalInfo) getInfo("X", "X",
				Info.Type.DISTANCE_TO_JOB_MILES);

		// Should be 91.05578657535243
		assertTrue(Math.abs(91.1 - distToJob.getValue()) < 0.1 );

		DecimalInfo timeToJob = (DecimalInfo) getInfo("X", "X",
				Info.Type.TIME_TO_JOB_MINUTES);

		// TODO: What should this be?
		assertTrue(Math.abs(121.4 - timeToJob.getValue()) < 0.1 );

		DecimalInfo minLate = (DecimalInfo) getInfo("X", "X",
				Info.Type.MINUTES_LATE_TO_JOB);
	
		// TODO: Should be same as minutesToJob
		assertTrue(Math.abs(121.4 - minLate.getValue()) < 0.1 );

		//TODO: Test the "already on job" case
	}
	
	public void testOldPositions() throws Exception {
		//TODO
	}

}
