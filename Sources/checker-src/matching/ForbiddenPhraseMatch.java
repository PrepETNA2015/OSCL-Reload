/**
 * 
 *   Copyright (C) 2007 Lauri Koponen
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

package checker.matching;

import java.util.ArrayList;

import checker.license.License;

/**
 * Forbidden phrase match found from a file.
 */
public class ForbiddenPhraseMatch extends LicenseMatch {

	// Most of the variables in the superclass will be null, should this be
	// subclass of LicenseMatch at all?

	private License phrase;

	/**
	 * @param phrase
	 *            Identified forbidden phrase
	 * @param positionStart
	 *            Start position of the match in the file
	 * @param positionEnd
	 *            End position of the match in the file
	 * 
	 */
	public ForbiddenPhraseMatch(License phrase,
			MatchPosition position) {
		this.phrase = phrase;
		setLicense(phrase);
		
		ArrayList<MatchPosition> mp = new ArrayList<MatchPosition>();
		mp.add(position);
		setMatchPositions(mp);
		
		/* forbidden phrase is always a full match */
		setMatchPr(1.0f);
	}
}
