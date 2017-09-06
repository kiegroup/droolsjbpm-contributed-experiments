package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.LeafNode;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.AttributeChooser;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.eval.stopping.StoppingCriterion;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C45Learner  implements Learner {
    protected static final transient Logger     log            = LoggerFactory.getLogger(C45Learner.class);
    protected HashSet<Instance>            missclassifiedData;
    protected ArrayList<StoppingCriterion> criteria;
    private   int                          dataSize, dataSizePerTree;
    // must be deleted, goes to builder
    //	private DecisionTree best_tree;
    private InstanceList     inputData;

    private AttributeChooser chooser;

    public C45Learner(Heuristic hf) {
        chooser = new AttributeChooser(hf);

        this.dataSize = 0;
        this.dataSizePerTree = 0;

        criteria = new ArrayList<StoppingCriterion>(4);
        missclassifiedData = new HashSet<Instance>();
    }

    @Override
    public DecisionTree trainTree(InstanceList workingInstances) {
        String targetReference = this.getTargetDomain().getFReferenceName();
        //System.out.println("(Learner) target   "+ target_reference);
        DecisionTree dt = new DecisionTree(inputData.getSchema(), targetReference);

        //flog.debug("Num of attributes: "+ dt.getAttrDomains().size());

        InstDistribution statsByClass = new InstDistribution(dt.getTargetDomain(), workingInstances.getInstances());

        dt.FACTS_READ += workingInstances.getSize();

        ArrayList<Domain> attrsToClassify = new ArrayList<Domain>();
        attrsToClassify.addAll(dt.getAttrDomains());

        train(dt, null, null, statsByClass, attrsToClassify, 0);
        //flog.debug("Result tree\n" + dt);
        return dt;
    }

    private void train(DecisionTree dt, TreeNode father,  Object incomingCategory, InstDistribution dataStats, ArrayList<Domain> attrsToClassify, int depth) {//List<Instance> data) {

        if (dataStats.getSum() == 0) {
            throw new RuntimeException("Nothing to classify, factlist is empty");
        }

        // let's get the statistics of the results
        //InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());///* the target domain*/);
        dataStats.evaluateMajority();

        // if all elements are classified to the same value
        if (dataStats.getNumberOfSupportedCategories() == 1) {  // number of supported target categories

            LeafNode classifiedNode = new LeafNode(dt.getTargetDomain(), dataStats.getWinnerClass(), incomingCategory, father, dt );
            classifiedNode.setRank(dataStats.getSum() / this.getTrainingDataSize()/* total size of data fed to dt */);
            classifiedNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
            classifiedNode.setNumClassification(dataStats.getSum()); //num of classified instances at the leaf node
            //classifiedNode.setInfoMea(mea)
            return;
        }

        List<Domain> attributeDomains = dt.getAttrDomains();
        // if there is no attribute left in order to continue
        if (attributeDomains.size() == 0) {
            createLeaf(dt, father, incomingCategory, dataStats);

            return;
        }

        InformationContainer bestAttrEval = new InformationContainer(dataStats, depth, getTrainingDataSizePerTree() );

        /* choosing the best attribute in order to branch at the current node */
        chooser.chooseAttribute(bestAttrEval, dataStats, attributeDomains);

        if (criteria != null && criteria.size() > 0) {
            for (StoppingCriterion sc : criteria) {
                if (sc.stop(bestAttrEval)) {
                    createLeaf(dt, father, incomingCategory, dataStats);
                    return;
                }
            }
        }

        Domain nodeDomain = bestAttrEval.getDomain();
        log.debug("{} 1st best attr: {} ", Util.ntimes("*", 20) , nodeDomain);

        TreeNode currentNode = new TreeNode(nodeDomain, incomingCategory,father, dt);
        currentNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
        currentNode.setRank(dataStats.getSum() / this.getTrainingDataSize() ); // total size of data fed to trainer
        currentNode.setInfoMea(bestAttrEval.getAttributeEval());
        //what the highest represented class is and what proportion of items at that node actually are that class
        currentNode.setLabel(dataStats.getWinnerClass());
        currentNode.setNumLabeled(dataStats.getSupportersFor(dataStats.getWinnerClass()).size());

        HashMap<Object, InstDistribution> filteredStats = dataStats.split(bestAttrEval);

        dt.FACTS_READ += dataStats.getSum();

        for (int c = 0; c < nodeDomain.getCategoryCount(); c++) {
            /* split the last two class at the same time */
            Object category = nodeDomain.getCategory(c);
            if (log.isDebugEnabled()) {
                log.debug("{" + nodeDomain + ":" + category + "}" + (c == nodeDomain.getCategoryCount() - 1 ? "\n" : ""));
            }

            /* list of domains except the choosen one (&target domain) */

            if (categoryExistsWithData(filteredStats, category)) {
                attrsToClassify.remove(nodeDomain);
                train(dt, currentNode, category, filteredStats.get(category), attrsToClassify,depth + 1);//, attributeNames_copy
            } else {
                /* majority !!!! */
                LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), dataStats.getWinnerClass(), incomingCategory, father, dt);
                majorityNode.setRank(-1.0); //it does not classify any instance
                majorityNode.setNumMatch(0);
                majorityNode.setNumClassification(0);
                //currentNode.setInfoMea(best_attr_eval.attribute_eval);
                //dt.setTrainingError((int) (dt.getTrainingError() + data_stats.getSum()));

//                majorityNode.setFather(currentNode);
//                currentNode.putNode(category, majorityNode);
            }
        }
    }

    private void createLeaf(DecisionTree dt, TreeNode father, Object incomingCategory, InstDistribution dataStats) {
    /* an heuristic of the leaf classification */
        Object   winner              = dataStats.getWinnerClass(); // winner target category
        LeafNode noAttributeLeftNode = new LeafNode(dt.getTargetDomain(), winner, incomingCategory, father, dt);
        noAttributeLeftNode.setRank(dataStats.getVoteFor(winner) / this.getTrainingDataSize() ); // total size of data fed to dt
        noAttributeLeftNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
        noAttributeLeftNode.setNumClassification(dataStats.getVoteFor(winner)); //num of classified instances at the leaf node
        //noAttributeLeftNode.setInfoMea(best_attr_eval.attribute_eval);

            /*
             * we need to know how many guys cannot be classified and who these
             * guys are
             */
        dataStats.missClassifiedInstances(missclassifiedData);
        dt.changeTrainError((dataStats.getSum() - dataStats.getVoteFor(winner)) / (double) getTrainingDataSize());
    }

    private boolean categoryExistsWithData(HashMap<Object, InstDistribution> filteredStats, Object category) {
        return filteredStats != null || filteredStats.get(category) != null || filteredStats.get(category).getSum() == 0;
    }

    @Override public Domain getTargetDomain() {
        Iterator<String> itTarget = inputData.getTargets().iterator();
        // TODO check if there is a target candidate
        String target = itTarget.next();
        //System.out.println("(Learner) What is target?? "+ target +" and the domain "+ input_data.getSchema().getAttrDomain(target));
        return inputData.getSchema().getAttrDomain(target);
    }

