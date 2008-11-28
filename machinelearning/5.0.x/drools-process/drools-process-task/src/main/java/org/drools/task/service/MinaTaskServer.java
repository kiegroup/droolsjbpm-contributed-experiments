package org.drools.task.service;


public class MinaTaskServer extends BaseMinaServer
    implements
    Runnable {
    public MinaTaskServer(TaskService service) {
        super( new TaskServerHandler( service ),
               9123 );
    }
}