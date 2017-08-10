package org.drools.learner.builder;

import java.util.Hashtable;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.LeafNode;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.AttributeChooser;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.eval.stopping.StoppingCriterion;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;

public class C45Learner extends Learner {

    private AttributeChooser chooser;

    public C45Learner(Heuristic hf) {
        super();
        super.setDomainAlgo(DomainAlgo.QUANTITATIVE);
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
            classifiedNode.setRank(dataStats.getSum() / (double) this
                .getTrainingDataSize()/* total size of data fed to dt */);
            classifiedNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
            classifiedNode.setNumClassification(dataStats.getSum()); //num of classified instances at the leaf node

            //classifiedNode.setInfoMea(mea)
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
            noAttributeLeftNode.setRank(dataStats.getVoteFor(winner) / (double) this
                .getTrainingDataSize() /* total size of data fed to dt */);
            noAttributeLeftNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
            noAttributeLeftNode.setNumClassification(dataStats.getVoteFor(winner)); //num of classified instances at the leaf node
            //noAttributeLeftNode.setInfoMea(best_attr_eval.attribute_eval);

            /*
             * we need to know how many guys cannot be classified and who these
             * guys are
             */
            dataStats.missClassifiedInstances(missclassifiedData);
            dt.changeTrainError((dataStats.getSum() - dataStats.getVoteFor(winner)) / (double) getTrainingDataSize());
            return noAttributeLeftNode;
        }

        InformationContainer bestAttrEval = new InformationContainer();
        bestAttrEval.setStats(dataStats);
        bestAttrEval.setDepth(depth);
        bestAttrEval.setTotalNumData(getTrainingDataSizePerTree());

        /* choosing the best attribute in order to branch at the current node */
        chooser.chooseAttribute(bestAttrEval, dataStats, attributeDomains);

        if (super.criteria != null && criteria.size() > 0) {
            for (StoppingCriterion sc : criteria) {
                if (sc.stop(bestAttrEval)) {
                    Object   winner       = dataStats.getWinnerClass();
                    LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), winner);
                    majorityNode.setRank((double) dataStats.getVoteFor(winner) / (double) this
                        .getTrainingDataSize() /*
                                                    * total size of data fed to
                                                    * trainer
                                                    */);
                    majorityNode.setNumMatch(dataStats.getSum());
                    majorityNode.setNumClassification(dataStats.getVoteFor(winner));

                    /*
                     * we need to know how many guys cannot be classified and
                     * who these guys are
                     */
                    dataStats.missClassifiedInstances(missclassifiedData);
                    dt.changeTrainError((dataStats.getSum() - dataStats.getVoteFor(winner)) / (double) getTrainingDataSize());
                    return majorityNode;
                }
            }
        }
        Domain nodeDomain = bestAttrEval.domain;
        if (slog.debug() != null) {
            slog.debug().log("\n" + Util.ntimes("*", 20) + " 1st best attr: " + nodeDomain);
        }

        TreeNode currentNode = new TreeNode(nodeDomain);
        currentNode.setNumMatch(dataStats.getSum()); //num of matching instances to the leaf node
        currentNode.setRank(dataStats.getSum() / (double) this
            .getTrainingDataSize() /* total size of data fed to trainer */);
        currentNode.setInfoMea(bestAttrEval.attributeEval);
        //what the highest represented class is and what proportion of items at that node actually are that class
        currentNode.setLabel(dataStats.getWinnerClass());
        currentNode.setNumLabeled(dataStats.getSupportersFor(dataStats.getWinnerClass()).size());

        Hashtable<Object, InstDistribution> filteredStats = null;
        try {
            filteredStats = dataStats.split(bestAttrEval);
        } catch (FeatureNotSupported e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // node_domain);

        dt.FACTS_READ += dataStats.getSum();

        for (int c = 0; c < nodeDomain.getCategoryCount(); c++) {
            /* split the last two class at the same time */
            Object category = nodeDomain.getCategory(c);
            if (slog.debug() != null) {
                slog.debug().log("{" + nodeDomain + ":" + category + "}" + (c == nodeDomain.getCategoryCount() - 1 ? "\n" : ""));
            }

            /* list of domains except the choosen one (&target domain) */
            DecisionTree childDt = new DecisionTree(dt, nodeDomain);
            childDt.FACTS_READ = dt.FACTS_READ;

            if (filteredStats == null || filteredStats.get(category) == null || filteredStats.get(category).getSum() == 0) {
                /* majority !!!! */
                LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), dataStats.getWinnerClass());
                majorityNode.setRank(-1.0); //it does not classify any instance
                majorityNode.setNumMatch(0);
                majorityNode.setNumClassification(0);
                //currentNode.setInfoMea(best_attr_eval.attribute_eval);
                //dt.setTrainingError((int) (dt.getTrainingError() + data_stats.getSum()));

                majorityNode.setFather(currentNode);
                currentNode.putNode(category, majorityNode);
            } else {
                TreeNode newNode = train(childDt, filteredStats.get(category), depth + 1);//, attributeNames_copy
                newNode.setFather(currentNode);
                currentNode.putNode(category, newNode);
            }
        }

        return currentNode;
    }
}
