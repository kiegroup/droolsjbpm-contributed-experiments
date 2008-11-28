package org.drools.task.service;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.task.AccessType;
import org.drools.task.BaseTest;
import org.drools.task.Content;
import org.drools.task.Status;
import org.drools.task.Task;

public class TaskServiceLifeCycleTest extends BaseTest {
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
    
    public void testNewTaskWithNoPotentialOwners() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // Task should remain in Created state with no actual owner
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( task1.getTaskData().getStatus(), Status.Created );     
        assertNull( task1.getTaskData().getActualOwner() );        
    }

    public void testNewTaskWithSinglePotentialOwner() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // Task should be assigned to the single potential owner and state set to Reserved
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Reserved, task1.getTaskData().getStatus() );     
        assertEquals( users.get( "bobba" ), task1.getTaskData().getActualOwner() );
    }
    
    public void testNewTaskWithContent() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        ContentData data = new ContentData();
        data.setAccessType(AccessType.Inline);
        data.setType("type");
        data.setContent("content".getBytes());
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, data, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // Task should be assigned to the single potential owner and state set to Reserved
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( AccessType.Inline, task1.getTaskData().getDocumentAccessType() );
        assertEquals( "type", task1.getTaskData().getDocumentType() );
        long contentId = task1.getTaskData().getDocumentContentId();
        assertTrue( contentId != -1 ); 

        BlockingGetContentResponseHandler getContentResponseHandler = new BlockingGetContentResponseHandler();
        client.getContent(contentId, getContentResponseHandler);
        Content content = getContentResponseHandler.getContent();
        assertEquals("content", new String(content.getContent()));
    }
    
    public void testClaimWithMultiplePotentialOwners() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // A Task with multiple potential owners moves to "Ready" state until someone claims it.
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Ready , task1.getTaskData().getStatus() );     
        
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );        
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );
    }

    public void testStartFromReadyStateWithPotentialOwner() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // A Task with multiple potential owners moves to "Ready" state until someone claims it.
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Ready , task1.getTaskData().getStatus() );     
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );        
    }
    
    public void testStartFromReadyStateWithIncorrectPotentialOwner() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // A Task with multiple potential owners moves to "Ready" state until someone claims it.
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Ready , task1.getTaskData().getStatus() );     
        
        // State should not change as user isn't potential owner
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "tony" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );
        assertNull( task2.getTaskData().getActualOwner() );        
    }    
    
    public void testStartFromReserved() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // Task should be assigned to the single potential owner and state set to Reserved
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Reserved, task1.getTaskData().getStatus());     
        assertEquals( users.get( "bobba" ), task1.getTaskData().getActualOwner() );
        
        // Should change to InProgress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "bobba" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals( Status.InProgress, task2.getTaskData().getStatus() );     
        assertEquals( users.get( "bobba" ), task1.getTaskData().getActualOwner() );        
    }
    
    public void testStartFromReservedWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();
        
        // Task should be assigned to the single potential owner and state set to Reserved
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals( Status.Reserved , task1.getTaskData().getStatus());     
        assertEquals( users.get( "bobba" ), task1.getTaskData().getActualOwner() );
        
        // Should change not change
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "tony" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals( Status.Reserved, task2.getTaskData().getStatus() );     
        assertEquals( users.get( "bobba" ), task1.getTaskData().getActualOwner() );        
    }
    
    public void testStop() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );        
        
        // Now Stop
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.stop( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                
    }    
    
    public void testStopWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );        
        
        // Should not stop
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.stop( taskId, users.get( "bobba" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                
    }   
    
    public void testReleaseFromInprogress() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Released
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.release( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );
        assertNull( task2.getTaskData().getActualOwner() );                  
    }    
    
    public void testReleaseFromReserved() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Released
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.release( taskId, users.get( "darth" ).getId(), responseHandler );  
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );
        assertNull( task2.getTaskData().getActualOwner() );                  
    }     
    
    public void testReleaseWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is not changed
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.release( taskId, users.get( "bobba" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() ); 
    }

    public void testSuspendFromReady() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Check is Ready
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Ready, task1.getTaskData().getStatus() );
        assertNull( task1.getTaskData().getActualOwner() );  
        
        // Check is Suspended
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.suspend( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Suspended, task2.getTaskData().getStatus() );
        assertEquals( Status.Ready, task2.getTaskData().getPreviousStatus() );
        assertNull( task1.getTaskData().getActualOwner() );                  
    }
    
    public void testSuspendFromReserved() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Check is Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );   
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Suspended
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.suspend( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task2.getTaskData().getPreviousStatus() );
        assertEquals(  Status.Suspended, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );          
    }    
    
    public void testSuspendFromReservedWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Check is Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is not changed
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.suspend( taskId, users.get( "bobba" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );      
    }    
    
    public void testResumeFromReady() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Check is Ready
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Ready, task1.getTaskData().getStatus() );
        assertNull( task1.getTaskData().getActualOwner() );  
        
        // Check is Suspended
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.suspend( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Suspended, task2.getTaskData().getStatus() );
        assertEquals( Status.Ready, task2.getTaskData().getPreviousStatus() );
        assertNull( task1.getTaskData().getActualOwner() );    
        
        // Check is Resumed
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.resume( taskId, users.get( "darth" ).getId(), responseHandler );   
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task3 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Ready, task3.getTaskData().getStatus() );
        assertEquals( Status.Suspended, task3.getTaskData().getPreviousStatus() );
        assertNull( task3.getTaskData().getActualOwner() );         
    }
    
    public void testResumeFromReserved() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Check is Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is suspended
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.suspend( taskId, users.get( "darth" ).getId(), responseHandler );        
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task2.getTaskData().getPreviousStatus() );
        assertEquals(  Status.Suspended, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() ); 
        
        // Check is Resumed
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.resume( taskId, users.get( "darth" ).getId(), responseHandler ); 
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task3 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task3.getTaskData().getStatus() );
        assertEquals( Status.Suspended, task3.getTaskData().getPreviousStatus() );
        assertEquals( users.get( "darth" ), task3.getTaskData().getActualOwner() );           
    }    
    
    public void testResumeFromReservedWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Check is Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check not changed
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.suspend( taskId, users.get( "bobba" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );      
    }     
          
    public void testSkipFromReady() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { skipable = true} ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ] }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();                     
        
        // Check is Complete
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.skip( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Obsolete, task1.getTaskData().getStatus() );
        assertNull(  task1.getTaskData().getActualOwner() );                  
    }    
    
    public void testSkipFromReserved() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { skipable = true} ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ] }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready 
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        // Check is Complete
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.skip( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Obsolete, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );                  
    }     
    
    public void testDelegateFromReady() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );        
        long taskId = addTaskResponseHandler.getTaskId();                     
        
        // Check is Delegated
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.delegate( taskId, users.get( "darth" ).getId(), users.get( "tony" ).getId(), responseHandler );    
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );        
        Task task2 = getTaskResponseHandler.getTask();
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "darth" ) ) );
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "tony" ) ) );
        assertEquals( users.get( "tony" ), task2.getTaskData().getActualOwner() );
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );             
    }     
    
    public void testDelegateFromReserved() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Claim and Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Delegated
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.delegate( taskId, users.get( "darth" ).getId(), users.get( "tony" ).getId(), responseHandler );    
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );        
        Task task2 = getTaskResponseHandler.getTask();
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "darth" ) ) );
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "tony" ) ) );
        assertEquals( users.get( "tony" ), task2.getTaskData().getActualOwner() );
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );             
    }     
    
    public void testDelegateFromReservedWithIncorrectUser() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Claim and Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check was not delegated
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.delegate( taskId, users.get( "bobba" ).getId(), users.get( "tony" ).getId(), responseHandler );    
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );        
        Task task2 = getTaskResponseHandler.getTask();
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "darth" ) ) );
        assertFalse( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "tony" ) ) );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );
        assertEquals(  Status.Reserved, task2.getTaskData().getStatus() );             
    }  
    
    public void testForwardFromReady() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();                     
        
        // Check is Forwarded
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.forward( taskId, users.get( "darth" ).getId(), users.get( "tony" ).getId(), responseHandler );    
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );        
        Task task2 = getTaskResponseHandler.getTask();
        assertFalse( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "darth" ) ) );
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "tony" ) ) );
        assertNull( task2.getTaskData().getActualOwner() );
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );             
    }  
    
    public void testForwardFromReserved() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Claim and Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Delegated
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.forward( taskId, users.get( "darth" ).getId(), users.get( "tony" ).getId(), responseHandler );    
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );        
        Task task2 = getTaskResponseHandler.getTask();
        assertFalse( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "darth" ) ) );
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "tony" ) ) );
        assertNull( task2.getTaskData().getActualOwner() );
        assertEquals(  Status.Ready, task2.getTaskData().getStatus() );             
    }     
    
    public void testForwardFromReservedWithIncorrectUser() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Claim and Reserved
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Reserved, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check was not delegated
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.forward( taskId, users.get( "bobba" ).getId(), users.get( "tony" ).getId(), responseHandler );    
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );        
        Task task2 = getTaskResponseHandler.getTask();
        assertTrue( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "darth" ) ) );
        assertFalse( task2.getPeopleAssignments().getPotentialOwners().contains( users.get( "tony" ) ) );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );
        assertEquals(  Status.Reserved, task2.getTaskData().getStatus() );             
    }      
    
    public void testComplete() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Complete
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "darth" ).getId(), null, responseHandler ); 
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Completed, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                  
    }
        
    public void testCompleteWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );        
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Should not complete as wrong user
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "bobba" ).getId(), null, responseHandler );  
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                  
    }    

    public void testCompleteWithContent() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        ContentData data = new ContentData();
        data.setAccessType(AccessType.Inline);
        data.setType("type");
        data.setContent("content".getBytes());
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.complete( taskId, users.get( "darth" ).getId(), data, responseHandler ); 
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals( AccessType.Inline, task2.getTaskData().getOutputAccessType() );
        assertEquals( "type", task2.getTaskData().getOutputType() );
        long contentId = task2.getTaskData().getOutputContentId();
        assertTrue( contentId != -1 ); 
        
        BlockingGetContentResponseHandler getContentResponseHandler = new BlockingGetContentResponseHandler();
        client.getContent(contentId, getContentResponseHandler);
        Content content = getContentResponseHandler.getContent();
        assertEquals("content", new String(content.getContent()));
    }
        
    public void testFail() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Check is Failed
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.fail( taskId, users.get( "darth" ).getId(), null, responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.Failed, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                  
    }
    
    public void testFailWithIncorrectUser() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );      
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        // Should not fail as wrong user
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.fail( taskId, users.get( "bobba" ).getId(), null, responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task2.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task2.getTaskData().getActualOwner() );                  
    }    

    public void testFailWithContent() {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );
        
        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( str ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        
        long taskId = addTaskResponseHandler.getTaskId();             
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.start( taskId, users.get( "darth" ).getId(), responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task1 = getTaskResponseHandler.getTask();
        assertEquals(  Status.InProgress, task1.getTaskData().getStatus() );
        assertEquals( users.get( "darth" ), task1.getTaskData().getActualOwner() );  
        
        FaultData data = new FaultData();
        data.setAccessType(AccessType.Inline);
        data.setType("type");
        data.setFaultName("faultName");
        data.setContent("content".getBytes());
        responseHandler = new BlockingTaskOperationResponseHandler();
        client.fail( taskId, users.get( "darth" ).getId(), data, responseHandler );
        responseHandler.waitTillDone( 3000 );
        
        getTaskResponseHandler = new BlockingGetTaskResponseHandler();  
        client.getTask( taskId, getTaskResponseHandler );
        Task task2 = getTaskResponseHandler.getTask();
        assertEquals( Status.Failed, task2.getTaskData().getStatus() );
        assertEquals( AccessType.Inline, task2.getTaskData().getFaultAccessType() );
        assertEquals( "type", task2.getTaskData().getFaultType() );
        assertEquals( "faultName", task2.getTaskData().getFaultName() );
        long contentId = task2.getTaskData().getFaultContentId();
        assertTrue( contentId != -1 ); 
        
        BlockingGetContentResponseHandler getContentResponseHandler = new BlockingGetContentResponseHandler();
        client.getContent(contentId, getContentResponseHandler);
        Content content = getContentResponseHandler.getContent();
        assertEquals("content", new String(content.getContent()));
    }
    
}
