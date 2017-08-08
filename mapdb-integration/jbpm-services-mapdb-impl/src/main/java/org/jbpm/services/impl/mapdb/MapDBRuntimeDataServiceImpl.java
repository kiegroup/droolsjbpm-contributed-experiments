package org.jbpm.services.impl.mapdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jbpm.kie.services.api.DeploymentIdResolver;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.kie.services.impl.security.DeploymentRolesManager;
import org.jbpm.runtime.manager.impl.identity.UserDataServiceProvider;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.jbpm.services.impl.mapdb.model.MapDBProcessInstanceDesc;
import org.jbpm.services.impl.mapdb.model.ProcessInstanceDescSerializer;
import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.persistence.query.MapDBQuery;
import org.jbpm.services.task.persistence.query.MapDBQueryRegistry;
import org.jbpm.shared.services.impl.QueryManager;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.internal.task.query.TaskSummaryQueryBuilder;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;


public class MapDBRuntimeDataServiceImpl implements RuntimeDataService, DeploymentEventListener {

    private final DB db;
    
    private final BTreeMap<Long, MapDBProcessInstanceDesc> processInstancesById;
    
    protected Set<String> deploymentIds = new HashSet<String>();
    protected Set<ProcessDefinition> availableProcesses = new HashSet<ProcessDefinition>();

    private IdentityProvider identityProvider;

    protected TaskService taskService;

    private DeploymentRolesManager deploymentRolesManager = new DeploymentRolesManager();

    private UserGroupCallback userGroupCallback = UserDataServiceProvider.getUserGroupCallback();
    
    private final TaskTableService taskTableService;
    
    private static final List<Status> allActiveStatus = Arrays.asList(new Status[]{
            Status.Created,
            Status.Ready,
            Status.Reserved,
            Status.InProgress,
            Status.Suspended
          });

    
    public MapDBRuntimeDataServiceImpl(DB db) {
        this.db = db;
        this.taskTableService = new TaskTableService(db, new ObjectStoringStrategy[0]);
        this.processInstancesById = db.treeMap("runtime-data-service-processinstances-by-id", Serializer.LONG, new ProcessInstanceDescSerializer()).createOrOpen();
    }
   
