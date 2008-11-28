package org.drools.task.service;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.eventmessaging.EventKey;
import org.drools.eventmessaging.Payload;
import org.drools.task.BaseTest;
import org.drools.task.MockUserInfo;
import org.drools.task.Status;
import org.drools.task.Task;
import org.drools.task.event.TaskClaimedEvent;
import org.drools.task.event.TaskEventKey;
import org.drools.task.event.TaskUserEvent;

public class TaskServiceEventMessagingTest extends BaseTest {
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
        
        MockUserInfo userInfo = new MockUserInfo();
        userInfo.getEmails().put( users.get( "tony" ),
                                  "tony@domain.com" );
        userInfo.getEmails().put( users.get( "steve" ),
                                  "steve@domain.com" );

        userInfo.getLanguages().put( users.get( "tony" ),
                                     "en-UK" );
        userInfo.getLanguages().put( users.get( "steve" ),
                                     "en-UK" );
        taskService.setUserinfo( userInfo );
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        client.disconnect();
        server.stop();
    }    
    
    public void testClaimEvent() throws Exception {      
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
        
        EventKey key = new TaskEventKey(TaskClaimedEvent.class, taskId );           
        BlockingEventResponseHandler handler = new BlockingEventResponseHandler(); 
        client.registerForEvent( key, true, handler );
        Thread.sleep( 3000 );
        
        taskSession.taskOperation( Operation.Claim, taskId, users.get( "darth" ).getId(), null, null );          
        handler.waitTillDone( 5000 );
        
        Payload payload = handler.getPayload();
        TaskUserEvent event = ( TaskUserEvent ) payload.get();
        assertNotNull( event );        
    }
  
}
