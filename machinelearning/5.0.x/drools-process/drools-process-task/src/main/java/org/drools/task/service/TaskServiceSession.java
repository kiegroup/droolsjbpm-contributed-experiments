package org.drools.task.service;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.task.Attachment;
import org.drools.task.Comment;
import org.drools.task.Content;
import org.drools.task.Deadline;
import org.drools.task.Group;
import org.drools.task.OrganizationalEntity;
import org.drools.task.PeopleAssignments;
import org.drools.task.Status;
import org.drools.task.Task;
import org.drools.task.TaskData;
import org.drools.task.User;
import org.drools.task.query.DeadlineSummary;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.TaskService.ScheduledTaskDeadline;

public class TaskServiceSession {
	
    private TaskService service;
    private EntityManager em;
    private Map<String, RuleBase> ruleBases;  
    private Map<String, Map<String, Object>> globals;  

    public TaskServiceSession(TaskService service,
                              EntityManager em) {
        this.service = service;
        this.em = em;
    }

    public void dispose() {
        em.close();
    }

    public EntityManager getEntityManager() {
        return em;
    }
    
    public void setRuleBase(String type, RuleBase ruleBase) {
    	if (ruleBases == null) {
    		ruleBases = new HashMap<String, RuleBase>();
    	}
    	ruleBases.put(type, ruleBase);
    }
    
    public void setGlobals(String type, Map<String, Object> globals) {
    	if (this.globals == null) {
    		this.globals = new HashMap<String, Map<String, Object>>();
    	}
    	this.globals.put(type, globals);
    }

    public void addUser(User user) {
        em.getTransaction().begin();
        em.persist( user );
        em.getTransaction().commit();
    }

    public void addGroup(Group group) {
        em.getTransaction().begin();
        em.persist( group );
        em.getTransaction().commit();
    }

    public void addTask(Task task, ContentData contentData) {
        TaskData taskData = task.getTaskData();
        // new tasks start off with status created
        taskData.setStatus( Status.Created );

        // execute "addTask" rules
        if (ruleBases != null) {
        	RuleBase ruleBase = ruleBases.get("addTask");
        	if (ruleBase != null) {
        		StatefulSession session = ruleBase.newStatefulSession();
    			Map<String, Object> globals = this.globals.get("addTask");
    			if (globals != null) {
    				for (Map.Entry<String, Object> entry: globals.entrySet()) {
    					session.setGlobal(entry.getKey(), entry.getValue());
    				}
    			}
    			TaskServiceRequest request = new TaskServiceRequest("addTask", null, null);
    			session.setGlobal("request", request);
        		session.insert(task);
        		session.insert(contentData);
        		session.fireAllRules();
        		if (!request.isAllowed()) {
        			String error = "Cannot add Task:\n";
        			if (request.getReasons() != null) {
        				for (String reason: request.getReasons()) {
        					error += reason + "\n";
        				}
        			}
        			throw new RuntimeException(error);
        		}
        	}
        }

        if ( task.getPeopleAssignments() != null ) {
            List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();
            if ( potentialOwners.size() == 1 ) {
                // if there is a single potential owner, assign and set status to Reserved
                taskData.setActualOwner( (User) potentialOwners.get( 0 ) );
                taskData.setStatus( Status.Reserved );
            } else if ( potentialOwners.size() > 1 ) {
                // multiple potential owners, so set to Ready so one can claim.
                taskData.setStatus( Status.Ready );
            } else {
                //@TODO we have no potential owners
            }
        } else {
            //@TODO we have no potential owners
        }

        // set the CreatedOn date if it's not already set
        if ( taskData.getCreatedOn() == null ) {
            taskData.setCreatedOn( new Date() );
        }

        //@FIXME for now we activate on creation, unless date is supplied
        if ( taskData.getActivationTime() == null ) {
            taskData.setActivationTime( taskData.getCreatedOn() );
        }
        
        em.getTransaction().begin();
        em.persist( task );
        if (contentData != null) {
        	Content content = new Content();
        	content.setContent(contentData.getContent());
        	em.persist( content );
            taskData.setDocumentAccessType( contentData.getAccessType() );
            taskData.setDocumentType( contentData.getType() );
            taskData.setDocumentContentId( content.getId() );
        }
        em.getTransaction().commit();

        long now = System.currentTimeMillis();
        // schedule after it's been persisted, otherwise the id's won't be assigned
        if ( task.getDeadlines() != null ) {
            if ( task.getDeadlines().getStartDeadlines() != null ) {
                for ( Deadline deadline : task.getDeadlines().getStartDeadlines() ) {
                    if ( !deadline.isEscalated() ) {
                        // only escalate when true - typically this would only be true
                        // if the user is requested that the notification should never be escalated
                        Date date = deadline.getDate();
                        service.schedule( new ScheduledTaskDeadline( task.getId(),
                                                                     deadline.getId(),
                                                                     service ),
                                          date.getTime() - now );
                    }
                }
            }

            if ( task.getDeadlines().getEndDeadlines() != null ) {
                for ( Deadline deadline : task.getDeadlines().getEndDeadlines() ) {
                    // only escalate when true - typically this would only be true
                    // if the user is requested that the notification should never be escalated
                    if ( !deadline.isEscalated() ) {
                        Date date = deadline.getDate();
                        service.schedule( new ScheduledTaskDeadline( task.getId(),
                                                                     deadline.getId(),
                                                                     service ),
                                          date.getTime() - now );
                    }
                }
            }
        }

        if ( task.getTaskData().getStatus() == Status.Reserved ) {
            // Task was reserved so owner should get icals
            SendIcal.getInstance().sendIcalForTask( task,
                                                    service.getUserinfo() );

            // trigger event support
            service.getEventSupport().fireTaskClaimed( task.getId(),
                                                       task.getTaskData().getActualOwner().getId() );
        }
    }

