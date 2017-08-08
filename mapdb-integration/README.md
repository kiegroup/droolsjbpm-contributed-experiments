This is a MapDB based implementation of the Drools and jBPM persistent components. Main projects involved are

* drools-persistence-mapdb: Contains MapDB implementation of the Drools Persistence API components.
* jbpm-persistence-mapdb: Contains MapDB implementation of the jBPM Persistence API components
* jbpm-human-task-mapdb: Contains MapDB implementation of jBPM Human Task components.
* jbpm-runtime-manager-mapdb: Contains MapDB implementation of jBPM Runtime Manager component.
* jbpm-services-mapdb-impl: Contains MapDB implementation of jBPM services component.
* kie-server-jbpm-mapdb: Contains MapDB implementation of KIE Server jBPM extension component.

The main usage of the components depends on very few implementation steps:

__Basic "low level" API usage__

Adding the following dependencies (while making sure we don't have any KIE JPA dependencies, since they may clash):

```
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>drools-persistence-mapdb</artifactId>
  <version>8.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>jbpm-persistence-mapdb</artifactId>
  <version>8.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>jbpm-human-task-mapdb</artifactId>
  <version>8.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>jbpm-runtime-manager-mapdb</artifactId>
  <version>8.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>jbpm-services-mapdb-impl</artifactId>
  <version>8.0.0-SNAPSHOT</version>
</dependency>
```
Depending on what API is to be used number of dependencies can be reduced. For example below only:

* drools-persistence-mapdb
* jbpm-persistence-mapdb
* jbpm-human-task-mapdb

dependencies are needed

```
HashMap<String, Object> context = new HashMap<>();
DB db = DBMaker.memoryDB().concurrencyScale(64).transactionEnable().make();
Environment env = EnvironmentFactory.newEnvironment();
env.set(MapDBEnvironmentName.DB_OBJECT, db);
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


__Runtime Manager usage__

jBPM Runtime Manager API can be used with Map DB as well by making sure that org.jbpm.runtime.manager.mapdb.MapDBRuntimeManagerFactoryImpl is used as implementation of RuntimeManagerFactory that can be given by system property named:
```
org.jbpm.runtime.manager.class
```
In addition to that, Map DB based RuntimeManager requires to have Map DB to be given as environment entry:

```
RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .addEnvironmentEntry(MapDBEnvironmentName.DB_OBJECT, db)
                .addAsset(ResourceFactory.newClassPathResource("BPMN2-ScriptTask.bpmn2"), ResourceType.BPMN2)
                .get();
```

That's all that is required to use Map DB based Runtime Manager.                


__jBPM Services usage__

Map DB based jBPM services override certain parts of the actual services implementation to fit into Map DB environment. See org.jbpm.services.impl.mapdb.util.AbstractKieServicesBaseTest for details on how the jBPM services are created.

Note that not all services are supported, for instance:
* jBPM executor
* QueryService
are not supported with Map DB


__KIE Server usage__

Map DB implementation can be used as replacement of default jBPM extension in KIE Server. To do that following libraries must be copied to KIE Server .war WEB-INF/lib folder:
* drools-persistence-mapdb-8.0.0-SNAPSHOT.jar
* jbpm-persistence-mapdb-8.0.0-SNAPSHOT.jar
* jbpm-human-task-mapdb-8.0.0-SNAPSHOT.jar
* jbpm-runtime-manager-mapdb-8.0.0-SNAPSHOT.jar
* jbpm-services-mapdb-impl-8.0.0-SNAPSHOT.jar
* kie-server-jbpm-mapdb-8.0.0-SNAPSHOT.jar

in addition that that Map DB and its dependencies must be added, for this maven dependency plugin has been added to kie-server-jbpm-mapdb project to extract required depdendencies, all of them can be found in:
kie-server-jbpm-mapdb/target/dependencies

Note that some libs are already present in WEB-INF/lib of kie server so make sure they are not duplicated.

Next jpa based libraries must be removed from KIE Server .war/WEB-INF/lib folder:
* drools-persistence-jpa
* jbpm-persistence-jpa
* jbpm-human-task-jpa
* jbpm-human-task-audit

Then when starting KIE Server pass following system properties:
-Dorg.jbpm.mapdb.server.ext.disabled=false -Dorg.jbpm.server.ext.disabled=true

these will enable map db based extension and disable default (jpa) extenstion for KIE Server. Note that Map DB has less capabilities compared to default one.

