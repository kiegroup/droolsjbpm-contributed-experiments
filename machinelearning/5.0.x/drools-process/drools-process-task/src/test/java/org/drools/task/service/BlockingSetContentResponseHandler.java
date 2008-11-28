/**
 * 
 */
package org.drools.task.service;

import org.drools.task.service.TaskClientHandler.SetDocumentResponseHandler;

public class BlockingSetContentResponseHandler extends AbstractBlockingResponseHandler implements SetDocumentResponseHandler {
    private volatile long contentId;

    public synchronized void execute(long contentId) {
        synchronized ( this.done ) {        
            this.contentId = contentId;
            this.done = true;
            notifyAll();
        }
    }    
    
    public synchronized long getContentId() {
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
            throw new RuntimeException("Timeout : unable to retrieve Content Id" );
        }
        
        return contentId;
    }
}