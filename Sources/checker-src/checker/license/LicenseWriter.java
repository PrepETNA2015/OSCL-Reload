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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class for writing the license files according to the input given by the user.
 * 
 * @author Johannes Heikkinen (jpkheikk)
 * 
 */

public class LicenseWriter {

	/*
	 * The following static string is used to separate the licenses
	 * added by the user from the licenses originally included in 
	 * the software when naming the newly made licenses.
	 */
	public static String USERINDENTIFIER = "user";
	
	private String licenseName, licenseFullName, licenseText;
	private String[] compatibleLicenses;
	private int selectedTag;
	private int compatibleCount = 0;
	private LicenseLister lLister = new LicenseLister();
	FileWriter writer;
	//File pathToFolder = new File("oslcv2/licenses", "jpkheikktest.txt");
	
	/**
	 * OLD!!! Class constructor used for writing the license.txt files.
	 * 
	 * @author jpkheikk
	 * 
	 * @param licenseName	the name of the license to be written
	 * @param licenseText	the text of the license
	 * 
	 */
	public LicenseWriter(String licenseName, String licenseText) {
		this.licenseName = licenseName;
		this.licenseFullName = licenseName;
		this.licenseText = licenseText;
	}
	
	/**
	 * OLD!!! Class constructor used for writing the license.meta files.
	 * 
	 * @author jpkheikk
	 * 
	 * @param licenseName	the name of the license to be written
	 * @param compatibleLicenses	list of user given compatible licenses
	 * @param countOfCompatibleLicenses	count of user given compatible licenses
	 */
	public LicenseWriter(String licenseName, String[] compatibleLicenses, int countOfCompatibleLicenses) {
		this.compatibleLicenses = new String[countOfCompatibleLicenses];
		this.compatibleLicenses = compatibleLicenses;
		this.licenseName = licenseName;
		this.licenseFullName = licenseName;
		this.compatibleCount = countOfCompatibleLicenses;
		//pathToFolder = 
	}
	
	/**
	 * NEW! Class constructor used for writing the license.txt files.
	 * 
	 * @author jpkheikk
	 * 
	 * @param licenseName	the name of the license to be written
	 * @param licenseText	the text of the license
	 * @param selectedTag	the tag to indicate if user has selected some tag for this license
	 * 
	 */
	public LicenseWriter(String licenseFullName, String licenseText, int selectedTag) {
		this.licenseName = licenseFullName;
		//this.licenseFullName = licenseName;
		this.licenseText = licenseText;
		this.selectedTag = selectedTag;
	}
	/**
	 * NEW! Class constructor used for writing the license.meta file.
	 * 
	 * @author jpkheikk
	 * 
	 * @param licenseFullName
	 * @param compatibleLicenses
	 * @param countOfCompatibleLicenses 
	 * @param selectedTag the tag to indicate if user has selected some tag for this license
	 */
	public LicenseWriter(String licenseFullName, String[] compatibleLicenses, int countOfCompatibleLicenses, int selectedTag) {
		this.compatibleLicenses = new String[countOfCompatibleLicenses];
		this.compatibleLicenses = compatibleLicenses;
		this.licenseName = licenseFullName;
		//this.licenseFullName = licenseFullName;
        	this.compatibleCount = countOfCompatibleLicenses;
        	this.selectedTag = selectedTag;
	}
	/**
	 * NEW! Class constructor used for writing both of the files.
	 * 
	 * @author jpkheikk
	 * 
	 * @param licenseFullName
	 * @param compatibleLicenses
	 * @param countOfCompatibleLicenses 
	 * @param selectedTag the tag to indicate if user has selected some tag for this license
	 */
	public LicenseWriter(String licenseFullName, String licenseText, String[] compatibleLicenses, int countOfCompatibleLicenses, int selectedTag) {
		this.compatibleLicenses = new String[countOfCompatibleLicenses];
		this.compatibleLicenses = compatibleLicenses;
		this.licenseName = licenseFullName;
		this.licenseText = licenseText;
        	this.compatibleCount = countOfCompatibleLicenses;
        	this.selectedTag = selectedTag;
	}

