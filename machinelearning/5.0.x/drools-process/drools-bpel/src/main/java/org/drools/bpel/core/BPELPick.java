package org.drools.bpel.core;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.node.CompositeNode;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.Split;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELPick extends CompositeNode implements BPELActivity {

    private static final long serialVersionUID = 400L;

    private boolean createInstance;
    private Split split;
    private Join join;
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;

    public BPELPick() {
    	split = new Split();
    	split.setType(Split.TYPE_AND);
        split.setMetaData("hidden", true);
        addNode(split);
		join = new Join();
        join.setType(Join.TYPE_XOR);
        join.setMetaData("hidden", true);
        addNode(join);
        linkIncomingConnections(
            Node.CONNECTION_DEFAULT_TYPE,
            new CompositeNode.NodeAndType(
                split, Node.CONNECTION_DEFAULT_TYPE));
        linkOutgoingConnections(
            new CompositeNode.NodeAndType(
                join, Node.CONNECTION_DEFAULT_TYPE),
            Node.CONNECTION_DEFAULT_TYPE);
    }
    
    public boolean isCreateInstance() {
		return createInstance;
	}

	public void setCreateInstance(boolean createInstance) {
		this.createInstance = createInstance;
	}

	public void addOnMessage(OnMessage onMessage) {
    	BPELReceive receive = new BPELReceive();
    	receive.setOperation(onMessage.getPartnerLink(), onMessage.getPortType(), onMessage.getOperation());
    	receive.setMetaData("hidden", true);
    	receive.setVariable(onMessage.getVariable());
    	addNode(receive);
    	addNode(onMessage.getActivity());
        new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            receive, Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
            receive, Node.CONNECTION_DEFAULT_TYPE,
            onMessage.getActivity(), Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
    		onMessage.getActivity(), Node.CONNECTION_DEFAULT_TYPE,
            join, Node.CONNECTION_DEFAULT_TYPE);
    }
    
	public void addOnAlarm(OnAlarm onAlarm) {
		BPELWait waitNode = new BPELWait();
		waitNode.setForExpression(onAlarm.getForExpression());
		waitNode.setUntilExpression(onAlarm.getUntilExpression());
		waitNode.setMetaData("hidden", true);
		addNode(waitNode);
    	addNode(onAlarm.getActivity());
        new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            waitNode, Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
            waitNode, Node.CONNECTION_DEFAULT_TYPE,
            onAlarm.getActivity(), Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
    		onAlarm.getActivity(), Node.CONNECTION_DEFAULT_TYPE,
            join, Node.CONNECTION_DEFAULT_TYPE);
    }
	
    public SourceLink[] getSourceLinks() {
        return sourceLinks;
    }

    public void setSourceLinks(SourceLink[] sourceLinks) {
        this.sourceLinks = sourceLinks;
    }

    public TargetLink[] getTargetLinks() {
        return targetLinks;
    }

    public void setTargetLinks(TargetLink[] targetLinks) {
        this.targetLinks = targetLinks;
    }
    
    public class OnMessage {
    	
    	private String partnerLink;
    	private String portType;
    	private String operation;
    	private String variable;
    	private BPELCorrelation[] correlations;
    	private BPELActivity activity;
    	
		public String getPartnerLink() {
			return partnerLink;
		}
		
		public void setPartnerLink(String partnerLink) {
			this.partnerLink = partnerLink;
		}
		
		public String getPortType() {
			return portType;
		}
		
		public void setPortType(String portType) {
			this.portType = portType;
		}
		
		public String getOperation() {
			return operation;
		}
		
		public void setOperation(String operation) {
			this.operation = operation;
		}
		
		public String getVariable() {
			return variable;
		}
		
		public void setVariable(String variable) {
			this.variable = variable;
		}
		
		public BPELCorrelation[] getCorrelations() {
			return correlations;
		}
		
		public void setCorrelations(BPELCorrelation[] correlations) {
			this.correlations = correlations;
		}
    	
		public BPELActivity getActivity() {
			return activity;
		}
		
		public void setActivity(BPELActivity activity) {
			this.activity = activity;
		}
    	
    }
    
    public class OnAlarm {
    	
    	private String forExpression;
    	private String untilExpression;
    	private BPELActivity activity;
    	
		public String getForExpression() {
			return forExpression;
		}
		
		public void setForExpression(String forExpression) {
			this.forExpression = forExpression;
		}
		
		public String getUntilExpression() {
			return untilExpression;
		}
		
		public void setUntilExpression(String untilExpression) {
			this.untilExpression = untilExpression;
		}
		
		public BPELActivity getActivity() {
			return activity;
		}
		
		public void setActivity(BPELActivity activity) {
			this.activity = activity;
		}
    	
    }

}
