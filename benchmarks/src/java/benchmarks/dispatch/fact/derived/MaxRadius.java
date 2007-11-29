package benchmarks.dispatch.fact.derived;

import java.io.Serializable;

import benchmarks.dispatch.fact.independent.Job;

public class MaxRadius implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String jobId;
	private Double maxRadius;

	public MaxRadius(Job job) {
		this.jobId = job.getJobId();
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Double getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(Double maxRadius) {
		this.maxRadius = maxRadius;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MaxRadius other = (MaxRadius) obj;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		return true;
	}

}
