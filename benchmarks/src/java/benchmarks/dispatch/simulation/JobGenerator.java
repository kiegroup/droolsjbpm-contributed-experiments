package benchmarks.dispatch.simulation;

import java.util.Date;
import java.util.Random;

import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;

import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.VehicleSize;

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

public class JobGenerator extends AbstractGenerator {

	public JobGenerator(long seed, Date now) {
		super(seed, now);
	}

	public Job generateJob() {
		Job j = new Job();

		j.setStartTime(nextFutureDate());

		j.setJobId(rdi.nextHexString(10));

		// Seattle 47.609722, -122.333056
		// San Diego 32.78, -117.15
		// Portland, ME 43.665116, -70.269086
		// Talahassee, FL 29.07°, -81.14

		// 30-48
		j.setLatitude(rdi.nextUniform(30, 48));

		// -75 - 120
		j.setLongitude(rdi.nextUniform(-120, -75));
		j.setNumberOfRocksRequired(nextUnitRequired());

		j.setNumberOfSticksRequired(nextUnitRequired());

		// For now, all are pending
		j.setStatus(Job.Status.PENDING);
		j.setVehicleSizeRequired(nextVehicleSize());
		// 30% require wrenches
		j.setWrenchRequired(rdi.nextUniform(0, 1) > 0.7);
		return j;
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
		return new Date(now.getTime() + (120 + rdi.nextInt(0, 4 * 24 * 60))
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
