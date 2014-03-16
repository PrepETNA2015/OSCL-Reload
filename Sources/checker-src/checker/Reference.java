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


/**
 * A single reference between two source code files.
 */
public class Reference {
	
	
	public Reference(FileID sourceFile, FileID targetFile){
		
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
			
	}

	/**
	 * Allowed reference types.
	 */
	public enum ReferenceType {
		IMPORT, IMPORT_STANDARD_LIBRARY, UNPARSABLE,
		STATIC_INCLUDE, DYNAMIC_INCLUDE
	}; 
	
	/**
	 * Reference from.
	 */
	public FileID sourceFile;

	/**
	 * Reference to. (can be null if referenceType = unparsable)
	 * FileID to the targetFile is created during reference parsing
	 */
	public FileID targetFile;

	/**
	 * Type of the reference.
	 */
	public ReferenceType referenceType;
	
	/**
	 * Reference declaration
	 */
	public String declaration;
	/**
	 * Any other information.
	 */
	public String information;
	
	public String toString() {
		return "Reference type:"+referenceType+" source:"+sourceFile+" target:"+targetFile+" info:"+information;
	}
}