    public TaskError evalCommand(Operation operation,
                                 List<OperationCommand> commands,
                                 Task task,
                                 User user,
                                 OrganizationalEntity targetEntity) {
        TaskData taskData = task.getTaskData();
        boolean statusMatched = false;
        for ( OperationCommand command : commands ) {
            // first find out if we have a matching status
            if ( command.getStatus() != null ) {
                for ( Status status : command.getStatus() ) {
                    if ( taskData.getStatus() == status ) {
                        statusMatched = true;

                        // next find out if the user can execute this operation                
                        if ( !isAllowed( command,
                                         task,
                                         user,
                                         targetEntity ) ) {
                            return new TaskError( "User '" + user + "' does not have permissions to execution operation '" + operation + "' on task id " + task.getId() );

                        }

                        commands( command,
                                  task,
                                  user,
                                  targetEntity);
                        return null;
                    }
                }
            }

            if ( command.getPreviousStatus() != null ) {
                for ( Status status : command.getPreviousStatus() ) {
                    if ( taskData.getPreviousStatus() == status ) {
                        statusMatched = true;

                        // next find out if the user can execute this operation                
                        if ( !isAllowed( command,
                                         task,
                                         user,
                                         targetEntity ) ) {
                            return new TaskError( "User '" + user + "' does not have permissions to execution operation '" + operation + "' on task id " + task.getId() );
                        }

                        commands( command,
                                  task,
                                  user,
                                  targetEntity);
                        return null;
                    }
                }
            }
        }
        if ( !statusMatched ) {
            return new TaskError( "User '" + user + "' was unable to execution operation '" + operation + "' on task id " + task.getId() + " due to no 'current status' matchines" );
        }

        return null;
    }

    private boolean isAllowed(OperationCommand command,
                              Task task,
                              User user,
                              OrganizationalEntity targetEntity) {
        PeopleAssignments people = task.getPeopleAssignments();
        TaskData taskData = task.getTaskData();

        boolean operationAllowed = false;
        for ( Allowed allowed : command.getAllowed() ) {
            if ( operationAllowed ) {
                break;
            }
            switch ( allowed ) {
                case Owner : {
                    operationAllowed = (taskData.getActualOwner() != null && taskData.getActualOwner().getId() == user.getId());
                    break;
                }
                case Initiator : {
                    operationAllowed = (taskData.getCreatedBy() != null && taskData.getCreatedBy().getId() == user.getId());
                    break;
                }
                case PotentialOwner : {
                    operationAllowed = isAllowed( user,
                                                  people.getPotentialOwners() );
                    break;
                }
                case BusinessAdministrator : {
                    operationAllowed = isAllowed( user,
                                                  people.getBusinessAdministrators() );
                    break;
                }
            }
        }

        if ( operationAllowed && command.isUserIsExplicitPotentialOwner() ) {
            // if user has rights to execute the command, make sure user is explicitely specified (not as a group)
            operationAllowed = people.getPotentialOwners().contains( user );
        }
        
        if ( operationAllowed && command.isSkippable() ) {
        	operationAllowed = taskData.isSkipable();
        }

        return operationAllowed;
    }

