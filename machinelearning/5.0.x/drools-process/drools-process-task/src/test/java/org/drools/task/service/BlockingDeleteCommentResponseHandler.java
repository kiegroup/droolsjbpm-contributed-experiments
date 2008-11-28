/**
 * 
 */
package org.drools.task.service;

import org.drools.task.service.TaskClientHandler.DeleteCommentResponseHandler;

public class BlockingDeleteCommentResponseHandler extends AbstractBlockingResponseHandler implements DeleteCommentResponseHandler {
      public void setIsDone(boolean done) {
          synchronized ( this.done ) {
            this.done = done;
        }
      }
}