    public void setIdentityProvider(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public void setUserGroupCallback(UserGroupCallback userGroupCallback) {
        this.userGroupCallback = userGroupCallback;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setDeploymentRolesManager(DeploymentRolesManager deploymentRolesManager) {
        this.deploymentRolesManager = deploymentRolesManager;
    }
    
    public BTreeMap<Long, MapDBProcessInstanceDesc> getProcessInstancesById() {
        return processInstancesById;
    }

    private void addProcessDefinition( ProcessAssetDesc asset) {
        availableProcesses.add(asset);
        deploymentIds.add(asset.getDeploymentId());
     }

     private void removeAllProcessDefinitions( Collection<ProcessAssetDesc> assets) {
         Iterator<ProcessAssetDesc> iter = assets.iterator();
         while( iter.hasNext() ) {
             ProcessAssetDesc asset = iter.next();
             availableProcesses.remove(asset);
             deploymentIds.remove(asset.getDeploymentId());
         }
     }

     private String getLatestDeploymentId(String deploymentId) {
         String matched = deploymentId;
         if (deploymentId != null && deploymentId.toLowerCase().endsWith("latest")) {
             matched = DeploymentIdResolver.matchAndReturnLatest(deploymentId, deploymentIds);
         }
         return matched;
     }


     /*
      * start
      * helper methods to index data upon deployment
      */
     public void onDeploy(DeploymentEvent event) {
         Collection<DeployedAsset> assets = event.getDeployedUnit().getDeployedAssets();
         List<String> roles = null;
         for( DeployedAsset asset : assets ) {
             if( asset instanceof ProcessAssetDesc ) {
                 addProcessDefinition((ProcessAssetDesc) asset);
                 if (roles == null) {
                     roles = ((ProcessAssetDesc) asset).getRoles();
                 }
             }
         }
         if (roles == null) {
             roles = Collections.emptyList();
         }
         deploymentRolesManager.addRolesForDeployment(event.getDeploymentId(), roles);
     }

     public void onUnDeploy(DeploymentEvent event) {
         Collection<ProcessAssetDesc> outputCollection = new HashSet<ProcessAssetDesc>();
         CollectionUtils.select(availableProcesses, new UnsecureByDeploymentIdPredicate(event.getDeploymentId()), outputCollection);

         removeAllProcessDefinitions(outputCollection);
         deploymentRolesManager.removeRolesForDeployment(event.getDeploymentId());
     }


     @Override
     public void onActivate(DeploymentEvent event) {
         Collection<ProcessAssetDesc> outputCollection = new HashSet<ProcessAssetDesc>();
         CollectionUtils.select(availableProcesses, new UnsecureByDeploymentIdPredicate(event.getDeploymentId()), outputCollection);

         for (ProcessAssetDesc process : outputCollection) {
             process.setActive(true);
         }

     }

     @Override
     public void onDeactivate(DeploymentEvent event) {
         Collection<ProcessAssetDesc> outputCollection = new HashSet<ProcessAssetDesc>();
         CollectionUtils.select(availableProcesses, new UnsecureByDeploymentIdPredicate(event.getDeploymentId()), outputCollection);

         for (ProcessAssetDesc process : outputCollection) {
             process.setActive(false);
         }
     }
    
    
    @Override
    public Collection<ProcessInstanceDesc> getProcessInstances(QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstances(List<Integer> states, String initiator, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getInitiator().equals(initiator) && states.contains(pi.getState()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessId(List<Integer> states, String processId, String initiator, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getProcessId().equals(processId) && states.contains(pi.getState()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessName(List<Integer> states, String processName, String initiator, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getProcessName().equals(processName) && states.contains(pi.getState()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByDeploymentId(String deploymentId, List<Integer> states, QueryContext queryContext) {
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getDeploymentId().equals(deploymentId) && states.contains(pi.getState()))
                .collect(Collectors.toList());
    }

    @Override
    public ProcessInstanceDesc getProcessInstanceById(long processInstanceId) {
        return processInstancesById.get(processInstanceId);
    }

    @Override
    public ProcessInstanceDesc getProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
        
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getCorrelationKey().equals(correlationKey.toExternalForm()))
                .findFirst().get();
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByCorrelationKey(CorrelationKey correlationKey, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getCorrelationKey().equals(correlationKey.toExternalForm()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByCorrelationKeyAndStatus(CorrelationKey correlationKey, List<Integer> states, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getCorrelationKey().equals(correlationKey.toExternalForm()) && states.contains(pi.getState()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessDefinition(String processDefId, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getProcessId().equals(processDefId))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessDefinition(String processDefId, List<Integer> states, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getProcessId().equals(processDefId) && states.contains(pi.getState()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByVariable(String variableName, List<Integer> states, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> states, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<ProcessInstanceDesc> getProcessInstancesByParent(Long parentProcessInstanceId, List<Integer> states, QueryContext queryContext) {
        int start = queryContext.getOffset();
        int end = start + queryContext.getCount();
        return processInstancesById.getValues().stream()
                .filter(pi -> pi.getParentId().equals(parentProcessInstanceId) && states.contains(pi.getState()))
                .skip(start)
                .limit(end)
                .collect(Collectors.toList());
    }

    @Override
    public NodeInstanceDesc getNodeInstanceForWorkItem(Long workItemId) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<NodeInstanceDesc> getProcessInstanceHistoryActive(long processInstanceId, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<NodeInstanceDesc> getProcessInstanceHistoryCompleted(long processInstanceId, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<NodeInstanceDesc> getProcessInstanceFullHistory(long processInstanceId, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<NodeInstanceDesc> getProcessInstanceFullHistoryByType(long processInstanceId, EntryType type, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<NodeInstanceDesc> getNodeInstancesByNodeType(long processInstanceId, List<String> nodeTypes, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<NodeInstanceDesc> getNodeInstancesByCorrelationKeyNodeType(CorrelationKey correlationKey, List<Integer> states, List<String> nodeTypes, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<VariableDesc> getVariablesCurrentState(long processInstanceId) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public Collection<VariableDesc> getVariableHistory(long processInstanceId, String variableId, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    public Collection<ProcessDefinition> getProcessesByDeploymentId(String deploymentId, QueryContext queryContext) {        

        List<ProcessDefinition> outputCollection = new ArrayList<ProcessDefinition>();
        CollectionUtils.select(availableProcesses, new ByDeploymentIdPredicate(deploymentId, identityProvider.getRoles()), outputCollection);

        applySorting(outputCollection, queryContext);
        return applyPaginition(outputCollection, queryContext);
    }

    public ProcessDefinition getProcessesByDeploymentIdProcessId(String deploymentId, String processId) {

        List<ProcessDefinition> outputCollection = new ArrayList<ProcessDefinition>();
        CollectionUtils.select(availableProcesses, new ByDeploymentIdProcessIdPredicate(deploymentId, processId, identityProvider.getRoles(), true), outputCollection);

        if (!outputCollection.isEmpty()) {
            return outputCollection.iterator().next();
        }
        return null;
    }

    public Collection<ProcessDefinition> getProcessesByFilter(String filter, QueryContext queryContext) {
        List<ProcessDefinition> outputCollection = new ArrayList<ProcessDefinition>();
        CollectionUtils.select(availableProcesses, new RegExPredicate("(?i)^.*"+filter+".*$", identityProvider.getRoles()), outputCollection);

        applySorting(outputCollection, queryContext);
        return applyPaginition(outputCollection, queryContext);
    }

    @Deprecated
    public ProcessDefinition getProcessById(String processId){

        Collection<ProcessDefinition> definitions = getProcessesById(processId);
        if (!definitions.isEmpty()) {
            return definitions.iterator().next();
        }

        return null;
    }


    public Collection<ProcessDefinition> getProcessesById(String processId){

        Collection<ProcessDefinition> outputCollection = new HashSet<ProcessDefinition>();
        CollectionUtils.select(availableProcesses, new ByProcessIdPredicate(processId, identityProvider.getRoles()), outputCollection);

        return outputCollection;
    }

    public Collection<ProcessDefinition> getProcesses(QueryContext queryContext) {
        List<ProcessDefinition> outputCollection = new ArrayList<ProcessDefinition>();
        CollectionUtils.select(availableProcesses, new SecurePredicate(identityProvider.getRoles(), false), outputCollection);

        applySorting(outputCollection, queryContext);
        return applyPaginition(outputCollection, queryContext);
    }

    @Override
    public Collection<String> getProcessIds(String deploymentId, QueryContext queryContext) {        

        List<String> processIds = new ArrayList<String>(availableProcesses.size());
        if( deploymentId == null || deploymentId.isEmpty() ) {
            return processIds;
        }
        for( ProcessDefinition procAssetDesc : availableProcesses ) {
            if( ((ProcessAssetDesc)procAssetDesc).getDeploymentId().equals(deploymentId) && ((ProcessAssetDesc)procAssetDesc).isActive()) {
                processIds.add(procAssetDesc.getId());
            }
        }
        return applyPaginition(processIds, queryContext);
    }

    @Override
    public UserTaskInstanceDesc getTaskByWorkItemId(Long workItemId) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TaskByWorkItemId");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("workItemId", workItemId);
              
        return (UserTaskInstanceDesc) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public UserTaskInstanceDesc getTaskById(Long taskId) {
        Task task = taskTableService.getById().get(taskId);
        if (task == null) {
            return null;
        }
        UserTaskInstanceDesc desc = new org.jbpm.kie.services.impl.model.UserTaskInstanceDesc(task.getId(), 
                task.getTaskData().getStatus().name(),
                task.getTaskData().getActivationTime(),
                task.getName(), 
                task.getDescription(),
                task.getPriority(),
                valueOf(task.getTaskData().getActualOwner()),
                valueOf(task.getTaskData().getCreatedBy()),
                task.getTaskData().getDeploymentId(),
                task.getTaskData().getProcessId(),
                task.getTaskData().getProcessInstanceId(),
                task.getTaskData().getCreatedOn(),
                task.getTaskData().getExpirationTime());
        return desc;
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String userId, QueryFilter filter) {
        
        return getTasksAssignedAsBusinessAdministratorByStatus(userId, allActiveStatus, filter);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsBusinessAdministratorByStatus(String userId, List<Status> statuses, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksAssignedAsBusinessAdministratorByStatus");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("status", statuses);        
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksAssignedAsPotentialOwner");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, List<String> groupIds, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksAssignedAsPotentialOwnerWithGroups");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("groupIds", groupIds);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatus(String userId, List<Status> status, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksAssignedAsPotentialOwnerByStatus");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("status", status);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, List<String> groupIds, List<Status> status, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksAssignedAsPotentialOwnerWithGroups");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("groupIds", groupIds);
        params.put("status", status);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByExpirationDateOptional(String userId, List<Status> status, Date from, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksAssignedAsPotentialOwnerStatusByExpirationDateOptional");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("status", status);
        params.put("expirationDate", from);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksOwnedByExpirationDateOptional(String userId, List<Status> strStatuses, Date from, QueryFilter filter) {
        return getTasksAssignedAsPotentialOwnerByExpirationDateOptional(userId, strStatuses, from, filter);
    }

    @Override
    public List<TaskSummary> getTasksOwned(String userId, QueryFilter filter) {
        return getTasksOwnedByStatus(userId, allActiveStatus, filter);
    }

    @Override
    public List<TaskSummary> getTasksOwnedByStatus(String userId, List<Status> status, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("NewTasksOwned");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("status", status);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<Long> getTasksByProcessInstanceId(Long processInstanceId) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksByProcessInstanceId");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("processInstanceId", processInstanceId);
               
        return (List<Long>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<TaskSummary> getTasksByStatusByProcessInstanceId(Long processInstanceId, List<Status> status, QueryFilter filter) {
        MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery("TasksByStatusByProcessId");
        if (query == null) {
            throw new UnsupportedOperationException("Query not implemented yet");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("processInstanceId", processInstanceId);
        params.put("status", status);
        applyQueryContext(params, filter);        
        return (List<TaskSummary>) query.execute(userGroupCallback, params, taskTableService, false);
    }

    @Override
    public List<AuditTask> getAllAuditTask(String userId, QueryFilter filter) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public List<AuditTask> getAllAuditTaskByStatus(String userId, QueryFilter filter) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public List<AuditTask> getAllGroupAuditTask(String userId, QueryFilter filter) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public List<AuditTask> getAllAdminAuditTask(String userId, QueryFilter filter) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public List<TaskEvent> getTaskEvents(long taskId, QueryFilter filter) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public TaskSummaryQueryBuilder taskSummaryQuery(String userId) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public List<TaskSummary> getTasksByVariable(String userId, String variableName, List<Status> statuses, QueryContext queryContext) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }

    @Override
    public List<TaskSummary> getTasksByVariableAndValue(String userId, String variableName, String variableValue, List<Status> statuses, QueryContext context) {
        throw new UnsupportedOperationException("Not supported for Map DB implementation");
    }
    
    /*
     * start
     * predicates for collection filtering
     */

    private class RegExPredicate extends SecurePredicate {
        private String pattern;

        private RegExPredicate(String pattern, List<String> roles) {
            super(roles, false);
            this.pattern = pattern;
        }

        @Override
        public boolean evaluate(Object object) {
            if (object instanceof ProcessAssetDesc) {
                ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
                boolean hasAccess = super.evaluate(object);
                if (!hasAccess) {
                    return false;
                }
                if (pDesc.getId().matches(pattern)
                        || pDesc.getName().matches(pattern)) {
                    return true;
                }
            }
            return false;
        }

    }

    private class ByDeploymentIdPredicate extends SecurePredicate {
        private String deploymentId;

        private ByDeploymentIdPredicate(String deploymentId, List<String> roles) {
            super(roles, false);
            this.deploymentId = deploymentId;
        }

        @Override
        public boolean evaluate(Object object) {
            if (object instanceof ProcessAssetDesc) {
                ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
                boolean hasAccess = super.evaluate(object);
                if (!hasAccess) {
                    return false;
                }
                if (pDesc.getDeploymentId().equals(deploymentId)) {
                    return true;
                }
            }
            return false;
        }

    }

    private class ByProcessIdPredicate extends SecurePredicate {
        private String processId;

        private ByProcessIdPredicate(String processId, List<String> roles) {
            super(roles, false);
            this.processId = processId;
        }

        @Override
        public boolean evaluate(Object object) {
            if (object instanceof ProcessAssetDesc) {
                ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
                boolean hasAccess = super.evaluate(object);
                if (!hasAccess) {
                    return false;
                }
                if (pDesc.getId().equals(processId)) {
                    return true;
                }
            }
            return false;
        }

    }

    private class ByDeploymentIdProcessIdPredicate extends SecurePredicate {
        private String processId;
        private String depoymentId;

        private ByDeploymentIdProcessIdPredicate(String depoymentId, String processId, List<String> roles) {
            super(roles, false);
            this.depoymentId = depoymentId;
            this.processId = processId;
        }

        private ByDeploymentIdProcessIdPredicate(String depoymentId, String processId, List<String> roles, boolean skipActiveCheck) {
            super(roles, skipActiveCheck);
            this.depoymentId = depoymentId;
            this.processId = processId;
        }

        @Override
        public boolean evaluate(Object object) {
            if (object instanceof ProcessAssetDesc) {
                ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
                boolean hasAccess = super.evaluate(object);
                if (!hasAccess) {
                    return false;
                }
                if (pDesc.getId().equals(processId) && pDesc.getDeploymentId().equals(depoymentId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class SecurePredicate extends ActiveOnlyPredicate {
        private List<String> roles;
        private boolean skipActivCheck;

        private SecurePredicate(List<String> roles, boolean skipActivCheck) {
            this.roles = roles;
            this.skipActivCheck = skipActivCheck;
        }

        public boolean evaluate(Object object) {
            if (!skipActivCheck) {
                boolean isActive = super.evaluate(object);
                if (!isActive) {
                    return false;
            }
            }
            ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
            if (this.roles == null || this.roles.isEmpty() || pDesc.getRoles() == null || pDesc.getRoles().isEmpty()) {
                return true;
            }


            return CollectionUtils.containsAny(roles, pDesc.getRoles());
        }
    }


    private class UnsecureByDeploymentIdPredicate implements Predicate {
        private String deploymentId;

        private UnsecureByDeploymentIdPredicate(String deploymentId) {
            this.deploymentId = deploymentId;
        }

        @Override
        public boolean evaluate(Object object) {
            if (object instanceof ProcessAssetDesc) {
                ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
                if (pDesc.getDeploymentId().equals(deploymentId)) {
                    return true;
                }
            }
            return false;
        }

    }

    private class ActiveOnlyPredicate implements Predicate {

        private ActiveOnlyPredicate() {
        }

        @Override
        public boolean evaluate(Object object) {
            if (object instanceof ProcessAssetDesc) {
                ProcessAssetDesc pDesc = (ProcessAssetDesc) object;
                if (pDesc.isActive()) {
                    return true;
                }
            }
            return false;
        }

    }

    protected void applyQueryContext(Map<String, Object> params, QueryContext queryContext) {
        if (queryContext != null) {
            params.put("firstResult", queryContext.getOffset());
            params.put("maxResults", queryContext.getCount());

            if (queryContext.getOrderBy() != null && !queryContext.getOrderBy().isEmpty()) {
                params.put(QueryManager.ORDER_BY_KEY, queryContext.getOrderBy());

                if (queryContext.isAscending()) {
                    params.put(QueryManager.ASCENDING_KEY, "true");
                } else {
                    params.put(QueryManager.DESCENDING_KEY, "true");
                }
            }
        }
    }
    
    protected String valueOf(OrganizationalEntity entity) {
        if (entity == null) {
            return "";
        }
        
        return entity.getId();
    }
    
    protected <T> Collection<T> applyPaginition(List<T> input, QueryContext queryContext) {
        if (queryContext != null) {
            int start = queryContext.getOffset();
            int end = start + queryContext.getCount();
            if (input.size() < start) {
                // no elements in given range
                return new ArrayList<T>();
            } else if (input.size() >= end) {
                return Collections.unmodifiableCollection(new ArrayList<T>(input.subList(start, end)));
            } else if (input.size() < end) {
                return Collections.unmodifiableCollection(new ArrayList<T>(input.subList(start, input.size())));
            }

        }

        return Collections.unmodifiableCollection(input);
    }

    protected void applySorting(List<ProcessDefinition> input, final QueryContext queryContext) {
        if (queryContext != null && queryContext.getOrderBy() != null && !queryContext.getOrderBy().isEmpty()) {
            Collections.sort(input, new Comparator<ProcessDefinition>() {

                @Override
                public int compare(ProcessDefinition o1, ProcessDefinition o2) {
                    if ("ProcessName".equals(queryContext.getOrderBy())) {
                        return o1.getName().compareTo(o2.getName());
                    } else if ("ProcessVersion".equals(queryContext.getOrderBy())) {
                        return o1.getVersion().compareTo(o2.getVersion());
                    } else if ("Project".equals(queryContext.getOrderBy())) {
                        return o1.getDeploymentId().compareTo(o2.getDeploymentId());
                    }
                    return 0;
                }
            });

            if (!queryContext.isAscending()) {
                Collections.reverse(input);
            }
        }
    }
}