    private void commands(OperationCommand command,
                          Task task,
                          User user,
                          OrganizationalEntity targetEntity) {
        PeopleAssignments people = task.getPeopleAssignments();
        TaskData taskData = task.getTaskData();

        if ( command.getNewStatus() != null ) {
            taskData.setStatus( command.getNewStatus() );
        } else if ( command.isSetToPreviousStatus() ) {
            taskData.setStatus( taskData.getPreviousStatus() );
        }

        if ( command.isAddTargetEntityToPotentialOwners() && !people.getPotentialOwners().contains( targetEntity ) ) {
            people.getPotentialOwners().add( targetEntity );
        }

        if ( command.isRemoveUserFromPotentialOwners() ) {
            people.getPotentialOwners().remove( user );
        }

        if ( command.isSetNewOwnerToUser() ) {
            taskData.setActualOwner( (User) user );
        }

        if ( command.isSetNewOwnerToNull() ) {
            taskData.setActualOwner( null );
        }

        if ( command.getExec() != null ) {
            switch ( command.getExec() ) {
                case Claim : {
                    taskData.setActualOwner( (User) targetEntity );
                    // Task was reserved so owner should get icals
                    SendIcal.getInstance().sendIcalForTask( task,
                                                            service.getUserinfo() );

                    // trigger event support
                    service.getEventSupport().fireTaskClaimed( task.getId(),
                                                               task.getTaskData().getActualOwner().getId() );
                    break;
                }
            }
        }
    }

    public TaskError taskOperation(Operation operation,
                                   long taskId,
                                   String userId,
                                   String targetEntityId,
                                   ContentData data) {
        Task task = em.find( Task.class,
                             taskId );

        User user = em.find( User.class,
                             userId );

        OrganizationalEntity targetEntity = null;
        if ( targetEntityId != null ) {
            targetEntity = em.find( OrganizationalEntity.class,
                                    targetEntityId );
        }
        
        em.getTransaction().begin();
        TaskError error = null;
        try {
            Map<Operation, List<OperationCommand>> dsl = service.getOperations();
            List<OperationCommand> commands = dsl.get( operation );
            error = evalCommand( operation,
                                 commands,
                                 task,
                                 user,
                                 targetEntity);

            if ( error != null ) {
                throw new RuntimeException( "TaskOperationException" );
            }
            switch ( operation ) {
                case Claim : {
                    // Task was reserved so owner should get icals
                    SendIcal.getInstance().sendIcalForTask( task,
                                                            service.getUserinfo() );
                    // trigger event support
                    service.getEventSupport().fireTaskClaimed( task.getId(),
                                                               task.getTaskData().getActualOwner().getId() );
                    break;
                }
                
                case Complete : {
                	// set output data 
                	if (data != null) {
	                	TaskData taskData = task.getTaskData();
	                	Content content = new Content();
	                	content.setContent( data.getContent() );
	                	em.persist( content );
	                	taskData.setOutputContentId( content.getId() );
	                	taskData.setOutputAccessType( data.getAccessType() );
	                	taskData.setOutputType( data.getType() );
                	}

                	// trigger event support
                    service.getEventSupport().fireTaskCompleted( task.getId(),
                                                                 task.getTaskData().getActualOwner().getId() );
                    break;
                }
                
                case Fail : {
                	// set fault data 
                	if (data != null) {
	                	TaskData taskData = task.getTaskData();
	                	Content content = new Content();
	                	content.setContent( data.getContent() );
	                	em.persist( content );
	                	taskData.setFaultContentId( content.getId() );
	                	taskData.setFaultAccessType( data.getAccessType() );
	                	taskData.setFaultType( data.getType() );
	                	taskData.setFaultName( ((FaultData) data).getFaultName() );
                	}

                    // trigger event support
                    service.getEventSupport().fireTaskFailed( task.getId(),
                                                              task.getTaskData().getActualOwner().getId() );
                    break;
                }

                case Skip : {
                    // trigger event support
                    service.getEventSupport().fireTaskSkipped( task.getId(),
                                                               userId );
                    break;
                }
                
            }

        } catch ( Exception e ) {
        	e.printStackTrace();
            em.getTransaction().rollback();

            em.getTransaction().begin();
            task.getTaskData().setStatus( Status.Error );
            em.getTransaction().commit();

            error = new TaskError( "User '" + user + "' was unable to execution operation '" + operation + "' on task id " + task.getId() + " due to exception:\n" + e.getMessage() );
        } finally {
            if ( em.getTransaction().isActive() ) {
                em.getTransaction().commit();
            }
        }
        return error;
    }

