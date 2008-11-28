package org.drools.bpel.core;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.impl.ConstraintImpl;
import org.drools.workflow.core.node.CompositeNode;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.Split;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELIf extends CompositeNode implements BPELActivity {

    private static final long serialVersionUID = 400L;

    private Split split;
    private Join join;
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;

    public BPELIf() {
    	split = new Split();
        split.setType(Split.TYPE_XOR);
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
        ConnectionImpl connection = new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            join, Node.CONNECTION_DEFAULT_TYPE);
        ConstraintImpl constraint = new ConstraintImpl();
        constraint.setConstraint("true");
        constraint.setType("code");
        constraint.setDialect("mvel");
        constraint.setPriority(Integer.MAX_VALUE - 1);
        split.setConstraint(connection, constraint);
    }
    
    public void addCase(String expression, BPELActivity activity) {
    	addNode(activity);
        ConnectionImpl connection = new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            activity, Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
            activity, Node.CONNECTION_DEFAULT_TYPE,
            join, Node.CONNECTION_DEFAULT_TYPE);
        ConstraintImpl constraint = new ConstraintImpl();
        constraint.setConstraint(expression == null ? "true" : expression);
        constraint.setType("code");
        constraint.setDialect(expression == null ? "mvel" : "XPath2.0");
        constraint.setPriority(getNodes().length - 2);
        split.setConstraint(connection, constraint);
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
