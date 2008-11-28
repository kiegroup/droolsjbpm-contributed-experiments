package org.drools.task.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.task.BaseTest;
import org.drools.task.MockUserInfo;
import org.drools.task.Task;
import org.drools.util.ChainedProperties;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class IcalTest extends BaseTest {
    MinaTaskServer server;
    MinaTaskClient client;

    Wiser          wiser;
    
    String emailHost;
    String emailPort;    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        ChainedProperties props = new ChainedProperties( "process.email.conf" );
        emailHost = props.getProperty( "host", "locahost" );
        emailPort = props.getProperty( "port", "2345" );        
        
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
        wiser.setHostname( emailHost );
        wiser.setPort( Integer.parseInt( emailPort ) );         
        wiser.start();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        client.disconnect();
        server.stop();

        wiser.stop();
    }

    public void testSendWithStartandEndDeadline() throws Exception {
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );
        vars.put( "now",
                  new Date() );

        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { createdOn = now, createdBy = users['tony'], activationTime = now}), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) {potentialOwners = [users['steve' ], users['tony' ]]}), ";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')],";
        str += "subjects = [ new I18NText( 'en-UK', 'This is my task subject')],";
        str += "descriptions = [ new I18NText( 'en-UK', 'This is my task description')],";
        str += "deadlines = (with (new Deadlines() ) {";
        str += "    startDeadlines = [ ";
        str += "       (with (new Deadline()) {";
        str += "           date = now";
        str += "       } ) ],";
        str += "    endDeadlines = [";
        str += "        (with (new Deadline()) {";
        str += "             date = new Date( now.time + ( 1000 * 60 * * 60 * 24 ) )"; // set to tomorrow
        str += "        } ) ]";
        str += "} ) })";

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

        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = (Task) eval( new StringReader( str ),
                                 vars );
        client.addTask( task, null,
                        addTaskResponseHandler );

        long taskId = addTaskResponseHandler.getTaskId();
        
        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId,
                      users.get( "steve" ).getId(),
                      responseHandler );
        responseHandler.waitTillDone( 5000 );

        assertEquals( 2,
                      wiser.getMessages().size() );
        
        assertEquals( "steve@domain.com", wiser.getMessages().get( 0 ).getEnvelopeReceiver() );
        assertEquals( "steve@domain.com", wiser.getMessages().get( 1 ).getEnvelopeReceiver() );

        String subject = "Summary\n-------\n\nThis is my task subject\n\n";
        String description = "Description\n-----------\n\nThis is my task description";

        MimeMessage msg = ((WiserMessage) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEqualsIgnoreWhitespace( "multipart/alternative;boundary=\"----=_Part_",
                                      msg.getContentType(),
                                      0,
                                      47 );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getFrom()[0]).getAddress() );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getReplyTo()[0]).getAddress() );
        assertEquals( "steve@domain.com",
                      ((InternetAddress) msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( "Task Assignment Start Event: This is my task name",
                      msg.getSubject() );
        
        MimeMultipart multiPart = (MimeMultipart) msg.getContent();
                        
        BodyPart messageBodyPart = multiPart.getBodyPart( 0 );
        assertEquals( "text/plain; charset=UTF8;", messageBodyPart.getDataHandler().getContentType() );
        String content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );        
        assertEqualsIgnoreWhitespace( subject + description, content );
        
        messageBodyPart = multiPart.getBodyPart( 1 );
        assertEquals( "text/calendar; charset=UTF8; name=ical-Start-1.ics", messageBodyPart.getDataHandler().getContentType() );
        content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );
        assertEqualsIgnoreWhitespace( "BEGIN:VCALENDARPRODID:-//iCal4j 1.0//ENCALSCALE:GREGORIANVERSION:2.0METHOD:REQUESTBEGIN:VEVENTDTSTART;TZID=UTC:", content.substring( 0, 123) );           
        assertEqualsIgnoreWhitespace( "SUMMARY:\"Task Start : This is my task subject\"DESCRIPTION:\"This is my task description\"PRIORITY:55END:VEVENTEND:VCALENDAR", content.substring( content.length()-131, content.length()) );
        
        
        msg = ((WiserMessage) wiser.getMessages().get( 1 )).getMimeMessage();
        assertEqualsIgnoreWhitespace( "multipart/alternative;boundary=\"----=_Part_",
                                      msg.getContentType(),
                                      0,
                                      47 );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getFrom()[0]).getAddress() );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getReplyTo()[0]).getAddress() );
        assertEquals( "steve@domain.com",
                      ((InternetAddress) msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( "Task Assignment End Event: This is my task name",
                      msg.getSubject() );
        
        multiPart = (MimeMultipart) msg.getContent();
                        
        messageBodyPart = multiPart.getBodyPart( 0 );
        assertEquals( "text/plain; charset=UTF8;", messageBodyPart.getDataHandler().getContentType() );
        content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );        
        assertEqualsIgnoreWhitespace( subject + description, content );
        
        messageBodyPart = multiPart.getBodyPart( 1 );
        assertEquals( "text/calendar; charset=UTF8; name=ical-End-1.ics", messageBodyPart.getDataHandler().getContentType() );
        content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );
        assertEqualsIgnoreWhitespace( "BEGIN:VCALENDARPRODID:-//iCal4j 1.0//ENCALSCALE:GREGORIANVERSION:2.0METHOD:REQUESTBEGIN:VEVENTDTSTART;TZID=UTC:", content.substring( 0, 123) );           
        assertEqualsIgnoreWhitespace( "SUMMARY:\"Task End : This is my task subject\"DESCRIPTION:\"This is my task description\"PRIORITY:55END:VEVENTEND:VCALENDAR", content.substring( content.length()-131, content.length()) );
    }
    
    public void testSendWithStartDeadline() throws Exception {
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );
        vars.put( "now",
                  new Date() );

        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { createdOn = now, createdBy = users['tony'], activationTime = now}), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) {potentialOwners = [users['steve' ], users['tony' ]]}), ";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')],";
        str += "subjects = [ new I18NText( 'en-UK', 'This is my task subject')],";
        str += "descriptions = [ new I18NText( 'en-UK', 'This is my task description')],";
        str += "deadlines = (with (new Deadlines() ) {";
        str += "    startDeadlines = [ ";
        str += "       (with (new Deadline()) {";
        str += "           date = now";
        str += "       } ) ]";
        str += "} ) })";

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

        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = (Task) eval( new StringReader( str ),
                                 vars );
        client.addTask( task, null,
                        addTaskResponseHandler );

        long taskId = addTaskResponseHandler.getTaskId();

        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId,
                      users.get( "steve" ).getId(),
                      responseHandler );
        responseHandler.waitTillDone( 5000 );

        assertEquals( 1,
                      wiser.getMessages().size() );

        assertEquals( "steve@domain.com", wiser.getMessages().get( 0 ).getEnvelopeReceiver() );

        String subject = "Summary\n-------\n\nThis is my task subject\n\n";
        String description = "Description\n-----------\n\nThis is my task description";

        MimeMessage msg = ((WiserMessage) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEqualsIgnoreWhitespace( "multipart/alternative;boundary=\"----=_Part_",
                                      msg.getContentType(),
                                      0,
                                      47 );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getFrom()[0]).getAddress() );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getReplyTo()[0]).getAddress() );
        assertEquals( "steve@domain.com",
                      ((InternetAddress) msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( "Task Assignment Start Event: This is my task name",
                      msg.getSubject() );
        
        MimeMultipart multiPart = (MimeMultipart) msg.getContent();
                        
        BodyPart messageBodyPart = multiPart.getBodyPart( 0 );
        assertEquals( "text/plain; charset=UTF8;", messageBodyPart.getDataHandler().getContentType() );
        String content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );        
        assertEqualsIgnoreWhitespace( subject + description, content );
        
        messageBodyPart = multiPart.getBodyPart( 1 );
        assertEquals( "text/calendar; charset=UTF8; name=ical-Start-1.ics", messageBodyPart.getDataHandler().getContentType() );
        content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );
        assertEqualsIgnoreWhitespace( "BEGIN:VCALENDARPRODID:-//iCal4j 1.0//ENCALSCALE:GREGORIANVERSION:2.0METHOD:REQUESTBEGIN:VEVENTDTSTART;TZID=UTC:", content.substring( 0, 123) );           
        assertEqualsIgnoreWhitespace( "SUMMARY:\"Task Start : This is my task subject\"DESCRIPTION:\"This is my task description\"PRIORITY:55END:VEVENTEND:VCALENDAR", content.substring( content.length()-131, content.length()) );
    }    
    
    public void testSendWithEndDeadline() throws Exception {
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );
        vars.put( "now",
                  new Date() );

        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { createdOn = now, createdBy = users['tony'], activationTime = now}), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) {potentialOwners = [users['steve' ], users['tony' ]]}), ";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')],";
        str += "subjects = [ new I18NText( 'en-UK', 'This is my task subject')],";
        str += "descriptions = [ new I18NText( 'en-UK', 'This is my task description')],";
        str += "deadlines = (with (new Deadlines() ) {";
        str += "    endDeadlines = [";
        str += "        (with (new Deadline()) {";
        str += "             date = new Date( now.time + ( 1000 * 60 * * 60 * 24 ) )"; // set to tomorrow
        str += "        } ) ]";
        str += "} ) })";

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

        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = (Task) eval( new StringReader( str ),
                                 vars );
        client.addTask( task, null,
                        addTaskResponseHandler );

        long taskId = addTaskResponseHandler.getTaskId();

        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId,
                      users.get( "steve" ).getId(),
                      responseHandler );

        responseHandler.waitTillDone( 5000 );

        assertEquals( 1,
                      wiser.getMessages().size() );
        
        assertEquals( "steve@domain.com", wiser.getMessages().get( 0 ).getEnvelopeReceiver() );

        String subject = "Summary\n-------\n\nThis is my task subject\n\n";
        String description = "Description\n-----------\n\nThis is my task description";
                
        MimeMessage msg = ((WiserMessage) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEqualsIgnoreWhitespace( "multipart/alternative;boundary=\"----=_Part_",
                                      msg.getContentType(),
                                      0,
                                      47 );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getFrom()[0]).getAddress() );
        assertEquals( "tony@domain.com",
                      ((InternetAddress) msg.getReplyTo()[0]).getAddress() );
        assertEquals( "steve@domain.com",
                      ((InternetAddress) msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( "Task Assignment End Event: This is my task name",
                      msg.getSubject() );
        
        MimeMultipart multiPart = (MimeMultipart) msg.getContent();
                        
        BodyPart messageBodyPart = multiPart.getBodyPart( 0 );
        assertEquals( "text/plain; charset=UTF8;", messageBodyPart.getDataHandler().getContentType() );
        String content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );        
        assertEqualsIgnoreWhitespace( subject + description, content );
        
        messageBodyPart = multiPart.getBodyPart( 1 );
        assertEquals( "text/calendar; charset=UTF8; name=ical-End-1.ics", messageBodyPart.getDataHandler().getContentType() );
        content = new String( getBytes( messageBodyPart.getDataHandler().getInputStream() ) );
        assertEqualsIgnoreWhitespace( "BEGIN:VCALENDARPRODID:-//iCal4j 1.0//ENCALSCALE:GREGORIANVERSION:2.0METHOD:REQUESTBEGIN:VEVENTDTSTART;TZID=UTC:", content.substring( 0, 123) );           
        assertEqualsIgnoreWhitespace( "SUMMARY:\"Task End : This is my task subject\"DESCRIPTION:\"This is my task description\"PRIORITY:55END:VEVENTEND:VCALENDAR", content.substring( content.length()-131, content.length()) );
    }
    
    public void testSendWithNoDeadline() throws Exception {
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );
        vars.put( "now",
                  new Date() );

        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { createdOn = now, createdBy = users['tony'], activationTime = now}), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) {potentialOwners = [users['steve' ], users['tony' ]]}), ";
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')],";
        str += "subjects = [ new I18NText( 'en-UK', 'This is my task subject')],";
        str += "descriptions = [ new I18NText( 'en-UK', 'This is my task description')]";
        str += "})";

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

        BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        Task task = (Task) eval( new StringReader( str ),
                                 vars );
        client.addTask( task, null,
                        addTaskResponseHandler );

        long taskId = addTaskResponseHandler.getTaskId();

        BlockingTaskOperationResponseHandler responseHandler = new BlockingTaskOperationResponseHandler();
        client.claim( taskId,
                      users.get( "steve" ).getId(),
                      responseHandler );

        responseHandler.waitTillDone( 5000 );

        assertEquals( 0,
                      wiser.getMessages().size() );        
    }       
    
    private static void assertEqualsIgnoreWhitespace(final String expected,
                                                     final String actual) {
        assertEqualsIgnoreWhitespace(expected, actual, 0, actual.length());
    }

    private static void assertEqualsIgnoreWhitespace(final String expected,
                                                     final String actual,
                                                     int beginIndex,
                                                     int endIndex) {
        final String cleanExpected = expected.replaceAll( "\\s+",
                                                          "" ).replaceAll( "\\n", "" ).replaceAll( "\\r", "" );
        
        final String cleanActual = actual.substring( beginIndex,
                                                     endIndex ).replaceAll( "\\s+",
                                                      "" ).replaceAll( "\\n", "" ).replaceAll( "\\r", "" );
        assertEquals( cleanExpected,
                      cleanActual );
    } 
    
    private static byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[512];
     
        // Read bytes from the input stream in bytes.length-sized chunks and write
        // them into the output stream
        int readBytes;
        while ((readBytes = inputStream.read(bytes)) > 0) {
            outputStream.write(bytes, 0, readBytes);
        }
     
        // Convert the contents of the output stream into a byte array
        byte[] byteData = outputStream.toByteArray();
        
        // Close the streams
        inputStream.close();
        outputStream.close();
     
        return byteData;

    }

}
