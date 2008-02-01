package benchmarks.dispatch;

import java.util.List;

import org.drools.FactHandle;

import benchmarks.dispatch.api.JobEligibility;
import benchmarks.dispatch.api.JobExcludedException;

public interface IDispatchWrapper {

	public List<JobEligibility> getEligibileWorkersForJob(String jobId)
			throws Exception, JobExcludedException ;

	public Object insert(Object o) throws Exception;

	public void fireAllRules() throws Exception;

}
