package org.drools.process.workitem.email;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

public class SendHtml {

    public static void sendHtml(Email email) {
        org.drools.process.workitem.email.Message message = email.getMessage();
        Connection connection = email.getConnection();

        String subject = message.getSubject();
        String from = message.getFrom();
        String replyTo = message.getReplyTo();
        String mailhost = connection.getHost();
        String mailer = "sendhtml";
        
        if ( from == null ) {
            throw new RuntimeException("Email must have 'from' address" );
        }
        
        if ( replyTo == null ) {
            replyTo = from;
        }
        boolean debug = false;
        try {
            Properties props = new Properties();
            // XXX - could use Session.getTransport() and Transport.connect()
            // XXX - assume we're using SMTP
            if ( mailhost != null && mailhost.trim().length() > 0 ) props.setProperty( "mail.smtp.host", mailhost );
            if ( connection.getPort() != null && connection.getPort().trim().length() > 0 ) {
                props.setProperty( "mail.smtp.port", connection.getPort() );                
            }               
                        
            // Get a Session object
            Session session = Session.getInstance( props, null );
            if ( debug ) session.setDebug( true );
            // construct the message
            Message msg = new MimeMessage( session );
            
            msg.setFrom( new InternetAddress( from ) );
            msg.setReplyTo( new InternetAddress[] {  new InternetAddress( replyTo ) }  );
            
            for ( Recipient recipient : message.getRecipients().getRecipients() ) {
                RecipientType type = null;
                if ( "To".equals( recipient.getType() ) ) {
                    type = Message.RecipientType.TO;
                } else if ( "Cc".equals( recipient.getType() ) ) {
                    type = Message.RecipientType.CC;
                } else if ( "Bcc".equals( recipient.getType() ) ) {
                    type = Message.RecipientType.BCC;
                } else {
                    throw new RuntimeException( "Unable to determine recipient type" );
                }

                msg.addRecipients( type, InternetAddress.parse( recipient.getEmail(), false ) );
            }
            msg.setSubject( subject );
            collect( message.getBody(), msg );
            msg.setHeader( "X-Mailer", mailer );
            msg.setSentDate( new Date() );
            // send the thing off
            Transport.send( msg );
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to send email", e );
        }
    }

    public static void collect(String body, Message msg) throws MessagingException, IOException {
//        String subject = msg.getSubject();
        StringBuffer sb = new StringBuffer();
//        sb.append( "<HTML>\n" );
//        sb.append( "<HEAD>\n" );
//        sb.append( "<TITLE>\n" );
//        sb.append( subject + "\n" );
//        sb.append( "</TITLE>\n" );
//        sb.append( "</HEAD>\n" );
//        sb.append( "<BODY>\n" );
//        sb.append( "<H1>" + subject + "</H1>" + "\n" );
        sb.append( body );
//        sb.append( "</BODY>\n" );
//        sb.append( "</HTML>\n" );
        msg.setDataHandler( new DataHandler( new ByteArrayDataSource( sb.toString(), "text/html" ) ) );
    }
}
