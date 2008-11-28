package org.drools.bpel.instance;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.ode.utils.xsd.Duration;
import org.drools.bpel.core.BPELWait;
import org.drools.bpel.xpath.XPathReturnValueEvaluator;
import org.drools.process.core.timer.Timer;
import org.drools.process.instance.ProcessInstance;
import org.drools.spi.ProcessContext;
import org.drools.workflow.instance.NodeInstance;
import org.drools.workflow.instance.node.TimerNodeInstance;
import org.w3c.dom.Element;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELWaitInstance extends TimerNodeInstance {

    private static final long serialVersionUID = 400L;
    
    public BPELWait getBPELWait() {
    	return (BPELWait) getNode();
    }
    
    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
        	super.internalTrigger(from, type);
        }
    }
    
    protected Timer createTimer() {
    	Timer timer = new Timer();
    	BPELWait wait = getBPELWait();
    	if (wait.getForExpression() != null) {
    		timer.setDelay(getTimerDelayFor(wait.getForExpression()));
        } else if (wait.getUntilExpression() != null) {
            timer.setDelay(getTimerDelayUntil(wait.getUntilExpression()));
        }
    	timer.setPeriod(0);
    	return timer;
    }

    private long getTimerDelayFor(String forExpression) {
    	try {
	    	XPathReturnValueEvaluator evaluator = new XPathReturnValueEvaluator();
	        evaluator.setExpression(forExpression);
	        ProcessContext processContext = new ProcessContext();
	        processContext.setNodeInstance(this);
	        String literal = (String) evaluator.evaluate(
        		((ProcessInstance) getProcessInstance()).getWorkingMemory(), processContext, XPathConstants.STRING);
	        
	        Calendar cal = Calendar.getInstance();
	        Duration duration = new Duration(literal);
	        duration.addTo(cal);
	        long delay = cal.getTime().getTime() - Calendar.getInstance().getTime().getTime();
	        if (delay < 0) {
	        	delay = 0;
	        }
	        return delay;
    	} catch (Throwable t) {
    		throw new IllegalArgumentException(
				"Could not get timer delay for", t);
    	}
    }
    
    private long getTimerDelayUntil(String untilExpression) {
    	try {
			XPathReturnValueEvaluator evaluator = new XPathReturnValueEvaluator();
		    evaluator.setExpression(untilExpression);
		    ProcessContext processContext = new ProcessContext();
		    processContext.setNodeInstance(this);
		    List literal = (List) evaluator.evaluate(
	    		((ProcessInstance) getProcessInstance()).getWorkingMemory(), processContext, XPathConstants.NODESET);
			Calendar calendar = null;
		    if (literal.size() == 0) {
		        throw new IllegalArgumentException("No results for timer until");
		    }
		    if (literal.size() > 1) {
		        throw new IllegalArgumentException("Multiple results for timer until");
		    }
		    Object date = literal.get(0);
		    if (date instanceof Calendar) {
		    	calendar = (Calendar) date;
		    } else if (date instanceof Date) {
		        calendar = Calendar.getInstance();
		        calendar.setTime((Date) date);
		    } else if (date instanceof Element) {
				date = ((Element) date).getTextContent();
				// TODO
		    }
		    long delay = calendar.getTime().getTime() - Calendar.getInstance().getTime().getTime();
		    if (delay < 0) {
		    	delay = 0;
		    }
		    return delay;
    	} catch (Throwable t) {
    		throw new IllegalArgumentException(
				"Could not get timer delay until", t);
    	}
    }
    
    public void triggerCompleted() {
        super.triggerCompleted();
        BPELLinkManager.activateTargetLinks(this);
    }
}
