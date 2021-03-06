package com.jnj.tva;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.salesforce.migrationtoolutils.Utils;

public class GenerateOrgFromJJEDS {

	private static ArrayList<String> allowedMRCs = new ArrayList<String>();

	private static final String TVAUSERSFILE = "TVAusersfile";
	private static final String IDFIELD = "idfield";
	private static final String WWIDFIELD = "wwidfield";
	private static final String ALLOWEDMRCS = "allowedMRCs";
	private static final String INPUTUSERSFILE = "inputUsersfile";
	private static final String INPUTWWIDFIELD = "inputWWIDField";
	private static final String INPUTDEPTNAME = "inputDeptName";
	private static final String INPUTDEPTID = "inputDeptId";
	private static final String INPUTNAME = "inputName";
	private static final String INPUTEMAIL = "inputEmail";
	private static final String INPUTTITLE = "inputTitle";
	private static final String INPUTSPONSOREMAIL = "inputSponsorEmail";
	private static final String INPUTSUPERVISOR = "inputSupervisor";
	private static final String INPUTMRC = "inputMRC";

	private static final String OUTPUTHIERARCHYFILENAME = "outputHierarchyFile";
	private static final String OUTPUTHIERARCHYCSVFILENAME = "outputHierarchyCSVFile";
	private static final String OUTPUTDEPTFILENAME = "outputDeptFile";

	private static final String OUTPUTUSERSDATAFILENAME = "outputUsersDataFile";
	private static final String OUTPUTROLESFILENAME = "outputRolesFile";
	private static final String OUTPUTUSERROLESFILENAME = "outputUserRolesFile";

	private static final String HIERARCHYOUTPUTPREFIX = "hierarchyOutputPrefix";