//    // TODO how are we going to select the target domain if there is more than one candidate
//    private DecisionTree selectTarget(InstanceList workingInstances) {
//        DecisionTree dt = null;
//        for (String target : inputData.getTargets()) {
//            dt = new DecisionTree(inputData.getSchema(), target);
//
//            //flog.debug("Num of attributes: "+ dt.getAttrDomains().size());
//
//            InstDistribution statsByClass = new InstDistribution(dt.getTargetDomain());
//            statsByClass.calculateDistribution(workingInstances.getInstances());
//            dt.FACTS_READ += workingInstances.getSize();
//
//            TreeNode root = train(dt, statsByClass, 0);
//            dt.setRoot(root);
//            //flog.debug("Result tree\n" + dt);
//        }
//        return dt;
//    }

    @Override public ArrayList<StoppingCriterion> getStoppingCriteria() {
        return criteria;
    }

    @Override public void addStoppingCriteria(StoppingCriterion c) {
        criteria.add(c);
    }

    @Override public int getTrainingDataSizePerTree() {
        return this.dataSizePerTree;
    }

    @Override public void setTrainingDataSizePerTree(int num) {
        this.dataSizePerTree = num;
    }

    @Override public int getTrainingDataSize() {
        return this.dataSize;
    }

    @Override public void setTrainingDataSize(int num) {
        this.dataSize = num;
    }

    // must be deleted, goes to builder
    //	public DecisionTree getTree() {
    //		return best_tree;
    //	}

    @Override public void setInputData(InstanceList inputData) {
        this.inputData = inputData;
    }

    //	public InstanceList getInputData() {
    //		return input_data;
    //	}

    // must be deleted, goes to builder
    //	public void setBestTree(DecisionTree dt) {
    //		this.best_tree = dt;
    //	}
}
