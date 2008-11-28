package org.drools.bpel.core;

import org.drools.workflow.core.Node;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public interface BPELActivity extends Node {
	
	// TODO
	// String getJoinCondition();
	// void setJoinCondition(String joinCondition);
	
	// TODO
	// boolean isSuppressJoinFailure();
	// void setSuppressJoinFailure(boolean suppressJoinFailure);

    SourceLink[] getSourceLinks();
    
    void setSourceLinks(SourceLink[] sourceLinks);
    
    TargetLink[] getTargetLinks();
    
    void setTargetLinks(TargetLink[] targetLinks);
    
    public class SourceLink {
    	
    	private String linkName;
    	private String transitionCondition;
    	
		public SourceLink(String linkName) {
			this.linkName = linkName;
		}
		
		public String getLinkName() {
			return linkName;
		}
		
		public String getTransitionCondition() {
			return transitionCondition;
		}
		
		public void setTransitionCondition(String transitionCondition) {
			this.transitionCondition = transitionCondition;
		}
		
    }
    
    public class TargetLink {
    	
    	private String linkName;
    	
    	public TargetLink(String linkName) {
    		this.linkName = linkName;
    	}

		public String getLinkName() {
			return linkName;
		}

    }
    
}
