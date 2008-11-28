package org.drools.bpel.core;

import org.drools.workflow.core.node.EndNode;

public class BPELExit extends EndNode implements BPELActivity {

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
