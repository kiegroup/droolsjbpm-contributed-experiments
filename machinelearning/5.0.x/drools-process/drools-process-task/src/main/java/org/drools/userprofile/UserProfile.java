package org.drools.userprofile;

/**
 * UserProfile is a base class to represent user profile related information. As the user profile information various 
 * from application to application, the only common information we have in this base class is user id.
 * Then it is up to the sub class to provide application specific information, see DroolsTaskUserProfile.
 *   
 */
public class UserProfile {
	String id;
	
    public String getID(){
    	return id;
    }
    
    public void setID(String id){
    	this.id = id;
    }
 }
