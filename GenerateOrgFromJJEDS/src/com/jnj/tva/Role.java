package com.jnj.tva;

import java.util.HashMap;
import java.util.HashSet;

public class Role {
	public String id;
	public String name;
	public String parentId;
	public String devName;
	public String description;
	public Dept correspondingDept;
	public static HashMap<String,Role> rolesMapById = new HashMap<String,Role>();
	public static HashMap<String,Role> rolesMapByDevName = new HashMap<String,Role>();
	public HashSet<Employee> employees = new HashSet<Employee>();
	
	
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
	
	public static Role getRoleById (String id) {
		return rolesMapById.containsKey(id) ? rolesMapById.get(id) : null;
	}
	
	public static Role getRoleByDevName (String devName) {
		return rolesMapByDevName.containsKey(devName) ? rolesMapById.get(devName) : null;
	}

	public String getRoleAsCsv() {
		return String.join(",", (new String[]{id,"\"" + name + "\"",parentId,devName, "\"" + description + "\"", employees.size() > 0 ? "true" : "false"}));
	}
}
