package org.drools.persistence.jpa;

import javax.persistence.EntityTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class JPAXaResource<T> implements XAResource {
	
	private JPAPersister<T> persister;
    private EntityTransaction transaction;
    
    public JPAXaResource(JPAPersister<T> persister, EntityTransaction transaction) {
    	this.persister = persister;
        this.transaction = transaction;
    }
    
    public boolean isInTransaction() {
    	return transaction.isActive();
    }

    public void start(Xid xid, int flags) throws XAException {
        transaction.begin();
    }     
    
    public void rollback(Xid xid) throws XAException {   
        transaction.rollback();
    }    
    
    public void commit(Xid xid, boolean onePhase) throws XAException {
    	persister.updateObject();
        transaction.commit();        
    }
    
    public void end(Xid xid,
                    int flags) throws XAException {    
    }        

    public void forget(Xid xid) throws XAException {        
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    public Xid[] recover(int flag) throws XAException {
        return null;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }      

}
