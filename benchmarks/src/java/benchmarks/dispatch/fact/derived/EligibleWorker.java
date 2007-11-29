package benchmarks.dispatch.fact.derived;

import benchmarks.dispatch.fact.independent.Worker;

public class EligibleWorker {
	
	private String            workerId;
	
	public EligibleWorker(Worker w){
		this.workerId = w.getWorkerId();
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((workerId == null) ? 0 : workerId.hashCode());
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
		final EligibleWorker other = (EligibleWorker) obj;
		if (workerId == null) {
			if (other.workerId != null)
				return false;
		} else if (!workerId.equals(other.workerId))
			return false;
		return true;
	}
	
	
	
	
	
	
}
