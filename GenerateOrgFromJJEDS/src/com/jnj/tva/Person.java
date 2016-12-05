package com.jnj.tva;

import java.util.HashSet;
import java.util.Vector;

public abstract class Person implements Comparable<Person>{
	
	protected static final String prefix = "|---";
	public String name;
	public String employeeNumber;
	public HashSet<Person> employees = new HashSet<Person>();
	
	public Vector<String> getEmployees() {
		Vector<String> retval = new Vector<String>();

		retval.add("(" + getDeptId() + ": " + name + ") " + name);
		if (!employees.isEmpty()) {
			for (Person e : employees) {
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
	
	public abstract String getDeptId();
	public abstract String toString();

	public int compareTo(Person e) {
		return name.compareTo(e.name);
	}
}
