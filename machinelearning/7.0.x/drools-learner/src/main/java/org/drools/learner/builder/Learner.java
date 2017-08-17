package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.InstanceList;
import org.drools.learner.eval.stopping.StoppingCriterion;

public interface Learner {
    DomainAlgo DEFAULT_DOMAIN = DomainAlgo.QUANTITATIVE;
    DataType   DEFAULT_DATA   = DataType.PRIMITIVE;

    DecisionTree instantiateTree();

    void trainTree(DecisionTree dt, InstanceList workingInstances);

    DecisionTree trainTree(InstanceList workingInstances);

    Domain getTargetDomain();

    ArrayList<StoppingCriterion> getStoppingCriteria();

    void addStoppingCriteria(StoppingCriterion c);

    int getTrainingDataSizePerTree();

    void setTrainingDataSizePerTree(int num);

    int getTrainingDataSize();

    void setTrainingDataSize(int num);

    DomainAlgo getDomainAlgo();

    void setDomainAlgo(DomainAlgo type);

    void setInputSpec(InstanceList classInstances);

    enum DomainAlgo {
        CATEGORICAL, QUANTITATIVE
    }

    enum DataType {
        PRIMITIVE, STRUCTURED, COLLECTION
    }
}
