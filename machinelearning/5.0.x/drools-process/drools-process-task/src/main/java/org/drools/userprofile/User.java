package org.drools.userprofile;

public class User extends OrganizationalEntity {
    UserProfile userProfile;
    
    public User() {
        super();
    }
    
    public User(String id) {
        super(id);
    }

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}   
 
}
