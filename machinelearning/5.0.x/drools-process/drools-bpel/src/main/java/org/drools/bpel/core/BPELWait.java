package org.drools.bpel.core;

import org.drools.workflow.core.node.TimerNode;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELWait extends TimerNode implements BPELActivity {

	private static final long serialVersionUID = 4L;

	private String forExpression;
	private String untilExpression;
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

	public void setForExpression(String forExpression) {
		this.forExpression = forExpression;
	}

	public void setUntilExpression(String untilExpression) {
		this.untilExpression = untilExpression;
	}

	public String getForExpression() {
		return forExpression;
	}

	public String getUntilExpression() {
		return untilExpression;
	}
	
}
