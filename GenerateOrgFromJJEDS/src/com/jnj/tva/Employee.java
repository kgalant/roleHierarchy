package com.jnj.tva;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class Employee implements Comparable<Employee>{

	private static final String prefix = "|---";

	public String deptId;
	public String managerWWID;
	public Employee manager;
	public String WWID;
	public String email;
	public String deptName;
	public String name;
	public String title;
	public Dept managesDept;
	public Dept partOfDept;
	public HashSet<Employee> employees = new HashSet<Employee>();
	public String roleId;
	
	public int compareTo(Employee e) {
		return name.compareTo(e.name);
	}
	

	public Vector<String> getEmployees() {
		Vector<String> retval = new Vector<String>();

		retval.add("(" + deptId + ": " + deptName + ") " + name);
		if (!employees.isEmpty()) {
			for (Employee e : employees) {
				Vector<String> recursedRetval = e.getEmployees();
				// now prefix the returned strings

				//Collections.sort(recursedRetval);

				for (String s : recursedRetval) {
					retval.add(prefix + s);
				}
			}			
		}
		return retval;
	}
	
	// TODO: this method needs refactoring, too much duplicated code

	public void generateDeptStructure() {

		if (manager == null) {
			// if I have no Manager, I am a top-level node

			String newDeptKey = getNewDeptKey(deptId);

			Dept myNewDept = generateNewDept(newDeptKey);
			
			// generate a dept for direct reports without own reports (leaf nodes)

			generateNewReportsDept(myNewDept);

		} else if (manager != null && employees.isEmpty()) {
			// if I have a manager, but no employees, I am a leaf node

			// I am a part of my manager's direct reports dept.
			// find that dept first
			
			Dept directReportsDept = null;
			
			for (Dept d : manager.managesDept.childDepts) {
				if (d.isDirectReportsDept) {
					directReportsDept = d;
				}
			}
			
			if (directReportsDept == null) {
				System.out.println("Couldn't find a reports dept for employee: " + name +  ", manager is: " + manager.name);
			} else {
				partOfDept = directReportsDept;
				directReportsDept.employees.add(this);	
				System.out.println(name + ", part of: " + directReportsDept.deptName);
			}



		} else if (manager != null && !employees.isEmpty()) {
			// if I have both a manager and employees, I am an intermediate node

			// I have my department which my subordinates will be part of

			// check if the dept I am part of already exists

			String newDeptKey = getNewDeptKey(deptId);

			Dept myNewDept = generateNewDept(newDeptKey);

			// generate a dept for direct reports without own reports (leaf nodes)

			generateNewReportsDept(myNewDept);
		}

		ArrayList<Employee> employeeList = new ArrayList<Employee>(employees);
		Collections.sort(employeeList);
		
		for (Employee e : employeeList) {
			e.generateDeptStructure();
		}
	}

	private Dept getNewDept(String newDeptKey, String deptName) {
		Dept retval = new Dept();

		retval.deptId = newDeptKey;
		retval.manager = this;
		
		String nameSuffix = "";

		boolean hasNameTwin = false;

		if (manager != null) {
			for (Employee e : manager.employees) {
				if (e != this && e.title.equals(title)) {
					hasNameTwin = true;
				}
			}
		}

		if (hasNameTwin) {
			nameSuffix = " (" + name + ")";
		}
		
		retval.deptName = deptName + nameSuffix;

		return retval;

	}

	public String getRole(HashMap<String,String> WWIDToUserIDsMap) {
		if (partOfDept == null) {
			System.out.println("User: " + name + " not assigned to department, skipping role update.");
			return null;
		}
		
		String sfId = WWIDToUserIDsMap.get(WWID);
		
		if (sfId == null) {
			System.out.println("User: " + name + " not in TVA, skipping role update.");
			return null;
		}
		
		ArrayList<String> CSVParts = new ArrayList<String>();
		CSVParts.add(sfId != null ? sfId : "");
		CSVParts.add(WWID);
		CSVParts.add(name);
		CSVParts.add(partOfDept.getDeveloperName());
		return String.join(",",CSVParts);
	}
	
	private String getNewDeptKey(String deptId) {
		String newDeptKey = deptId;

		// check if the dept I am part of already exists

		if (Dept.deptsMapByDeptId.containsKey(newDeptKey)) {
			// dept with my deptid already seen, so generate a new deptid

			int suffix = 1;

			do {
				newDeptKey = deptId + "-" + suffix++;
			} while (Dept.deptsMapByDeptId.containsKey(newDeptKey));						
		}
		return newDeptKey;
	}
	
	private Dept generateNewDept(String newDeptKey) {
		Dept myNewDept = getNewDept(newDeptKey, title);
		myNewDept.manager = this;
		myNewDept.employees.add(this);
		managesDept = myNewDept;
		partOfDept = myNewDept;
		myNewDept.getDeveloperName();
		
		if (manager != null) {
			myNewDept.parentDept = manager.managesDept;
			manager.managesDept.childDepts.add(myNewDept);
		}
		Dept.deptsMapByDeptId.put(newDeptKey, myNewDept);
		System.out.println("Created dept: " + myNewDept.deptName + "(" + myNewDept.deptId + "), manager: " + myNewDept.manager.name +
				(manager != null ? ", child of dept: " + myNewDept.parentDept.deptName : ""));
		return myNewDept;
	}
	
	private Dept generateNewReportsDept(Dept myNewDept) {
		Dept directReportsDept = getNewDept(myNewDept.deptId + " reports", title + " reports");
		directReportsDept.manager = this;
		directReportsDept.parentDept = myNewDept;
		directReportsDept.isDirectReportsDept = true;
		directReportsDept.getDeveloperName();
		myNewDept.childDepts.add(directReportsDept);

		Dept.deptsMapByDeptId.put(myNewDept.deptId + " reports", directReportsDept);
		
		System.out.println("Created dept: " + directReportsDept.deptName + "(" + directReportsDept.deptId + "), manager: " + directReportsDept.manager.name);
		return directReportsDept;
	}
	
	public String getRole() {
		String retval = "";
		Role myRole = Role.rolesMapById.get(roleId);
		if (myRole != null) {
			retval = myRole.devName;
		}
		return retval;
	}
	
}
