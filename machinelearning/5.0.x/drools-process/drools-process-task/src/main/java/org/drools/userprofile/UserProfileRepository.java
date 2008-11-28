package org.drools.userprofile;

import java.util.List;

public interface UserProfileRepository {
	UserProfile getUserProfile(String userName);
	void setUserProfile(UserProfile info);
	
	List<User> getUsers();
	String[] getUserIds();
	List<Group> getGroups();
	String[] getGroupIds();
}
