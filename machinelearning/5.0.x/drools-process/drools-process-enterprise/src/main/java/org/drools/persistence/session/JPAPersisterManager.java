package org.drools.persistence.session;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.persistence.Persister;
import org.drools.persistence.jpa.JPAPersister;

public class JPAPersisterManager {

	private EntityManagerFactory emf;
	
	public JPAPersisterManager() {
		emf = Persistence.createEntityManagerFactory("org.drools.persistence.jpa");
	}
	
	public void dispose() {
		emf.close();
	}
	
	public Persister<StatefulSession> getSessionPersister(StatefulSession session) {
		return new JPAPersister<StatefulSession>(emf, new StatefulSessionSnapshotter(session));
	}
	
	public Persister<StatefulSession> getSessionPersister(RuleBase ruleBase) {
		return new JPAPersister<StatefulSession>(emf, new StatefulSessionSnapshotter(ruleBase.newStatefulSession()));
	}
	
	public Persister<StatefulSession> getSessionPersister(String uniqueId, RuleBase ruleBase) {
		Persister<StatefulSession> persister = new JPAPersister<StatefulSession>(
			emf, new StatefulSessionSnapshotter(ruleBase));
		persister.setUniqueId(uniqueId);
		persister.load();
		return persister;
	}
	
}
