/**
 * 
 *   Copyright (C) 2008 Johannes Heikkinen
 *
 *   This program is free software; you can redistribute it and/or modify it under the terms of
 *   the GNU General Public License as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along with this program;
 *   if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *   MA 02111-1307 USA
 *
 */
package checker.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Class for reading the license files to be used for getting the compatability data.
 * 
 * @author Johannes Heikkinen (jpkheikk)
 *
 */

public class LicenseReader {
	LicenseLister lLister = new LicenseLister();
	String compatibleRow = "";
	String[] compatibleLicenses = new String[lLister.getLicenseCount()];
	boolean rowFound = false;
	/**
	 * Class constructor.
	 */
	public LicenseReader() {
		
	}
	/**
	 * Method for getting the compatible licenses from the .meta file
	 * to be put in a String table for later use.
	 * 
	 * TODO get rid of this method.
	 * 
	 */
	public String[] listCompatibleLicenses(String licenseName) {
		InputStream in = null;
		Reader reader = null;
		BufferedReader breader = null;
		String licenseNameWithEnding = licenseName + ".meta";
		File pathToFolder = new File("licenses", licenseNameWithEnding);
		try {
			//creating a binary stream for reading a file
			in = new FileInputStream(pathToFolder);
			//Creating a string stream, which reads the chars from the binary stream.
			//with utf-8 character set
			//TODO what character set to use?
			reader = new InputStreamReader(in, "UTF-8");
			//Buffering the stream.
			breader = new BufferedReader(reader);
			String row = null;
			//BufferedReader -class has an method for reading rows.
			//It returns null if at EOF.
			//int i = 0;
			while ((row =breader.readLine()) != null ){
				//System.out.println(row);
				this.getCompatibleRow(row);
				//compatibleRow[i] = row;
				//i++;
			}
			if (this.rowFound == true) {			
				this.rowFound = false;
				return parseMetaFile();
				
			} return null;
		} catch (IOException ioe) {
			//System.out.println("File not found?");
			ioe.printStackTrace();
			return null;
		} finally {
			//Closing the stream.
			try {
				if (breader != null) breader.close();
                if (reader != null) reader.close();
                if (in != null) in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	/**
	 * Method for getting a row of text from the .meta file.
	 * 
	 * @param licenseName Name of the license WITHOUT the ending. (.meta)
	 * @param identifierTag Name of the identifier tag e.g. "fullName", "isCompatible".
	 * 
	 * @return parsedRow if a row matching with the identifierTag is found.
	 * @return null if a row matching with the identifierTag is NOT found.
	 * 
	 */
	private String getMetaFileTextRow(String licenseName, String identifierTag) {
		InputStream in = null;
		Reader reader = null;
		BufferedReader breader = null;
		String parsedRow = "";
		String licenseNameWithEnding = licenseName + ".meta";
		File pathToFolder = new File("licenses", licenseNameWithEnding);
		try {
			//creating a binary stream for reading a file
			in = new FileInputStream(pathToFolder);
			//Creating a string stream, which reads the chars from the binary stream.
			//UTF-8 character set.
			reader = new InputStreamReader(in, "UTF-8");
			//Buffering the stream.
			breader = new BufferedReader(reader);
			String row = null;
			//BufferedReader -class has an method for reading rows.
			//It returns null if at EOF.
			while ((row =breader.readLine()) != null ){
				//checking if any of the meta file rows match the identifier
				parsedRow = this.getMetaTextRow(row, identifierTag);
			}
			//if a matching text row is found it is returned.
			if (!parsedRow.equalsIgnoreCase("notFound")) {			
				return parsedRow;
				
			}
			//if no matching row has been found null is returned.
			else return null;
		} catch (IOException ioe) {
			//System.out.println("File not found?");
			ioe.printStackTrace();
			return null;
		} finally {
			//Closing the stream.
			try {
				if (breader != null) breader.close();
                if (reader != null) reader.close();
                if (in != null) in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	
	private String readLicenseText(String licenseIDName) {
		InputStream in = null;
		Reader reader = null;
		BufferedReader breader = null;
		String licenseNameWithEnding = licenseIDName + ".txt";
		File pathToFolder = new File("licenses", licenseNameWithEnding);
		try {
			//creating a binary stream for reading a file
			in = new FileInputStream(pathToFolder);
			//Creating a string stream, which reads the chars from the binary stream.
			//UTF-8 character set.
			reader = new InputStreamReader(in, "UTF-8");
			//Buffering the stream.
			breader = new BufferedReader(reader);
			String row = null;
			String licenseText = "";
			//BufferedReader -class has an method for reading rows.
			//It returns null if at EOF.
			while ((row =breader.readLine()) != null ){
				//putting each read row of text to the String table.
				licenseText.concat(row);
			}
			return licenseText;

		} catch (IOException ioe) {
			//System.out.println("File not found?");
			ioe.printStackTrace();
			return null;
		} finally {
			//Closing the stream.
			try {
				if (breader != null) breader.close();
                if (reader != null) reader.close();
                if (in != null) in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	/**
	 * Method used for finding the specific row of text from the meta file.
	 * The row must start with the syntax given in the parameter indentifierTag.
	 * 
	 * @param row is one row read from the text (.txt or .meta) file.
	 * @param indentifierTag is the String tag used to identify the row to be read.
	 */
	private String getMetaTextRow(String row, String identifierTag) {
		String foundMatchingRow = "notFound";
		if (row.length() >= identifierTag.length() && 
				row.substring(0,identifierTag.length()).equalsIgnoreCase(identifierTag + " :")) {
			foundMatchingRow = row;
			return foundMatchingRow;
			
		} else {
			return foundMatchingRow;
		}
		
	}
	/**
	 * Method used for finding the specific row of text from the meta file.
	 * The row must start with the syntax given in the "if"-sentence.
	 * 
	 * @param row is one row read from the text (.txt or .meta) file.
	 */
	private void getCompatibleRow(String row) {
		if (row.length() > 13 && row.substring(0,14).equalsIgnoreCase("isCompatible :")) {
			this.rowFound = true;
			this.compatibleRow = row;
			
		}
		
	}

	private String[] parseMetaFile() {
		String parsedRow = "";
		parsedRow = this.compatibleRow.substring(15);
		String[] result = parsedRow.split("\\s");
		return result;
		
			
	}
	/**
	 * Method used for fetching the full name of a specified license
	 * from the .meta file. Returns "noFullName" if full name is not specified in the meta file.
	 * 
	 * @param licenseIDName name of the license file in the database. WITHOUT ending (.meta).
	 * 	
	 */
	public String getFullName(String licenseIDName) {
		String foundRow = this.getMetaFileTextRow(licenseIDName, "fullName");
		if (foundRow == null) {
			return licenseIDName;
		} else {
			//parsing the identifier out of the full name
			String fullName = foundRow.substring(11);
			return fullName;
		}
		
		
	}
	public String getLicenseText(String licenseIDName) {
		String licenseText = this.readLicenseText(licenseIDName);
		return licenseText;
	}
		
	
	
}
