package org.drools.task.service;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.process.instance.impl.DefaultWorkItemManager;
import org.drools.runtime.process.WorkItemManager;
import org.drools.task.BaseTest;
import org.drools.task.Content;
import org.drools.task.MockUserInfo;
import org.drools.task.OrganizationalEntity;
import org.drools.task.Status;
import org.drools.task.Task;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class TaskServiceDeadlinesTest extends BaseTest {
    MinaTaskServer server;
    MinaTaskClient client;
    Properties conf;
    
    Wiser wiser;

    @Override
    protected void setUp() throws Exception {        
        super.setUp();
        
        conf = new Properties();
        conf.setProperty( "mail.smtp.host", "localhost" );
        conf.setProperty( "mail.smtp.port", "2345" );
        conf.setProperty( "from", "from@domain.com" );
        conf.setProperty( "replyTo", "replyTo@domain.com" );
        conf.setProperty( "defaultLanguage", "en-UK" );
        
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
        
        wiser = new Wiser();
        wiser.setHostname( conf.getProperty( "mail.smtp.host" ) );
        wiser.setPort( Integer.parseInt( conf.getProperty( "mail.smtp.port" ) ) );        
        wiser.start();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        client.disconnect();
        server.stop();
        
        wiser.stop();
    }
    
    public void testDelayedEmailNotificationOnDeadline() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );
        vars.put( "now", new Date() ); 
        
        DefaultEscalatedDeadlineHandler notificationHandler = new DefaultEscalatedDeadlineHandler( conf );
        WorkItemManager manager = new DefaultWorkItemManager( null );
        notificationHandler.setManager( manager );
        
        MockUserInfo userInfo = new MockUserInfo();
        userInfo.getEmails().put( users.get("tony"), "tony@domain.com" );
        userInfo.getEmails().put( users.get("darth"), "darth@domain.com" );
        
        userInfo.getLanguages().put(  users.get("tony"), "en-UK" );
        userInfo.getLanguages().put(  users.get("darth"), "en-UK" );
        notificationHandler.setUserInfo( userInfo );    
        
        taskService.setEscalatedDeadlineHandler( notificationHandler );
        
        String string = toString( new InputStreamReader( getClass().getResourceAsStream( "DeadlineWithNotification.mvel" ) ) );
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( string ), vars );
        client.addTask( task, null, addTaskResponseHandler );
        long taskId = addTaskResponseHandler.getTaskId();    
                                        
        Content content = new Content();
        content.setContent( "['subject' : 'My Subject', 'body' : 'My Body']".getBytes() );
        BlockingSetContentResponseHandler setContentResponseHandler  = new BlockingSetContentResponseHandler();
        client.setDocumentContent( taskId, content, setContentResponseHandler );
        long contentId = setContentResponseHandler.getContentId();
        BlockingGetContentResponseHandler  getResponseHandler = new BlockingGetContentResponseHandler();
        client.getContent( contentId, getResponseHandler );
        content = getResponseHandler.getContent();
        assertEquals( "['subject' : 'My Subject', 'body' : 'My Body']", new String( content.getContent() ) );
        
        // emails should not be set yet
        assertEquals(0, wiser.getMessages().size() );             
        Thread.sleep( 1000 );
        
        // nor yet
        assertEquals(0, wiser.getMessages().size() );     
        
        long time = 0;
        while ( wiser.getMessages().size() != 2 && time < 15000 ) {
            Thread.sleep( 500 );
            time += 500;
        }
        
        
        // 1 email with two recipients should now exist
        assertEquals(2, wiser.getMessages().size() );        
        
        List<String> list = new ArrayList<String>(2);
        list.add( wiser.getMessages().get( 0 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 1 ).getEnvelopeReceiver() );
        
        assertTrue( list.contains("tony@domain.com"));
        assertTrue( list.contains("darth@domain.com"));
        
        
        MimeMessage msg = (( WiserMessage  ) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEquals( "My Body", msg.getContent() );
        assertEquals( "My Subject", msg.getSubject() );
        assertEquals( "from@domain.com", ((InternetAddress)msg.getFrom()[0]).getAddress() );
        assertEquals( "replyTo@domain.com", ((InternetAddress)msg.getReplyTo()[0]).getAddress() );
        assertEquals( "tony@domain.com", ((InternetAddress)msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( "darth@domain.com", ((InternetAddress)msg.getRecipients( RecipientType.TO )[1]).getAddress() );        
    }
    
    public void testDelayedReassignmentOnDeadline() throws Exception {
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );
        vars.put( "now", new Date() ); 
        
        DefaultEscalatedDeadlineHandler notificationHandler = new DefaultEscalatedDeadlineHandler(conf);
        WorkItemManager manager = new DefaultWorkItemManager( null );
        notificationHandler.setManager( manager );
        
        MockUserInfo userInfo = new MockUserInfo();
        userInfo.getEmails().put( users.get("tony"), "tony@domain.com" );
        userInfo.getEmails().put( users.get("luke"), "luke@domain.com" );
        userInfo.getEmails().put( users.get("bobba"), "luke@domain.com" );
        userInfo.getEmails().put( users.get("jabba"), "luke@domain.com" );
        
        userInfo.getLanguages().put(  users.get("tony"), "en-UK" );
        userInfo.getLanguages().put(  users.get("luke"), "en-UK" );
        userInfo.getLanguages().put(  users.get("bobba"), "en-UK" );
        userInfo.getLanguages().put(  users.get("jabba"), "en-UK" );
        notificationHandler.setUserInfo( userInfo );    
        
        taskService.setEscalatedDeadlineHandler( notificationHandler );
        
        String string = toString( new InputStreamReader( getClass().getResourceAsStream( "DeadlineWithReassignment.mvel" ) ) );
            
        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = ( Task )  eval( new StringReader( string ), vars );               
        client.addTask( task, null, addTaskResponseHandler );
        long taskId = addTaskResponseHandler.getTaskId();    
        
        // Shouldn't have re-assigned yet
        Thread.sleep( 1000 );
        BlockingGetTaskResponseHandler getTaskHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskHandler );
        task = getTaskHandler.getTask();
        List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();
        List<String> ids = new ArrayList<String>(potentialOwners.size());
        for ( OrganizationalEntity entity : potentialOwners ) {
            ids.add( entity.getId() );
        }
        assertTrue( ids.contains( users.get( "tony" ).getId() ));
        assertTrue( ids.contains( users.get( "luke" ).getId() ));        
        
        // should have re-assigned by now
        long time = 0;
        while ( wiser.getMessages().size() != 2 && time < 15000 ) {
            Thread.sleep( 500 );
            time += 500;
        }
        
        getTaskHandler = new BlockingGetTaskResponseHandler(); 
        client.getTask( taskId, getTaskHandler );
        task = getTaskHandler.getTask();
        assertEquals( Status.Ready, task.getTaskData().getStatus()  );
        potentialOwners = task.getPeopleAssignments().getPotentialOwners();
        System.out.println( potentialOwners );
        ids = new ArrayList<String>(potentialOwners.size());
        for ( OrganizationalEntity entity : potentialOwners ) {
            ids.add( entity.getId() );
        }
        assertTrue( ids.contains( users.get( "bobba" ).getId() ));
        assertTrue( ids.contains( users.get( "jabba" ).getId() ));                  
    }
}
