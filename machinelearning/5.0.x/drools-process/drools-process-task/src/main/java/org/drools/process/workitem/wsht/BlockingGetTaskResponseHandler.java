/**
 * 
 */
package org.drools.process.workitem.wsht;

import org.drools.task.Task;
import org.drools.task.service.TaskClientHandler.GetTaskResponseHandler;

public class BlockingGetTaskResponseHandler extends AbstractBlockingResponseHandler implements GetTaskResponseHandler {
    private volatile Task task;

    public synchronized void execute(Task task) {
        synchronized ( this.done ) {        
            this.task = task;
            this.done = true;
            notifyAll();     
        }
    }
    
    public synchronized Task getTask() {
        if ( task == null ) {                  
            try {
                wait( 10000 );
            } catch ( InterruptedException e ) {
                // swallow as this is just a notifiation
            }
        }
        
        if ( task == null ) {
            throw new RuntimeException("Timeout : unable to retrieve Task Id" );
        }
        
        return task;
    }       
}