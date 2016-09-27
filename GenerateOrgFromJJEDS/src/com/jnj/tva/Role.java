package com.jnj.tva;

import java.util.HashMap;

public class Role {
	public String id;
	public String name;
	public String parentId;
	public String devName;
	public String description;
	public Dept correspondingDept;
	public static HashMap<String,Role> rolesMapById;
	public static HashMap<String,Role> rolesMapByDevName;
	
	public Role (String id, String name, String parentId, String devName, String description) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.devName = devName;
		this.description = description;
		rolesMapById.put(id, this);
		rolesMapByDevName.put(devName, this);
	}
	
	public Role getParentRole() {
		if (rolesMapById.containsKey(parentId)) {
			return rolesMapById.get(parentId);
		} else {
			return null;
		}
	}
}
