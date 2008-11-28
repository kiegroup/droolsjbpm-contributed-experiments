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
public class BPELWhile extends CompositeNode implements BPELActivity {

    private static final long serialVersionUID = 400L;

    private Join join;
    private Split split;
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;

    public BPELWhile() {
        join = new Join();
        join.setType(Join.TYPE_XOR);
        join.setMetaData("hidden", true);
        addNode(join);
    	split = new Split();
        split.setType(Split.TYPE_XOR);
        split.setMetaData("hidden", true);
        addNode(split);
        BPELEmpty empty = new BPELEmpty();
        empty.setMetaData("hidden", true);
        addNode(empty);
        linkIncomingConnections(
            Node.CONNECTION_DEFAULT_TYPE,
            new CompositeNode.NodeAndType(
                join, Node.CONNECTION_DEFAULT_TYPE));
        linkOutgoingConnections(
            new CompositeNode.NodeAndType(
                empty, Node.CONNECTION_DEFAULT_TYPE),
            Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
            join, Node.CONNECTION_DEFAULT_TYPE,
            split, Node.CONNECTION_DEFAULT_TYPE);
        ConnectionImpl connection = new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            empty, Node.CONNECTION_DEFAULT_TYPE);
        ConstraintImpl constraint = new ConstraintImpl();
        constraint.setConstraint("true");
        constraint.setType("code");
        constraint.setDialect("mvel");
        constraint.setPriority(Integer.MAX_VALUE - 1);
        split.setConstraint(connection, constraint);
    }
    
    public void setActivity(String condition, BPELActivity activity) {
    	addNode(activity);
        ConnectionImpl connection = new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            activity, Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
            activity, Node.CONNECTION_DEFAULT_TYPE,
            join, Node.CONNECTION_DEFAULT_TYPE);
        ConstraintImpl constraint = new ConstraintImpl();
        constraint.setConstraint(condition);
        constraint.setType("code");
        constraint.setDialect("XPath2.0");
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