    public void addComment(long taskId,
                           Comment comment) {
        Task task = em.find( Task.class,
                             taskId );
        if ( task == null ) {
            // throw some exception
        }

        em.getTransaction().begin();

        List<Comment> list = task.getTaskData().getComments();
        if ( list == null || list == Collections.<Comment> emptyList() ) {
            list = new ArrayList<Comment>( 1 );
            task.getTaskData().setComments( list );
        }

        list.add( comment );

        em.getTransaction().commit();
    }

    public void addAttachment(long taskId,
                              Attachment attachment,
                              Content content) {
        Task task = em.find( Task.class,
                             taskId );

        if ( task == null ) {
            // throw some exception
        }

        em.getTransaction().begin();

        em.persist( content );
        attachment.setSize( content.getContent().length );
        attachment.setAttachmentContentId( content.getId() );

        List<Attachment> list = task.getTaskData().getAttachments();
        if ( list == null || list == Collections.<Attachment> emptyList() ) {
            list = new ArrayList<Attachment>( 1 );
            task.getTaskData().setAttachments( list );
        }

        list.add( attachment );
        em.getTransaction().commit();
    }

    public void setDocumentContent(long taskId,
                                   Content content) {
        Task task = em.find( Task.class,
                             taskId );

        if ( task == null ) {
            // throw some exception
        }

        em.getTransaction().begin();

        em.persist( content );

        task.getTaskData().setDocumentContentId( content.getId() );

        em.getTransaction().commit();
    }

    public Content getContent(long contentId) {
        Content content = em.find( Content.class,
                                   contentId );
        return content;
    }

    public void deleteAttachment(long taskId,
                                 long attachmentId,
                                 long contentId) {
        // @TODO I can't get this to work with HQL deleting the Attachment. Hibernate needs both the item removed from the collection
        // and also the item deleted, so for now have to load the entire Task, I suspect that this is due to using the same EM which 
        // is caching things.
        Task task = em.find( Task.class,
                             taskId );

        em.getTransaction().begin();
        for ( Iterator<Attachment> it = task.getTaskData().getAttachments().iterator(); it.hasNext(); ) {
            Attachment attachment = it.next();
            if ( attachment.getId() == attachmentId ) {
                it.remove();
                em.remove( attachment ); // need to do this otherwise it just removes the link id, without removing the attachment
                break;
            }
        }

        // we do this as HQL to avoid streaming in the entire HQL
        String deleteContent = "delete from Content where id = :id";
        em.createQuery( deleteContent ).setParameter( "id",
                                                      contentId ).executeUpdate();

        em.getTransaction().commit();
    }

    public void deleteComment(long taskId,
                              long commentId) {
        // @TODO I can't get this to work with HQL deleting the Comment. Hibernate needs both the item removed from the collection
        // and also the item deleted, so for now have to load the entire Task, I suspect that this is due to using the same EM which 
        // is caching things.
        Task task = em.find( Task.class,
                             taskId );
        em.getTransaction().begin();
        for ( Iterator<Comment> it = task.getTaskData().getComments().iterator(); it.hasNext(); ) {
            Comment comment = it.next();
            if ( comment.getId() == commentId ) {
                it.remove();
                em.remove( comment ); // need to do this otherwise it just removes the link id, without removing the comment
                break;
            }
        }
        em.getTransaction().commit();
    }

    public Task getTask(long taskId) {
        Task task = em.find( Task.class,
                             taskId );
        return task;
    }

    public List<DeadlineSummary> getUnescalatedDeadlines() {
        return (List<DeadlineSummary>) em.createNamedQuery( "UnescalatedDeadlines" ).getResultList();
    }

