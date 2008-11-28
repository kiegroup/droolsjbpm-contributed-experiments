package org.drools.userprofile;

import java.util.ArrayList;
import java.util.List;

public class MockFileBasedUserProfileRepository implements UserProfileRepository {
	public UserProfile getUserProfile(String userName) {
		//load the property file, get user info.
		DroolsTaskUserProfile ui = new DroolsTaskUserProfile();

		ui.setID(userName);
		return ui;
	}

	public void setUserProfile(UserProfile info) {
		if(!(info instanceof DroolsTaskUserProfile)) {
			return;
		}
		DroolsTaskUserProfile dtup = (DroolsTaskUserProfile)info;
		//update file properties
	}
	
	
    public List<User> getUsers() {
    	//may need to look into RMDB to get the list of all users
		return null;
	}
    
    public String[] getUserIds() {
    	//may need to look into RMDB to get the list of all users
		return null;
	}
	
	public List<Group> getGroups() {
		List<OrganizationalEntity> members = new ArrayList<OrganizationalEntity>();
		members.add(new User());
		
		Group group = new Group();
		group.setMembers(members);

		List<Group> result = new ArrayList<Group>();
		
		result.add(group);
		return null;
    }
	
	public String[] getGroupIds() {
		return null;
    }
}
