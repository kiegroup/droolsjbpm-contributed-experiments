package benchmarks.dispatch.simulation;

import java.util.Date;

import benchmarks.dispatch.fact.independent.VehicleSize;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;

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

public class WorkerGenerator extends AbstractGenerator {

	public WorkerGenerator(long seed, Date now) {
		super(seed, now);
	}

	public Worker generateWorker() {

		Worker w = new Worker();
		w.setWorkerId(rdi.nextHexString(10));
		w.setHasWrench(rdi.nextUniform(0, 1) > 0.5);
		w.setNumberOfRocks(nextUnitRequired());
		w.setNumberOfSticks(nextUnitRequired());
		w.setStatus(nextWorkerStatus());
		w.setVehicleSize(nextVehicleSize());

		if (w.getStatus() == Worker.Status.DISPATCHED_FOR_JOB) {;
			w.setCurrentJobLatitude(rdi.nextUniform(30, 48));
			w.setCurrentJobLongitude(rdi.nextUniform(-120, -75));
			w.setTimeAvailable(nextFutureDate());
		}

		return w;
	}

	public WorkerPosition generateWorkerPosition(Worker w) {
		WorkerPosition wp = new WorkerPosition();
		wp.setLatitude(rdi.nextUniform(30, 48));

		// -75 - 120
		wp.setLongitude(rdi.nextUniform(-120, -75));
		wp.setWorkerId(w.getWorkerId());
		return wp;
	}

	public Worker.Status nextWorkerStatus() {
		switch (rdi.nextInt(0, 2)) {
		case 0:
			return Worker.Status.DISPATCHED_FOR_JOB;
		default: // 2x as likely
			return Worker.Status.WAITING_FOR_JOB;
		}
	}

	public VehicleSize nextVehicleSize() {
		switch (rdi.nextInt(0, 4)) {
		case 0:
			return VehicleSize.SMALL;
		case 1:
			return VehicleSize.MEDIUM;
		case 2:
			return VehicleSize.LARGE;
		default: // 2x as likely
			return VehicleSize.EXTRA_LARGE;
		}
	}

	// two hours in the future + up to four days
	public Date nextFutureDate() {
		return new Date(now.getTime() + (120 + rdi.nextInt(0, 1 * 24 * 60))
				* 60 * 1000);
	}

	public int nextUnitRequired() {
		if (rdi.nextUniform(0, 1) < 0.9) {
			return 0;
		} else {
			return rdi.nextInt(1, 3);
		}
	}
}
