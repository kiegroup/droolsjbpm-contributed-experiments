package org.drools.learner.eval.heuristic;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.eval.InstDistribution;

public interface Heuristic {

    public void init(InstDistribution instsByTarget);

    public double evalCategorical(Domain attrDomain);

    public double evalContinuous(Domain attrDomain);

    public Domain getDomain();

    public ArrayList<Instance> getSortedInstances();

    public double getWorstEval();

    //	public abstract double info_attr(InstDistribution insts_by_target, Domain attr_domain);
    //	public abstract double info_contattr(List<Instance> data, Domain targetDomain, QuantitativeDomain splitDomain);
    //	
    //	public abstract double calc_info_attr( CondClassDistribution instances_by_attr);
    //	public abstract double calc_info(ClassDistribution quantity_by_class);
}
