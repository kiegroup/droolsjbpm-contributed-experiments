package benchmarks.dispatch;

/*
 * Copyright 2007 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.FactHandle;

import benchmarks.dispatch.api.JobEligibility;
import benchmarks.dispatch.api.JobExcludedException;
import benchmarks.dispatch.fact.independent.DispatchState;
import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;
import benchmarks.dispatch.simulation.JobGenerator;
import benchmarks.dispatch.simulation.WorkerGenerator;

public class DispatchBenchmark {

	private DispatchWrapper dw;

	public DispatchBenchmark() throws Exception {
		dw = new DispatchWrapper();
	}

	public DispatchBenchmark(DispatchWrapper dw) throws Exception {
		this.dw = dw;
	}

	public void go(int numWorkers, int numJobs) throws Exception {

		DispatchState ds = new DispatchState();
		ds.setCurrentTime(new Date());
		FactHandle dsfh = dw.insert(ds);

		WorkerGenerator wg = new WorkerGenerator(100, ds.getCurrentTime());

		Map<FactHandle, WorkerPosition> positions = new HashMap<FactHandle, WorkerPosition>();

		for (int i = 0; i < numWorkers; i++) {
			Worker w = wg.generateWorker();
			dw.insert(w);
			WorkerPosition wp = wg.generateWorkerPosition(w);
			positions.put(dw.insert(wp), wp);
		}

		Set<Job> jobs = new HashSet<Job>();

		JobGenerator jg = new JobGenerator(100, ds.getCurrentTime());

		for (int i = 0; i < numJobs; i++) {
			Job j = jg.generateJob();
			jobs.add(j);
			dw.insert(j);
		}

		dw.fireAllRules();

		// test clock updates
/*
		long startClock = System.currentTimeMillis();
		// 50 clock cycles, 20 min each
		for (int i = 0; i < 50; i++) {
			ds.setCurrentTime(new Date(ds.getCurrentTime().getTime() + 1000 * 20));
			dw.getWM().update(dsfh, ds);
			dw.getWM().fireAllRules();
		}

		System.out.println("ClockUpdates:" + (System.currentTimeMillis() - startClock));

		startClock = System.currentTimeMillis();
		// 50 clock cycles, 20 min each
		for (int i = 0; i < 10; i++) {
			for (Map.Entry<FactHandle, WorkerPosition> e : positions.entrySet()) {
				e.getValue().setLongitude(e.getValue().getLongitude() + 1);
				e.getValue().setLatitude(e.getValue().getLatitude() + 1);
				dw.getWM().insert(e.getValue());
				//dw.getWM().update(e.getKey(), e.getValue());
			}
			System.out.println("Now firing rules");
			dw.getWM().fireAllRules();
			System.out.println("Just fired rules");
		}

		System.out.println("PositionUpdates:" + (System.currentTimeMillis() - startClock));

*/

		System.out.println("Inserts:" + dw.wmel.inserts);
		System.out.println("Retracts:" + dw.wmel.retracts);
		System.out.println("Updates:" + dw.wmel.updates);
		System.out.println("-----");
		System.out.println("Cancels:" + dw.ael.cancels);
		System.out.println("Creates:" + dw.ael.creates);
		System.out.println("FireAfter:" + dw.ael.fireAfter);
		System.out.println("FireBefore:" + dw.ael.fireBefore);
		System.out.println("Pops:" + dw.ael.pops);
		System.out.println("Pushes:" + dw.ael.pushes);
		System.out.println("--");
		dw.ael.dump();

		System.out.println("--");

		for (Job j : jobs) {

			List<JobEligibility> eligibilities = null;

			try {
				eligibilities = dw.getEligibileWorkersForJob(j.getJobId());
			} catch (JobExcludedException jee) {
				System.out.println("Job:" + j.getJobId() + ":" + "EXCLUDED");
				continue;
			}

			// TEMP for ILOG
			if (eligibilities.size() == 0)
				continue;

			System.out.println("Job:" + j.getJobId() + ":" + eligibilities.size() + ":"
					+ eligibilities.get(0).getDistanceToJobInMiles());

		}

	}

	public static void main(String args[]) throws Exception {
		int numWorkers = 1;
		int numJobs = 1;

		if (args.length == 2) {
			numWorkers = new Integer(args[0]);
			numJobs = new Integer(args[1]);
		}

		System.out.println("Running benchmark with " + numWorkers + " workers and " + numJobs + " jobs.");

		long startTime = System.currentTimeMillis();

		DispatchBenchmark db = new DispatchBenchmark();

		db.go(numWorkers, numJobs);

		System.out.println("Completed in " + (System.currentTimeMillis() - startTime) + "ms");

		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();

		long endMem = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();

		System.out.println("PostGC WM Size:" + (endMem));

	}

}
