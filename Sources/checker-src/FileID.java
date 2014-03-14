/**
 * 
 *   Copyright (C) 2006 Sakari K��ri�inen, Lauri Koponen
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

package checker;

import java.io.File;


/**
 * Proxy class for a file in a package. 
 */
public class FileID {
	
	public FileID(String sPath, String sName){
		path = sPath;
		name = sName;
	}
	
	/**
	 * Pathname relative to the package root. null if in root.
	 */
	public String path;
	
	/**
	 * Filename.
	 */
	public String name;

	public boolean equals(Object obj) {
		if (obj instanceof FileID) {
			FileID objFile = (FileID)obj;
			
			if (objFile.name == null) {
				if(name != null) return false;
			} else {
				if(!objFile.name.equals(name)) return false;
			}

			if (objFile.path == null) {
				if(path != null) return false;
			} else {
				if(!objFile.path.equals(path)) return false;
			}
			
			return true;
		} 
		return false;
	}
	
	public int hashCode() {
		if(path == null) {
			if(name == null) return 0;
			return name.hashCode();
		}
		if(name == null) return path.hashCode();
		
		return path.hashCode() + name.hashCode();
	}
	
	public String toString() {
		if(path == null) return name;
		if(name == null) return path + File.separator; 
		
		return path + File.separator + name;
	}
}

