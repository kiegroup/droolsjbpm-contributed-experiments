package org.drools.task.service;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.drools.eventmessaging.EventResponseHandler;
import org.drools.eventmessaging.Payload;
import org.drools.task.Content;
import org.drools.task.Task;
import org.drools.task.query.TaskSummary;

public class TaskClientHandler extends BaseMinaHandler {
    private MinaTaskClient client;

    public MinaTaskClient getClient() {
        return client;
    }

    public void setClient(MinaTaskClient client) {
        this.client = client;
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
        switch ( cmd.getName() ) {
            case OperationResponse : {
                TaskOperationResponseHandler responseHandler = (TaskOperationResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.setIsDone( true );
                    }
                }
                break;
            }
            case GetTaskResponse : {
                Task task = (Task) cmd.getArguments().get( 0 );
                GetTaskResponseHandler responseHandler = (GetTaskResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( task );
                    }
                }
                break;
            }
            case AddTaskResponse : {
                long taskId = (Long) cmd.getArguments().get( 0 );
                AddTaskResponseHandler responseHandler = (AddTaskResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( taskId );
                    }
                }
                break;
            }
            case AddCommentResponse : {
                long commentId = (Long) cmd.getArguments().get( 0 );
                AddCommentResponseHandler responseHandler = (AddCommentResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( commentId );
                    }
                }
                break;
            }
            case DeleteCommentResponse : {
                DeleteCommentResponseHandler responseHandler = (DeleteCommentResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.setIsDone( true );
                    }
                }
                break;                
            }
            case AddAttachmentResponse : {
                long attachmentId = (Long) cmd.getArguments().get( 0 );
                long contentId = (Long) cmd.getArguments().get( 1 );
                AddAttachmentResponseHandler responseHandler = (AddAttachmentResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( attachmentId,
                                                 contentId );
                    }
                }
                break;
            }
            case DeleteAttachmentResponse : {
                DeleteAttachmentResponseHandler responseHandler = (DeleteAttachmentResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.setIsDone( true );
                    }
                }
                break;                
            }            
            case GetContentResponse : {
                Content content = (Content) cmd.getArguments().get( 0 );
                GetContentResponseHandler responseHandler = (GetContentResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( content );
                    }
                }
                break;
            }
            case SetDocumentContentResponse : {
                long contentId = (Long) cmd.getArguments().get( 0 );
                SetDocumentResponseHandler responseHandler = (SetDocumentResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( contentId );
                    }
                }
                break;
            }
            case QueryTaskSummaryResponse : {
                List<TaskSummary> results = (List<TaskSummary>) cmd.getArguments().get( 0 );
                TaskSummaryResponseHandler responseHandler = (TaskSummaryResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( results );
                    }
                }
                break;
            }
            case EventTriggerResponse : {
                Payload payload = (Payload) cmd.getArguments().get( 0 );
                EventResponseHandler responseHandler = (EventResponseHandler) responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() &&  cmd.getArguments().get( 0 ) instanceof TaskError ) {
                        responseHandler.setError( ((TaskError) cmd.getArguments().get( 0 )).getMessage() );
                    } else {
                        responseHandler.execute( payload );
                    }
                }
                break;
            }
            default : {

            }
        }
    }

    public static interface GetTaskResponseHandler
        extends
        ResponseHandler {
        public void execute(Task task);
    }

    public static interface AddTaskResponseHandler
        extends
        ResponseHandler {
        public void execute(long taskId);
    }

    public static interface TaskOperationResponseHandler
        extends
        ResponseHandler {
        public void setIsDone(boolean done);
    }

    public static interface AddCommentResponseHandler
        extends
        ResponseHandler {
        public void execute(long commentId);
    }

    public static interface DeleteCommentResponseHandler
        extends
        ResponseHandler {
        public void setIsDone(boolean done);
    }

    public static interface AddAttachmentResponseHandler
        extends
        ResponseHandler {
        public void execute(long attachmentId,
                            long contentId);
    }

    public static interface DeleteAttachmentResponseHandler
        extends
        ResponseHandler {
        public void setIsDone(boolean done);
    }

    public static interface SetDocumentResponseHandler
        extends
        ResponseHandler {
        public void execute(long contentId);
    }

    public static interface GetContentResponseHandler
        extends
        ResponseHandler {
        public void execute(Content content);
    }

    public static interface TaskSummaryResponseHandler
        extends
        ResponseHandler {
        public void execute(List<TaskSummary> results);
    }
}