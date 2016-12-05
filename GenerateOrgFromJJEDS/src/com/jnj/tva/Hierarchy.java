package com.jnj.tva;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Hierarchy {

	public HashMap<String,BaseDept> deptsMapById = new HashMap<String,BaseDept>();
	public HashMap<String,BaseDept> deptsMapByDevName = new HashMap<String,BaseDept>();
	public HashMap<String,BaseDept> directReportdeptsMapByManagerWWID = new HashMap<String,BaseDept>();
	public HashMap<String,BaseDept> deptsMapByManagerWWID = new HashMap<String,BaseDept>();
	public HashMap<String, ArrayList<Person>> personsListByRoleDevNameMap = new HashMap<String, ArrayList<Person>>();
	public ArrayList<BaseDept> topLevelDepts = new ArrayList<BaseDept>();
	public final HashMap<String, Person> myWWIDToPersonMap = new HashMap<String, Person>(); 
	
	public void runTopLevelCheck() {
		for (BaseDept db : deptsMapByDevName.values()) {
			if (db.isTopLevel()) {
				topLevelDepts.add(db);
			}
		}
	}
	
	public void assignPeopleAsManager
	
	public void addDept(BaseDept deptToAdd) {
		deptsMapByDevName.put(deptToAdd.devName, deptToAdd);
		deptsMapById.put(deptToAdd.id, deptToAdd);
	}
	
	public void outputHierarchyHumanReadable(OutputStream os) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		for (BaseDept bd : topLevelDepts) {
			for (String s : bd.getHumanReadableWithPersons("")) {
				bw.write(s + System.lineSeparator());
			};
		}
		bw.flush();
		bw.close();
	}
	
	public void addPerson(Person personToAdd) {
		myWWIDToPersonMap.put(personToAdd.employeeNumber, personToAdd);
		if (personToAdd instanceof User) {
			addPersonToRole(personToAdd);	
		}
		
		
	}
	
	private void addPersonToRole(Person p) {
		ArrayList<Person> personsList = personsListByRoleDevNameMap.get(((User)p).userRoleId);
		if (personsList == null) {
			personsList = new ArrayList<Person>();
			personsListByRoleDevNameMap.put(((User)p).userRoleId, personsList);
		}
		personsList.add(p);
	}
	
	private InferredDept generateNewDept(String deptKey, Employee manager, BaseDept parent) {
		InferredDept myNewDept = new InferredDept();
		
		myNewDept.parent = parent;
		myNewDept.id = deptKey;
		myNewDept.deptName = manager.title;
		myNewDept.getDeveloperName();
		myNewDept.id = deptKey;
		myNewDept.manager = manager;
		myNewDept.managerWWID = manager.employeeNumber;
		
		deptsMapByManagerWWID.put(manager.employeeNumber, myNewDept);
		
		return myNewDept;
	}
	
	private InferredDept generateNewReportsDept(InferredDept myNewDept) {
		InferredDept directReportsDept = new InferredDept();//getNewDept(myNewDept.id + " reports", e.title + " reports", e);
		
		directReportsDept.parent = myNewDept;
		directReportsDept.id = myNewDept.id + " reports";
		directReportsDept.deptName = myNewDept.deptName + " reports";
		directReportsDept.getDeveloperName();
		directReportsDept.manager = myNewDept.manager;
		directReportsDept.managerWWID = myNewDept.manager.employeeNumber;
		directReportsDept.isDirectReportsDept = true;
		
		myNewDept.childDepts.add(directReportsDept);

		deptsMapById.put(myNewDept.id + " reports", directReportsDept);
		directReportdeptsMapByManagerWWID.put(myNewDept.managerWWID, directReportsDept);
		
		System.out.println("Created dept: " + directReportsDept.deptName + "(" + directReportsDept.id + "), manager: " + directReportsDept.manager.name);
		return directReportsDept;
	}
	
	/*
	 * 
	 * This method will infer a dept structure based on the reporting lines in the myWWIDToPersonsMap
	 * It will add to any structure in the depts maps a new, inferred one using InferredDept objects
	 * 
	 */
	
	public void inferDeptStructure(String employeeWWID) {
		
		Employee e = (Employee)myWWIDToPersonMap.get(employeeWWID);
		
		if (e.manager == null) {
			// if I have no Manager, I am a top-level node

			String newDeptKey = getNewDeptKey(e.deptId);

			InferredDept myNewDept = generateNewDept(newDeptKey, e, null);
	
			// generate a dept for direct reports without own reports (leaf nodes)

			generateNewReportsDept(myNewDept);

		} else if (e.manager != null && e.employees.isEmpty()) {
			// if I have a manager, but no employees, I am a leaf node

			// I am a part of my manager's direct reports dept.
			// find that dept first
			
			InferredDept directReportsDept = (InferredDept)directReportdeptsMapByManagerWWID.get(e.managerWWID);
			
			if (directReportsDept == null) {
				System.out.println("Couldn't find a reports dept for employee: " + e.name +  ", manager is: " + e.manager.name);
			} else {
				e.partOfDept = directReportsDept;
				directReportsDept.personsInDept.add(e);	
				System.out.println(e.name + ", part of: " + directReportsDept.deptName);
			}
		} else if (e.manager != null && !e.employees.isEmpty()) {
			// if I have both a manager and employees, I am an intermediate node

			// I have my department which my subordinates will be part of

			// check if the dept I am part of already exists

			String newDeptKey = getNewDeptKey(e.deptId);

			InferredDept myNewDept = generateNewDept(newDeptKey, e, deptsMapByManagerWWID.get(e.managerWWID));

			// generate a dept for direct reports without own reports (leaf nodes)

			generateNewReportsDept(myNewDept);
		}

		ArrayList<Person> employeeList = new ArrayList<Person>(e.employees);
		Collections.sort(employeeList);
		
		for (Person p : employeeList) {
			inferDeptStructure(((Employee)p).employeeNumber);
		}
	}

	private String getNewDeptKey(String deptId) {
		String newDeptKey = deptId;

		// check if the dept I am part of already exists

		if (deptsMapById.containsKey(newDeptKey)) {
			// dept with my deptid already seen, so generate a new deptid

			int suffix = 1;

			do {
				newDeptKey = deptId + "-" + suffix++;
			} while (deptsMapById.containsKey(newDeptKey));						
		}
		return newDeptKey;
	}

}
