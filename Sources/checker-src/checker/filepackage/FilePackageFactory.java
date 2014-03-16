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

import java.io.File;
import java.util.ArrayList;

/**
 * Class for creating FilePackage instances. 
 */
public class FilePackageFactory {

	
	/**
	 * Gets a list of supported package types.
	 * @return
	 */
	public static ArrayList<String> getSupportedPackageTypeIDs() {
		// TODO
		return null;
	}
	
	/**
	 * Creates a new FilePackage object that represents the given package file.
	 * @param packageFile File or directory of the package.
	 */
	public static FilePackage createFilePackage(File packageFile) throws Exception {
		// TODO: FIX THIS PROPERLY

        
		if(packageFile.getName().toUpperCase().endsWith(".ZIP")) {
			return new ZipFilePackage(packageFile);
		} else if(packageFile.getName().toUpperCase().endsWith(".JAR")) {
			return new JarFilePackage(packageFile);
		} else if(packageFile.getName().toUpperCase().endsWith(".TAR") ||
                  packageFile.getName().toUpperCase().endsWith(".TAR.GZ") ||
                  packageFile.getName().toUpperCase().endsWith(".TGZ")) {
			return new TarFilePackage(packageFile);
		}
		
		return new FileSystemFilePackage(packageFile);
	}

	/**
	 * Is the given file package file/directory?
	 * @param packageFile Package file candidate.
	 * @return true if the file is a package file. 
	 */
	public static boolean isFilePackage(File packageFile) {
		// TODO
		return false;
	}
	
	/**
	 * Creates a new X object that represents the given repository.
	 * @param connectParameters
	 * @return
	 */
	/*
	public static FilePackage openRepository(String parameters) {
		// TODO
		return null;
	}
	*/

}
