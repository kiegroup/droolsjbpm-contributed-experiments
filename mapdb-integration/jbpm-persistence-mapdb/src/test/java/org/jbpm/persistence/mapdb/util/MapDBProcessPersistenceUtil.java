/*
 * Copyright 2017 Red Hat Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.persistence.mapdb.util;

import static org.drools.persistence.mapdb.MapDBEnvironmentName.DB_OBJECT;
import static org.kie.api.runtime.EnvironmentName.GLOBALS;
import static org.kie.api.runtime.EnvironmentName.TRANSACTION;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.persistence.mapdb.KnowledgeStoreServiceImpl;
import org.kie.api.runtime.Environment;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.utils.ServiceRegistryImpl;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDBProcessPersistenceUtil {

    public static final String MAPDB_FILE_NAME = "jbpm-mapdb.db";
    
    public static void cleanUp(Map<String, Object> context) {
        DB db = (DB) context.get(DB_OBJECT);
        db.close();
//        new File(MAPDB_FILE_NAME).delete();
    }
    
    public static HashMap<String, Object> setupMapDB() {
        new KnowledgeStoreServiceImpl(); //TODO this reference is to make sure it registers the store service
        ServiceRegistryImpl.getInstance().addDefault(CorrelationKeyFactory.class, "org.jbpm.persistence.correlation.MapDBCorrelationKeyFactory");
        HashMap<String, Object> context = new HashMap<>();
        DB db = makeDB();
        context.put(DB_OBJECT, db);
        //context.put(TRANSACTION, new MapDBUserTransaction(db));
        return context;
    }

    public static DB makeDB() {
        DB db = DBMaker.memoryDB().
                concurrencyScale(64).
                transactionEnable().
                make();
        /*DB db = DBMaker.fileDB(MAPDB_FILE_NAME).
                cleanerHackEnable().
                concurrencyScale(512).
                transactionEnable().
                make();*/
        return db;
    }

    public static Environment createEnvironment(Map<String, Object> context) {
        // TODO Auto-generated method stub
        Environment env = EnvironmentFactory.newEnvironment();
        UserTransaction ut = (UserTransaction) context.get(TRANSACTION);
        if (ut != null) {
            env.set(TRANSACTION, ut);
        }

        env.set(DB_OBJECT, context.get(DB_OBJECT));
        env.set(GLOBALS, new MapGlobalResolver());

        return env;
    }
}
