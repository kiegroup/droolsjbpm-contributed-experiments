package org.drools.userprofile;

import java.util.List;

public class DroolsTaskUserProfile extends UserProfile {
	
    public String getDisplayName(OrganizationalEntity entity){
    	return null;
    }
    
    public List<OrganizationalEntity> getMembersForGroup(Group group){
    	return null;
    }
    
    public boolean hasEmail(Group group) {
    	return false;
    }
    
    public String getEmailForEntity(OrganizationalEntity entity) {
    	return null;
    }
    
    public String getLanguageForEntity(OrganizationalEntity entity) {
    	return null;
    }
}