	public static void main(String[] args) throws IOException {

		// set up configuration

		if (args.length < 1) {
			System.out.println("No parameters supplied. First parameter must be path of config file.");
			System.exit(-1);
		}

		String propsFilename = args[0];

		Properties props = Utils.initProps(propsFilename);

		String usersfileFilename = props.getProperty(TVAUSERSFILE);
		String idFieldName = props.getProperty(IDFIELD);
		String wwidFieldName = props.getProperty(WWIDFIELD);	
		String allowedMRCsList = props.getProperty(ALLOWEDMRCS);
		allowedMRCs.addAll(Arrays.asList(allowedMRCsList.split(",")));
		String inputUsersFile = props.getProperty(INPUTUSERSFILE);
		String inputWwidFieldName = props.getProperty(INPUTWWIDFIELD);
		String inputDeptName = props.getProperty(INPUTDEPTNAME);
		String inputDeptId = props.getProperty(INPUTDEPTID);
		String inputName = props.getProperty(INPUTNAME);
		String inputEmail = props.getProperty(INPUTEMAIL);
		String inputTitle = props.getProperty(INPUTTITLE);
		String inputSponsorEmail = props.getProperty(INPUTSPONSOREMAIL);
		String inputSupervisor = props.getProperty(INPUTSUPERVISOR);
		String inputMRC = props.getProperty(INPUTMRC);
		String outputHierarchyFilename = props.getProperty(OUTPUTHIERARCHYFILENAME);
		String outputHierarchyCSVFilename = props.getProperty(OUTPUTHIERARCHYCSVFILENAME);
		String outputDeptFilename = props.getProperty(OUTPUTDEPTFILENAME);
		String outputUsersDataFilename = props.getProperty(OUTPUTUSERSDATAFILENAME);
		String outputRolesFilename = props.getProperty(OUTPUTROLESFILENAME);
		String outputUserRolesFile = props.getProperty(OUTPUTUSERROLESFILENAME);
		String hierarchyOutputPrefix = props.getProperty(HIERARCHYOUTPUTPREFIX);



		// start off - generate a map of employee IDs currently in org

		HashMap<String,String> myWWIDToUserIDsMap = new HashMap<String,String>();


		File csvData = new File(usersfileFilename);
		CSVParser parser = CSVParser.parse(csvData, Charset.forName("UTF-8") , CSVFormat.EXCEL.withHeader());
		int counter = 0;


		for (CSVRecord csvRecord : parser) {

			String id = csvRecord.get(idFieldName);
			String WWID = csvRecord.get(wwidFieldName);

			if (WWID == null || WWID.length()<0) continue;

			myWWIDToUserIDsMap.put(WWID, id);
		}

		HashMap<String, Dept> myDeptsMap = new HashMap<String, Dept>();
		HashMap<String, Employee> myEmailToEmployeeMap = new HashMap<String, Employee>();
		HashMap<String, Employee> myWWIDToEmployeeMap = new HashMap<String, Employee>();



		csvData = new File(inputUsersFile);
		parser = CSVParser.parse(csvData, Charset.forName("UTF-8") , CSVFormat.EXCEL.withHeader());
		counter = 0;
		// first pass, generate a list of employees

		boolean hasWWID;
		boolean hasManager;

		for (CSVRecord csvRecord : parser) {
			hasWWID = false;
			hasManager = false;

			counter++;
			if (counter % 1000 == 0) System.out.println("Processed: " + counter + " records...");


			String WWID = csvRecord.get(inputWwidFieldName);
			if (WWID != null && !WWID.isEmpty()) {
				hasWWID = true;
			}

			String deptName = csvRecord.get(inputDeptName);
			String deptId = csvRecord.get(inputDeptId);
			String name = csvRecord.get(inputName);
			String email = csvRecord.get(inputEmail);
			String title = csvRecord.get(inputTitle);
			String sponsorEmail = csvRecord.get(inputSponsorEmail);
			String supervisor = csvRecord.get(inputSupervisor);
			//if (supervisor.length()<2) continue;
			String[] supervisorSplits = supervisor.split(",");
			String managerWWID = null;
			if (supervisorSplits.length == 3) {
				managerWWID = supervisorSplits[2];
				hasManager = true;
			} else {
				if (sponsorEmail != null && !sponsorEmail.isEmpty() && myEmailToEmployeeMap.get(sponsorEmail.toLowerCase()) != null) {
					managerWWID = myEmailToEmployeeMap.get(sponsorEmail.toLowerCase()).WWID;
					if (managerWWID != null && !managerWWID.isEmpty()) {
						hasManager = true;
					}
				}
			}


			String mrc = csvRecord.get(inputMRC);



			// if it doesn't have a wwid, and supervisor, or not in allowed MRCs,throw it away

			if (!hasWWID || !hasManager || !allowedMRCs.contains(mrc)) {
				//System.out.println("Discarding user: " + name + " hasWWID: " 
				//	+ hasWWID + " hasManager: " + hasManager + " in allowedMRC: " + allowedMRCs.contains(mrc));
				continue;
			}

			Employee e = new Employee();

			e.WWID = WWID;
			e.deptId = deptId;
			e.deptName = deptName;
			e.name = name;
			e.managerWWID = managerWWID;
			e.email = email.toLowerCase();
			e.title = title;

			myWWIDToEmployeeMap.put(WWID, e);
			myEmailToEmployeeMap.put(email.toLowerCase(), e);
		}

		// second pass, now that we have all possible employees, generate the relationships (i.e. map managers where they exist)

		for (Employee e : myWWIDToEmployeeMap.values()) {
			e.manager = myWWIDToEmployeeMap.get(e.managerWWID);
			if (e.manager != null) {
				myWWIDToEmployeeMap.get(e.managerWWID).employees.add(e);
			}
		}

		// third pass - write out all the managers

		for (Employee e : myWWIDToEmployeeMap.values()) {
			if (e.manager == null) {
				// top level employee - output it
				Vector<String> employeeNames = e.getEmployees();
				for (String s : employeeNames) {
					System.out.println(s);
				}
			}
		}

		// fourth pass - generate dept structure based on employee reporting hierarchy

		for (Employee e : myWWIDToEmployeeMap.values()) {
			if (e.manager == null) {
				// top level employee - generate dept structure
				e.generateDeptStructure(myDeptsMap);
			}
		}

		// fifth pass - output dept structure - human-friendly

		File outputData = new File(outputHierarchyFilename);
		FileWriter fw = new FileWriter(outputData);

		for (Dept d : myDeptsMap.values()) {
			if (d.parentDept == null) {
				// top level dept - output it
				Vector<String> deptNames = d.getDepts(myWWIDToUserIDsMap, hierarchyOutputPrefix);
				for (String s : deptNames) {
					fw.write(s + System.lineSeparator());
				}
			}
		}
		fw.flush();
		fw.close();

		// pass 5.5 - output dept structure - human-friendly CSV

		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputHierarchyCSVFilename), "UTF-8"));


		for (Dept d : myDeptsMap.values()) {
			if (d.parentDept == null) {
				// top level dept - output it
				Vector<String> deptNames = d.getDeptsDeptPrefixed(myWWIDToUserIDsMap, null);
				for (String s : deptNames) {
					out.write(s + System.lineSeparator());
				}
			}
		}
		out.flush();
		out.close();

		// sixth pass - output dept structure - for csv load


		out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDeptFilename), "UTF-8"));


		// header row

		out.write("Dept_ID_source__c,Parent_ID_source__c,Name_source__c,Manager_WWID,Manager_source__c,Manager_Name" + System.lineSeparator());

		for (Dept d : myDeptsMap.values()) {
			if (d.parentDept == null) {
				// top level dept - output it
				Vector<String> deptNames = d.getDeptsCSV(myWWIDToUserIDsMap);
				for (String s : deptNames) {
					out.write(s + System.lineSeparator());
				}
			}
		}
		out.flush();
		out.close();

		// seventh pass - output employee mappings to department, managerID updates

		outputData = new File(outputUsersDataFilename);
		fw = new FileWriter(outputData);

		// header row

		fw.write("Id,ManagerId,Dept_ID__c,Name,ManagerName" + System.lineSeparator());

		for (Employee e : myWWIDToEmployeeMap.values()) {
			if (myWWIDToUserIDsMap.get(e.WWID) == null) continue; //don't bother with users we don't have a UserID for 

			ArrayList<String> CSVParts = new ArrayList<String>();

			CSVParts.add(myWWIDToUserIDsMap.get(e.WWID));
			CSVParts.add(myWWIDToUserIDsMap.get(e.managerWWID) == null ? "" : myWWIDToUserIDsMap.get(e.managerWWID));
			CSVParts.add(e.manager != null ? e.manager.deptId : "");
			CSVParts.add(e.name);
			CSVParts.add(e.manager != null ? e.manager.name : "");

			fw.write(String.join(",",CSVParts) + System.lineSeparator());

		}
		fw.flush();
		fw.close();

		// eighth pass - role hierarchy

		outputData = new File(outputRolesFilename);
		fw = new FileWriter(outputData);

		// header row

		fw.write("RollupDescription,DeveloperName,Name,ParentRoleDevName,Id,OpportunityAccessForAccountOwner,Level" + System.lineSeparator());
		ArrayList<String> lines = new ArrayList<String>();
		for (Dept d : myDeptsMap.values()) {
			if (d.parentDept == null) {
				//if (d.manager != null && d.manager.WWID.equals("85010410")) {
				// top level dept - output it
				Vector<String> deptNames = d.getRolesCSV(0, myWWIDToUserIDsMap);
				lines.addAll(deptNames);
			}
		}


		ArrayList<String> linesLeftOver = new ArrayList<String>();

		int level = 0;
		do {
			level++;
			System.out.println("Level: " + level);
			System.out.println("Lines: " + lines.size());
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.endsWith(""+level)) {
					fw.write(line + System.lineSeparator());
				} else {
					linesLeftOver.add(line);
				}
			}
			lines.clear(); lines.addAll(linesLeftOver);
			System.out.println("LinesLeftOver: " + linesLeftOver.size());
			linesLeftOver.clear();
		} while (!lines.isEmpty() && level < 10);




		fw.flush();
		fw.close();

		// ninth pass - users in role hierarchy

		outputData = new File(outputUserRolesFile);
		fw = new FileWriter(outputData);

		// header row
		/*
		fw.write("Id,WWID,RoleDeveloperName" + System.lineSeparator());
		lines.clear();
		for (Dept d : myDeptsMap.values()) {
			if (d.parentDept == null) {
			//if (d.manager != null && d.manager.WWID.equals("85010410")) { // manager hack to output below a certain manager only
				// top level dept - output it
				Vector<String> deptNames = d.getUserRolesCSV(myWWIDToUserIDsMap);
				for (String s : deptNames) {
					fw.write(s + System.lineSeparator());
				}
			}
		}

		fw.flush();
		fw.close();

		 */

		// tenth pass - users in role hierarchy

		outputData = new File(outputUserRolesFile);
		fw = new FileWriter(outputData);

		// header row

		fw.write("Id,WWID,Name,RoleDeveloperName" + System.lineSeparator());
		lines.clear();
		for (Employee e : myWWIDToEmployeeMap.values()) {		
			if (e.getRole(myWWIDToUserIDsMap) != null) {
				fw.write(e.getRole(myWWIDToUserIDsMap) + System.lineSeparator());
			}
		}

		fw.flush();
		fw.close();

	}

}
