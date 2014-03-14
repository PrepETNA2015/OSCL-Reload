/**
 * 
 *   Copyright (C) 2006 Jussi Sirpoma
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
 *   Also add information on how to contact you by electronic and paper mail.
 *
 */
package checker.filepackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

import checker.FileID;
import checker.filepackage.PackageFile;

/**
 * Inteface for different package types. Also acts as a superclass of Repository
 * interface.
 */
public interface FilePackage extends Iterable<PackageFile> {

	/**
	 * Returns a list of file extensions that can be used to identify the type
	 * of package. Repositories should return null.
	 */
	public ArrayList<String> getFileExtensions();

	/**
	 * Gets the name of this package type. (for example: zip,jar,tar.gz or file
	 * system)
	 * 
	 * @return
	 */
	public String getPackageTypeID();
	
	/**
	 * Returns the root file/directory of this package.
	 * @return
	 */
	public File getRootFile();

    public Iterator<PackageFile> iterator();

    /**
     * Obsolete and possibly exponential implementation for opening a file
     *
     */
    public ArrayList<String> readFile(FileID file) throws Exception;
    
}
