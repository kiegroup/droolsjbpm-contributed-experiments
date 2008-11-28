/**
 * 
 */
package org.drools.process.workitem.wsht;

import org.drools.eventmessaging.EventResponseHandler;
import org.drools.eventmessaging.Payload;

public class BlockingEventResponseHandler extends AbstractBlockingResponseHandler implements EventResponseHandler {
    private volatile Payload payload;

    public synchronized void execute(Payload payload) {
        synchronized ( this.done ) {            
            this.payload = payload;        
            this.done = true;
        }
        notifyAll();        
    }
    
    public synchronized Payload getPayload() {
        boolean isDone;
        synchronized ( done ) {
            isDone = this.done;
        }
        if ( !isDone ) {                  
            try {
                wait( 1000000 );
            } catch ( InterruptedException e ) {
                // swallow as this is just a notification
            }
        }        
        synchronized ( done ) {
            isDone = this.done;
        }        
        if ( !isDone ) {
            throw new RuntimeException("Timeout : unable to retrieve event payload" );
        }
        
        return payload;
    }       
}