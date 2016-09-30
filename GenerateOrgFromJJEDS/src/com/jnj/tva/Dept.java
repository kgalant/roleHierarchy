package com.jnj.tva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class Dept {

	private static final String prefix = "|---";
	public String deptId;
	public String parentDeptId;
	public Dept parentDept;
	public String managerWWID;
	public Employee manager;
	public String deptName;
	public String developerName;
	public HashSet<Dept> childDepts = new HashSet<Dept>();
	public HashSet<Employee> employees = new HashSet<Employee>();
	public HashSet<String> employeeWWIDs = new HashSet<String>();
	public boolean isDirectReportsDept = false;
	public boolean isDeptIDManuallySet = false;
	
	public static HashMap<String, Dept> deptsMapByDevName = new HashMap<String, Dept>();
	public static HashMap<String, Dept> deptsMapByDeptId = new HashMap<String, Dept>();
	
	public Role correspondingRole;

	public boolean hasRealUsers(HashMap<String,String> WWIDToUserIDsMap) {
		boolean hasTVAUser = false;

		for (Employee e : employees) {
			if (WWIDToUserIDsMap.containsKey(e.WWID)) {
				hasTVAUser = true;
			}
			if (hasTVAUser) {
				break;
			}
		}
		if (!hasTVAUser) {
			for (Dept d : childDepts) {
				hasTVAUser = d.hasRealUsers(WWIDToUserIDsMap);
				if (hasTVAUser) {
					break;
				}
			}			
		}

		return hasTVAUser;
	}

	public Vector<String> getDepts(HashMap<String,String> WWIDToUserIDsMap, String prefixProvided, boolean showNonTVAUsers) {
		Vector<String> retval = new Vector<String>();
		
		String myPrefix = prefixProvided != null ? prefixProvided : Dept.prefix;

		if (!hasRealUsers(WWIDToUserIDsMap)) {
			return retval;
		}

		retval.add("D: ID:" + deptId + " | NAME: " + deptName + " | manager: " + manager.name);

		for (Employee e : employees) {
			if (WWIDToUserIDsMap.get(e.WWID) != null || showNonTVAUsers) {
				retval.add(myPrefix + "E: " + (WWIDToUserIDsMap.get(e.WWID) != null ? "" : " ** ") + e.name + " | " + deptName);
			}
		}

		if (!childDepts.isEmpty()) {
			for (Dept d : childDepts) {
				Vector<String> recursedRetval = d.getDepts(WWIDToUserIDsMap, myPrefix, showNonTVAUsers);
				// now prefix the returned strings

				for (String s : recursedRetval) {
					retval.add(myPrefix + s);
				}
			}			
		}
		return retval;
	}
	
	public Vector<String> getDeptsDeptPrefixed(HashMap<String,String> WWIDToUserIDsMap, String prefixProvided) {
		Vector<String> retval = new Vector<String>();
		
		String myPrefix = prefixProvided == null ? deptId : prefixProvided + "," + deptId;

		if (!hasRealUsers(WWIDToUserIDsMap)) {
			return retval;
		}

		retval.add(myPrefix + ",D: " + deptName + "," + manager.name);

		for (Employee e : employees) {
			retval.add(myPrefix + ",E: " + e.name + "," + deptName);
		}

		if (!childDepts.isEmpty()) {
			for (Dept d : childDepts) {
				Vector<String> recursedRetval = d.getDeptsDeptPrefixed(WWIDToUserIDsMap, myPrefix);
				// now prefix the returned strings

				//Collections.sort(recursedRetval);

				for (String s : recursedRetval) {
					retval.add(s);
					//retval.add(myPrefix + "," + s);
				}
			}			
		}
		return retval;
	}

	// generate CSV lines like this "Dept_ID_source__c,Parent_ID_source__c,Name_source__c,Manager_source__c,Manager_Name"

	public Vector<String> getDeptsCSV(HashMap<String,String> WWIDToUserIDsMap) {
		Vector<String> retval = new Vector<String>();

		if (!hasRealUsers(WWIDToUserIDsMap)) {
			return retval;
		}

		ArrayList<String> CSVParts = new ArrayList<String>();

		CSVParts.add(deptId);
		CSVParts.add(parentDept != null ? parentDept.deptId : "");
		CSVParts.add("\"" + deptName + "\"");
		CSVParts.add(manager != null ? manager.WWID : "");
		CSVParts.add(manager != null && WWIDToUserIDsMap.get(manager.WWID) != null ? WWIDToUserIDsMap.get(manager.WWID) : "");
		CSVParts.add(manager != null ? "\"" + manager.name + "\"" : "");

		retval.add(String.join(",",CSVParts));

		if (!childDepts.isEmpty()) {
			for (Dept d : childDepts) {
				retval.addAll(d.getDeptsCSV(WWIDToUserIDsMap));
			}			
		}
		return retval;
	}

	public Vector<String> getRolesCSV(int level, HashMap<String,String> WWIDToUserIDsMap) {
		level++;
		Vector<String> retval = new Vector<String>();

		if (!hasRealUsers(WWIDToUserIDsMap)) {
			return retval;
		}

		ArrayList<String> CSVParts = new ArrayList<String>();

		// check if we have another department with the same name

		/*String nameSuffix = "";

		boolean hasNameTwin = false;

		if (parentDept != null) {
			for (Dept d : parentDept.childDepts) {
				if (d.deptId != deptId && !d.isDirectReportsDept && d.manager.title == manager.title) {
					hasNameTwin = true;
				}
			}
		}

		if (hasNameTwin) {
			nameSuffix = " (" + manager.name + ")";
		}*/

		CSVParts.add("\"" + deptName + "\"");
		//CSVParts.add("\"" + manager.title + nameSuffix + (isDirectReportsDept ? " (directs)" : "") + "\"");
		CSVParts.add("\"" + getDeveloperName() + "\"");
		CSVParts.add("\"" + deptName + "\"");
		//CSVParts.add("\"" + deptName.replaceAll(",", "") + " (" + deptId + ")" + "\"");
		//CSVParts.add("\"" + manager.title + nameSuffix + (isDirectReportsDept ? " (directs)" : "") + "\"");
		CSVParts.add(parentDept != null ? "\"" + parentDept.getDeveloperName() + "\"": "" );
		CSVParts.add("");
		CSVParts.add("None");
		CSVParts.add("None");
		CSVParts.add("Edit");
		CSVParts.add(""+level);

		retval.add(String.join(",",CSVParts));

		if (!childDepts.isEmpty()) {
			for (Dept d : childDepts) {
				retval.addAll(d.getRolesCSV(level, WWIDToUserIDsMap));
			}			
		}
		return retval;
	}

	public Vector<String> getUserRolesCSV(HashMap<String,String> WWIDToUserIDsMap) {

		Vector<String> retval = new Vector<String>();

		ArrayList<String> CSVParts = new ArrayList<String>();



		CSVParts.add(manager != null && WWIDToUserIDsMap.get(manager.WWID) != null ? WWIDToUserIDsMap.get(manager.WWID) : "");
		CSVParts.add(manager != null ? manager.WWID : "");
		CSVParts.add(getDeveloperName());

		retval.add(String.join(",",CSVParts));

		if (!childDepts.isEmpty()) {
			for (Dept d : childDepts) {
				retval.addAll(d.getUserRolesCSV(WWIDToUserIDsMap));
			}			
		} else {			
			for (Employee e : manager.employees) {
				CSVParts.clear();
				CSVParts.add("");
				CSVParts.add(e.WWID);
				CSVParts.add(e.partOfDept.getDeveloperName());
				retval.add(String.join(",",CSVParts));
			}



		}
		return retval;
	}

	public String getDeveloperName() {
		if (developerName == null && !isDeptIDManuallySet) {
			developerName = getDeveloperName(deptName, deptId);
		} else if (isDeptIDManuallySet) {
			developerName = deptId;
		}
		deptsMapByDevName.put(developerName, this);
		return developerName;
	}

	public static String getDeveloperName (String originalName, String deptId) {
		String retval = originalName + "_" + deptId;
		retval = retval.replace(" ", "_");
		retval = retval.replace("'", "_");
		retval = retval.replace("&", "A");
		retval = retval.replace(",", "_");
		retval = retval.replace("-", "_");
		retval = retval.replace("?", "q");
		retval = retval.replaceAll(",", "");
		retval = retval.replace(".", "");
		retval = retval.replace("(", "");
		retval = retval.replace(")", "");
		retval = retval.replace("/", "_");
		retval = retval.replaceAll("_+", "_");
		retval = "R" + retval;
		return retval;
	}

}