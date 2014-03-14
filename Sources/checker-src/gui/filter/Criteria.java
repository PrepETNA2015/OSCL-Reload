/**
 * 
 *   Copyright (C) <2006> <Veli-Jussi Raitila>
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

package checker.gui.filter;

import checker.localization.Locale;

/**
 * Filtering criteria 
 * Could be extended to support several criteria
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class Criteria {
	/* Localization */
	private Locale loc = new Locale();

	private Criterion criterion;

	public static final Criteria ALL = new Criteria(null);
	
	public Criteria(Criterion c) {
		criterion = c;
	}
	
	public Criterion getCriterion() {
		return criterion;
	}
	
	public String toString() {
		if (criterion == null) return loc.lc("All");
		else return criterion.toString();
	}
	
}
