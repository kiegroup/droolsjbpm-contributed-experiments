package org.drools.task.service;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.eventmessaging.EventKey;
import org.drools.eventmessaging.Payload;
import org.drools.task.BaseTest;
import org.drools.task.Status;
import org.drools.task.Task;
import org.drools.task.event.TaskCompletedEvent;
import org.drools.task.event.TaskEventKey;
import org.drools.task.query.TaskSummary;

public class TaskLifeCycleTest extends BaseTest {
	
    MinaTaskServer server;
    MinaTaskClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new MinaTaskServer( taskService );
        Thread thread = new Thread( server );
        thread.start();
        Thread.sleep( 500 );
        
        client = new MinaTaskClient( "client 1",
                                     new TaskClientHandler() );
        NioSocketConnector connector = new NioSocketConnector();
        SocketAddress address = new InetSocketAddress( "127.0.0.1",
                                                       9123 );
        client.connect( connector,
                        address );
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        client.disconnect();
        server.stop();
    }    
    
    @SuppressWarnings("unchecked")
	public void testLifeCycle() throws Exception {      
        Map<String, Object> vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );                

        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { workItemId = 1 } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba']], }),";                        
        str += "descriptions = [ new I18NText( 'en-UK', 'This is my description')], ";
        str += "subjects = [ new I18NText( 'en-UK', 'This is my subject')], ";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        EventKey key = new TaskEventKey(TaskCompletedEvent.class, taskId );           
        BlockingEventResponseHandler handler = new BlockingEventResponseHandler(); 
        client.registerForEvent( key, true, handler );
        
        BlockingTaskSummaryResponseHandler taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        List<TaskSummary> tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(1, tasks.size());
        assertEquals(Status.Reserved, tasks.get(0).getStatus());
        
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "bobba" ).getId(), responseHandler );  

        taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(1, tasks.size());
        assertEquals(Status.InProgress, tasks.get(0).getStatus());
        
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "bobba" ).getId(), null, responseHandler );
        
        taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(0, tasks.size());
        
        Payload payload = handler.getPayload();
        TaskCompletedEvent event = ( TaskCompletedEvent ) payload.get();
        assertNotNull( event );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Completed , task1.getTaskData().getStatus() );         
    }
  
    @SuppressWarnings("unchecked")
	public void testLifeCycleMultipleTasks() throws Exception {      
        Map<String, Object> vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );                

        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { workItemId = 1 } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba']], }),";                        
        str += "descriptions = [ new I18NText( 'en-UK', 'This is my description')], ";
        str += "subjects = [ new I18NText( 'en-UK', 'This is my subject')], ";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        long taskId = addTaskResponseHandler.getTaskId();
        
        EventKey key = new TaskEventKey(TaskCompletedEvent.class, taskId );           
        BlockingEventResponseHandler handler = new BlockingEventResponseHandler(); 
        client.registerForEvent( key, true, handler );
        
        BlockingTaskSummaryResponseHandler taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        List<TaskSummary> tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(1, tasks.size());
        assertEquals(Status.Reserved, tasks.get(0).getStatus());
        
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "bobba" ).getId(), responseHandler );  

        taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(1, tasks.size());
        assertEquals(Status.InProgress, tasks.get(0).getStatus());
        
        BlockingAddTaskResponseHandler addTaskResponseHandler2 = new BlockingAddTaskResponseHandler();
        Task task2 = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task2, null, addTaskResponseHandler2 );
        long taskId2 = addTaskResponseHandler.getTaskId();
        
        EventKey key2 = new TaskEventKey(TaskCompletedEvent.class, taskId2 );           
        BlockingEventResponseHandler handler2 = new BlockingEventResponseHandler(); 
        client.registerForEvent( key2, true, handler2 );
        
        taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(2, tasks.size());
        
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "bobba" ).getId(), null, responseHandler );
        
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId2, users.get( "bobba" ).getId(), responseHandler );  

        taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        client.getTasksAssignedAsPotentialOwner(users.get( "bobba" ).getId(), "en-UK", taskSummaryResponseHandler);
        tasks = taskSummaryResponseHandler.getResults(); 
        assertEquals(1, tasks.size());
        
        Payload payload = handler.getPayload();
        TaskCompletedEvent event = ( TaskCompletedEvent ) payload.get();
        assertNotNull( event );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        task = getTaskResponseHandler.getTask();
        assertEquals( Status.Completed , task.getTaskData().getStatus() );
        
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId2, users.get( "bobba" ).getId(), null, responseHandler );
        
        payload = handler.getPayload();
        event = ( TaskCompletedEvent ) payload.get();
        assertNotNull( event );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler2 = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId2, getTaskResponseHandler2 );
        task2 = getTaskResponseHandler2.getTask();
        assertEquals( Status.Completed , task2.getTaskData().getStatus() );
    }
    
}
