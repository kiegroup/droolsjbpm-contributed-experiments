package benchmarks.dispatch.test;

import benchmarks.dispatch.fact.derived.Exclusion;
import benchmarks.dispatch.fact.independent.VehicleSize;

public class TestWorkerEligibility extends AbstractDispatchTest {
	public void testWrenchRule() throws Exception {
		
		Couple c = createNeighboringCouple();
		
		c.job.setVehicleSizeRequired(VehicleSize.EXTRA_LARGE);
		c.worker.setVehicleSize(VehicleSize.LARGE);

		wm.insert(c.job);
		wm.insert(c.worker);
		wm.insert(c.workerPosition);
		
	
		wm.fireAllRules();

		assertTrue("Exclusion for Vehicle Size Not Present", getExclusion(c.job.getJobId(), c.worker.getWorkerId(),
				Exclusion.Type.VEHICLE_SIZE) != null);

	}
	
	public void testRocksRule() throws Exception {
		
		Couple c = createNeighboringCouple();
		
		c.job.setNumberOfRocksRequired(2);
		c.worker.setNumberOfRocks(1);

		wm.insert(c.job);
		wm.insert(c.worker);
		wm.insert(c.workerPosition);
		
	
		wm.fireAllRules();

		assertTrue("Exclusion for Number of Rocks Not Present", getExclusion(c.job.getJobId(), c.worker.getWorkerId(),
				Exclusion.Type.NUM_ROCKS) != null);

	}
	
	public void testSticksRule() throws Exception {
		
		Couple c = createNeighboringCouple();
		
		c.job.setNumberOfSticksRequired(1);
		c.worker.setNumberOfSticks(0);

		wm.insert(c.job);
		wm.insert(c.worker);
		wm.insert(c.workerPosition);
		
	
		wm.fireAllRules();

		assertTrue("Exclusion for Number of Sticks Not Present", getExclusion(c.job.getJobId(), c.worker.getWorkerId(),
				Exclusion.Type.NUM_STICKS) != null);

	}
	
	
}
