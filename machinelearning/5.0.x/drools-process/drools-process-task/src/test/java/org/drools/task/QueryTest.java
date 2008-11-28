package org.drools.task;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.drools.task.query.DeadlineSummary;
import org.drools.task.service.TaskServiceSession;
import org.drools.task.service.TaskServiceEscalationTest.MockEscalatedDeadlineHandler;

public class QueryTest extends BaseTest {

    public void testUnescalatedDeadlines() throws Exception {
        MockEscalatedDeadlineHandler handler = new MockEscalatedDeadlineHandler();
        taskService.setEscalatedDeadlineHandler( handler );
        TaskServiceSession taskSession = taskService.createSession();        
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );


        //Reader reader;
        Reader reader = new InputStreamReader( getClass().getResourceAsStream( "QueryData_UnescalatedDeadlines.mvel" ) );
        List<Task> tasks = (List<Task>) eval( reader,
                                              vars );
        for ( Task task : tasks ) {
            taskSession.addTask( task, null );
        }
        long now = ((Date)vars.get( "now" )).getTime();
        
        // should be three, one is marked as escalated
        List<DeadlineSummary> list = taskSession.getUnescalatedDeadlines();
        
        assertEquals( 3,
                      list.size() );

        DeadlineSummary result = list.get( 0 );
        assertEquals( now + 20000,
                      result.getDate().getTime() );

        result = list.get( 1 );
        assertEquals( now + 22000 ,
                      result.getDate().getTime() );

        result = list.get( 2 );
        assertEquals( now + 24000,
                      result.getDate().getTime());    
        taskSession.dispose();
    }
    

}
