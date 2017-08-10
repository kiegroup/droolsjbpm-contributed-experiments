package org.drools.learner.builder;

import java.util.Hashtable;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.LeafNode;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.AttributeChooser;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.tools.Util;

public class ID3Learner extends Learner {

    private AttributeChooser chooser;

    public ID3Learner(Heuristic hf) {
        super();
        super.setDomainAlgo(DomainAlgo.CATEGORICAL);
        chooser = new AttributeChooser(hf);
    }

    protected TreeNode train(DecisionTree dt, InstDistribution dataStats, int depth) {//List<Instance> data) {

        if (dataStats.getSum() == 0) {
            throw new RuntimeException("Nothing to classify, factlist is empty");
        }

        /* let's get the statistics of the results */
        //InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());///* the target domain*/);
        dataStats.evaluateMajority();

        /* if all elements are classified to the same value */
        if (dataStats.getNumIdeas( /* number of supported target categories */) == 1) {

            LeafNode classifiedNode = new LeafNode(
                dt.getTargetDomain() /* target domain */,
                dataStats.getWinnerClass() /* winner target category */);
            classifiedNode.setRank((double) dataStats.getSum() / (double) this
                .getTrainingDataSize()/* total size of data fed to dt */);
            classifiedNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
            classifiedNode.setNumClassification(dataStats.getSum()); //num of classified instances at the leaf node
            return classifiedNode;
        }
        List<Domain> attributeDomains = dt.getAttrDomains();

        /* if there is no attribute left in order to continue */
        if (attributeDomains.size() == 0) {
            /* an heuristic of the leaf classification */
            Object winner = dataStats
                .getWinnerClass(); /* winner target category */
            LeafNode noAttributeLeftNode = new LeafNode(
                dt.getTargetDomain() /* target domain */, winner);
            noAttributeLeftNode.setRank((double) dataStats.getVoteFor(winner) / (double) this
                .getTrainingDataSize() /* total size of data fed to dt */);
            noAttributeLeftNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
            noAttributeLeftNode.setNumClassification(dataStats.getVoteFor(winner)); //num of classified instances at the leaf node
            return noAttributeLeftNode;
        }

        /*
         * id3 starts: choose the attribute according to the entropy function
         * entrophy function: data, info of the data wrt target domain, info for
         * each attribute
         */
        Domain nodeDomain = chooser.chooseAttributeAsCategorical(dataStats, attributeDomains);

        if (flog.stat() != null) {
            flog.stat().log(Util.ntimes("*", 20) + " 1st best attr: " + nodeDomain);
        }

        TreeNode currentNode = new TreeNode(nodeDomain);
        currentNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
        currentNode.setRank((double) dataStats.getSum() / (double) this
            .getTrainingDataSize() /* total size of data fed to trainer */);
        //currentNode.setInfoMea(best_attr_eval.attribute_eval);
        //what the highest represented class is and what proportion of items at that node actually are that class
        currentNode.setLabel(dataStats.getWinnerClass());
        currentNode.setNumLabeled(dataStats.getSupportersFor(dataStats.getWinnerClass()).size());

        Hashtable<Object, InstDistribution> filteredStats = dataStats.splitFromCategorical(nodeDomain, null);
        dt.FACTS_READ += dataStats.getSum();

        for (int c = 0; c < nodeDomain.getCategoryCount(); c++) {
            /* split the last two class at the same time */
            Object category = nodeDomain.getCategory(c);

            /* list of domains except the choosen one (&target domain) */
            DecisionTree childDt = new DecisionTree(dt, nodeDomain);

            if (filteredStats.get(category) == null || filteredStats.get(category).getSum() == 0) {
                /* majority !!!! */
                LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), dataStats.getWinnerClass());
                majorityNode.setRank(-1.0); //it does not classify any instance
                majorityNode.setNumMatch(0);
                majorityNode.setNumClassification(0);
                currentNode.putNode(category, majorityNode);
            } else {
                TreeNode newNode = train(childDt, filteredStats.get(category), depth + 1);//, attributeNames_copy
                currentNode.putNode(category, newNode);
            }
        }
        return currentNode;
    }
}