	/**
	 * Method for reading the license information. Kind of useless at the moment.
	 * 
	 * @author jpkheikk
	 * @param licenseName
	 * @param licenseText
	 */
	private void readLicense(String licenseName, String licenseText) {
		this.licenseName = licenseName;
		this.licenseText = licenseText;
	}
	/**
	 * Method for creating the license ID name from the
	 * full name given by the user.
	 * 
	 * The idea is that all the " " are replaced with "_"
	 * and all the "-" are replaced with "_" too.
	 * Then proper tags are (-f, -l, -s) are added at the end
	 * of the license name according to the user selections.
	 * 
	 * selectedTag values:
	 * 	normal = 0
	 * 	-s = 1
	 * 	-l = 2
	 * 	-f = 3
	 * 
	 * txtOrMeta values:
	 * 	0 = txt
	 * 	1 = meta
	 * 
	 */
	public String getLicenseIDName(String licenseFullName, int selectedTag, int txtOrMeta) {
		//to lowercase
		licenseFullName.toLowerCase();
		//adding the user added license identifier
		String underlineName = USERINDENTIFIER;
		String[] result = licenseFullName.split("\\s");
		//adding the underlines
		for (int i=0; i<result.length; i++ ) {
			underlineName = underlineName.concat("_"+result[i]);
			
		}
		//checks for forbidden characters to prevent foul naming
		underlineName = this.checkForForbiddenCharacters(underlineName);
		//tagging the name
		//normal license file
		if (selectedTag == 0) {
			//System.out.println("normal license file");
		} 
		//short version
		else if (selectedTag == 1) {
			underlineName = underlineName.concat("-s");
		} 
		//long version
		else if (selectedTag == 2) {
			underlineName = underlineName.concat("-l");	
		} 
		//forbidden phrase
		else if (selectedTag == 3) {
			underlineName = underlineName.concat("-f");
		} else {
			System.out.println("No tag value was given.");
		}
		//adding the file ending (.txt or .meta)
		//and returning the modified license name to writer
		if (txtOrMeta == 0) {
			underlineName = underlineName.concat(".txt");
			return underlineName;
		} else if (txtOrMeta == 1) {
			underlineName = underlineName.concat(".meta");
			return underlineName;
		} else {
			System.out.println("There is something wrong with the txtOrMeta");
			underlineName = underlineName.concat("error.txt");
			return underlineName;
		}
		
		
		 
	} 
	private String checkForForbiddenCharacters(String licenseName) {
		//Checks if user given name contains "-" and replaces with underline "_".
		String modifiedName = licenseName;
		modifiedName = modifiedName.replaceAll("-", "_");
		//TODO more checks here if needed
		return modifiedName;
	}
	/**
	 * Method for writing the license.txt file to the licenses directory.
	 * 
	 */
	public void writeLicense() {
		//getting the license id name
		String licenseNameWithEnding = getLicenseIDName(this.licenseName, this.selectedTag, 0);
		File pathToFolder = new File("licenses", licenseNameWithEnding);
		BufferedWriter bwriter = null;
		// TODO check if already exists!
		//Checking if a license with this name already exists in the database.
		//boolean isExisting = this.isLicenseExisting(licenseNameWithEnding);
		//if(isExisting) {
		//	System.out.println("License with that name already exists.");
		//} else {
			try {
				// creating the text stream for license file			
				writer = new FileWriter(pathToFolder);
				// creating a buffered text stream which buffers the already created stream
				bwriter = new BufferedWriter(writer);
				// Writing license text to the file		
				bwriter.write(this.licenseText);
				bwriter.newLine();
				// flushing		 
				bwriter.flush();
			
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				// Closing the stream
				try {
					if (bwriter != null) bwriter.close();
                    if (writer != null) writer.close();
				
				} catch ( IOException ioe) {
					ioe.printStackTrace();
				}
			}
		//}
		
		
	}
	/**
	 * Method for writing the license.meta file to the licenses directory.
	 * 
	 */
	public void writeMetaFile() { // TODO must throw name already exists exception!!!
		//getting the license id name
		String licenseNameWithEnding = getLicenseIDName(this.licenseName, this.selectedTag, 1);
		String fullNameWithTag = this.licenseName;
		if (this.selectedTag == 1) {
			fullNameWithTag = fullNameWithTag.concat(" (short)");
		} 
		//long version
		if (this.selectedTag == 2) {
			fullNameWithTag = fullNameWithTag.concat(" (long)");	
		} 
		File pathToFolder = new File("licenses", licenseNameWithEnding);
		BufferedWriter bwriter = null;
		//Checking if a license with this name already exists in the database.
		//boolean isExisting = this.isLicenseExisting(licenseNameWithEnding);
		
		//if(isExisting) {
		//	System.out.println("License with that name already exists.");
		//} else {
			try {
				// creating the textstream for license file
				// TODO check if already exists!
				writer = new FileWriter(pathToFolder);
				// creating a buffered text stream which buffers the already created stream
				bwriter = new BufferedWriter(writer);
				//Writing compatible licenses to meta file
				bwriter.write("#meta file for " + this.licenseName);
				bwriter.newLine();
				//Adding compatible licenses list
				bwriter.write("isCompatible : ");
				for (int i =0; i<this.compatibleCount; i++) {
					if (i == 0) {
						bwriter.write(this.compatibleLicenses[i]);
					} 
					if (i > 0 && i < this.compatibleCount){
						bwriter.write(" " + this.compatibleLicenses[i]);
					}
					
				}
				bwriter.newLine();
				//Adding full name of the license tag
				bwriter.write("fullName : " + fullNameWithTag);
				bwriter.newLine();
				// flushing		 
				bwriter.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				// Closing the stream
				try {
					if (bwriter != null) bwriter.close();
                    if (writer != null) writer.close();
				
				} catch ( IOException ioe) {
					ioe.printStackTrace();
				}
			}
		//}
	}
	/**
	 * Method to be used with license name exception handling.
	 * Checks if the license already exists.
	 * Used in LicenseMain to prevent overwriting licenses.
	 * 
	 * @param licenseName name of the license which is going to be created.
	 * @return boolean which tells if the license already exists
	 */
	public boolean isLicenseExisting(String licenseName) {
		boolean exists = false;
		for (int i =0; i < lLister.getLicenseCount(); i++) {
			
				if (licenseName.equalsIgnoreCase(lLister.getLicenseName(i)+".txt") || licenseName.equalsIgnoreCase(lLister.getLicenseName(i)+".meta")) {
					exists = true;
					break;
				} else {
					exists = false;
				}
			
		}
		
		
		return exists;
	}
	
	
}

