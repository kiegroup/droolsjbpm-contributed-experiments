package benchmarks.dispatch;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DrlParser;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.event.ActivationCancelledEvent;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.AgendaEventListener;
import org.drools.event.AgendaGroupPoppedEvent;
import org.drools.event.AgendaGroupPushedEvent;
import org.drools.event.BeforeActivationFiredEvent;
import org.drools.event.ObjectInsertedEvent;
import org.drools.event.ObjectRetractedEvent;
import org.drools.event.ObjectUpdatedEvent;
import org.drools.event.WorkingMemoryEventListener;
import org.drools.lang.descr.PackageDescr;

import benchmarks.dispatch.api.JobEligibility;
import benchmarks.dispatch.api.JobExcludedException;
import benchmarks.dispatch.fact.derived.DecimalInfo;
import benchmarks.dispatch.fact.derived.Info;
import benchmarks.dispatch.fact.derived.ScoreComponent;
import benchmarks.dispatch.fact.derived.TopWorkers;

public class DispatchWrapper {

	public DispatchWrapper() throws Exception {
		createWM();
	}

	public WorkingMemory getWM() {
		return wm;
	}

	private static final Set<String> rules = new HashSet<String>();

	static {
		rules.add("/benchmarks/dispatch/Derivations.dslr");
		rules.add("/benchmarks/dispatch/EligibilityExclusions.dslr");
		rules.add("/benchmarks/dispatch/LogicalTopTier.dslr");
		rules.add("/benchmarks/dispatch/Queries.drl");
		rules.add("/benchmarks/dispatch/Scoring.dslr");
		rules.add("/benchmarks/dispatch/ScoreManagement.dslr");
		rules.add("/benchmarks/dispatch/WorkerEligibility.dslr");
	}

	private WorkingMemory wm;
	public WMEL wmel;
	public AEL ael;

	public void createWM() throws Exception {

		RuleBaseConfiguration conf = new RuleBaseConfiguration();

		conf.setAssertBehaviour(RuleBaseConfiguration.AssertBehaviour.EQUALITY);
		conf.setRemoveIdentities(true);
		conf.setLogicalOverride(RuleBaseConfiguration.LogicalOverride.DISCARD);
		conf.setMaintainTms(true);
		conf.setShadowProxy(true);

		RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);

		PackageBuilderConfiguration pbc = new PackageBuilderConfiguration();

		pbc.addAccumulateFunction("top_workers",
				"benchmarks.dispatch.accumulator.TopWorkerAccumulator");

		PackageBuilder builder = new PackageBuilder(pbc);

		for (String ruleFile : rules) {
			DrlParser parser = new DrlParser();
			PackageDescr desc = parser.parse(getReader(ruleFile),
					getReader("/benchmarks/dispatch/dispatch.dsl"));
			builder.addPackage(desc);
		}

		ruleBase.addPackage(builder.getPackage());

		wm = ruleBase.newStatefulSession();

		wmel = new WMEL();

		wm.addEventListener(wmel);

		ael = new AEL();
		wm.addEventListener(ael);