    public List<TaskSummary> getTasksOwned(String userId,
                                           String language) {
        Query tasksOwned = em.createNamedQuery( "TasksOwned" );
        tasksOwned.setParameter( "userId",
                                 userId );
        tasksOwned.setParameter( "language",
                                 language );
        List<TaskSummary> list = (List<TaskSummary>) tasksOwned.getResultList();
        return list;
    }

    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String userId,
                                                                     String language) {
        Query tasksAssignedAsBusinessAdministrator = em.createNamedQuery( "TasksAssignedAsBusinessAdministrator" );
        tasksAssignedAsBusinessAdministrator.setParameter( "userId",
                                                           userId );
        tasksAssignedAsBusinessAdministrator.setParameter( "language",
                                                           language );
        List<TaskSummary> list = (List<TaskSummary>) tasksAssignedAsBusinessAdministrator.getResultList();
        return list;
    }

    public List<TaskSummary> getTasksAssignedAsExcludedOwner(String userId,
                                                             String language) {
        Query tasksAssignedAsExcludedOwner = em.createNamedQuery( "TasksAssignedAsExcludedOwner" );
        tasksAssignedAsExcludedOwner.setParameter( "userId",
                                                   userId );
        tasksAssignedAsExcludedOwner.setParameter( "language",
                                                   language );
        List<TaskSummary> list = (List<TaskSummary>) tasksAssignedAsExcludedOwner.getResultList();
        return list;
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
                                                              String language) {
        Query tasksAssignedAsPotentialOwner = em.createNamedQuery( "TasksAssignedAsPotentialOwner" );
        tasksAssignedAsPotentialOwner.setParameter( "userId",
                                                    userId );
        tasksAssignedAsPotentialOwner.setParameter( "language",
                                                    language );
        List<TaskSummary> list = (List<TaskSummary>) tasksAssignedAsPotentialOwner.getResultList();
        return list;
    }

    public List<TaskSummary> getTasksAssignedAsRecipient(String userId,
                                                         String language) {
        Query tasksAssignedAsRecipient = em.createNamedQuery( "TasksAssignedAsRecipient" );
        tasksAssignedAsRecipient.setParameter( "userId",
                                               userId );
        tasksAssignedAsRecipient.setParameter( "language",
                                               language );
        List<TaskSummary> list = (List<TaskSummary>) tasksAssignedAsRecipient.getResultList();
        return list;
    }

    public List<TaskSummary> getTasksAssignedAsTaskInitiator(String userId,
                                                             String language) {
        Query tasksAssignedAsTaskInitiator = em.createNamedQuery( "TasksAssignedAsTaskInitiator" );
        tasksAssignedAsTaskInitiator.setParameter( "userId",
                                                   userId );
        tasksAssignedAsTaskInitiator.setParameter( "language",
                                                   language );
        List<TaskSummary> list = (List<TaskSummary>) tasksAssignedAsTaskInitiator.getResultList();
        return list;
    }

    public List<TaskSummary> getTasksAssignedAsTaskStakeholder(String userId,
                                                               String language) {
        Query tasksAssignedAsTaskStakeholder = em.createNamedQuery( "TasksAssignedAsTaskStakeholder" );
        tasksAssignedAsTaskStakeholder.setParameter( "userId",
                                                     userId );
        tasksAssignedAsTaskStakeholder.setParameter( "language",
                                                     language );
        List<TaskSummary> list = (List<TaskSummary>) tasksAssignedAsTaskStakeholder.getResultList();
        return list;
    }

    public boolean isAllowed(User user,
                             List<OrganizationalEntity>[] people) {
        for ( List<OrganizationalEntity> list : people ) {
            if ( isAllowed( user,
                            list ) ) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowed(User user,
                             List<OrganizationalEntity> entities) {
        // for now just do a contains, I'll figure out group membership later.
        for ( OrganizationalEntity entity : entities ) {
            if ( entity.getId().equals( user.getId() ) ) {
                return true;
            }
        }
        return false;
    }

    public static String toString(Reader reader) throws IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer( 1024 );
        while ( (charValue = reader.read()) != -1 ) {
            //result = result + (char) charValue;
            sb.append( (char) charValue );
        }
        return sb.toString();
    }
}
