package org.drools.learner.eval;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public class GlobalCategorizer {
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(GlobalCategorizer.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(GlobalCategorizer.class, SimpleLogger.DEFAULT_LEVEL);

	public GlobalCategorizer(Memory mem) {
		InstanceList class_instances = mem.getClassInstances();

		if (class_instances.getTargets().size()>1) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates");
			
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
		build(class_instances);
		
	
	}
	
	public void build(InstanceList working_instances) {
		
		Iterator<String> it_target= working_instances.getTargets().iterator();
		String target = it_target.next();
		
		DecisionTree dt = new DecisionTree(working_instances.getSchema(), target);

		//flog.debug("Num of attributes: "+ dt.getAttrDomains().size());
		
		InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
		stats_by_class.calculateDistribution(working_instances.getInstances());
		dt.FACTS_READ += working_instances.getSize();//

		categorize(stats_by_class, dt.getAttrDomains());
		//flog.debug("Result tree\n" + dt);
		return;// dt;
	}
	
	public void categorize(InstDistribution insts_by_target, List<Domain> attr_domains) {

		for (int attr_idx = 0 ; attr_idx< attr_domains.size(); attr_idx++) {
			Domain attr_domain = attr_domains.get(attr_idx);
			/* No need to clone the domain as soon as no need to change the domain
			 * All domains are categorical so i will use them the way they are
			 */
			if (attr_domain.isCategorical()) {
				// do nothing
			} else {
//				the continuous domain

				QuantitativeDomain trialDomain = QuantitativeDomain.createFromDomain(attr_domain);
				Categorizer visitor = new Categorizer(insts_by_target);
				visitor.findSplits(trialDomain);

//				trial domain is modified				
				if (trialDomain.getNumIndices()==1) {
				} else {					
				}
				
				InformationContainer categorized_eval = new InformationContainer();
				categorized_eval.domain =  trialDomain;
				categorized_eval.sorted_data = visitor.getSortedInstances();
			
				
				Hashtable<Object, InstDistribution> filtered_stats = null;
				try {
					filtered_stats = insts_by_target.split(categorized_eval);
					
					/* need to create a fake domain from trial domain 
					 * with possible number of values = trialDomain.trialDomain.getNumIndices();
					 */
					trialDomain.setFName(attr_domain.getFReferenceName()+"_fake");	// TODO how do we know whose fake copy is that domain
					trialDomain.setCategorical(true);
					
					for (Object category: filtered_stats.keySet()) {
						InstDistribution category_dist = filtered_stats.get(category);
						
						for (int target_cat = 0; target_cat<insts_by_target.getClassDomain().getCategoryCount(); target_cat++) {
							Object targetCategory = insts_by_target.getClassDomain().getCategory(target_cat); 
							for (Instance i : category_dist.getSupportersFor(targetCategory)) {
								i.setAttr(attr_domain.getFReferenceName()+"_fake", category);
							}
						}
						
						
					}
					attr_domains.add(trialDomain);
					
				} catch (FeatureNotSupported e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}
		return;
	}

}
