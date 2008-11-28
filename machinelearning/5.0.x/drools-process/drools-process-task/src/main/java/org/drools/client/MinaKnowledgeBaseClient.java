package org.drools.client;

import java.util.ArrayList;
import java.util.List;

import org.drools.task.Task;
import org.drools.task.service.BaseMinaClient;
import org.drools.task.service.Command;
import org.drools.task.service.CommandName;
import org.drools.task.service.TaskClientHandler;
import org.drools.task.service.TaskClientHandler.AddTaskResponseHandler;

public class MinaKnowledgeBaseClient extends BaseMinaClient {
    
    public MinaKnowledgeBaseClient(String name,
                          TaskClientHandler handler) {
        super( name,
               handler );
    }            

    public void addPackage(Task task,
                        AddTaskResponseHandler responseHandler) {
        List args = new ArrayList( 2 );
        args.add( task );
        args.add( null );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.AddTaskRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }


}