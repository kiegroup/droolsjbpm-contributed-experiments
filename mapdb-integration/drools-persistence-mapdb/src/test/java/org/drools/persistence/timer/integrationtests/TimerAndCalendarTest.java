/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.persistence.timer.integrationtests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import org.drools.core.ClockType;
import org.drools.core.time.SessionPseudoClock;
import org.drools.persistence.mapdb.util.MapDBPersistenceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.Resource;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.time.SessionClock;
import org.kie.internal.io.ResourceFactory;

import com.arjuna.ats.jta.TransactionManager;

public class TimerAndCalendarTest {
    
    private Map<String, Object> context;

    @BeforeClass
    public static void configureTx() {
        try {
            InitialContext initContext = new InitialContext();

            initContext.rebind("java:comp/UserTransaction", com.arjuna.ats.jta.UserTransaction.userTransaction());
            initContext.rebind("java:comp/TransactionManager", TransactionManager.transactionManager());
            initContext.rebind("java:comp/TransactionSynchronizationRegistry", new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Before
    public void before() throws Exception {
        context = MapDBPersistenceUtil.setupMapDB();
    }

    @After
    public void after() throws Exception {
        MapDBPersistenceUtil.cleanUp(context);
    }

    @Test @Ignore("beta4 phreak")
    public void testTimerRuleAfterIntReloadSession() throws Exception {
        KieBase kbase = KieServices.Factory.get().getKieClasspathContainer().newKieBase(null);
        KieSession ksession = createSession( kbase );

        // must advance time or it won't save.
        SessionPseudoClock clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 300,
                           TimeUnit.MILLISECONDS );

        // if we do not call 'ksession.fireAllRules()', this test will run successfully.
        ksession.fireAllRules();

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 300,
                           TimeUnit.MILLISECONDS );

        ksession = disposeAndReloadSession( ksession,
                                            kbase );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 300,
                           TimeUnit.MILLISECONDS );

        // build timer rule, if the rule is fired, the list size will increase every 500ms
        String timerRule = "package org.drools.test\n" +
                           "global java.util.List list \n" +
                           "rule TimerRule \n" +
                           "   timer (int:1000 500) \n" +
                           "when \n" + "then \n" +
                           "        list.add(list.size()); \n" +
                           " end";
        Resource resource = ResourceFactory.newByteArrayResource(timerRule.getBytes());
        KieFileSystem kfs = KieServices.Factory.get().newKieFileSystem();
        kfs.write("src/main/resources/r1.drl", resource);
        KieBuilder kbuilder = KieServices.Factory.get().newKieBuilder(kfs);
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages(Level.ERROR)) {
            fail (kbuilder.getResults().toString());
        }
        kbase = KieServices.Factory.get().newKieContainer(kbuilder.getKieModule().getReleaseId()).getKieBase();

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 10, TimeUnit.MILLISECONDS );
        ksession.fireAllRules();

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 10, TimeUnit.MILLISECONDS );

        ksession = disposeAndReloadSession( ksession, kbase );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 10, TimeUnit.MILLISECONDS );

        List<Integer> list = Collections.synchronizedList( new ArrayList<Integer>() );
        ksession.setGlobal( "list",
                            list );
        Assert.assertEquals( 0, list.size() );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 1700, TimeUnit.MILLISECONDS );
        Assert.assertEquals( 2, list.size() );

        ksession = disposeAndReloadSession( ksession, kbase );
        ksession.setGlobal( "list", list );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 1000, TimeUnit.MILLISECONDS );

        // if the rule is fired, the list size will greater than one.
        Assert.assertEquals( 4, list.size() );
    }

    @Test @Ignore("beta4 phreak")
    public void testTimerRuleAfterCronReloadSession() throws Exception {
        KieBase kbase = KieServices.Factory.get().getKieClasspathContainer().newKieBase(null);
        KieSession ksession = createSession( kbase );

        // must advance time or it won't save.
        SessionPseudoClock clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 300, TimeUnit.MILLISECONDS );

        // if we do not call 'ksession.fireAllRules()', this test will run successfully.
        ksession.fireAllRules();

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 300, TimeUnit.MILLISECONDS );

        ksession = disposeAndReloadSession( ksession, kbase );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 300, TimeUnit.MILLISECONDS );

        // build timer rule, if the rule is fired, the list size will increase every 300ms
        String timerRule = "package org.drools.test\n" +
                           "global java.util.List list \n" +
                           "rule TimerRule \n" +
                           "   timer (cron: * * * * * ?) \n" +
                           "when \n" + "then \n" +
                           "        list.add(list.size()); \n" +
                           " end";
        Resource resource = ResourceFactory.newByteArrayResource( timerRule.getBytes() );
        KieFileSystem kfs = KieServices.Factory.get().newKieFileSystem();
        kfs.write("src/main/resources/r1.drl", resource);
        KieBuilder kbuilder = KieServices.Factory.get().newKieBuilder(kfs);
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages(Level.ERROR)) {
            fail (kbuilder.getResults().toString());
        }
        kbase = KieServices.Factory.get().newKieContainer(kbuilder.getKieModule().getReleaseId()).getKieBase();

        List<Integer> list = Collections.synchronizedList( new ArrayList<Integer>() );
        ksession.setGlobal( "list", list );

        ksession.setGlobal( "list", list );
        clock.advanceTime( 10, TimeUnit.MILLISECONDS );
        ksession.fireAllRules();

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 10,
                           TimeUnit.MILLISECONDS );

        ksession = disposeAndReloadSession( ksession,
                                            kbase );
        ksession.setGlobal( "list",
                            list );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 10,
                           TimeUnit.MILLISECONDS );

        Assert.assertEquals( 1, list.size() );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 3, TimeUnit.SECONDS );
        Assert.assertEquals( 4, list.size() );

        ksession = disposeAndReloadSession( ksession, kbase );
        ksession.setGlobal( "list", list );

        clock = (SessionPseudoClock) ksession.<SessionClock>getSessionClock();
        clock.advanceTime( 2, TimeUnit.SECONDS );

        // if the rule is fired, the list size will greater than one.
        Assert.assertEquals( 6, list.size() );
    }

    @Test
    public void testEventExpires() throws Exception {
        String timerRule = "package org.drools.test\n" +
                           "declare TestEvent \n" +
                           "    @role( event )\n" +
                           "    @expires( 10s )\n" +
                           "end\n" +
                           "" +
                           "rule TimerRule \n" +
                           "    when \n" +
                           "        TestEvent( ) from entry-point \"Test\"\n" +
                           "    then \n" +
                           "end";
        Resource resource = ResourceFactory.newByteArrayResource( timerRule.getBytes() );
        KieFileSystem kfs = KieServices.Factory.get().newKieFileSystem();
        kfs.write("src/main/resources/r1.drl", resource);
        KieBuilder kbuilder = KieServices.Factory.get().newKieBuilder(kfs);
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages(Level.ERROR)) {
            fail (kbuilder.getResults().toString());
        }
        KieBase kbase = KieServices.Factory.get().newKieContainer(kbuilder.getKieModule().getReleaseId()).getKieBase();
        KieSession ksession = createSession( kbase );
        FactType type = kbase.getFactType( "org.drools.test", "TestEvent" );
        Assert.assertNotNull( "could not get type", type );
        ksession = disposeAndReloadSession( ksession, kbase );
        ksession.getEntryPoint( "Test" ).insert( type.newInstance() );
        ksession.fireAllRules();
        ksession = disposeAndReloadSession( ksession, kbase );
        ksession = disposeAndReloadSession( ksession, kbase );
    }

   

    private KieSession createSession(KieBase kbase) {
        final KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();
        conf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() ) );
        Environment env = MapDBPersistenceUtil.createEnvironment(context);
        KieSession ksession = KieServices.Factory.get().getStoreServices().newKieSession( kbase, conf, env );
        return ksession;
    }

    private KieSession disposeAndReloadSession(KieSession ksession, KieBase kbase) {
        long ksessionId = ksession.getIdentifier();
        ksession.dispose();
        final KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();
        conf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() ) );
        KieSession newksession = KieServices.Factory.get().getStoreServices().loadKieSession( ksessionId,
                                                                                                 kbase,
                                                                                                 conf,
                                                                                                 MapDBPersistenceUtil.createEnvironment(context) );
        return newksession;
    }
 

}
