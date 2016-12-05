package com.jnj.tva;

import java.util.HashMap;

public class User extends Person {
	public String id;
	public String userRoleId;
	public Dept correspondingDept;
	public static HashMap<String,User> usersMapById = new HashMap<String,User>();
	public static HashMap<String,User> userMapByEmpNum = new HashMap<String,User>();
	public Role myRole;
	private static boolean regenerateMapNeeded = true;
	private static HashMap<String,String> WWIDToUserIdMap = new HashMap<String,String>();
	
	
	public User (String id, String name, String employeeNumber, String userRoleId) {
		this.id = id;
		this.name = name;
		this.employeeNumber = employeeNumber;
		this.userRoleId = userRoleId;
		if (Role.rolesMapById.containsKey(userRoleId)) {
			myRole = Role.rolesMapById.get(userRoleId);
			myRole.addUser(this);
		}
		usersMapById.put(id, this);
		userMapByEmpNum.put(employeeNumber, this);
		regenerateMapNeeded = true;
	}
	
	public static User getUserById (String id) {
		return usersMapById.containsKey(id) ? usersMapById.get(id) : null;
	}
	
	public static User getUserByEmpNum (String empNum) {
		return userMapByEmpNum.containsKey(empNum) ? userMapByEmpNum.get(empNum) : null;
	}

	public String getRoleAsCsv() {
		return String.join(",", (new String[]{id,"\"" + name + "\"",employeeNumber,userRoleId, "\"" + myRole != null ? myRole.devName : ""}));
	}
	
	public static String getRoleIdForUserId(String userId) {
		return usersMapById.containsKey(userId) ? usersMapById.get(userId).userRoleId : null; 
	}
	
	public static String getUserIdForEmpNum(String empNum) {
		return userMapByEmpNum.containsKey(empNum) ? userMapByEmpNum.get(empNum).id : null;
	}
	
	public static HashMap<String,String> getWWIDToUserIDsMap() {
		if (!regenerateMapNeeded) {
			return WWIDToUserIdMap;
		} else {
			WWIDToUserIdMap.clear();
			for (User u : usersMapById.values()) {
				WWIDToUserIdMap.put(u.employeeNumber, u.id);
			}
			regenerateMapNeeded = false;
			return WWIDToUserIdMap;
		}
	}
	
	public String getUserAsString() {
		return name + " ID: " + id + " WWID: " + employeeNumber;
	}
	
	@Override
	public String toString() {
		return "E: " + name + " (" + employeeNumber + ") ID: " + id;
	}
	
	@Override
	public String getDeptId() {
		return myRole == null ? null : myRole.devName;
	}
}
