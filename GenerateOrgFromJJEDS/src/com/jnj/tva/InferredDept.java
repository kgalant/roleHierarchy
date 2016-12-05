package com.jnj.tva;

import java.util.ArrayList;

public class InferredDept extends BaseDept {

	public String parentDeptId;
	public String managerWWID;
	public String deptName;
	public boolean isDirectReportsDept = false;
	public boolean isDeptIDManuallySet = false;
	
	@Override
	public String getDeveloperName() {
		if (devName == null && !isDeptIDManuallySet) {
			devName = getDeveloperName(deptName, id);
		} else if (isDeptIDManuallySet) {
			devName = id;
		}
		return devName;
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
	
	@Override
	public ArrayList<String> getHumanReadable(String myPrefix) {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add( myPrefix + "D: " + id + " | NAME: " + deptName + " | MGR: " + manager.name);
		return retval;
	}
	
	@Override
	public ArrayList<String> getHumanReadableWithPersons(String myPrefix) {
		ArrayList<String> retval = new ArrayList<String>();
		retval.addAll(getHumanReadable(myPrefix));
		for (Person p : personsInDept) {
			retval.add(prefix + myPrefix + p.toString());
		}
		for (BaseDept b : childDepts) {
			retval.addAll(b.getHumanReadableWithPersons(prefix + myPrefix));
		}
		return retval;
	}
}
