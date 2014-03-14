/**
 * 
 *   Copyright (C) <2007> <Veli-Jussi Raitila>
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

package checker.gui.table;

import checker.localization.Locale;

/**
 * Conflict row in a JTable
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class ConflictTableRow {
	/* Localization */
	private Locale loc = new Locale();

	private String license1;
	private String license2;
	
	public ConflictTableRow(String l1, String l2) {
		license1 = l1;
		license2 = l2;
	}

	public String getLicense1() {
		return license1;
	}

	public String getLicense2() {
		return license2;
	}

	public String getConflictType() {
		/* FIXME For now only global conflicts are listed */
		return this.loc.lc("Global");
	}

}
