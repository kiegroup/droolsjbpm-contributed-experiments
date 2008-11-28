package org.drools.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.drools.persistence.ByteArraySnapshotter;
import org.drools.persistence.Persister;
import org.drools.persistence.Transaction;

public class JPAPersister<T> implements Persister<T> {

	private String id;
	private ByteArrayObject object;
	private ByteArraySnapshotter<T> snapshotter;
    private EntityManagerFactory emf;
    private EntityManager em;

    public JPAPersister(EntityManagerFactory emf, ByteArraySnapshotter<T> snapshotter) {
        this.emf = emf;
        this.em = emf.createEntityManager();
        this.snapshotter = snapshotter;
    }
    
    public EntityManagerFactory getEntityManagerFactory() {
    	return emf;
    }
    
    public EntityManager getEntityManager() {
    	return em;
    }
    
    public void save() {
        em.getTransaction().begin();
        updateObject();
        em.getTransaction().commit();
    }
    
    public void updateObject() {
        if (object == null) {
        	object = new ByteArrayObject();
        }
        object.setByteArray(snapshotter.getSnapshot());
        em.persist(object);
        if (this.id == null) {
        	id = object.getId() + "";
        }
    }

    public void load() {
		object = em.find(ByteArrayObject.class, new Long(id));
		snapshotter.loadSnapshot(object.getByteArray());
    }

	public T getObject() {
		return snapshotter.getObject();
	}

	public Transaction getTransaction() {
		return new Transaction(getUniqueXID(), getXAResource());
	}

	public String getUniqueId() {
		return object == null ? null : object.getId() + "";
	}

	public XAResource getXAResource() {
		return new JPAXaResource<T>(this, em.getTransaction());
	}

	public void setUniqueId(String id) {
		this.id = id;
	}
	
	private Xid getUniqueXID() {
		return null;
	}

}
