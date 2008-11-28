package org.drools.task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.apache.commons.collections.map.HashedMap;
import org.drools.task.service.SendIcal;
import org.drools.task.service.TaskService;
import org.drools.task.service.TaskServiceSession;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

public abstract class BaseTest extends TestCase {
    protected EntityManagerFactory emf;

    protected Map<String, User>    users;
    protected Map<String, Group>   groups;

    protected TaskService          taskService;
    protected TaskServiceSession   taskSession;

    protected void setUp() throws Exception {
        Properties conf = new Properties();
        conf.setProperty( "mail.smtp.host", "localhost" );
        conf.setProperty( "mail.smtp.port", "2345" );
        conf.setProperty( "from", "from@domain.com" );
        conf.setProperty( "replyTo", "replyTo@domain.com" );
        conf.setProperty( "defaultLanguage", "en-UK" );
        SendIcal.initInstance( conf );
        
        // Use persistence.xml configuration
        emf = Persistence.createEntityManagerFactory( "org.drools.task" );

        taskService = new TaskService( emf );
        taskSession = taskService.createSession();
        MockUserInfo userInfo = new MockUserInfo();
        taskService.setUserinfo( userInfo );
        Map vars = new HashedMap();

        Reader reader = new InputStreamReader( BaseTest.class.getResourceAsStream( "LoadUsers.mvel" ) );
        users = (Map<String, User>) eval( reader,
                                          vars );
        for ( User user : users.values() ) {
            taskSession.addUser( user );
        }

        reader = new InputStreamReader( BaseTest.class.getResourceAsStream( "LoadGroups.mvel" ) );
        groups = (Map<String, Group>) eval( reader,
                                            vars );
        for ( Group group : groups.values() ) {
            taskSession.addGroup( group );
        }
    }

    protected void tearDown() throws Exception {
        taskSession.dispose();
        emf.close();
    }

    public Object eval(Reader reader,
                       Map vars) {
        try {
            return eval( toString( reader ),
                         vars );
        } catch ( IOException e ) {
            throw new RuntimeException( "Exception Thrown",
                                        e );
        }
    }

    public String toString(Reader reader) throws IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer( 1024 );
        while ( (charValue = reader.read()) != -1 ) {
            //result = result + (char) charValue;
            sb.append( (char) charValue );
        }
        return sb.toString();
    }

    public Object eval(String str,
                       Map vars) {
        ExpressionCompiler compiler = new ExpressionCompiler( str.trim() );

        ParserContext context = new ParserContext();
        context.addPackageImport( "org.drools.task" );
        context.addPackageImport( "org.drools.task.service" );
        context.addPackageImport( "org.drools.task.query" );
        context.addPackageImport( "java.util" );
        
        vars.put( "now",
                  new Date() );
        return MVEL.executeExpression( compiler.compile( context ),
                                       vars );
    }
}
