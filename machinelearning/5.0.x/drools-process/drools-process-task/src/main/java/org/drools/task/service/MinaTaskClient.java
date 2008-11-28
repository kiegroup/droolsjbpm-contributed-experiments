package org.drools.task.service;

import java.util.ArrayList;
import java.util.List;

import org.drools.eventmessaging.EventKey;
import org.drools.eventmessaging.EventResponseHandler;
import org.drools.task.Attachment;
import org.drools.task.Comment;
import org.drools.task.Content;
import org.drools.task.Task;
import org.drools.task.service.TaskClientHandler.AddAttachmentResponseHandler;
import org.drools.task.service.TaskClientHandler.AddCommentResponseHandler;
import org.drools.task.service.TaskClientHandler.AddTaskResponseHandler;
import org.drools.task.service.TaskClientHandler.DeleteAttachmentResponseHandler;
import org.drools.task.service.TaskClientHandler.DeleteCommentResponseHandler;
import org.drools.task.service.TaskClientHandler.GetContentResponseHandler;
import org.drools.task.service.TaskClientHandler.GetTaskResponseHandler;
import org.drools.task.service.TaskClientHandler.SetDocumentResponseHandler;
import org.drools.task.service.TaskClientHandler.TaskOperationResponseHandler;
import org.drools.task.service.TaskClientHandler.TaskSummaryResponseHandler;

public class MinaTaskClient extends BaseMinaClient {

    public MinaTaskClient(String name,
                          TaskClientHandler handler) {
        super( name,
               handler );
    }

    public void addTask(Task task, ContentData content, 
                        AddTaskResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( task );
        args.add( content );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.AddTaskRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void getTask(long taskId,
                        GetTaskResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 1 );
        args.add( taskId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.GetTaskRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );

    }

    public void addComment(long taskId,
                           Comment comment,
                           AddCommentResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( taskId );
        args.add( comment );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.AddCommentRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void deleteComment(long taskId,
                              long commentId,
                              DeleteCommentResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( taskId );
        args.add( commentId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.DeleteCommentRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void addAttachment(long taskId,
                              Attachment attachment,
                              Content content,
                              AddAttachmentResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( taskId );
        args.add( attachment );
        args.add( content );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.AddAttachmentRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void deleteAttachment(long taskId,
                                 long attachmentId,
                                 long contentId,
                                 DeleteAttachmentResponseHandler responseHandler ) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( taskId );
        args.add( attachmentId );
        args.add( contentId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.DeleteAttachmentRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        
        session.write( cmd );
    }

    public void setDocumentContent(long taskId,
                                   Content content,
                                   SetDocumentResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( taskId );
        args.add( content );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.SetDocumentContentRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void getContent(long contentId,
                           GetContentResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 1 );
        args.add( contentId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.GetContentRequest,
                                   args );

        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void claim(long taskId,
                      String userId,
                      TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Claim );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void start(long taskId,
                      String userId,
                      TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Start );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void stop(long taskId,
                     String userId,
                     TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Stop );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void release(long taskId,
                        String userId,
                        TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Release );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void suspend(long taskId,
                        String userId,
                        TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Suspend );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void resume(long taskId,
                       String userId,
                       TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Resume );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void skip(long taskId,
                     String userId,
                     TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Skip );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }
    
    public void delegate(long taskId,
                         String userId,
                         String targetUserId,
                         TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 4 );
        args.add( Operation.Delegate );
        args.add( taskId );
        args.add( userId );
        args.add( targetUserId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );      
    }
    
    public void forward(long taskId,
                        String userId,
                        String targetEntityId,
                        TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 4 );
        args.add( Operation.Forward );
        args.add( taskId );
        args.add( userId );
        args.add( targetEntityId );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );      
    }    

    public void complete(long taskId,
                         String userId,
                         ContentData outputData,
                         TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 5 );
        args.add( Operation.Complete );
        args.add( taskId );
        args.add( userId );
        args.add( null );
        args.add( outputData );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void fail(long taskId,
                     String userId,
                     FaultData faultData,
                     TaskOperationResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 5 );
        args.add( Operation.Fail );
        args.add( taskId );
        args.add( userId );
        args.add( null );
        args.add( faultData );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );

        session.write( cmd );
    }

    public void getTasksOwned(String userId,
                              String language,
                              TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksOwned,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void getTasksAssignedAsBusinessAdministrator(String userId,
                                                        String language,
                                                        TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsBusinessAdministrator,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void getTasksAssignedAsExcludedOwner(String userId,
                                                String language,
                                                TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsExcludedOwner,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void getTasksAssignedAsPotentialOwner(String userId,
                                                 String language,
                                                 TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsPotentialOwner,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void getTasksAssignedAsRecipient(String userId,
                                            String language,
                                            TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsRecipient,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void getTasksAssignedAsTaskInitiator(String userId,
                                                String language,
                                                TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsTaskInitiator,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void getTasksAssignedAsTaskStakeholder(String userId,
                                                  String language,
                                                  TaskSummaryResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsTaskStakeholder,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

    public void registerForEvent(EventKey key,
                                 boolean remove,
                                 EventResponseHandler responseHandler) {
        List<Object> args = new ArrayList<Object>( 3 );
        args.add( key );
        args.add( remove );
        args.add( this.name );
        Command cmd = new Command( counter.getAndIncrement(),
                                   CommandName.RegisterForEventRequest,
                                   args );
        handler.addResponseHandler( cmd.getId(),
                                    responseHandler );
        session.write( cmd );
    }

}