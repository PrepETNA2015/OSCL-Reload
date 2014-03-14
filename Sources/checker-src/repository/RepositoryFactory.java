/**
 * 
 *   Copyright (C) 2006 Sakari K��ri�inen
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
package checker.repository;

import java.util.ArrayList;

import checker.filepackage.FilePackage;

public class RepositoryFactory {

	/**
	 * Gets a list of supported repository type ID:s. (like cvs...)
	 * 
	 * @return
	 */
	public static ArrayList<String> getSupportedRepositoryTypeIDs() {
		// TODO
		return null;
	}

	/**
	 * Creates a new Repository object that represents a desired repository.
	 * 
	 * @param repositoryTypeID
	 *            ID of the repository type
	 * @param parameters
	 *            Variable list of parameters that are needed to open the
	 *            repository.
	 * @return
	 */
	public static FilePackage createRepository(String repositoryTypeID,
			String... parameters) {
		// TODO
		return null;
	}

	/**
	 * Gets a list of parameter descriptions that the repository requires. Must
	 * be in the same order as when supplied back to createRepository.
	 * 
	 * @param repositoryID
	 * @return Parameter descriptions (will be shown in the GUI)
	 */
	public static ArrayList<String> getParameterDescriptions(String repositoryID)
			throws Exception {
		// TODO
		return null;
	}
}
