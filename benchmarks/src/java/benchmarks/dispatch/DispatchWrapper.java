package benchmarks.dispatch;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.drools.FactHandle;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DrlParser;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.lang.descr.PackageDescr;

public class DispatchWrapper {
	
	public DispatchWrapper() throws Exception {
		createWM();
	}
	
	public WorkingMemory getWM(){
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

	public void createWM() throws Exception {

		RuleBaseConfiguration conf = new RuleBaseConfiguration();

		conf.setAssertBehaviour(RuleBaseConfiguration.AssertBehaviour.EQUALITY);
		conf.setRemoveIdentities(true);
		conf.setLogicalOverride(RuleBaseConfiguration.LogicalOverride.DISCARD);
		conf.setMaintainTms(true);
		conf.setShadowProxy(true);

		RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);

		PackageBuilderConfiguration pbc = new PackageBuilderConfiguration();

		pbc
				.addAccumulateFunction("top_workers",
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
		
		System.out.println("Created WM");

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
