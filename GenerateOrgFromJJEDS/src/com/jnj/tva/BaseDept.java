package com.jnj.tva;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class BaseDept {
	protected static final String prefix = "|---";
	
	public HashSet<BaseDept> childDepts = new HashSet<BaseDept>();
	public HashSet<Person> personsInDept = new HashSet<Person>();
	public Person manager;
	public BaseDept parent;
	public String id;	
	public String devName;
	
	public boolean isTopLevel() {
		if (parent == null) return true;
		return false;
	}
	
	public abstract String getDeveloperName();	
	
	public ArrayList<String> getHumanReadable(String myPrefix) {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add( myPrefix + "D: " + devName + " | ID: " + id + " | MGR: " + (manager != null ? manager.name : "none"));
		return retval;
	}
	
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
