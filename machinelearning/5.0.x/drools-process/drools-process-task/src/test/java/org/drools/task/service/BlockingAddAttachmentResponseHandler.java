/**
 * 
 */
package org.drools.task.service;

import org.drools.task.service.TaskClientHandler.AddAttachmentResponseHandler;

public class BlockingAddAttachmentResponseHandler extends AbstractBlockingResponseHandler implements AddAttachmentResponseHandler {
    private volatile long attachmentId ;
    private volatile long contentId;

    public synchronized void execute(long attachmentId, long contentId) {
        synchronized ( this.done ) {        
            this.attachmentId = attachmentId;
            this.contentId = contentId;
            this.done = true;
            notifyAll();     
        }
    }
    
    public synchronized long getAttachmentId() {
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
            throw new RuntimeException("Timeout : unable to retrieve Attachment Id" );
        }
        
        return attachmentId;
    }       
    
    public synchronized long getContentId() {
        boolean isDone;
        synchronized ( done ) {
            isDone = this.done;
        }
        if ( !isDone ) {                  
            try {
                wait( 3000 );
            } catch ( InterruptedException e ) {
                // swallow as this is just a notification
            }
        }        
        synchronized ( done ) {
            isDone = this.done;
        }        
        if ( !isDone ) {
            throw new RuntimeException("Timeout : unable to retrieve Attachment Content Id" );
        }
        
        return contentId;
    }
}