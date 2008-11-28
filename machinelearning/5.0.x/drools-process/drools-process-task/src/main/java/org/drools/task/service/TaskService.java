package org.drools.task.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.eventmessaging.EventKeys;
import org.drools.task.Deadline;
import org.drools.task.Task;
import org.drools.task.UserInfo;
import org.drools.task.event.MessagingTaskEventListener;
import org.drools.task.event.TaskEventListener;
import org.drools.task.event.TaskEventSupport;
import org.drools.task.query.DeadlineSummary;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

public class TaskService {
    EntityManagerFactory             emf;
    EntityManager                    em;

    ScheduledThreadPoolExecutor      scheduler;

    private EscalatedDeadlineHandler escalatedDeadlineHandler;

    private UserInfo                 userInfo;

    private TaskEventSupport         eventSupport;
    private EventKeys                eventKeys;
    
    Map<Operation, List<OperationCommand>> operations;

    public TaskService(EntityManagerFactory emf) {
        this(emf, null);
    }
    
    public TaskService(EntityManagerFactory emf, EscalatedDeadlineHandler escalationHandler) {
        this.emf = emf;
        this.em = emf.createEntityManager();
        if ( escalationHandler != null ) {
            this.escalatedDeadlineHandler = escalationHandler;
        }

        eventSupport = new TaskEventSupport();
        eventKeys = new EventKeys();
        eventSupport.addEventListener( new MessagingTaskEventListener( eventKeys ) );
        scheduler = new ScheduledThreadPoolExecutor( 3 );

        long now = System.currentTimeMillis();
        for ( Object object : em.createNamedQuery( "UnescalatedDeadlines" ).getResultList() ) {
            DeadlineSummary summary = (DeadlineSummary) object;
            schedule( new ScheduledTaskDeadline( summary.getTaskId(),
                                                 summary.getDeadlineId(),
                                                 this ),
                      summary.getDate().getTime() - now );
        }
        
        Map vars = new HashMap();
        
        //Reader reader;
        Reader reader = new InputStreamReader( getClass().getResourceAsStream( "operations-dsl.mvel" ) );
        try {
        operations  = (Map<Operation,  List<OperationCommand>>) eval( toString( reader ),
                                                                      vars );        
        } catch (IOException e) {
            throw new RuntimeException( "Unable To initialise TaskService, could not load Operations DSL" );
        }
    }

    public TaskServiceSession createSession() {
        return new TaskServiceSession( this,
                                       emf.createEntityManager() );
    }

    public void schedule(ScheduledTaskDeadline deadline,
                         long delay) {
        scheduler.schedule( deadline,
                            delay,
                            TimeUnit.MILLISECONDS );
    }
    
    public Map<Operation,  List<OperationCommand>> getOperations() {
        return operations;
    }

    public EventKeys getEventKeys() {
        return eventKeys;
    }

    public void addEventListener(final TaskEventListener listener) {
        this.eventSupport.addEventListener( listener );
    }

    public void removeEventListener(final TaskEventListener listener) {
        this.eventSupport.removeEventListener( listener );
    }

    public List<TaskEventListener> getWorkingMemoryEventListeners() {
        return this.eventSupport.getEventListeners();
    }

    public TaskEventSupport getEventSupport() {
        return eventSupport;
    }

    public UserInfo getUserinfo() {
        return userInfo;
    }

    public void setUserinfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public void setEscalatedDeadlineHandler(EscalatedDeadlineHandler escalatedDeadlineHandler) {
        this.escalatedDeadlineHandler = escalatedDeadlineHandler;
    }

    public void executeEscalatedDeadline(long taskId,
                                         long deadlineId) {
        EntityManager localEm = emf.createEntityManager();
        Task task = localEm.find( Task.class,
                                  taskId );
        Deadline deadline = localEm.find( Deadline.class,
                                          deadlineId );

        if ( escalatedDeadlineHandler == null ) {
            escalatedDeadlineHandler = new DefaultEscalatedDeadlineHandler();
        }

        escalatedDeadlineHandler.executeEscalatedDeadline( task,
                                                           deadline,
                                                           localEm,
                                                           this );
        localEm.close();
    }

    public static String toString(Reader reader) throws IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer( 1024 );
        while ( (charValue = reader.read()) != -1 ) {
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

        return MVEL.executeExpression( compiler.compile( context ),
                                       vars );
    }    

    public static class ScheduledTaskDeadline
        implements
        Callable {
        private long        taskId;
        private long        deadlineId;
        private TaskService service;

        public ScheduledTaskDeadline(long taskId,
                                     long deadlineId,
                                     TaskService service) {
            this.taskId = taskId;
            this.deadlineId = deadlineId;
            this.service = service;
        }

        public long getTaskId() {
            return taskId;
        }

        public long getDeadlineId() {
            return deadlineId;
        }

        public Object call() throws Exception {
            try {
                service.executeEscalatedDeadline( taskId,
                                                  deadlineId );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (deadlineId ^ (deadlineId >>> 32));
            result = prime * result + (int) (taskId ^ (taskId >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( !(obj instanceof ScheduledTaskDeadline) ) return false;
            ScheduledTaskDeadline other = (ScheduledTaskDeadline) obj;
            if ( deadlineId != other.deadlineId ) return false;
            if ( taskId != other.taskId ) return false;
            return true;
        }

    }
}
