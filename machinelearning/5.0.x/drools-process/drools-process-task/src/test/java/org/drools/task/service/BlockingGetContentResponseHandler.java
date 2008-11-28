/**
 * 
 */
package org.drools.task.service;

import org.drools.task.Content;
import org.drools.task.service.TaskClientHandler.GetContentResponseHandler;

public class BlockingGetContentResponseHandler extends AbstractBlockingResponseHandler implements GetContentResponseHandler {
    private volatile Content content;

    public synchronized void execute(Content content) {
        synchronized ( this.done ) {        
            this.content = content;
            this.done = true;
            notifyAll();                
        }
    }
    
    public synchronized Content getContent() {
        if ( content == null ) {                  
            try {
                wait( 10000 );
            } catch ( InterruptedException e ) {
                // swallow as this is just a notifiation
            }
        }
        
        if ( content == null ) {
            throw new RuntimeException("Timeout : unable to retrieve Attachment Content" );
        }
        
        return content;
    }       
}