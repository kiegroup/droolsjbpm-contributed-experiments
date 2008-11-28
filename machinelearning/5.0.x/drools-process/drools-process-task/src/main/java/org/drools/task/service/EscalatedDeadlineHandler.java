/**
 * 
 */
package org.drools.task.service;

import javax.persistence.EntityManager;

import org.drools.task.Deadline;
import org.drools.task.Task;

public interface EscalatedDeadlineHandler {
    public void executeEscalatedDeadline(Task task, Deadline deadline, EntityManager em, TaskService service);
}