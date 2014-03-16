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
 * Match criterion
 * Used to filter results based on the match%
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class MatchCriterion extends Criterion {
	/* Localization */
	private Locale loc = new Locale();

	float matchp;
	
	public MatchCriterion(float m) {
		matchp = m;
	}
	
	@Override
	public Object getValue() {
		return new Float(matchp);
	}

	@Override
	public String toString() {
		return String.format(loc.lc("less than %1.0f%% match"),
				Math.floor(matchp * 100.0));
	}

}
