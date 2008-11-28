package org.drools.bpel.core;

import java.util.List;

import org.drools.process.core.context.exception.ExceptionScope;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.node.CompositeContextNode;
import org.drools.workflow.core.node.CompositeNode;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.Split;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELScope extends CompositeContextNode implements BPELActivity, BPELFaultHandlerContainer {

    private static final long serialVersionUID = 400L;

	private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;
    private Join join;

    public BPELScope() {
	    VariableScope variableScope = new VariableScope();
	    addContext(variableScope);
	    setDefaultContext(variableScope);
	    join = new Join();
        join.setType(Join.TYPE_XOR);
        join.setMetaData("hidden", true);
        addNode(join);
        linkOutgoingConnections(join.getId(), Node.CONNECTION_DEFAULT_TYPE, Node.CONNECTION_DEFAULT_TYPE);	}
	
	public VariableScope getVariableScope() {
	    return (VariableScope) getDefaultContext(VariableScope.VARIABLE_SCOPE);
	}

    public void setActivity(BPELActivity activity) {
        addNode(activity);
        linkIncomingConnections(
            Node.CONNECTION_DEFAULT_TYPE,
            new CompositeNode.NodeAndType(
                activity, Node.CONNECTION_DEFAULT_TYPE));
        new ConnectionImpl(
    		activity, Node.CONNECTION_DEFAULT_TYPE,
    		join, Node.CONNECTION_DEFAULT_TYPE);
    }

    public void setFaultHandlers(List<BPELFaultHandler> faultHandlers) {
        ExceptionScope exceptionScope = new ExceptionScope();
        addContext(exceptionScope);
        setDefaultContext(exceptionScope);
        Split split = new Split();
        addNode(split);
        for (BPELFaultHandler faultHandler: faultHandlers) {
        	BPELFaultHandlerScope faultHandlerScope = new BPELFaultHandlerScope();
            addNode(faultHandlerScope);
        	Node activity = faultHandler.getActivity();
        	faultHandlerScope.addNode(activity);
        	faultHandlerScope.linkIncomingConnections(
    			Node.CONNECTION_DEFAULT_TYPE, activity.getId(), Node.CONNECTION_DEFAULT_TYPE);
        	faultHandlerScope.linkOutgoingConnections(
    			activity.getId(), Node.CONNECTION_DEFAULT_TYPE, Node.CONNECTION_DEFAULT_TYPE);
            exceptionScope.setExceptionHandler(faultHandler.getFaultName(), faultHandler);
            new ConnectionImpl(
        		split, Node.CONNECTION_DEFAULT_TYPE,
        		faultHandlerScope, Node.CONNECTION_DEFAULT_TYPE);
            new ConnectionImpl(
        		faultHandlerScope, Node.CONNECTION_DEFAULT_TYPE,
        		join, Node.CONNECTION_DEFAULT_TYPE);
        }
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
    
}
