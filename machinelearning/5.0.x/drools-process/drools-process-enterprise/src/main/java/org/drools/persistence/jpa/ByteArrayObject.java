package org.drools.persistence.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ByteArrayObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)        
    private long id;
    private byte[] byteArray;
    
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public byte[] getByteArray() {
		return byteArray;
	}
	
	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}
    
}