		System.out.println("Created WM");

	}

	public static class AEL implements AgendaEventListener {

		public long cancels, creates, fireAfter, fireBefore, pops, pushes;

		public Map<String, Integer> fires = new HashMap<String, Integer>();

		public void dump() {
			for (Map.Entry<String, Integer> e : fires.entrySet()) {
				System.out.println(e.getKey() + ":" + e.getValue());
			}
		}

		public void activationCancelled(ActivationCancelledEvent event,
				WorkingMemory workingMemory) {
			cancels++;

		}

		public void activationCreated(ActivationCreatedEvent event,
				WorkingMemory workingMemory) {
			creates++;

		}

		public void afterActivationFired(AfterActivationFiredEvent event,
				WorkingMemory workingMemory) {
			String name = event.getActivation().getRule().getName();
			Integer current = fires.get(name);
			if (current == null) {
				current = new Integer(0);
			}
			fires.put(name, ++current);
			fireAfter++;

		}

		public void agendaGroupPopped(AgendaGroupPoppedEvent event,
				WorkingMemory workingMemory) {
			pops++;

		}

		public void agendaGroupPushed(AgendaGroupPushedEvent event,
				WorkingMemory workingMemory) {
			pushes++;

		}

		public void beforeActivationFired(BeforeActivationFiredEvent event,
				WorkingMemory workingMemory) {
			fireBefore++;

		}

	}

	public static class WMEL implements WorkingMemoryEventListener {

		public long inserts, updates, retracts;

		public void objectInserted(ObjectInsertedEvent event) {
			inserts++;
		}

		public void objectRetracted(ObjectRetractedEvent event) {
			retracts++;

		}

		public void objectUpdated(ObjectUpdatedEvent event) {
			updates++;

		}

	}

	public List<JobEligibility> getEligibileWorkersForJob(String jobId)
			throws Exception, JobExcludedException {
		if (isJobExcluded(jobId)) {
			throw new JobExcludedException();
		}

		TopWorkers topWorkers = getTopWorkersForJob(jobId);

		List<JobEligibility> eligibilities = new ArrayList<JobEligibility>();

		for (String workerId : topWorkers.getTopWorkers()) {
			JobEligibility je = new JobEligibility();
			je.setWorkerId(workerId);

			// TODO: Handle the "no position" case

			je.setDistanceToJobInMiles(((DecimalInfo) getInfo(jobId, workerId,
					Info.Type.DISTANCE_TO_JOB_MILES)).getValue());
			je.setMinutesLateToJob(((DecimalInfo) getInfo(jobId, workerId,
					Info.Type.MINUTES_LATE_TO_JOB)).getValue());
			je.setTimeToJobMinutes(((DecimalInfo) getInfo(jobId, workerId,
					Info.Type.TIME_TO_JOB_MINUTES)).getValue());
			
			Set scores = getResultSetFromWM("getScoreComponents", new Object[]{jobId, workerId});
			
			for (Object o: scores){
				ScoreComponent score = (ScoreComponent)o;
				je.addScoreComponent(score);
				
			}
			eligibilities.add(je);
		}

		return eligibilities;
	}

	private TopWorkers getTopWorkersForJob(String jobId) throws Exception {
		return (TopWorkers) getSingleResultFromWM("getTopWorkersForJob",
				new Object[] { jobId });
	}

	private Info getInfo(String jobId, String workerId, Info.Type type) {
		return (Info) getSingleResultFromWM("getInfo", new Object[] { jobId,
				workerId, type });
	}

	private boolean isJobExcluded(String jobId) {
		return getSingleResultFromWM("getJobExclusion", new Object[] { jobId }) != null;
	}
	
	private Set getResultSetFromWM(String query, Object[] params) {

		Set resultSet = new HashSet();
		
		QueryResults qrs = wm.getQueryResults(query, params);
		Iterator it = qrs.iterator();
		while (it.hasNext()) {
			QueryResult qr = (QueryResult) it.next();

			for (int i = 0; i < qr.size(); i++) {
				resultSet.add(qr.get(i));
			}
		}

		return resultSet;
	}

	private Object getSingleResultFromWM(String query, Object[] params) {

		QueryResults qrs = wm.getQueryResults(query, params);
		Iterator it = qrs.iterator();
		while (it.hasNext()) {
			QueryResult qr = (QueryResult) it.next();

			for (int i = 0; i < qr.size(); i++) {
				return qr.get(i);
			}
		}

		return null;
	}

	public FactHandle insert(Object o) throws Exception {
		return wm.insert(o);
	}

	public void fireAllRules() throws Exception {
		wm.fireAllRules();
	}

	private Reader getReader(String resourceName) {
		return new InputStreamReader(getClass().getResourceAsStream(
				resourceName));
	}

}
