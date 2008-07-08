package org.drools.jpdl.instance.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.jpdl.core.JpdlConnection;
import org.drools.jpdl.core.node.JpdlNode;
import org.drools.process.core.context.exception.ExceptionScope;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.core.timer.Timer;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.context.exception.ExceptionScopeInstance;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.process.instance.timer.TimerListener;
import org.drools.workflow.core.Node;
import org.drools.workflow.instance.NodeInstance;
import org.drools.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.JbpmException;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.ExceptionHandler;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.scheduler.def.CancelTimerAction;
import org.jbpm.scheduler.def.CreateTimerAction;

public class JpdlNodeInstance extends NodeInstanceImpl implements TimerListener {

	private static final long serialVersionUID = 1L;
	private static final BusinessCalendar BUSINESS_CALENDAR = new BusinessCalendar();
	
    private Map<Long, Action> timerActions = new HashMap<Long, Action>();
	private Map<String, List<Timer>> timers = new HashMap<String, List<Timer>>();
	
	public JpdlNode getJpdlNode() {
		return (JpdlNode) getNode();
	}
	
	private Action getAction() {
		return getJpdlNode().getAction();
	}

	public void internalTrigger(NodeInstance from, String type) {
		fireEvent(Event.EVENTTYPE_NODE_ENTER);
		execute(from, type);
	}
	
	public void execute(NodeInstance from, String type) {
	    Action action = getAction();
        if (action != null) {
            try {
                action.execute(new JpdlExecutionContext());
            } catch (Exception exception) {
                raiseException(exception);
            }
        } else {
            leave();
        }
    }

	public void leave() {
		if (getNode().getOutgoingConnections(Node.CONNECTION_DEFAULT_TYPE) != null) {
			leave(Node.CONNECTION_DEFAULT_TYPE);
		} else if (getNode().getOutgoingConnections().size() == 1) {
			String type = getNode().getOutgoingConnections().keySet().iterator().next() ;
			leave(type);
		} else {
			throw new IllegalArgumentException(
				"Could not find default leave transition: " + this);
		}
	}

	public void leave(String type) {
		JpdlConnection connection = (JpdlConnection)
		    getJpdlNode().getOutgoingConnection(type);
		if (connection == null) {
			throw new JbpmException("transition '" + type
				+ "' is not a leaving transition of node '" + this + "'");
		}
		fireEvent(Event.EVENTTYPE_NODE_LEAVE);
        getNodeInstanceContainer().removeNodeInstance(this);
        Event event = connection.getEvent(Event.EVENTTYPE_TRANSITION);
        if (event != null) {
            List<Action> actions = event.getActions();
            if (actions != null) {
                for (Action action: actions) {
                    try {
                        action.execute(new JpdlExecutionContext());
                    } catch (Exception exception) {
                        boolean handled = false;
                        List<ExceptionHandler> exceptionHandlers = connection.getExceptionHandlers();
                        try {
                            for (ExceptionHandler exceptionHandler: exceptionHandlers) {
                                if (exceptionHandler.matches(exception)) {
                                    exceptionHandler.handleException(new JpdlExecutionContext());
                                    handled = true;
                                }
                            }
                        } catch (Exception e) {
                            exception = e;
                        }
                        if (!handled) {
                            if (exception instanceof JbpmException) {
                                throw (JbpmException) exception;
                            } else {
                                throw new DelegationException(exception, null);
                            }
                        }
                    }
                }
            }
        }
        String condition = connection.getCondition();
        if (condition != null) {
            Object result = JbpmExpressionEvaluator.evaluate(
                condition, new JpdlExecutionContext());
            if (result == null) {
                throw new JbpmException("connection condition " + condition
                        + " evaluated to null");
            } else if (!(result instanceof Boolean)) {
                throw new JbpmException("connection condition " + condition
                        + " evaluated to non-boolean: "
                        + result.getClass().getName());
            } else if (!((Boolean) result).booleanValue()) {
                throw new JbpmException("connection condition " + condition
                        + " evaluated to 'false'");
            }
        }
        getNodeInstanceContainer().getNodeInstance(connection.getTo())
            .trigger(this, connection.getToType());
	}

    public void fireEvent(String eventType) {
        fireEvent(eventType, new JpdlExecutionContext());
    }

