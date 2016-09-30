package com.jnj.tva;

import java.util.HashMap;

public class OBSDept {
	public String id;
	public String managerName;
	public String parentId;
	public String name;
	public String deptId;
	public Dept correspondingDept;
	public static HashMap<String,OBSDept> OBSMapById = new HashMap<String,OBSDept>();
	public static HashMap<String,OBSDept> OBSMapByDeptId = new HashMap<String,OBSDept>();
	
	public OBSDept (String id, String name, String parentId, String managerName, String deptId) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.deptId = deptId;
		this.managerName = managerName;
		OBSMapById.put(id, this);
		OBSMapByDeptId.put(deptId, this);
	}
}
