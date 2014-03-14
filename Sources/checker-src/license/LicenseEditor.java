/**
 * 
 *   Copyright (C) 2009 Johannes Heikkinen
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

import java.io.File;

public class LicenseEditor {
	
	LicenseReader lReader = new LicenseReader();
	
	public LicenseEditor() {
		
	}
	
	/**
	 * Method for deleting the .txt file given in the parameter.
	 * 
	 * File name needed without the ending (.txt)!
	 * 
	 * Returns int variable which tells if something went wrong.
	 * Values represent the following:
	 * 	deleteHandler 	0 = deletion succeeded
	 * 			1 = no such file exists
	 *			2 = file is write protected
	 *			3 = some other for failure reason?
	 * 
	 * @param licenseIDName name of the license file in the database.
	 * @return true if file was successfully deleted, false if not.
	 */
	public int deleteLicenseFile(String licenseIDName) {
		String fileName = licenseIDName + ".txt";
		int deleteHandler = 0;
		// A File object to represent the filename
		File f = new File("licenses", fileName);

		// Make sure the file or directory exists and isn't write protected
		if (!f.exists()) {
			deleteHandler = 1;
		}
		//write protection, deleteHandler = 2
		if (!f.canWrite()) {
			deleteHandler = 2;
		}
		// Attempt to delete it
		boolean deleted = f.delete();
		if (deleted) {
			deleteHandler = 0;
		} 
		if (!deleted && (deleteHandler != 1 || deleteHandler != 2)) {
			deleteHandler = 3;
		}
		
		return deleteHandler;
	}
	/**
	 * Method for deleting the .meta file given in the parameter.
	 * 
	 * File name needed without the ending (.txt)!
	 * 
	 * Returns int variable which tells if something went wrong.
	 * Values represent the following:
	 * 	deleteHandler 	0 = deletion succeeded
	 * 			1 = no such file exists
	 *			2 = file is write protected
	 *			3 = some other for failure reason?
	 * 
	 * @param licenseIDName name of the license file in the database.
	 * @return true if file was successfully deleted, false if not.
	 */
	public int deleteMetaFile(String licenseIDName) {
		String fileName = licenseIDName + ".meta";
		int deleteHandler = 0;
		// A File object to represent the filename
		File f = new File("licenses", fileName);

		// Make sure the file or directory exists and isn't write protected
		if (!f.exists()) {
			deleteHandler = 1;
		}
		//write protection, deleteHandler = 2
		if (!f.canWrite()) {
			deleteHandler = 2;
		}
		// Attempt to delete it
		boolean deleted = f.delete();
		if (deleted) {
			deleteHandler = 0;
		} 
		if (!deleted && (deleteHandler != 1 || deleteHandler != 2)) {
			deleteHandler = 3;
		}
		
		return deleteHandler;
	}
	/**
	 * Method for deleting the .txt file given in the parameter.
	 * 
	 * File name needed without the ending (.txt)!
	 * 
	 * Returns int variable which tells if something went wrong.
	 * Values represent the following:
	 * 	deleteHandler 	0 = deletion succeeded
	 * 			1 = no such file exists
	 *			2 = file is write protected
	 *			3 = meta file not deleted (fatal error)
	 *			4 = txt file not deleted (fatal error)
	 * 
	 * @param licenseIDName name of the license file in the database.
	 * @return true if file was successfully deleted, false if not.
	 */
	public int deleteLicense(String licenseIDName) {
		String fileName = licenseIDName + ".txt";
		String fileName2 = licenseIDName + ".meta";
		int deleteHandler = 0;
		// A File object to represent the filename
		File f = new File("licenses", fileName);
		File f2 = new File("licenses", fileName2);

		// Make sure the file exists, deleteHandler = 1
		if (!f.exists() || !f2.exists()) {
			deleteHandler = 1;
		}
		//write protection, deleteHandler = 2
		if (!f.canWrite() || !f2.canWrite()) {
			deleteHandler = 2;
		}
		// Attempt to delete it, succeed = 0, fail = 3 or 4
		boolean deleted;
		boolean deleted2;
		if (deleteHandler != 1 || deleteHandler != 2) {
			deleted = f.delete();
			deleted2 = f2.delete();
			if (deleted && deleted2) {
				deleteHandler = 0;
			} 
			if (deleted && !deleted2){
				deleteHandler = 3;
			} 
			if (!deleted && deleted2) {
				deleteHandler = 4;
				
			}
		}		
		return deleteHandler;
	}
	/*
	public int deleteLicense(String licenseIDName) {
		int deleteHandler, deleteHandlerTxt = 0, deleteHandlerMeta = 0;
		deleteHandlerTxt = this.deleteLicenseFile(licenseIDName);
		deleteHandlerMeta = this.deleteMetaFile(licenseIDName);
		//checking that both deletions succeeded
		if (deleteHandlerTxt == 1 || deleteHandlerMeta == 1) {
			deleteHandler = 1;
		} else if (deleteHandlerTxt == 2 || deleteHandlerMeta == 2){
			deleteHandler = 2;
		} else if (deleteHandlerTxt == 3 || deleteHandlerMeta == 3) {
			deleteHandler = 3;
		} else {
			deleteHandler = 0;
		}
		
		
		return deleteHandler;
	}
	*/
	
	/**
	 * Method for updating license files after editing.
	 *   
 	 * First the method deletes old files if those can be deleted.
 	 * Then if deletion succeeded method will create new .txt and .meta file with the user edited input.
 	 * 
 	 * If old files cannot be deleted, new files will not be created. Return values represent the following:
 	 * 
 	 * 	deleteHandler 	0 = deletion succeeded
	 * 			1 = no such file(s) exist
	 *			2 = file(s) is(are) write protected
	 *			3 = meta file not deleted (fatal error)
	 *			4 = txt file not deleted (fatal error)
	 *
 	 * TODO cases 3 and 4 should be handled better even though those are extremely rare situations
 	 * 	if possible at all.
 	 * 
	 * @return condition, which is the success value of deletion.
	 * 
	 */
	public int updateLicense(String licenseIDName, String licenseText, String[] compatibleLicenses, int countOfCompatibleLicenses, int selectedTag) {
		int condition;
		//deleting old files
		condition = this.deleteLicense(licenseIDName);
		if (condition == 0) {
			LicenseReader lReader = new LicenseReader();
			String licenseFullName = lReader.getFullName(licenseIDName);
			LicenseWriter lWriter = 
				new LicenseWriter(licenseFullName, licenseText, compatibleLicenses, countOfCompatibleLicenses, selectedTag);
			lWriter.writeLicense();
			lWriter.writeMetaFile();
			return condition;
		} else {
			return condition;
		}
	}
	
	

}
