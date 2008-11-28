/**
 * 
 */
package org.drools.task.service;

import org.drools.task.service.TaskClientHandler.AddCommentResponseHandler;

public class BlockingAddCommentResponseHandler extends AbstractBlockingResponseHandler
    implements
    AddCommentResponseHandler {
    private volatile long    commentId;

    public synchronized void execute(long commentId) {
        synchronized ( this.done ) {
            this.commentId = commentId;
            this.done = true;
            notifyAll();
        }
    }

    public synchronized long getCommentId() {
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
            throw new RuntimeException( "Timeout : unable to retrieve Task Id" );
        }

        return commentId;
    }
}