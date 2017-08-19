package org.drools.learner;

import java.util.ArrayList;

import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.builder.Solution;
import org.drools.learner.builder.SolutionSet;
import org.drools.learner.eval.PrunerStats;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionTreePruner {

    private static final double EPSILON    = 0.0d;//0.0000000001;

    //	private PrunerStats best_stats;
    protected static final transient Logger log        = LoggerFactory.getLogger(DecisionTreePruner.class);
    private                          double INIT_ALPHA = 0.5d;
    //	private double best_error;
    private Solution            bestSolution;
    private ArrayList<Solution> prunedSol;

    private PrunerStats bestStatsEverFound;

    public DecisionTreePruner() {
        //		best_error = 1.0;
        bestStatsEverFound = new PrunerStats(1.0);//proc.getAlphaEstimate());
        prunedSol = new ArrayList<Solution>();
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public void prunToEstimate(SolutionSet solSet) {
        /*
         * The best tree is selected from this series of trees with the
         * classification error not exceeding an expected error rate on some
         * test set (cross-validation error), which is done at the second stage.
         */
        int i = 0;
        for (Solution sol : solSet.getSolutions()) {
            boolean updated = this.prunToEstimate(sol);
            if (updated) {
                solSet.setBestSolutionId(i);
            }
        }
    }

    public boolean prunToEstimate(Solution sol) {
        /*
         * The best tree is selected from this series of trees with the
         * classification error not exceeding an expected error rate on some
         * test set (cross-validation error), which is done at the second stage.
         */

        DecisionTree dt = sol.getTree();
        dt.calcNumNodeLeaves(dt.getRoot());

        double           epsilon   = EPSILON * numExtraMisClassIfPrun(dt.getRoot());
        MinAlphaProc     alphaProc = new MinAlphaProc(INIT_ALPHA, epsilon);
        TreeSequenceProc search    = new TreeSequenceProc(sol, alphaProc);//INIT_ALPHA

        search.initTree(); // alpha_1 = 0.0 
        search.iterateTrees(1);

        //		updates.add(search.getTreeSequence());
        //		equence_stats.add(search.getTreeSequenceStats());
        //		alpha_procs.add(alpha_proc);

        boolean betterFound = false;

        int sid    = 0;
        int bestId = 0;
        //		double best_error = 1.0d;
        System.out.println("Tree id\t Num_leaves\t Cross-validated\t Resubstitution\t Alpha\t");
        ArrayList<PrunerStats> trees = search.getTreeSequenceStats();
        for (PrunerStats st : trees) {
            if (st.getErrorEstimation() < bestStatsEverFound.getErrorEstimation()
                || (st.getErrorEstimation() == bestStatsEverFound.getErrorEstimation() && st.getNumTerminalNodes() < bestStatsEverFound.getNumTerminalNodes())) {
                bestStatsEverFound.iterationId(st.iterationId());
                bestStatsEverFound.setErrorEstimation(st.getErrorEstimation());
                bestStatsEverFound.setTrainError(st.getTrainError());
                bestStatsEverFound.setNumTerminalNodes(st.getNumTerminalNodes());
                bestStatsEverFound.setAlpha(st.getAlpha());
                bestId = sid;
                //				best_st = st.iteration_id();
                betterFound = true;
            } else {

            }
            System.out.println(sid + "\t " + st.getNumTerminalNodes() + "\t " + 100.0d * st.getErrorEstimation() + "\t " + 100.0d * st.getTrainError() + "\t " + st.getAlpha());
            sid++;
        }
        System.out.println("BEST " + bestId + "\t " + trees.get(bestId).getNumTerminalNodes() + "\t " + trees.get(bestId).getErrorEstimation() + "\t " + trees.get(bestId).getTrainError()
                               + "\t " + trees.get(bestId).getAlpha());

        //System.exit(0);

        int    N                     = dt.getTestingDataSize();// procedure.getTestDataSize(dt_i);
        double standartErrorEstimate = Math.sqrt((trees.get(bestId).getErrorEstimation() * (1 - trees.get(bestId).getErrorEstimation()) / (double) N));

        if (betterFound) {
            for (int i = search.treeSequence.size() - 1; i > 0; i--) {

                NodeUpdate nu = search.treeSequence.get(i);
                System.out.println(nu.iterationId + ">" + bestStatsEverFound.iterationId());
                if (nu.iterationId > bestStatsEverFound.iterationId()) {
                    search.addBack(nu.nodeUpdate, nu.oldNode);
                    SingleTreeTester t         = new SingleTreeTester(search.treeSol.getTree());
                    Stats            train     = t.test(search.treeSol.getList());
                    Stats            test      = t.test(search.treeSol.getTestList());
                    int              numLeaves = search.treeSol.getTree().getRoot().getNumLeaves();

                    System.out.println("Back " + i + "\t " + numLeaves + "\t " + test.getResult(Stats.INCORRECT) / (double) test.getTotal() + "\t "
                                           + train.getResult(Stats.INCORRECT) / (double) train.getTotal() + "\t ");
                } else {
                    break;
                }
            }

            bestSolution = search.treeSol;
            return true;
        }
        return false;
    }

    public void prunTree(Solution sol) {
        double           epsilon = 0.0000001 * numExtraMisClassIfPrun(sol.getTree().getRoot());
        TreeSequenceProc search  = new TreeSequenceProc(sol, new AnAlphaProc(bestStatsEverFound.getAlpha(), epsilon));
        search.iterateTrees(0);
        //search.getTreeSequence()// to go back

    }

    private void updateLeaves(TreeNode myNode, int i) {
        myNode.setNumLeaves(myNode.getNumLeaves() + i);
        TreeNode fatherNode = myNode.getFather();
        if (fatherNode != null) {
            updateLeaves(fatherNode, i);
        }
    }

    // returns the node missclassification cost
    private int R(TreeNode t) {
        if (log.isDebugEnabled()) {
            log.debug(":R:num_misclassified " + t.getNumMatch() + " " + t.getNumLabeled());
        }
        return (int) (t.getNumMatch() - t.getNumLabeled());
    }

    private int numExtraMisClassIfPrun(TreeNode myNode) {
        int numMisclassified = R(myNode); // needs to be cast because of

        if (log.isDebugEnabled()) {
            log.debug(":numExtraMisClassIfPrun:num_misclassified " + numMisclassified);
        }

        int kidsMisclassified = 0;
        for (Object key : myNode.getChildrenKeys()) {
            TreeNode child = myNode.getChild(key);
            kidsMisclassified += child.getMissClassified();
            if (log.isDebugEnabled()) {
                log.debug(" kid=" + kidsMisclassified);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("\n");
        }
        if (numMisclassified < kidsMisclassified) {
            System.out.println("Problem ++++++");
            //System.exit(0);
            return 0;
        }
        return numMisclassified - kidsMisclassified;
    }

    public boolean isChildOf(TreeNode curNode, TreeNode foundNode) {
        TreeNode parent = curNode.getFather();
        if (parent == null) {
            return false;
        }
        if (parent.equals(foundNode)) {
            return true;
        } else {
            return isChildOf(parent, foundNode);
        }
    }

    public interface AlphaSelectionProc {

        public double checkNode(double curAlpha, TreeNode curNode, ArrayList<TreeNode> nodes);

        public void initProc(double value);

        public double getAlpha();
    }

    public class TreeSequenceProc {

        private static final double MAX_ERROR_RATIO = 0.99;

        private Solution               treeSol;
        //private double the_alpha;
        private AlphaSelectionProc     alphaProc;
        private ArrayList<NodeUpdate>  treeSequence;
        //		private ArrayList<PrunerStats> tree_sequence_stats;
        private ArrayList<PrunerStats> treeSequenceStats;

        private PrunerStats bestTreeStats;

        public TreeSequenceProc(Solution sol, AlphaSelectionProc cond) { //, double init_alpha
            treeSol = sol;

            alphaProc = cond;
            treeSequence = new ArrayList<NodeUpdate>();
            //			tree_sequence_stats = new ArrayList<PrunerStats>();
            treeSequenceStats = new ArrayList<PrunerStats>();

            bestTreeStats = new PrunerStats(10000000.0d);

            System.out.println("From solution:" + treeSol.getTestError() + " " + treeSol.getTrainError() + " " + treeSol.getTree().getRoot().getNumLeaves());
            System.out.println("From tree:" + treeSol.getTree().getTestError() + " " + treeSol.getTree().getTrainError() + " " + treeSol.getTree().getRoot().getNumLeaves());

            NodeUpdate initTree = new NodeUpdate(treeSol.getTestError());
            treeSequence.add(initTree);

            //			PrunerStats init_tree_stats = new PrunerStats(tree_sol.getTree().getStats());
            //			init_tree_stats.setNum_terminal_nodes(tree_sol.getTree().getRoot().getNumLeaves());
            //			init_tree_stats.setAlpha(0.0d);	// dont know
            //			init_tree_stats.setCost_complexity(-1);	// dont known
            //			tree_sequence_stats.add(init_tree_stats);

            PrunerStats initTreeStats = new PrunerStats(treeSol.getTrainError(), treeSol.getTestError());
            initTreeStats.setNumTerminalNodes(treeSol.getTree().getRoot().getNumLeaves());
            initTreeStats.setAlpha(0.0d); // dont know
            initTreeStats.setCostComplexity(-1); // dont known
            treeSequenceStats.add(initTreeStats);
        }

        public DecisionTree getFocusTree() {
            return treeSol.getTree();
        }

        //		public ArrayList<PrunerStats> getTreeSequenceStats() {
        //			return tree_sequence_stats;
        //		}

        public ArrayList<PrunerStats> getTreeSequenceStats() {
            return treeSequenceStats;
        }

        public ArrayList<NodeUpdate> getTreeSequence() {
            return treeSequence;
        }

        private void initTree() {
            // initialize the tree to be prunned
            // T_1 is the smallest subtree of T_max satisfying R(T_1) = R(T_max)

            ArrayList<TreeNode> lastNonterminals = treeSol.getTree().getAnchestorOfLeaves(treeSol.getTree().getRoot());

            // R(t) <= sum R(t_children) by the proposition 2.14, R(t) node miss-classification cost
            // if R(t) = sum R(t_children) then prune off t_children

            boolean treeChanged = false; // (k)
            for (TreeNode t : lastNonterminals) {
                if (numExtraMisClassIfPrun(t) == 0) {
                    // prune off the candidate node
                    treeChanged = true;
                    pruneOff(t, 0);
                }
            }
            if (treeChanged) {
                PrunerStats stats = new PrunerStats();
                stats.iterationId(0);
                //update_tree_stats(stats, 0.0d, 0); // error_estimation = stats.getCostEstimation() for the set (cross_validation or test error)
                updateTreeStats(stats, 0.0d, 0);
                //			tree_sequence_stats.add(stats);
            }
        }

        private void iterateTrees(int i) {
            if (log.isDebugEnabled()) {
                log.debug(treeSol.getTree().toString() + "\n");
            }
            // if number of non-terminal nodes in the tree is more than 1
            // = if there exists at least one non-terminal node different than root
            if (treeSol.getTree().getNumNonTerminalNodes() < 1) {
                if (log.isDebugEnabled()) {
                    log.debug(":sequence_trees:TERMINATE-There is no non-terminal nodes? " + treeSol.getTree().getNumNonTerminalNodes() + "\n");
                }
                return;
            } else if (treeSol.getTree().getNumNonTerminalNodes() == 1 && treeSol.getTree().getRoot().getNumLeaves() <= 1) {
                if (log.isDebugEnabled()) {
                    log.debug(":sequence_trees:TERMINATE-There is only one node left which is root node " + treeSol.getTree().getNumNonTerminalNodes() + " and it has only one leaf (pruned)"
                                         + treeSol.getTree().getRoot().getNumLeaves() + "\n");
                }
                return;
            }
            // for each non-leaf subtree
            ArrayList<TreeNode> candidateNodes = new ArrayList<TreeNode>();
            // to find all candidates with min_alpha value
            //TreeSequenceProc search = new TreeSequenceProc(the_alpha, alpha_proc);//100000.0d, new MinAlphaProc());

            findCandidateNodes(treeSol.getTree().getRoot(), candidateNodes);
            //double min_alpha = search.getTheAlpha();
            double minAlpha = getTheAlpha();
            System.out.println("!!!!!!!!!!! dt:" + treeSol.getTree().getId() + " ite " + i + " alpha: " + minAlpha + " num_nodes_found " + candidateNodes.size());

            if (candidateNodes.size() > 0) {
                // The one or more subtrees with that value of will be replaced by leaves
                // instead of getting the first node, have to process all nodes and prune all
                // write a method to prune all
                //TreeNode best_node = candidate_nodes.get(0);

                int changeInTrainingMisclass = 0; // (k)
                for (TreeNode candidateNode : candidateNodes) {
                    // prune off the candidate node
                    pruneOff(candidateNode, i);
                    changeInTrainingMisclass += numExtraMisClassIfPrun(candidateNode); // extra misclassified guys
                }
                //				PrunerStats stats = new PrunerStats();
                //				stats.iteration_id(i);
                //				update_tree_stats(stats, min_alpha, change_in_training_misclass); // error_estimation = stats.getCostEstimation() for the set (cross_validation or test error)
                PrunerStats stats = new PrunerStats();
                stats.iterationId(i);
                updateTreeStats(stats, minAlpha, changeInTrainingMisclass);
                //				if (log.debug() !=null)
                //					log.debug().log(":sequence_trees:error "+ stats.getCostEstimation() +"<?"+ procedure.getErrorEstimate() * 1.6 +"\n");

                if (stats.getErrorEstimation() < MAX_ERROR_RATIO) { //procedure.getValidationErrorEstimate() * 1.6) {
                    // if the error of the tree is not that bad

                    if (stats.getErrorEstimation() < bestTreeStats.getErrorEstimation()) {
                        bestTreeStats = stats;
                        if (log.isDebugEnabled()) {
                            log.debug(":sequence_trees:best node updated \n");
                        }
                    }
                    //	TODO update alpha_proc by increasing the min_alpha				the_alpha += 10;
                    alphaProc.initProc(alphaProc.getAlpha() + INIT_ALPHA);
                    iterateTrees(i + 1);
                } else {
                    //TODO update.setStopTree();
                    return;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(":sequence_trees:no candidate node is found ???? \n");
                }
            }
        }

        private void pruneOff(TreeNode candidateNode, int i) {
            System.out.println(treeSol.getTree().getTargetDomain() + " " + candidateNode.getLabel());

            // initially LeafNode father is null, it'll be set if it's later added as a child
            LeafNode bestClone = new LeafNode(treeSol.getTree().getTargetDomain(), candidateNode.getLabel(), null, null, treeSol.getTree());
            bestClone.setRank(candidateNode.getRank());
            bestClone.setNumMatch(candidateNode.getNumMatch()); //num of matching instances to the leaf node
            bestClone.setNumClassification(candidateNode.getNumLabeled()); //num of (correctly) classified instances at the leaf node

            NodeUpdate update = new NodeUpdate(candidateNode, bestClone);
            //TODO
            update.iterationId(i);

            update.setDecisionTree(treeSol.getTree());
            //change_in_training_misclass += numExtraMisClassIfPrun(candidate_node); // extra misclassified guys
            int numLeaves = candidateNode.getNumLeaves();

            TreeNode fatherNode = candidateNode.getFather();
            if (fatherNode != null) {
                for (Object key : fatherNode.getChildrenKeys()) {
                    if (fatherNode.getChild(key).equals(candidateNode)) {
                        fatherNode.addChild(key, bestClone);
                        break;
                    }
                }
                updateLeaves(fatherNode, -numLeaves + 1);
            } else {
                // this node does not have any father node it is the root node of the tree
                treeSol.getTree().setRoot(bestClone);
            }

            //updates.get(dt_0.getId()).add(update);
            treeSequence.add(update);
        }

        public void addBack(TreeNode leafNode, TreeNode originalNode) {
            TreeNode father = leafNode.getFather();
            if (father == null) {
                treeSol.getTree().setRoot(originalNode);
                return;
            } else {
                int numLeaves = originalNode.getNumLeaves();
                for (Object key : father.getChildrenKeys()) {
                    if (father.getChild(key).equals(leafNode)) {
                        father.addChild(key, originalNode);
                        updateLeaves(father, numLeaves - 1);
                        break;
                    }
                }
            }
        }

        private void updateTreeStats(PrunerStats stats, double computedAlpha, int changeInTrainingError) {

            InstanceList     learningSet   = treeSol.getList();
            InstanceList     validationSet = treeSol.getTestList();
            int              numError      = 0;
            SingleTreeTester t             = new SingleTreeTester(treeSol.getTree());
            Stats            testStats     = t.test(treeSol.getTestList());

            //System.out.println(validation_set.getSize());
            for (int indexI = 0; indexI < validationSet.getSize(); indexI++) {
                Integer result = t.test(validationSet.getInstance(indexI));
                if (result == Stats.INCORRECT) {
                    numError++;
                }
            }
            double percentError = Util.division(numError, validationSet.getSize());
            System.out.println("From test: " + testStats.getResult(Stats.INCORRECT) / (double) testStats.getTotal() + " x " + percentError);
            int newNumLeaves = treeSol.getTree().getRoot().getNumLeaves();

            //double new_resubstitution_cost = tree_sol.getTree().getTrainError() + Util.division(change_in_training_error, learning_set.getSize());
            treeSol.changeTrainError(changeInTrainingError);//setTrainingError(new_resubstitution_cost);
            //tree_sol.setError();
            double costComplexity = treeSol.getTrainError() + computedAlpha * (newNumLeaves);

            if (log.isDebugEnabled()) {
                log.debug(":sequence_trees:cost_complexity of selected tree " + costComplexity + "\n");
            }

            stats.setAlpha(computedAlpha);
            stats.setErrorEstimation(percentError);
            stats.setTrainError(treeSol.getTrainError());
            // Cost Complexity = Resubstitution Misclassification Cost + \alpha . Number of terminal nodes
            stats.setCostComplexity(costComplexity);
            stats.setNumTerminalNodes(newNumLeaves);
            treeSequenceStats.add(stats);
        }

        // memory optimized
        public void findCandidateNodes(TreeNode myNode, ArrayList<TreeNode> nodes) {

            if (myNode instanceof LeafNode) {

                //leaves.add((LeafNode) my_node);
                return;
            } else {
                // if you prune that one k more instances are misclassified

                int k         = numExtraMisClassIfPrun(myNode);
                int numLeaves = myNode.getNumLeaves();

                if (k == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug(":search_alphas:k == 0\n");
                    }
                }
                double numTrainingData = (double) treeSol.getList().getSize();
                double alpha           = ((double) k / numTrainingData) / ((double) (numLeaves - 1));
                if (log.isDebugEnabled()) {
                    log.debug(":search_alphas:alpha " + alpha + "/" + alphaProc.getAlpha() + " k " + k + " num_leaves " + numLeaves + " all " + treeSol.getList().getSize() + "\n");
                }

                //the_alpha = alpha_proc.check_node(alpha, the_alpha, my_node, nodes);
                alphaProc.checkNode(alpha, myNode, nodes);

                for (Object attributeValue : myNode.getChildrenKeys()) {
                    TreeNode child = myNode.getChild(attributeValue);
                    findCandidateNodes(child, nodes);
                    //nodes.pop();
                }
            }
            return;
        }

        public double getTheAlpha() {
            return alphaProc.getAlpha();
        }
    }

    public class AnAlphaProc implements AlphaSelectionProc {

        private double anAlpha;
        private double CART_EPSILON;

        public AnAlphaProc(double value, double epsilon) {
            anAlpha = value;
            CART_EPSILON = epsilon;
        }

        //		public AnAlphaProc(double value) {
        //			an_alpha = value;
        //		}
        public void initProc(double value) {
            // TODO ????
        }

        public double checkNode(double curAlpha, TreeNode curNode, ArrayList<TreeNode> nodes) {
            if (Util.epsilon(curAlpha - anAlpha, CART_EPSILON)) {
                for (TreeNode parent : nodes) {
                    if (isChildOf(curNode, parent)) {
                        return curAlpha;// it is not added
                    }
                }
                // add this one to the set
                nodes.add(curNode);
            }
            return curAlpha;
        }

        public double getAlpha() {
            return anAlpha;
        }
    }

    public class MinAlphaProc implements AlphaSelectionProc {

        private double sumMinAlpha, initMin;
        private int numMinimum;

        private double CART_EPSILON;

        public MinAlphaProc(double value, double epsilon) {
            initMin = value;
            sumMinAlpha = 0;
            numMinimum = 0;
            CART_EPSILON = epsilon;
        }

        public void initProc(double value) {
            initMin = value;
            sumMinAlpha = 0;
            numMinimum = 0;
        }

        public double checkNode(double curAlpha, TreeNode curNode, ArrayList<TreeNode> nodes) {
            double averageOfMinAlphas = getAlpha();
            if (log.isDebugEnabled()) {
                log.debug(":search_alphas:alpha " + curAlpha + "/" + averageOfMinAlphas + " diff " + (curAlpha - averageOfMinAlphas) + "\n");
            }

            if (Util.epsilon(curAlpha - averageOfMinAlphas, CART_EPSILON)) {
                // check if the cur_node is a child of any node that has been added to the list before.
                for (TreeNode parent : nodes) {
                    if (isChildOf(curNode, parent)) {
                        return curAlpha;// if it is the case do not add the node
                    }
                }
                // else add this one to the set
                nodes.add(curNode);
                sumMinAlpha += curAlpha;
                numMinimum++;
                return curAlpha;
            } else if (curAlpha < averageOfMinAlphas) {

                nodes.clear(); // can not put a new 'cause then it does not update the global one = new ArrayList<TreeNode>();
                // remove the ones you found and replace with that one
                //tree_sequence.get(dt_id).put(my_node), alpha
                numMinimum = 1;
                sumMinAlpha = curAlpha;
                nodes.add(curNode);
                return curAlpha;
            } else {

            }
            return sumMinAlpha / numMinimum;
        }

        public double getAlpha() {
            if (numMinimum == 0) {
                return initMin;
            } else {
                return sumMinAlpha / numMinimum;
            }
        }
    }

    public class NodeUpdate {

        private boolean stopTree;

        private DecisionTree tree;

        private TreeNode oldNode, nodeUpdate;

        private int iterationId;

        // to set an node update with the worst cross validated error
        public NodeUpdate(double error) {
            //stats = new TreeStats(error);
            //cross_validated_cost = error;

            stopTree = false;
            oldNode = null;
            nodeUpdate = null;
        }

        public NodeUpdate(TreeNode oldN, LeafNode newN) {
            stopTree = false;
            oldNode = oldN;
            nodeUpdate = newN;
        }

        public void iterationId(int i) {
            iterationId = i;
        }

        public void setDecisionTree(DecisionTree dt0) {
            tree = dt0;
        }

        public void setStopTree() {
            stopTree = true;
        }
    }
}
