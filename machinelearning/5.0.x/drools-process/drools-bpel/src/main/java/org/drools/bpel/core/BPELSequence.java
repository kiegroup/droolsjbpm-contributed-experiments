package org.drools.bpel.core;

import java.util.List;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.node.CompositeNode;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELSequence extends CompositeNode implements BPELActivity {
    
    private static final long serialVersionUID = 400L;

    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;
    
    public void setActivities(List<BPELActivity> activities) {
        if (activities == null || activities.size() < 1) {
            throw new IllegalArgumentException(
                "A BPEL sequence must contain at least one activity!");
        }
        BPELActivity previous = activities.get(0);
        addNode(previous);
        linkIncomingConnections(
            Node.CONNECTION_DEFAULT_TYPE,
            new CompositeNode.NodeAndType(
                    previous, Node.CONNECTION_DEFAULT_TYPE));
        for (int i = 1; i < activities.size(); i++ ) {
            BPELActivity next = activities.get(i);
            addNode(next);
            new ConnectionImpl(
                previous, Node.CONNECTION_DEFAULT_TYPE,
                next, Node.CONNECTION_DEFAULT_TYPE);
            previous = next;
        }
        linkOutgoingConnections(
            new CompositeNode.NodeAndType(
                previous, Node.CONNECTION_DEFAULT_TYPE),
            Node.CONNECTION_DEFAULT_TYPE);
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
