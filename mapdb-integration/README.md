This is a MapDB based implementation of the Drools and jBPM persistent components. Main projects involved are

* drools-persistence-mapdb: Contains MapDB implementation of the Drools Persistence API components.
* jbpm-persistence-mapdb: Contains MapDB implementation of the jBPM Persistence API components
* jbpm-human-task-mapdb: Contains MapDB implementation of jBPM Human Task components.

The main usage of the components depends on very few implementation steps:

1) Adding the following dependencies (while making sure we don't have any KIE JPA dependencies, since they may clash):

```
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>drools-persistence-mapdb</artifactId>
  <version>7.1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>jbpm-persistence-mapdb</artifactId>
  <version>7.1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>jbpm-human-task-mapdb</artifactId>
  <version>7.1.0-SNAPSHOT</version>
</dependency>
```

2) Setting the configuration as shown in [MapDBPersistenceUtil](https://github.com/Multi-Support/droolsjbpm-contributed-experiments/blob/JBPM-5708_mapdb_integration/mapdb-integration/drools-persistence-mapdb/src/test/java/org/drools/persistence/mapdb/util/MapDBPersistenceUtil.java), [MapDBProcessPersistenceUtil](https://github.com/Multi-Support/droolsjbpm-contributed-experiments/blob/JBPM-5708_mapdb_integration/mapdb-integration/jbpm-persistence-mapdb/src/test/java/org/jbpm/persistence/mapdb/util/MapDBProcessPersistenceUtil.java), or [MapDBTaskPersistenceUtil](https://github.com/Multi-Support/droolsjbpm-contributed-experiments/blob/JBPM-5708_mapdb_integration/mapdb-integration/jbpm-human-task-mapdb/src/test/java/org/jbpm/services/task/util/MapDBTaskPersistenceUtil.java)

```
new KnowledgeStoreServiceImpl(); //Use this reference is to make sure it registers the store service
ServiceRegistryImpl.getInstance().addDefault(CorrelationKeyFactory.class, "org.jbpm.persistence.correlation.MapDBCorrelationKeyFactory");
HashMap<String, Object> context = new HashMap<>();
DB db = DBMaker.memoryDB().concurrencyScale(64).transactionEnable().make();
Environment env = EnvironmentFactory.newEnvironment();
env.set(MapDBEnvironmentName.DB_OBJECT, db);
env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager()); // for use with bitronix
env.set(EnvironentName.GLOBALS, new MapGlobalResolver());
KieStoreServices kstore = KieServices.Factory.get().getStoreServices();
KieSession ksession = kstore.newKieSession(KieServices.Factory.get().getKieClasspathContainer().getKieBase(), null, env);

TaskEventSupport taskEventSupport = new TaskEventSupport();
env.set(EnvironmentName.TASK_USER_GROUP_CALLBACK, new MvelUserGroupCallbackImpl(true));
env.set(EnvironmentName.TASK_USER_INFO, new DefaultUserInfo(true));
TransactionManager txm = new JtaTransactionManagerFactory().newTransactionManager(env);
env.set(EnvironmentName.TRANSACTION_MANAGER, txm);
MapDBTaskPersistenceContextManager tpcm = new MapDBTaskPersistenceContextManager(env);
env.set(EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, tpcm);
TaskCommandExecutorImpl commandExecutor = new TaskCommandExecutorImpl(env, taskEventSupport);
TaskTransactionInterceptor interceptor = new TaskTransactionInterceptor(env);
commandExecutor.addInterceptor(interceptor);
commandExecutor.addInterceptor(new TransactionLockInterceptor(env));
if (TaskDeadlinesServiceImpl.getInstance() == null) {
    TaskDeadlinesServiceImpl.initialize(commandExecutor);
}
TaskService taskService = new CommandBasedTaskService(commandExecutor, taskEventSupport);
```

From that point onward, the utility should be as straight to use as any ksession and taskService  
