package org.drools.task.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.collections.map.HashedMap;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.task.BaseTest;
import org.drools.task.Deadline;
import org.drools.task.Task;
import org.drools.task.service.TaskServiceEscalationTest.MockEscalatedDeadlineHandler.Item;

public class TaskServiceEscalationTest extends BaseTest {
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

    public void testUnescalatedDeadlines() throws Exception {
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );

        MockEscalatedDeadlineHandler handler = new MockEscalatedDeadlineHandler();
        taskService.setEscalatedDeadlineHandler( handler );  
        
        //Reader reader;
        Reader reader = new InputStreamReader( getClass().getResourceAsStream( "../QueryData_UnescalatedDeadlines.mvel" ) );
        List<Task> tasks = (List<Task>) eval( reader,
                                              vars );
        long now = ((Date)vars.get( "now" )).getTime();
        
        for ( Task task : tasks ) {  
            BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();            
            client.addTask( task, null, addTaskResponseHandler ); 
            addTaskResponseHandler.waitTillDone( 3000 );
        }

        handler.wait( 3, 30000 );
        
        assertEquals( 3, handler.list.size() );
        
        Item item0 = handler.list.get( 0 );        
        assertEquals( now + 20000,
                      item0.getDeadline().getDate().getTime() );
        
        Item item1 = handler.list.get( 1 );
        assertEquals( now + 22000,
                      item1.getDeadline().getDate().getTime() );
        
        Item item2 = handler.list.get( 2 );
        assertEquals( now + 24000,
                      item2.getDeadline().getDate().getTime() );        
    }
    
    public void testUnescalatedDeadlinesOnStartup() throws Exception {
        Map vars = new HashedMap();
        vars.put( "users",
                  users );
        vars.put( "groups",
                  groups );

        //Reader reader;
        Reader reader = new InputStreamReader( getClass().getResourceAsStream( "../QueryData_UnescalatedDeadlines.mvel" ) );
        List<Task> tasks = (List<Task>) eval( reader,
                                              vars );
        long now = ((Date)vars.get( "now" )).getTime();
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for ( Task task : tasks ) {
            // for this one we put the task in directly;
            em.persist( task );
        }
        em.getTransaction().commit();

        // now create a new service, to see if it initiates from the DB correctly
        MockEscalatedDeadlineHandler handler = new MockEscalatedDeadlineHandler();
        TaskService local = new TaskService(emf, handler);      
                
        handler.wait( 3, 30000 );
        
        assertEquals( 3, handler.list.size() );

        Item item0 = handler.list.get( 0 );
        assertEquals( item0.getDeadline().getDate().getTime(),
                      now + 20000 );
        
        Item item1 = handler.list.get( 1 );
        assertEquals( item1.getDeadline().getDate().getTime(),
                      now + 22000 );
        
        Item item2 = handler.list.get( 2 );
        assertEquals( item2.getDeadline().getDate().getTime(),
                      now + 24000 );            
    }

    public static class MockEscalatedDeadlineHandler
        implements
        EscalatedDeadlineHandler {

        List<Item> list = new ArrayList<Item>();
        
        TaskService taskService;

        public void executeEscalatedDeadline(Task task,
                                             Deadline deadline,
                                             EntityManager em,
                                             TaskService taskService) {
            list.add( new Item( task,
                                deadline,
                                em,
                                taskService ) );
        }
        
        public List<Item> getList() {
            return this.list;
        }

        public static class Item {
            Task          task;
            Deadline      deadline;
            EntityManager em;

            public Item(Task task,
                        Deadline deadline,
                        EntityManager em,
                        TaskService taskService) {
                this.deadline = deadline;
                this.em = em;
                this.task = task;
            }

            public Task getTask() {
                return task;
            }

            public void setTask(Task task) {
                this.task = task;
            }

            public Deadline getDeadline() {
                return deadline;
            }

            public void setDeadline(Deadline deadline) {
                this.deadline = deadline;
            }

            public EntityManager getEntityManager() {
                return em;
            }

            public void setEntityManager(EntityManager em) {
                this.em = em;
            }

            public EntityManager getEm() {
                return em;
            }

            public void setEm(EntityManager em) {
                this.em = em;
            }                        
        }   
        
        public synchronized void wait(int totalSize, int totalWait) {
            int wait = 0;
            int size = 0;
            
            while ( true ) {
                synchronized ( list ) {
                    size = list.size();
                }
                
                if ( size >= totalSize || wait >= totalWait ) {
                    break;
                }
                
                try {
                    Thread.sleep( 250 );
                } catch( Exception e ) {
                    throw new RuntimeException( "Unable to sleep", e);
                }
                wait += 250;
            }
        }
    }
    
}
