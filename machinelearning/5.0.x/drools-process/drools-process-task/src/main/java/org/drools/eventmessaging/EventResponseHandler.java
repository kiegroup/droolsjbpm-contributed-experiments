package org.drools.eventmessaging;

import org.drools.task.service.BaseMinaHandler.ResponseHandler;

public interface EventResponseHandler extends ResponseHandler {
    public void execute(Payload payload);
}
