package org.drools.jpdl.instance.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.drools.jpdl.core.node.TaskNode;
import org.drools.process.core.context.swimlane.SwimlaneContext;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemListener;
import org.drools.process.instance.WorkItemManager;
import org.drools.process.instance.context.swimlane.SwimlaneContextInstance;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.workflow.instance.NodeInstance;
import org.jbpm.JbpmException;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Event;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.def.TaskController;

public class TaskNodeInstance extends JpdlNodeInstance implements WorkItemListener {

	private static final long serialVersionUID = 1L;
	
    private List<WorkItemImpl> workItems = new ArrayList<WorkItemImpl>();
    
	public TaskNode getTaskNode() {
		return (TaskNode) getNode();
	}

	public void execute(NodeInstance from, String type) {
		Set<Task> tasks = getTaskNode().getTasks();
		if ((getTaskNode().isCreateTasks()) && (tasks != null)) {
			addEventListeners();
			for (Task task: tasks) {
				if (evaluateTaskCondition(task.getCondition())) {
					WorkItemManager workItemManager = getProcessInstance().getWorkingMemory().getWorkItemManager();
					WorkItemImpl workItem = new WorkItemImpl();
					workItem.setName("JpdlTask");
					workItem.setProcessInstanceId(getProcessInstance().getId());
					workItem.setParameter("name", task.getName());
					String description = task.getDescription();
					workItem.setParameter("signalling", task.isSignalling());
					workItem.setParameter("blocking", task.isBlocking());
					if ((description != null) && (description.indexOf("#{") != -1)) {
						Object result = JbpmExpressionEvaluator.evaluate(
							description, new JpdlExecutionContext());
						if (result != null) {
							description = result.toString();
						}
					}
					workItem.setParameter("Description", description);
					initializeVariables(workItem, task);
					if (task.getDueDate() != null) {
					    BusinessCalendar businessCalendar = new BusinessCalendar();
					    workItem.setParameter("dueDate", 
				            businessCalendar.add(new Date(), new Duration(task.getDueDate())));
				    }
					if (task.getSwimlane() != null) {
					    String swimlaneName = task.getSwimlane().getName();
					    SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance)
					        resolveContextInstance(SwimlaneContext.SWIMLANE_SCOPE, swimlaneName);
					    String actorId = swimlaneContextInstance.getActorId(swimlaneName);
					    if (actorId == null) {
					        actorId = assignTask(task);
					    }
					    workItem.setParameter("ActorId", actorId);
					}
					workItems.add(workItem);
					Event event = task.getEvent(Event.EVENTTYPE_TASK_CREATE);
			        if (event != null) {
			            // TODO this doesn't take event handlers of task itself
			            // into account
			            executeActions(event.getActions(), new JpdlTaskExecutionContext(task));
			        }
					workItemManager.internalExecuteWorkItem(workItem);
				}
			}
		}
		boolean continueExecution = false;
		switch (getTaskNode().getSignal()) {
			case org.jbpm.graph.node.TaskNode.SIGNAL_UNSYNCHRONIZED:
				continueExecution = true;
				break;
			case org.jbpm.graph.node.TaskNode.SIGNAL_FIRST_WAIT:
			case org.jbpm.graph.node.TaskNode.SIGNAL_LAST_WAIT:
			case org.jbpm.graph.node.TaskNode.SIGNAL_NEVER:
				continueExecution = false;
				break;
			case org.jbpm.graph.node.TaskNode.SIGNAL_FIRST:
			case org.jbpm.graph.node.TaskNode.SIGNAL_LAST:
				continueExecution = !hasSignallingWorkItems();
		}
		if (continueExecution) {
			leave();
		}
	}

	private boolean evaluateTaskCondition(String condition) {
		if (condition == null) {
			return true;
		}
		Object result = JbpmExpressionEvaluator.evaluate(
			condition, new JpdlExecutionContext());
		if (Boolean.TRUE.equals(result)) {
			return true;
		}
		return false;
	}
	
	private void initializeVariables(WorkItemImpl workItem, Task task) {
		TaskController taskController = task.getTaskController();
		if (taskController != null) {
			Delegation taskControllerDelegation = taskController.getTaskControllerDelegation();
		    if (taskControllerDelegation != null) {
		    	// TODO: delegation (API mismatch!)
		    } else {
		    	List<VariableAccess> variableAccesses = taskController.getVariableAccesses();
		    	if (variableAccesses != null) {
		    		for (VariableAccess variableAccess: variableAccesses) {
		    			String mappedName = variableAccess.getMappedName();
		    			if (variableAccess.isReadable()) {
		    				String variableName = variableAccess.getVariableName();
		    				VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
		    					resolveContextInstance(VariableScope.VARIABLE_SCOPE, variableName);
		    				Object value = variableScopeInstance.getVariable(variableName);
		    				workItem.setParameter(mappedName, value);
		    			}
			        }
		    	}
		    }
		}
	}
	
	private String assignTask(Task task) {
	    Event event = task.getEvent(Event.EVENTTYPE_TASK_ASSIGN);
        if (event != null) {
            executeActions(event.getActions(), new JpdlTaskExecutionContext(task));
        }
	    if (task.getActorIdExpression() != null) {
            return resolveActor(task.getActorIdExpression());
	    } else if (task.getSwimlane().getActorIdExpression() != null) {
	        return resolveActor(task.getSwimlane().getActorIdExpression());
	    }
	    // TODO support other assignment types
	    return null;
	}
	
	private String resolveActor(String expression) {
	    Object result = JbpmExpressionEvaluator.evaluate(expression, new JpdlExecutionContext());
        if (result == null) {
            throw new JbpmException("actor-id expression '" + expression + "' returned null");
        }
        if (result instanceof String) {
            return (String) result;
        } else {
            throw new JbpmException(
                "actor-id expression '" + expression + 
                "' didn't resolve to a java.lang.String: '" + result + 
                "' (" + result.getClass().getName() + ")");
        }
	}
	
	private boolean hasSignallingWorkItems() {
	    for (WorkItem workItem: workItems) {
	        if ((Boolean) workItem.getParameter("signalling") == true) {
	            return true;
	        }
	    }
	    return false; 
	}

    private boolean hasBlockingWorkItems() {
        for (WorkItem workItem: workItems) {
            if ((Boolean) workItem.getParameter("blocking") == true) {
                return true;
            }
        }
        return false; 
    }

	private void restoreVariables(WorkItemImpl workItem, Task task) {
		TaskController taskController = task.getTaskController();
		if (taskController != null) {
			Delegation taskControllerDelegation = taskController.getTaskControllerDelegation();
			if (taskControllerDelegation != null) {
				// TODO: delegation (API mismatch!)
		    } else {
		    	List<VariableAccess> variableAccesses = taskController.getVariableAccesses();
		    	if (variableAccesses != null) {
			        String missingTaskVariables = null;
		    		for (VariableAccess variableAccess: variableAccesses) {
		    			String mappedName = variableAccess.getMappedName();
		    			Object value = workItem.getParameter(mappedName);
		    			if (variableAccess.isRequired() && (value != null)) {
		    				if (missingTaskVariables == null) {
		    					missingTaskVariables = mappedName;
		    				} else {
		    					missingTaskVariables += ", "+mappedName;
		    				}
		    			}
		    		}
			        if (missingTaskVariables != null) {
			        	throw new IllegalArgumentException(
		        			"missing task variables: " + missingTaskVariables);
			        }

			        for (VariableAccess variableAccess: variableAccesses) {
			        	String mappedName = variableAccess.getMappedName();
			        	String variableName = variableAccess.getVariableName();
			        	if (variableAccess.isWritable()) {
			        		Object value = workItem.getParameter(mappedName);
			        		if (value != null) {
			        			VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
		    						resolveContextInstance(VariableScope.VARIABLE_SCOPE, variableName);
			        			variableScopeInstance.setVariable(variableName, value);
			        		}
			        	}
			        }
		    	}
		    }
		}
	}

    public void addEventListeners() {
        getProcessInstance().addWorkItemListener(this);
    }
    
    public void removeEventListeners() {
        getProcessInstance().removeWorkItemListener(this);
    }

    public void workItemAborted(WorkItem workItem) {
        if (workItems.remove(workItem)) {
            if (!hasBlockingWorkItems()) {
                removeEventListeners();
            	leave();
            }
        }
    }

    public void workItemCompleted(WorkItem workItem) {
    	if (workItems.remove(workItem)) {
    		String taskName = (String) workItem.getParameter("name");
    		Set<Task> tasks = getTaskNode().getTasks();
			for (Task task: tasks) {
				if (taskName.equals(task.getName())) {
		    		restoreVariables((WorkItemImpl) workItem, task);
		    		if (task.getSwimlane() != null) {
		    		    String swimlaneName = task.getSwimlane().getName();
		    		    SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance)
                            resolveContextInstance(SwimlaneContext.SWIMLANE_SCOPE, swimlaneName);
		    		    if (swimlaneContextInstance.getActorId(swimlaneName) == null) {
		    		        String actorId = (String) workItem.getResult("ActorId");
		    		        if (actorId != null) {
		    		            swimlaneContextInstance.setActorId(swimlaneName, 
	    		                    (String) workItem.getResult("ActorId"));
		    		        }
		    		    }
		    		}
	                Event event = task.getEvent(Event.EVENTTYPE_TASK_END);
	                if (event != null) {
	                    executeActions(event.getActions(), new JpdlTaskExecutionContext(task));
	                }
		            break;
				}
    		}
            if (!hasBlockingWorkItems()) {
                removeEventListeners();
                String result = (String) workItem.getResult("Result");
                if (result != null) {
                    leave(result);
                } else {
                    leave();
                }
            }
        }
    }
    
    public WorkItem findWorkItem(Task task) {
        for (WorkItem workItem: workItems) {
            if (task.getName().equals(workItem.getName())) {
                return workItem;
            }
        }
        return null;
    }

	public void leave(String type) {
		if (hasBlockingWorkItems()) {
			throw new IllegalStateException("task-node '"
				+ getNode().getName() + "' still has blocking tasks");
		}
		if (getTaskNode().isEndTasks()) {
			for (WorkItem workItem: workItems) {
				getProcessInstance().getWorkingMemory().getWorkItemManager()
					.internalAbortWorkItem(workItem.getId());
			}
		}
		super.leave(type);
	}
	
	public class JpdlTaskExecutionContext extends JpdlExecutionContext {
	    private Task task;
	    public JpdlTaskExecutionContext(Task task) {
	        this.task = task;
	    }
	    public Task getTask() {
	        return task;
	    }
	}
}
