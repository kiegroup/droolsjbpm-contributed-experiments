/**
 * 
 */
package org.drools.task.service;

import org.drools.task.service.TaskClientHandler.AddTaskResponseHandler;

public class BlockingAddTaskResponseHandler extends AbstractBlockingResponseHandler implements AddTaskResponseHandler {
    private volatile long taskId;

    public synchronized void execute(long taskId) {
        synchronized ( this.done ) {        
            this.taskId = taskId;
            this.done = true;
            notifyAll(); 
        }
    }
    
    public synchronized long getTaskId() {
        boolean isDone;
        synchronized ( done ) {
            isDone = this.done;
        }
        if ( !isDone ) {                  
            try {
                wait( 10000 );
            } catch ( InterruptedException e ) {
                // swallow as this is just a notification
            }
        }        
        synchronized ( done ) {
            isDone = this.done;
        }        
        if ( !isDone ) {
            throw new RuntimeException("Timeout : unable to retrieve Task Id" );
        }
        
        return taskId;
    }       
}