package org.drools.task.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.drools.eventmessaging.EventKey;
import org.drools.eventmessaging.EventTriggerTransport;
import org.drools.eventmessaging.Payload;
import org.drools.task.BaseTest;
import org.drools.task.Task;
import org.drools.task.event.TaskClaimedEvent;
import org.drools.task.event.TaskEventKey;

public class MockEventMessagingTest extends BaseTest {    
    public void testMockTransport() throws Exception {      
        Map  vars = new HashedMap();     
        vars.put( "users", users );
        vars.put( "groups", groups );        
        vars.put( "now", new Date() );                

        // One potential owner, should go straight to state Reserved
        String str = "(with (new Task()) { priority = 55, taskData = (with( new TaskData()) { } ), ";
        str += "peopleAssignments = (with ( new PeopleAssignments() ) { potentialOwners = [users['bobba' ], users['darth'] ], }),";                        
        str += "names = [ new I18NText( 'en-UK', 'This is my task name')] })";
            
        Task task = ( Task )  eval( new StringReader( str ), vars );
        taskSession.addTask( task, null );
        
        long taskId = task.getId();      
        
        EventKey key = new TaskEventKey(TaskClaimedEvent.class, taskId );        
        MockEventTriggerTransport transport = new MockEventTriggerTransport();   
        taskService.getEventKeys().register( key, transport );      
        
        
        taskSession.taskOperation( Operation.Claim, taskId, users.get( "darth" ).getId(), null, null );        
        
        assertEquals( 1, transport.list.size() );
        assertEquals( taskId, ((TaskClaimedEvent) ((Payload) transport.list.get(0)).get()).getTaskId() );
        assertEquals( users.get( "darth" ).getId(), ((TaskClaimedEvent) ((Payload) transport.list.get(0)).get()).getUserId() );
        
    }
    
    public static class MockEventTriggerTransport implements EventTriggerTransport {
        List<Payload> list = new ArrayList<Payload>();
        
        public void trigger(Payload payload) {
            list.add( payload );
        }

        public boolean isRemove() {
            return true;
        }                
    }
  
}
