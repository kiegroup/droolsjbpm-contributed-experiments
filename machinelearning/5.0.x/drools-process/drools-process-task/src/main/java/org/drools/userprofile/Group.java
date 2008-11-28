package org.drools.userprofile;

import java.util.ArrayList;
import java.util.List;

public class Group extends OrganizationalEntity {
	List<OrganizationalEntity> members = new ArrayList<OrganizationalEntity>();

    public Group() {
        super();
    }

    public Group(String id) {
        super( id );
    }
    
    List<OrganizationalEntity> getMembers() {
    	return members;
    }

	public void setMembers(List<OrganizationalEntity> members) {
		this.members = members;
	}
    
}
