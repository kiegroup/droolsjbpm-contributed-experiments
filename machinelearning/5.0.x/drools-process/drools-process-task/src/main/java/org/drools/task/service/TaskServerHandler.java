package org.drools.task.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.drools.eventmessaging.EventKey;
import org.drools.task.Attachment;
import org.drools.task.Comment;
import org.drools.task.Content;
import org.drools.task.Task;
import org.drools.task.query.TaskSummary;

public class TaskServerHandler extends IoHandlerAdapter {
    private TaskService            service;
    private Map<String, IoSession> clients;

    public TaskServerHandler(TaskService service) {
        this.service = service;
        this.clients = new HashMap<String, IoSession>();
    }

    @Override
    public void exceptionCaught(IoSession session,
                                Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session,
                                Object message) throws Exception {
        Command cmd = (Command) message;
        TaskServiceSession taskSession = service.createSession();
        TaskError error = null;
        CommandName response = null;
        try {
            switch ( cmd.getName() ) {
                case OperationRequest : {
                    response = CommandName.OperationResponse;
                    Operation operation = (Operation) cmd.getArguments().get( 0 );
                    long taskId = (Long) cmd.getArguments().get( 1 );
                    String userId = (String) cmd.getArguments().get( 2 );
                    String targetEntityId = null;
                    if ( cmd.getArguments().size() > 3 ) {
                        targetEntityId = (String) cmd.getArguments().get( 3 );
                    }
                    ContentData data = null;
                    if ( cmd.getArguments().size() > 4 ) {
                        data = (ContentData) cmd.getArguments().get( 4 );
                    }
                    error = taskSession.taskOperation( operation,
                                                       taskId,
                                                       userId,
                                                       targetEntityId,
                                                       data);
                    List args = null;
                    if ( error != null ) {
                        args = new ArrayList( 1 );
                        args.add( error );
                    } else {
                        args = Collections.emptyList();
                    }

                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.OperationResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case GetTaskRequest : {
                    response = CommandName.GetTaskResponse;
                    long taskId = (Long) cmd.getArguments().get( 0 );
                    EntityManager em = service.getEntityManagerFactory().createEntityManager();
                    try {
                        Task task = taskSession.getTask( taskId );
                        List args = new ArrayList( 1 );
                        args.add( task );
                        Command resultsCmnd = new Command( cmd.getId(),
                                                           CommandName.GetTaskResponse,
                                                           args );
                        session.write( resultsCmnd );
                    } finally {
                        em.close();
                    }
                    break;
                }
                case AddTaskRequest : {
                    response = CommandName.AddTaskResponse;
                    Task task = (Task) cmd.getArguments().get( 0 );
                    ContentData content = (ContentData) cmd.getArguments().get( 1 );
                    taskSession.addTask( task, content );

                    List args = new ArrayList( 1 );
                    args.add( task.getId() );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.AddTaskResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case AddCommentRequest : {
                    response = CommandName.AddCommentResponse;
                    Comment comment = (Comment) cmd.getArguments().get( 1 );
                    taskSession.addComment( (Long) cmd.getArguments().get( 0 ),
                                            comment );

                    List args = new ArrayList( 1 );
                    args.add( comment.getId() );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.AddCommentResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case DeleteCommentRequest : {
                    response = CommandName.DeleteCommentResponse;
                    long taskId = (Long) cmd.getArguments().get( 0 );
                    long commentId = (Long) cmd.getArguments().get( 1 );
                    taskSession.deleteComment( taskId,
                                               commentId );

                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.DeleteCommentResponse,
                                                       Collections.emptyList() );
                    session.write( resultsCmnd );
                    break;
                }
                case AddAttachmentRequest : {
                    response = CommandName.AddAttachmentResponse;
                    Attachment attachment = (Attachment) cmd.getArguments().get( 1 );
                    Content content = (Content) cmd.getArguments().get( 2 );
                    taskSession.addAttachment( (Long) cmd.getArguments().get( 0 ),
                                               attachment,
                                               content );

                    List args = new ArrayList( 2 );
                    args.add( attachment.getId() );
                    args.add( content.getId() );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.AddAttachmentResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case DeleteAttachmentRequest : {
                    response = CommandName.DeleteAttachmentResponse;
                    long taskId = (Long) cmd.getArguments().get( 0 );
                    long attachmentId = (Long) cmd.getArguments().get( 1 );
                    long contentId = (Long) cmd.getArguments().get( 2 );
                    taskSession.deleteAttachment( taskId,
                                                  attachmentId,
                                                  contentId );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.DeleteAttachmentResponse,
                                                       Collections.emptyList() );
                    session.write( resultsCmnd );
                    break;
                }
                case SetDocumentContentRequest : {
                    response = CommandName.SetDocumentContentResponse;
                    long taskId = (Long) cmd.getArguments().get( 0 );
                    Content content = (Content) cmd.getArguments().get( 1 );
                    taskSession.setDocumentContent( taskId,
                                                    content );

                    List args = new ArrayList( 1 );
                    args.add( content.getId() );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.SetDocumentContentResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case GetContentRequest : {
                    response = CommandName.GetContentResponse;
                    long contentId = (Long) cmd.getArguments().get( 0 );
                    Content content = taskSession.getContent( contentId );
                    List args = new ArrayList( 1 );
                    args.add( content );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.GetContentResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksOwned : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksOwned( (String) cmd.getArguments().get( 0 ),
                                                                           (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksAssignedAsBusinessAdministrator : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksAssignedAsBusinessAdministrator( (String) cmd.getArguments().get( 0 ),
                                                                                                     (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksAssignedAsPotentialOwner : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksAssignedAsPotentialOwner( (String) cmd.getArguments().get( 0 ),
                                                                                              (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksAssignedAsTaskInitiator : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksAssignedAsTaskInitiator( (String) cmd.getArguments().get( 0 ),
                                                                                             (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksAssignedAsExcludedOwner : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksAssignedAsExcludedOwner( (String) cmd.getArguments().get( 0 ),
                                                                                             (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksAssignedAsRecipient : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksAssignedAsRecipient( (String) cmd.getArguments().get( 0 ),
                                                                                         (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case QueryTasksAssignedAsTaskStakeholder : {
                    response = CommandName.QueryTaskSummaryResponse;
                    List<TaskSummary> results = taskSession.getTasksAssignedAsTaskStakeholder( (String) cmd.getArguments().get( 0 ),
                                                                                               (String) cmd.getArguments().get( 1 ) );
                    List args = new ArrayList( 1 );
                    args.add( results );
                    Command resultsCmnd = new Command( cmd.getId(),
                                                       CommandName.QueryTaskSummaryResponse,
                                                       args );
                    session.write( resultsCmnd );
                    break;
                }
                case RegisterForEventRequest : {
                    response = CommandName.EventTriggerResponse;
                    EventKey key = (EventKey) cmd.getArguments().get( 0 );
                    boolean remove = (Boolean) cmd.getArguments().get( 1 );
                    String uuid = (String) cmd.getArguments().get( 2 );
                    clients.put( uuid,
                                 session );
                    MinaEventTransport transport = new MinaEventTransport( uuid,
                                                                           cmd.getId(),
                                                                           clients,
                                                                           remove );
                    service.getEventKeys().register( key,
                                                     transport );
                    break;
                }
                case RegisterClient : {
                    String uuid = (String) cmd.getArguments().get( 0 );
                    clients.put( uuid,
                                 session );
                    break;
                }
                default : {
                    // unknown command
                }
            }

            Date date = new Date();
            System.out.println( "Message written : " + cmd.getName() + " : " + date );
        } catch ( Exception  e) {
            List list = new ArrayList( 1 );
            list.add( error );
            Command resultsCmnd = new Command( cmd.getId(),
                                               response,
                                               list );
            session.write( resultsCmnd );
        } finally {
            taskSession.dispose();
        }
    }

    @Override
    public void sessionIdle(IoSession session,
                            IdleStatus status) throws Exception {
        System.out.println( "IDLE " + session.getIdleCount( status ) );
    }
}