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

import org.drools.FactHandle;

import benchmarks.dispatch.fact.independent.DispatchState;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;
import benchmarks.dispatch.simulation.JobGenerator;
import benchmarks.dispatch.simulation.WorkerGenerator;

public class DispatchBenchmark {
	
	private DispatchWrapper dw;
	
	public DispatchBenchmark() throws Exception {
		dw = new DispatchWrapper();
	}
	
	public void go(int numWorkers, int numJobs) throws Exception {

		DispatchState ds = new DispatchState();
		ds.setCurrentTime(new Date());
		FactHandle dsfh = dw.insert(ds);

		WorkerGenerator wg = new WorkerGenerator(100, ds.getCurrentTime());

		for (int i = 0; i < numWorkers; i++) {
			Worker w = wg.generateWorker();
			dw.insert(w);
			WorkerPosition wp = wg.generateWorkerPosition(w);
			dw.insert(wp);
		}

		JobGenerator jg = new JobGenerator(100, ds.getCurrentTime());

		for (int i = 0; i < numJobs; i++) {
			dw.insert(jg.generateJob());
		}

		dw.fireAllRules();
	}

	public static void main(String args[]) throws Exception {
		int numWorkers = 1;
		int numJobs = 1;

		if (args.length == 2){
			numWorkers = new Integer(args[0]);
			numJobs = new Integer(args[1]);
		}
		
		System.out.println("Running benchmark with " + numWorkers + " workers and " + numJobs + " jobs.");
		
		DispatchBenchmark db = new DispatchBenchmark();
		
		long startTime = System.currentTimeMillis();
		
		db.go(numWorkers, numJobs);
		
		System.out.println("Completed in " + (System.currentTimeMillis() - startTime)+ "ms");
	}

}
