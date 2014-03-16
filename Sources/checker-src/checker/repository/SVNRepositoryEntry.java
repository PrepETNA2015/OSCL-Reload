/**
 *   Copyright (C) 2008 Lasse Parikka
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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import checker.FileID;
import checker.filepackage.PackageFile;

/**
 * The SVNRepositoryEntry class is a representation of a 
 * svn directory entry
 */
public class SVNRepositoryEntry  {

	//the person who last changed the entry
	private String author;
	
	//the date the entry was last changed
	private Date date;
	
	//the last changed revision of the entry
	private long revision;
	
	//an entry name
	private String nodeName;
	
	//path relative to the repository root.
	private String path;
    
	private boolean isFolder;
	
	/**
	 * Creates new entry.
	 * @param nodeName Name of a file or a folder 
	 * @param folder True if this node is a folder, false otherwise.
	 * @param spath Path relative to the root.
	 */
	public SVNRepositoryEntry(String name, boolean folder, String sPath, 
			String sAuthor, Date sDate, long sRevision) {
		
		nodeName = name;
		isFolder = folder;
		author = sAuthor;
		date = sDate;
		revision = sRevision;
		path = sPath;
	}
	
	
	/**
	 * Returns true if this node is a folder.
	 */
	public boolean isFolderNode() {
		return isFolder;
	}
	
	/**
	 * Returns path relative to the root.
	 */
	public String getPath() {
		return path;
		
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String toString() {
		return nodeName;
	}
	
	
	
}
