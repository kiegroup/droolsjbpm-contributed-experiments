package benchmarks.dispatch.api;

import java.util.HashSet;
import java.util.Set;

import benchmarks.dispatch.fact.derived.Score;
import benchmarks.dispatch.fact.derived.ScoreComponent;

public class JobEligibility {
	private String workerId;
	private Set<ScoreComponent> scores = new HashSet<ScoreComponent>();

	private Double distanceToJobInMiles, timeToJobMinutes, minutesLateToJob;

	public JobEligibility() {

	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public Set<ScoreComponent> getScoreComponents() {
		return scores;
	}

	public void setScoreComponents(Set<ScoreComponent> scores) {
		this.scores = scores;
	}
	
	public void addScoreComponent(ScoreComponent s){
		scores.add(s);
	}

	public Double getTotalScore() {
		double score = 0;

		for (ScoreComponent s : scores) {
			score += s.getContribution();
		}

		return score;
	}

	public Double getDistanceToJobInMiles() {
		return distanceToJobInMiles;
	}

	public void setDistanceToJobInMiles(Double distanceToJobInMiles) {
		this.distanceToJobInMiles = distanceToJobInMiles;
	}

	public Double getTimeToJobMinutes() {
		return timeToJobMinutes;
	}

	public void setTimeToJobMinutes(Double timeToJobMinutes) {
		this.timeToJobMinutes = timeToJobMinutes;
	}

	public Double getMinutesLateToJob() {
		return minutesLateToJob;
	}

	public void setMinutesLateToJob(Double minutesLateToJob) {
		this.minutesLateToJob = minutesLateToJob;
	}

}
