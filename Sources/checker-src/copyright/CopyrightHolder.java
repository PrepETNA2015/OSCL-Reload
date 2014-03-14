/**
 * 
 *   Copyright (C) 2008 Jyrki Laine
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

package checker.copyright;

import java.util.ArrayList;

/**
 * A person that owns a copyright in some of the source files
 * 
 * @author jvlaine
 */
public class CopyrightHolder {

	/**
	 * Name of the copyright holder
	 */
	public String name;

	/**
	 * Email address of the copyright holder
	 */
	public String email;

	/**
	 * Web site of the copyright holder
	 */
	public String website;

	/*
	 * Files in which the copyright holder has a copyright
	 */
	private ArrayList<CopyrightFile> copyrightFiles;

	/**
	 * The constructor
	 * 
	 * @param name
	 */
	public CopyrightHolder(String name) {
		this.name = name;
		this.copyrightFiles = new ArrayList<CopyrightFile>();
	}

	/**
	 * Returns all the files to which the copyright holder owns copyrights.
	 * 
	 * @return
	 */
	public ArrayList<CopyrightFile> getCopyrightFiles() {
		return this.copyrightFiles;
	}

	/**
	 * Adds a file into a list of copyrights owned by the copyright holder.
	 * 
	 * @param addedFile
	 */
	public void addCopyrightFile(CopyrightFile addedFile) {
		this.copyrightFiles.add(addedFile);
	}
}
// Reviewed 14.11.2008 by mkupsu
