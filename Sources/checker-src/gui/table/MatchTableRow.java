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

import java.awt.Color;

/**
 * Row of match information in the a JTable 
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class MatchTableRow {
	private Color color;
	private String license;
	private float match;
	
	public MatchTableRow(Color c, String l, float m) {
		color = c;
		license = l;
		match = m;
	}

	public Color getColor() {
		return color;
	}

	public String getLicense() {
		return license;
	}

	public String getMatch() {
		return String.valueOf(Math.floor(match * 100.0));
	}
	
}
