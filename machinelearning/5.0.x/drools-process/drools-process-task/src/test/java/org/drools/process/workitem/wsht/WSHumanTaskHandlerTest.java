package org.drools.process.workitem.wsht;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.drools.task.AccessType;
import org.drools.task.BaseTest;
import org.drools.task.Content;
import org.drools.task.Status;
import org.drools.task.Task;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.BlockingGetContentResponseHandler;
import org.drools.task.service.BlockingGetTaskResponseHandler;
import org.drools.task.service.BlockingTaskOperationResponseHandler;
import org.drools.task.service.BlockingTaskSummaryResponseHandler;
import org.drools.task.service.ContentData;
import org.drools.task.service.MinaTaskClient;
import org.drools.task.service.MinaTaskServer;
import org.drools.task.service.TaskClientHandler;

public class WSHumanTaskHandlerTest extends BaseTest {
	
    MinaTaskServer server;
    MinaTaskClient client;
    WSHumanTaskHandler handler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new MinaTaskServer( taskService );
        Thread thread = new Thread( server );
        thread.start();
        Thread.sleep( 500 );
        client = new MinaTaskClient("client 1", new TaskClientHandler());
		NioSocketConnector connector = new NioSocketConnector();
		SocketAddress address = new InetSocketAddress("127.0.0.1", 9123);
		client.connect(connector, address);
		handler = new WSHumanTaskHandler();
    }

    protected void tearDown() throws Exception {
        handler.dispose();
        client.disconnect();
        server.stop();
        super.tearDown();
    }
    
	public void testTask() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
		TaskSummary task = tasks.get(0);
		assertEquals("TaskName", task.getName());
		assertEquals(10, task.getPriority());
		assertEquals("Comment", task.getDescription());
		assertEquals(Status.Reserved, task.getStatus());
		assertEquals("Darth Vader", task.getActualOwner().getId());
		
		System.out.println("Starting task " + task.getId());
		BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.start(task.getId(), "Darth Vader", operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Started task " + task.getId());
		
		System.out.println("Completing task " + task.getId());
		operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Completed task " + task.getId());
		
		assertTrue(manager.isCompleted());
	}
	
	public void testTaskMultipleActors() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader, Dalai Lama");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
		TaskSummary task = tasks.get(0);
		assertEquals("TaskName", task.getName());
		assertEquals(10, task.getPriority());
		assertEquals("Comment", task.getDescription());
		assertEquals(Status.Ready, task.getStatus());
		
		System.out.println("Claiming task " + task.getId());
		BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.claim(task.getId(), "Darth Vader", operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Claimed task " + task.getId());
		
		System.out.println("Starting task " + task.getId());
		operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.start(task.getId(), "Darth Vader", operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Started task " + task.getId());
		
		System.out.println("Completing task " + task.getId());
		operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.complete(task.getId(), "Darth Vader", null, operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Completed task " + task.getId());
		
		assertTrue(manager.isCompleted());
	}
	
	public void testTaskFail() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
		TaskSummary task = tasks.get(0);
		assertEquals("TaskName", task.getName());
		assertEquals(10, task.getPriority());
		assertEquals("Comment", task.getDescription());
		assertEquals(Status.Reserved, task.getStatus());
		assertEquals("Darth Vader", task.getActualOwner().getId());
		
		System.out.println("Starting task " + task.getId());
		BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.start(task.getId(), "Darth Vader", operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Started task " + task.getId());
		
		System.out.println("Failing task " + task.getId());
		operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.fail(task.getId(), "Darth Vader", null, operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Failed task " + task.getId());
		
		assertTrue(manager.isAborted());
	}
	
	public void testTaskSkip() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
		TaskSummary task = tasks.get(0);
		assertEquals("TaskName", task.getName());
		assertEquals(10, task.getPriority());
		assertEquals("Comment", task.getDescription());
		assertEquals(Status.Reserved, task.getStatus());
		assertEquals("Darth Vader", task.getActualOwner().getId());
		
		System.out.println("Skipping task " + task.getId());
		BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.skip(task.getId(), "Darth Vader", operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Skipped task " + task.getId());
		
		assertTrue(manager.isAborted());
	}
	
	public void testTaskAbortSkippable() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		handler.abortWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(0, tasks.size());
	}
	
	public void testTaskAbortNotSkippable() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader");
		workItem.setParameter("Skippable", "false");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
		
		handler.abortWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
	}
	
	public void testTaskData() throws Exception {
		TestWorkItemManager manager = new TestWorkItemManager();
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setParameter("TaskName", "TaskName");
		workItem.setParameter("Comment", "Comment");
		workItem.setParameter("Priority", "10");
		workItem.setParameter("ActorId", "Darth Vader");
		workItem.setParameter("Content", "This is the content");
		handler.executeWorkItem(workItem, manager);
		
		Thread.sleep(500);
		
		BlockingTaskSummaryResponseHandler responseHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksAssignedAsPotentialOwner("Darth Vader", "en-UK", responseHandler);
		List<TaskSummary> tasks = responseHandler.getResults();
		assertEquals(1, tasks.size());
		TaskSummary taskSummary = tasks.get(0);
		assertEquals("TaskName", taskSummary.getName());
		assertEquals(10, taskSummary.getPriority());
		assertEquals("Comment", taskSummary.getDescription());
		assertEquals(Status.Reserved, taskSummary.getStatus());
		assertEquals("Darth Vader", taskSummary.getActualOwner().getId());
		
		BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();
		client.getTask(taskSummary.getId(), getTaskResponseHandler);
		Task task = getTaskResponseHandler.getTask();
		assertEquals(AccessType.Inline, task.getTaskData().getDocumentAccessType());
		long contentId = task.getTaskData().getDocumentContentId();
		assertTrue(contentId != -1);
		BlockingGetContentResponseHandler getContentResponseHandler = new BlockingGetContentResponseHandler();
		client.getContent(contentId, getContentResponseHandler);
		ByteArrayInputStream bis = new ByteArrayInputStream(getContentResponseHandler.getContent().getContent());
		ObjectInputStream in = new ObjectInputStream(bis);
		Object data = in.readObject();
		in.close();
		assertEquals("This is the content", data);
		
		System.out.println("Starting task " + task.getId());
		BlockingTaskOperationResponseHandler operationResponseHandler = new BlockingTaskOperationResponseHandler();
		client.start(task.getId(), "Darth Vader", operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Started task " + task.getId());
		
		System.out.println("Completing task " + task.getId());
		operationResponseHandler = new BlockingTaskOperationResponseHandler();
		ContentData result = new ContentData();
		result.setAccessType(AccessType.Inline);
		result.setType("java.lang.String");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject("This is the result");
		out.close();
		result.setContent(bos.toByteArray());
		client.complete(task.getId(), "Darth Vader", result, operationResponseHandler);
		operationResponseHandler.waitTillDone(5000);
		System.out.println("Completed task " + task.getId());
		
		assertTrue(manager.isCompleted());
		Map<String, Object> results = manager.getResults();
		assertNotNull(results);
		assertEquals("Darth Vader", results.get("ActorId"));
		assertEquals("This is the result", results.get("Result"));
	}
	
	private class TestWorkItemManager implements WorkItemManager  {
		
		private boolean completed;
		private boolean aborted;
		private Map<String, Object> results;
		
		public void abortWorkItem(long id) {
			aborted = true;
		}
		
		public boolean isAborted() {
			return aborted;
		}

		public void completeWorkItem(long id, Map<String, Object> results) {
			completed = true;
			this.results = results;
		}
		
		public boolean isCompleted() {
			return completed;
		}

		public WorkItem getWorkItem(long id) {
			return null;
		}

		public Set<WorkItem> getWorkItems() {
			return null;
		}
		
		public Map<String, Object> getResults() {
			return results;
		}

		public void internalAbortWorkItem(long id) {
		}

		public void internalAddWorkItem(WorkItem workItem) {
		}

		public void internalExecuteWorkItem(WorkItem workItem) {
		}

		public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
		}
		
	}
}
