package org.drools.learner.eval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

/* addition to the ClassDistribution 
 * it keeps the instances themselves by their target value in an hashmap */
public class InstDistribution extends ClassDistribution{
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(InstDistribution.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(InstDistribution.class, SimpleLogger.DEFAULT_LEVEL);

	private String attr_sum = Util.sum();
	private Hashtable<Object, List<Instance>> instance_by_class;
	
	//private Hashtable<Object, List<Instance>>
	
	public InstDistribution(Domain targetDomain) {
		super(targetDomain);
		
		instance_by_class = new Hashtable<Object, List<Instance>>(targetDomain.getCategoryCount());
		for (int t=0; t<targetDomain.getCategoryCount(); t++) {
			Object obj_t = targetDomain.getCategory(t);
			instance_by_class.put(obj_t, new ArrayList<Instance>());
		}
	}
	
	public void calculateDistribution(List<Instance> instances){
		double data_size = 0.0;
		String tName = super.getClassDomain().getFReferenceName();
		if (slog.debug() != null)
			slog.debug().log("tName : " +tName + "\n");
		for (Instance inst : instances) {
			if (slog.debug() != null)
				slog.debug().log("inst : " +inst + "\n");
			data_size += inst.getWeight();
			
			Object target_key = inst.getAttrValue(tName);
			super.change(target_key, inst.getWeight());		// add inst.getWeight() vote for the target value of the instance : target_key
			//super.change(attr_sum, inst.getWeight());		// ?????
			
			this.addSupporter(target_key, inst);
		}
		//super.change(attr_sum, data_size);	// TODO should i write special function for changing the sum 
		super.setSum(data_size);
	}

	public List<Instance> getSupportersFor(Object category) {
		return instance_by_class.get(category);
	}
	
	private void addSupporter(Object target_category, Instance inst) {
		this.instance_by_class.get(target_category).add(inst);
	}
	
	
	private Hashtable<Object, InstDistribution> instantiateLists(Domain splitDomain) {		
		Domain target_domain = super.getClassDomain();
		Hashtable<Object, InstDistribution> instLists = new Hashtable<Object, InstDistribution>(splitDomain.getCategoryCount());
		for (int c=0; c<splitDomain.getCategoryCount(); c++) {
			Object category = splitDomain.getCategory(c);
			instLists.put(category, new InstDistribution(target_domain));
		}
		return instLists;
	}
	
	/* spliting during the training for C45TreeIterator */
	public Hashtable<Object, InstDistribution> split(InformationContainer splitDomainEval) throws FeatureNotSupported {
		Domain splitDomain = splitDomainEval.domain;
		Hashtable<Object, InstDistribution> instLists = this.instantiateLists(splitDomain);
		
		if (splitDomain.isCategorical()) {	
			this.splitFromCategorical(splitDomain, instLists);
		} else {
			if (splitDomain instanceof QuantitativeDomain && splitDomainEval.sorted_data != null) {
				QuantitativeDomain q_splitDomain = (QuantitativeDomain) splitDomain;
				
				
				//Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
				this.splitFromQuantitative(splitDomainEval.sorted_data, q_splitDomain, instLists);
			}
			else {
				throw new FeatureNotSupported("Can not split a quatitative domain if it's object type is not QuantitativeDomain "+splitDomain);
			}
			
		}
		
		return instLists;
	}

	
	public Hashtable<Object, InstDistribution> splitFromCategorical(Domain splitDomain, Hashtable<Object, InstDistribution> instLists) {
		if (instLists == null)
			instLists = this.instantiateLists(splitDomain);
		
		Domain target_domain = super.getClassDomain();
		String attrName = splitDomain.getFReferenceName();
		for (int category = 0; category<target_domain.getCategoryCount() ; category++) {
			Object targetCategory = target_domain.getCategory(category); 
			//this.calculateDistribution(this.getSupportersFor(targetCategory));
			
			for (Instance inst: this.getSupportersFor(targetCategory)) {
				Object inst_attr_category = inst.getAttrValue(attrName);
				
				instLists.get(inst_attr_category).change(targetCategory, inst.getWeight());	// add one for vote for the target value : target_key
				instLists.get(inst_attr_category).change(attr_sum, inst.getWeight());
				instLists.get(inst_attr_category).addSupporter(targetCategory, inst);
				
			}
		}
		return instLists;
	}
	
	private void splitFromQuantitative(ArrayList<Instance> data, QuantitativeDomain attributeDomain, Hashtable<Object, InstDistribution> instLists) {
		
		String attributeName = attributeDomain.getFName();
		String targetName = super.getClassDomain().getFReferenceName();
		
		//flog.debug("FactProcessor.splitFacts_cont() attr_split "+ attributeName);

		
		int start_point = 0;
		for (int index = 0; index < attributeDomain.getNumIndices(); index ++) {
			int integer_index = attributeDomain.getSplit(index).getIndex(); //splits_it.next().intValue();
			
			Object inst_attr_category = attributeDomain.getSplit(index).getValue(); //splitValues.get(index);
			//System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);

			try {

				//flog.debug("FactProcessor.splitFacts_cont() new category: "+ inst_attr_category+
				//		" ("+start_point+","+integer_index+")");
				
				List<Instance> data_at_category = data.subList(start_point, integer_index+1);
				for (Instance inst: data_at_category) {
					
					Object targetCategory = inst.getAttrValue(targetName);
					
					instLists.get(inst_attr_category).change(targetCategory, inst.getWeight());	// add one for vote for the target value : target_key
					instLists.get(inst_attr_category).change(attr_sum, inst.getWeight());
					instLists.get(inst_attr_category).addSupporter(targetCategory, inst);
				}
				start_point = integer_index+1;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void missClassifiedInstances(HashSet<Instance> missclassification) {
		Object winner = super.get_winner_class();
		
		for (int idx = 0; idx < super.target_attr.getCategoryCount(); idx++) {
			Object looser = super.target_attr.getCategory(idx);
			double num_supp = this.getVoteFor(looser);
			
			if ((num_supp > 0) && !winner.equals(looser)) {
				
//				System.out.println(Util.ntimes("DANIEL", 2)+ " one looser ? "+looser + " num of sup="+num_supp);
//				System.out.println(" the num of supporters = "+ this.getVoteFor(looser));
//				System.out.println(" but the guys "+ this.getSupportersFor(looser));
//				System.out.println("How many bok: "+this.getSupportersFor(looser).size());
				missclassification.addAll(getSupportersFor(looser));
			} else {
//				System.out.println(Util.ntimes("DANIEL", 5)+ "how many times matching?? not a looser "+ looser );
			}
		}
		
	}
	
	
	
}


