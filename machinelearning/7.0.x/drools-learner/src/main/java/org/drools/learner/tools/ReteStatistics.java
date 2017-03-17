package org.drools.learner.tools;

// @mireynol - this can probably be removed and replaced with 
//  org.drools.core.util.debug.SessionInspector

//import java.util.ArrayList;
//import java.util.HashSet;
//
//import org.drools.RuleBase;
//import org.drools.common.AbstractRuleBase;
//import org.drools.learner.StatsPrinter;
//import org.drools.learner.eval.stopping.StoppingCriterion;
//import org.drools.reteoo.AlphaNode;
//import org.drools.reteoo.InitialFactImpl;
//import org.drools.reteoo.JoinNode;
//import org.drools.reteoo.LeftTupleSink;
//import org.drools.reteoo.LeftTupleSource;
//import org.drools.reteoo.ObjectSink;
//import org.drools.reteoo.ObjectSource;
//import org.drools.reteoo.ObjectTypeNode;
//import org.drools.reteoo.Rete;
//import org.drools.reteoo.ReteooRuleBase;
//import org.drools.reteoo.RuleTerminalNode;
//
public class ReteStatistics {
//	private String[] nodes = {"OBJECT_TYPE_NODE", "ALPHA_NODE", "BETA_NODE", "TERMINAL_NODE"};
//	
//	private final int OBJECT_TYPE_NODE_INDEX = 0;
//	private final int ALPHA_NODE_INDEX = 1;
//	private final int BETA_NODE_INDEX = 2;
//	private final int TERMINAL_NODE_INDEX = 3;
//	
//	private int []num_of_nodes;
//	private ReteooRuleBase reteRuleBase;
//	
//	private HashSet<Object> visitedNodes;
//	public ReteStatistics(RuleBase ruleBase) {
//		reteRuleBase = (ReteooRuleBase)ruleBase;
//		
//		num_of_nodes = new int[TERMINAL_NODE_INDEX + 1];
//		visitedNodes = new HashSet<Object>();
//	}
//	
//	public void calculateNumberOfNodes() {
//		
////		AbstractRuleBase aruleBase = (AbstractRuleBase)ruleBase;
////        ReteooRuleBase reteRuleBase = (ReteooRuleBase)ruleBase;
//        Rete root = reteRuleBase.getRete();   
//        //List<ObjectTypeNode> objectTypesNodes = root.getObjectTypeNodes();
//        //int num_object_type_nodes =0;
//        for (ObjectTypeNode o_type_node : root.getObjectTypeNodes()) {
//        	
//        	//ObjectSink[] objectSinks = otn.getSinkPropagator().getSinks();
// //       	System.out.println("calculateNumberOfNodes:ObjectTypeNode "+ o_type_node);
////        	if (o_type_node.getObjectType().matches(InitialFactImpl.class))
////        		continue;
//        	//num_object_type_nodes ++;
//        	recurseObjectSource(o_type_node);
//        }
//        
////        System.out.println("num_object_type_nodes "+ num_of_nodes[OBJECT_TYPE_NODE_INDEX]);
//        //num_of_nodes[OBJECT_TYPE_NODE_INDEX] = num_object_type_nodes;
//
//	}
//	
//	public void recurseObjectSource(ObjectSource o_source) {
//		
////		System.out.println("recurseObjectSource:1stObjectSource "+o_source);
//		if (o_source.getSinkPropagator().getSinks().length != 0 && o_source instanceof ObjectTypeNode) {
//			if (!visitedNodes.contains(o_source)) {
//				visitedNodes.add(o_source);
//				num_of_nodes[OBJECT_TYPE_NODE_INDEX] ++;
//			}
//		}
//		if (o_source instanceof AlphaNode) {
//			if (!visitedNodes.contains(o_source)) {
//				visitedNodes.add(o_source);
//				num_of_nodes[ALPHA_NODE_INDEX] ++;
//			}
//			
//		}
//		
//
//		for (ObjectSink o_sink: o_source.getSinkPropagator().getSinks()) {
////			System.out.println("recurseObjectSource:iterate:ObjectSink "+o_sink);
//    		if (o_sink instanceof ObjectSource) {
//    			ObjectSource new_os = (ObjectSource) o_sink;
////    			System.out.println("recurseObjectSource:2ndObjectSource "+new_os);
//    			recurseObjectSource(new_os);
//    			
//    		} else if (o_sink instanceof LeftTupleSource) {
//    			LeftTupleSource new_lts = (LeftTupleSource) o_sink;
// //   			System.out.println("recurseObjectSource:LeftTupleSource "+new_lts);
//    			recurseLeftTupleSource(new_lts);
//			} else {
//				System.out.println("System.exitrecurseObjectSource: what is it "+o_sink);
//    			System.exit(0);
//				
//			}
//    	}//
//	}
//	public void recurseLeftTupleSource(LeftTupleSource lt_source) {
//		
//		
//		for (LeftTupleSink lt_sink: lt_source.getSinkPropagator().getSinks()) {
////			System.out.println("recurseLeftTupleSource:LeftTupleSink "+lt_sink);
//			//lt_sink.
//    		if (lt_sink instanceof RuleTerminalNode) {
//    			RuleTerminalNode new_rtn = (RuleTerminalNode) lt_sink;
//    			//num_of_nodes[TERMINAL_NODE_INDEX] ++;
//    			
//    			if (!visitedNodes.contains(lt_sink)) {
//    				visitedNodes.add(lt_sink);
//    				num_of_nodes[TERMINAL_NODE_INDEX] ++;
//    			}
//  //  			System.out.println("recurseLeftTupleSource:RuleTerminalNode "+new_rtn + " terminal");
//    			return;
//    			//recurseObjectSource(new_os);
//    			
//    		} else if (lt_sink instanceof JoinNode) {
//    			JoinNode new_join = (JoinNode) lt_sink;
//    			if (!visitedNodes.contains(lt_sink)) {
//    				visitedNodes.add(lt_sink);
//    				num_of_nodes[BETA_NODE_INDEX] ++;
//    			}
//    			//num_of_nodes[BETA_NODE_INDEX] ++;
//    			recurseLeftTupleSource(new_join);
//			} else {
//    			//LeftTupleSource new_lts = (LeftTupleSource) lt_sink;
//    			System.out.println("System.exitrecurseLeftTupleSource: what is it "+lt_sink);
//    			System.exit(0);
//    			
//			}
//    	}
//	}
//
//	public void print() {
//		for (int i: num_of_nodes)
//			System.out.print(i +"\t");
//		
//	}
//	
//	public void print(String executionSignature) {
//		StringBuffer sb = new StringBuffer();
//
//		for (int i=0; i< num_of_nodes.length; i++) {
//			int num =num_of_nodes[i];
//			sb.append("\t&\t" +nodes[i]);
//		}
//		sb.append("\n");
//		for (int i=0; i< num_of_nodes.length; i++) {
//			int num =num_of_nodes[i];
//			sb.append("\t&\t" +num + "");
//		}
//		sb.append("\\\\\n");
//
//
//		StatsPrinter.print2file(sb, executionSignature, true);
//		
//	}


}
