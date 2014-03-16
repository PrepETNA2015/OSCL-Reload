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

import java.util.HashSet;

import checker.license.LicenseDatabase;
import checker.license.License;

public class LicenseLister {

	/**
	 * @author jpkheikk
	 * 
	 * TODO whole class could be merged to some other class ie. License.java since its name fetching method does not work. 
	 * But it is so full of text I thought that a separate class would be good.
	 * It would be good to change the name of the class at some point
	 * since for historical reasons the current name does not present so good of what it does.
	 *  
	 *  This class was made to simplify the process to get license names out of the database.
	 * 
	 */

	private String[] licenseNames;
	private String licenseName;
	private License license = new License();
	private HashSet<License> licenses = new HashSet<License>();
	private LicenseDatabase licenseDB = new LicenseDatabase("licenses");

	// constructor
	public LicenseLister() {

		if (licenseDB == null) {
			//System.out.println("licensedb is null. Building a new.");
			licenseDB.buildLicenseDatabase();
		} else { // licenseDB != null
			this.licenses = licenseDB.getLicenses();
			this.licenseNames = licenseDB.getLicenseNames();
		}
	}
	/**
	 * @author jpkheikk
	 * 
	 * A method to fetch a single license name in a given spot at a licenseNames table 
	 * created at LicenseDatabase.getLicenseNames[].
	 * 
	 * @param itemNumber
	 * @return licenseName
	 * 
	 */
	public String getLicenseName(int itemNumber) {
		/*if (licenseDB == null) {
			System.out.println("licensedb on null");
			licenseDB.buildLicenseDatabase();
		} */
		//System.out.println("license number: " + itemNumber);
		if (this.licenses != null) {
			// fetching the license name

			this.licenseName = this.licenseNames[itemNumber];
			//System.out.println("lisenssin nimi: " + this.licenseNames[itemNumber]);
			return this.licenseName;
			// TODO: Exception handling
		} else return "LicenseDB_null_error";

	}
	
	/**
	 * 
	 * @return
	 */
	public int getLicenseCount() {
		return this.licenseNames.length;
	}
}
//Reviewed by mkupsu 29.11.08
