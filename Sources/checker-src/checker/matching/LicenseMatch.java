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
import java.util.Comparator;
import java.util.HashMap;

import checker.license.KeyPhrase;
import checker.license.License;
import checker.license.Tag;

/**
 * License match found from a file.
 * Should each match algorithm have it's own LicenseMatch class? 
 */
public class LicenseMatch implements Comparable<LicenseMatch> {

	/**
	 * The license that the match is about
	 */
	private License license;

	/**
	 * Position of the match in a file
	 */
	private ArrayList<MatchPosition> positions;

	/**
	 * What is the match confidence?
	 * <li> % of matched words/charactes
	 * <li> range: 0.0-1.0
	 */
	private float matchPr;

	/**
	 * Which key phrases were been found?
	 */
	private ArrayList<KeyPhrase> keyPhrases;

	/**
	 * Strings that have been found in free-form fields
	 */
	private HashMap<Tag, String> tags;

	/**
	 * Tag start position, index to source text words
	 */
	private HashMap<Tag, Integer> tagStart;

	
	public LicenseMatch() {
	}

	/**
	 * @param license
	 *            Identified license.
	 * @param positions
	 *            Position of the match in the source file. Can contain multiple
	 *            segments (partial match / exact match around tags?)
	 */
	public LicenseMatch(License license, ArrayList<MatchPosition> positions) {
		this.license = license;
		this.positions = positions;
	}

	/**
	 * Is this an exact match?
	 * 
	 * @return True, if an exact match, otherwise false
	 */
	public boolean isExactMatch() {
		/* exact only if full match - no room for precision errors */
		return matchPr == 1.0;
	}

	/**
	 * Set the match confidence (in %, in range 0.0 .. 1.0)
	 * 
	 * @param matchPr match confidence
	 */
	public void setMatchPr(float matchPr) {
		this.matchPr = matchPr;
	}

	/**
	 * Get match confidence (in %, in range 0.0 .. 1.0)
	 * @return match confidence
	 */
	public float getMatchPr() {
		return matchPr;
	}

	/**
	 * Add an indentified key phrase.
	 * 
	 * @param keyPhrase Identified key phrase
	 */
	public void addKeyPhrase(KeyPhrase keyPhrase) {
		if (keyPhrases == null) {
			keyPhrases = new ArrayList<KeyPhrase>();
		}
		keyPhrases.add(keyPhrase);
	}

	public ArrayList<KeyPhrase> getKeyPhrases() {
		return keyPhrases;
	}

	/**
	 * Add a tag.
	 * 
	 * @param tag
	 *            Free-form field identifier
	 * @param tagContent
	 *            Field contents.
	 * @param start
	 * 			  Index of first tag word
	 */
	public void addTag(Tag tag, String tagContent, int start) {
		if (tags == null) {
			tags = new HashMap<Tag, String>();
		}
		if (tagStart == null) {
			tagStart = new HashMap<Tag, Integer>();
		}
		tags.put(tag, tagContent);
		tagStart.put(tag, new Integer(start));
	}

	/**
	 * Get all found tags.
	 * 
	 * @return Found tags
	 */
	public HashMap<Tag, String> getTags() {
		return tags;
	}

	/**
	 * Get the index of the first word of the tag.
	 * @param t Tag to get.
	 * @return Index of first word
	 */
	int getTagStart(Tag t) {
		Integer i = tagStart.get(t);
		if (i != null) return i.intValue();
		return -1;
	}
	
	public ArrayList<MatchPosition> getMatchPositions() {
		return positions;
	}

	public void setMatchPositions(ArrayList<MatchPosition> pos) {
		positions = pos;
	}

	public void setLicense(License l) {
		license = l;
	}
	
	public License getLicense() {
		return license;
	}
	
	public int getLongestLength() {
		int length = 0;
		
		for (MatchPosition p : positions) {
			int l = p.getNumWords(); 
			if (l > length)
				length = l; 
		}
		
		return length;
	}

	public int getOrigLength() {
		int match_length = 0;
		for (MatchPosition p : positions) {
			match_length += p.getOrigMatchLength();
		}
		return match_length;
	}
	
	public String toString() {
		return String.format("%s (%1.0f%%)",
				license.getId(),
				Math.floor(matchPr * 100.0));
	}

	/* Note: this comparator gives inverse order because we usually
	 * want the longest match first!
	 */
	public int compareTo(LicenseMatch o) {
		float opr = o.getMatchPr();
		
		/* inverted comparison! */
		if(matchPr < opr) return 1;
		if(matchPr > opr) return -1;
		
		return 0;
	}
}
