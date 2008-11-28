package org.drools.bpel.core;

import org.drools.workflow.core.node.FaultNode;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELThrow extends FaultNode implements BPELActivity {

	private static final long serialVersionUID = 4L;

    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;

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