	public void fireEvent(String eventType, ExecutionContext executionContext) {
		Event event = getJpdlNode().getEvent(eventType);
		if (event != null) {
		    executeActions(event.getActions(), executionContext);
		}
		// TODO runtime actions?
	}

    public void executeActions(List<Action> actions, ExecutionContext executionContext) {
        if (actions != null) {
            for (Action action: actions) {
                executeAction(action, executionContext);
            }
        }
    }

	public void executeAction(Action action, ExecutionContext executionContext) {
	    if (action instanceof CreateTimerAction) {
	        CreateTimerAction createTimerAction = (CreateTimerAction) action; 
	        String timerName = createTimerAction.getTimerName();
	        Timer timer = new Timer();
	        long delay = BUSINESS_CALENDAR.add(new Date(0),
                new Duration(createTimerAction.getDueDate())).getTime();
            timer.setDelay(delay);
            if (createTimerAction.getRepeat() != null) {
                long period = BUSINESS_CALENDAR.add(new Date(0),
                    new Duration(createTimerAction.getRepeat())).getTime();
                timer.setPeriod(period);
            }
	        if (timerActions.isEmpty()) {
	            registerTimerListener();
	        }
	        getProcessInstance().getWorkingMemory().getTimerManager()
	            .registerTimer(timer, getProcessInstance());
	        timerActions.put(timer.getId(), createTimerAction.getTimerAction());
	        List<Timer> timerList = timers.get(timerName);
	        if (timerList == null) {
	            timerList = new ArrayList<Timer>();
	            timers.put(timerName, timerList);
	        }
	        timerList.add(timer);
	    } else if (action instanceof CancelTimerAction) {
	        String timerName = ((CancelTimerAction) action).getTimerName();
	        List<Timer> timerList = timers.get(timerName);
	        if (timerList != null) {
	            for (Timer timer: timerList) {
	                timerActions.remove(timer.getId());
	                getProcessInstance().getWorkingMemory().getTimerManager()
	                    .cancelTimer(timer);
	            }
                timers.remove(timerName);
                if (timerActions.isEmpty()) {
                    removeTimerListener();
                }
	        }
	    } else {
    		try {
    			action.execute(executionContext);
    		} catch (Exception exception) {
    			raiseException(exception);
    		}
	    }
	}

	public void raiseException(Throwable exception) throws DelegationException {
		Class<?> clazz = exception.getClass();
		while (clazz != null) {
			ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance)
				resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, clazz.getName());
			if (exceptionScopeInstance != null) {
				exceptionScopeInstance.handleException(clazz.getName(), exception);
				return;
			}
			clazz = clazz.getSuperclass();
		}
		if (exception instanceof JbpmException) {
			throw (JbpmException) exception;
		} else {
			throw new DelegationException(exception, null);
		}
	}
	
    public void registerTimerListener() {
        getProcessInstance().addTimerListener(this);
    }
    
    public void removeTimerListener() {
        getProcessInstance().removeTimerListener(this);
    }

    public void timerTriggered(Timer timer) {
        timerTriggered(timer, new JpdlExecutionContext());
    }
    
    protected void timerTriggered(Timer timer, ExecutionContext executionContext) {
        Action action = timerActions.get(timer.getId());
        if (action != null) {
            executeAction(action, executionContext);
        }
    }

	public class JpdlExecutionContext extends ExecutionContext {
	    public JpdlExecutionContext() {
	        super((Token) null);
	    }
	    public void leaveNode() {
	        leave();
	    }
	    public void leaveNode(String transitionName) {
	        leave(transitionName);
	    }
	    public void leaveNode(Transition transition) {
	        leaveNode(transition.getName());
	    }
	    public void setVariable(String name, Object value) {
	        VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
	            resolveContextInstance(VariableScope.VARIABLE_SCOPE, name);
	        variableScopeInstance.setVariable(name, value);
        }
        public Object getVariable(String name) {
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
                resolveContextInstance(VariableScope.VARIABLE_SCOPE, name);
            return variableScopeInstance.getVariable(name);
        }
        public ProcessInstance getDroolsProcessInstance() {
            return JpdlNodeInstance.this.getProcessInstance();
        }
        public NodeInstance getDroolsNodeInstance() {
            return JpdlNodeInstance.this;
        }
	}

}
