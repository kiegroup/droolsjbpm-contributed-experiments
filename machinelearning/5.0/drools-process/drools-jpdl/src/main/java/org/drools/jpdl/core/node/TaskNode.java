package org.drools.jpdl.core.node;

import java.util.Set;

import org.jbpm.taskmgmt.def.Task;

public class TaskNode extends JpdlNode {

	private static final long serialVersionUID = 1L;

	private Set<Task> tasks;
	private int signal = org.jbpm.graph.node.TaskNode.SIGNAL_LAST;
	private boolean createTasks = true;
	private boolean endTasks = false;
	
	public Set<Task> getTasks() {
		return tasks;
	}
	
	public void setTasks(Set<Task> tasks) {
		this.tasks = tasks;
	}
	
	public int getSignal() {
		return signal;
	}
	
	public void setSignal(int signal) {
		this.signal = signal;
	}
	
	public boolean isCreateTasks() {
		return createTasks;
	}
	
	public void setCreateTasks(boolean createTasks) {
		this.createTasks = createTasks;
	}
	
	public boolean isEndTasks() {
		return endTasks;
	}
	
	public void setEndTasks(boolean endTasks) {
		this.endTasks = endTasks;
	}
	  
}
