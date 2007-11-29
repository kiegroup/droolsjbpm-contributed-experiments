package benchmarks.dispatch.function;

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

import benchmarks.dispatch.fact.independent.Job;

public class Helper {

	public static Double distanceToPickupInMiles(Job job,
			Double workerLatitude, Double workerLongitude) {

		Double jobLatitude = job.getLatitude();
		Double jobLongitude = job.getLongitude();

		jobLatitude = Math.toRadians(jobLatitude);
		workerLatitude = Math.toRadians(workerLatitude);
		jobLongitude = Math.toRadians(jobLongitude);
		workerLongitude = Math.toRadians(workerLongitude);

		double angle = Math.acos(Math.sin(jobLatitude)
				* Math.sin(workerLatitude) + Math.cos(jobLatitude)
				* Math.cos(workerLatitude)
				* Math.cos(jobLongitude - workerLongitude));

		return Math.toDegrees(angle) * 69.1105; // miles per degree
	}

	public static Double minutesLateToJob(Date jobStartTime,
			Date workerAvailableTime, Double minutesToJob) {
		Date earliestArrival = new Date(workerAvailableTime.getTime() + Math.round(minutesToJob * 1000 * 60));
		if (earliestArrival.before(jobStartTime)){
			return 0D;
		} else {
			long diff = earliestArrival.getTime() - jobStartTime.getTime();
			return diff / 1000.0 / 60.0;
		}
	}

	public static Double minutesBetweenDates(Date start, Date end) {
		return 0D;
	}
}
