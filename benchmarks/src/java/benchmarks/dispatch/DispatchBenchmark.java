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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
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

import benchmarks.dispatch.fact.independent.DispatchState;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;
import benchmarks.dispatch.simulation.JobGenerator;
import benchmarks.dispatch.simulation.WorkerGenerator;

public class DispatchBenchmark {

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

	private void createWM() throws Exception {

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
	}

	private Reader getReader(String resourceName) {
		return new InputStreamReader(getClass().getResourceAsStream(
				resourceName));
	}

	public void go(int numWorkers, int numJobs) throws Exception {
		createWM();
		
		DispatchState ds = new DispatchState();
		ds.setCurrentTime(new Date());
		FactHandle dsfh = wm.insert(ds);

		WorkerGenerator wg = new WorkerGenerator(100);

		for (int i = 0; i < numWorkers; i++) {
			Worker w = wg.generateWorker();
			wm.insert(w);
			WorkerPosition wp = wg.generateWorkerPosition(w);
			wm.insert(wp);
		}

		JobGenerator jg = new JobGenerator(100);

		for (int i = 0; i < numJobs; i++) {
			wm.insert(jg.generateJob());
		}

		wm.fireAllRules();
	}

	public static void main(String args[]) throws Exception {
		int numWorkers = 1;
		int numJobs = 1;
		
		
		if (args.length == 2){
			numWorkers = new Integer(args[0]);
			numJobs = new Integer(args[1]);
		}
		
		DispatchBenchmark db = new DispatchBenchmark();
		db.go(numWorkers, numJobs);
	}

}
