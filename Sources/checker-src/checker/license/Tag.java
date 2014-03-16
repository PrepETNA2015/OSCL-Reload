/**
 * Copyright (C) 2007 Xie Xiaolei
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General 
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package checker.license;


/**
 * Free-from tag inside a license.
 */
public class Tag {

	private String id; 
	private License license;
	
	/**
	 * @param id Identification of this tag. (Will be presentied in the user interface)
	 * @param license License where this tag is contained.
	 */
	public Tag(String id, License license) {
		this.id = id;
		this.license = license;
	}
	
	public String getId() {
		return id;
	}
	
	public License getLicense() {
		return license;
	}
}
