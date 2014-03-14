/**
 * 
 *   Copyright (C) 2008 Jyrki Laine
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

package checker.copyright;

import java.util.ArrayList;
import checker.CommentLine;
import checker.copyright.CopyrightHolder;

/**
 * A file in which copyrights are observed.
 */
public class CopyrightFile extends checker.FileID {

	/*
	 * Strings that are used for identifying the lines of text that include
	 * a copyright
	 */
	private static final String[] COPYRIGHT_IDENTIFIERS = {"(c)"};
	
	/*
	 * Strings that are typicalliy used in copyright templates. These words
	 * indicate that there is no real copyright in the current line
	 */
	private static final String[] NAME_EXCLUDERS = {"year", "name",
		"foo", "bar", "author", "authors", "license", "licensed", 
		"under", "terms" };
	
	/*
	 * Strings that are used for separating names of copyright holders from
	 * each other
	 */
	private static final String[] NAME_SEPARATORS = { "," };
	
	/*
	 * Strings that are typically found from lines that include a copyright.
	 * Can be removed to help finding the names of the copyright holders.
	 */
	private static final String[] JUNK_WORDS = { "code", "earlier", 
		"original", "and", "inc", "ltd", "copyrighted", "copyright",
		"all rights reserved", "by", "jr", "software" };
	
	/*
	 * String used to indentify email addresses
	 */
	private static final String AT_MARK = "@";
	
	/*
	 * String used to indentify web site addresses
	 */
	private static final String[] WEBSITE_IDENTIFIERS = { "www", ".com", ".net", ".org" };
	
	/*
	 * People who have a copyright in this file
	 */
	private ArrayList<CopyrightHolder> copyrightHolders;

	/**
	 * The constructor. Finds the copyright holders of the file.
	 * 
	 * @param path
	 * @param name
	 * @param commentLines
	 */
	public CopyrightFile(String path, String name, 
			ArrayList<CommentLine> commentLines) {
		super(path, name);
		this.copyrightHolders = findCopyrightHolders(commentLines);
	}
	

	/*
	 * Removes the words that are not likely included in the name of the copyright holder
	 * 
	 */
	private String removeJunkWords(String line) {
		for (int i = 0; i < JUNK_WORDS.length; i++) {
			line = line.replace(JUNK_WORDS[i], "");
		}
		return line;
	}
	
	/*
	 * Removes the copyright identifiers from the line
	 * 
	 */
	private String removeCopyrightIdentifiers(String line) {
		for (int i = 0; i < COPYRIGHT_IDENTIFIERS.length; i++) {
			line = line.replace(COPYRIGHT_IDENTIFIERS[i], "");
		}
		return line;
	}

	
	private ArrayList<String> separateNames(String line) {
		ArrayList<String> names = new ArrayList<String>();
		boolean containsSeparator = false;
		for (int i = 0; i < NAME_SEPARATORS.length; i++) {
			if (line.contains(NAME_SEPARATORS[i])) {
				containsSeparator = true;
				String[] namesTmp = line.split(NAME_SEPARATORS[i]);
				for (int j = 0; j < namesTmp.length; j++) {
					names.addAll(separateNames(namesTmp[j]));
				}
			}
		}
		if (!containsSeparator) {
			names.add(line); //Only one name
		}
		return names;
	}
	
	private boolean containsStringArray(String string, String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (string.contains(array[i])) {
				return true;
			}
		}
		return false;
	}


	/*
	 * Selects the lines that contain the copyright mark (c). 
	 * Removes everything else but the name, email and web site address
	 * of the copyright holder from the line. 
	 * 
	 */
	private ArrayList<CopyrightHolder> findCopyrightHolders(
			ArrayList<CommentLine> commentLines) {
		ArrayList<CopyrightHolder> copyrightHolders = 
			new ArrayList<CopyrightHolder>();
		for (CommentLine commentLine : commentLines) {
			
			/*It is easier to compare strings that are all lower case*/
			String commentLineContent = 
				commentLine.getContent().toLowerCase();
			if (containsStringArray(commentLineContent, COPYRIGHT_IDENTIFIERS)) {
				commentLineContent = 
					removeJunkWords(commentLineContent);
				commentLineContent = 
					removeCopyrightIdentifiers(commentLineContent);
				ArrayList<CopyrightHolder> newCopyrightHolders = 
					extractCopyrightHolders(commentLineContent);
				for (CopyrightHolder newCopyrightHolder : 
					newCopyrightHolders) {
					copyrightHolders.add(newCopyrightHolder);
				}
			}
		}
		return copyrightHolders;
	}


	/*
	 * Creates copyright holder objects from a comment line that contains
	 * the names of the copyright holders.
	 */
	private ArrayList<CopyrightHolder> extractCopyrightHolders(String line) {
		ArrayList<CopyrightHolder> copyrightHolders = 
			new ArrayList<CopyrightHolder>();
		ArrayList<String> names = separateNames(line);
		for (int i = 0; i < names.size(); i++) {
			if (containsStringArray(names.get(i), NAME_EXCLUDERS)) {
				continue;
			}
			String[] nameParts = names.get(i).split(" ");
			String name = "";
			String email = null;
			String website = null;
			
			/*
			 * Separates email and web site addresses from the names,
			 * removes special marks and changes the first character
			 * of the name upper case.
			 */
			for (int j = 0; j < nameParts.length; j++) {
				String plainName = 
					nameParts[j].replaceAll("[^a-zA-Z]", ""); 
				if(nameParts[j].contains(AT_MARK)) {
					email = nameParts[j];
				} else if(containsStringArray(nameParts[j], WEBSITE_IDENTIFIERS)) {
					website = nameParts[j];
				} else if (!plainName.equals("")) {
					String firstChar = 
						"" + plainName.charAt(0);
					plainName = firstChar.toUpperCase() 
						+ plainName.substring(1, 
								plainName.length());
					name += plainName + " ";
				}
			}
			name = name.trim();
			CopyrightHolder newCopyrightHolder = null;
			if (!name.equals("")) {
				newCopyrightHolder = new CopyrightHolder(name);
			}
			if (email != null) {
				newCopyrightHolder.email = email;
			}
			if (website != null) {
				newCopyrightHolder.website = website;
			}
			if (newCopyrightHolder != null) {
				newCopyrightHolder.addCopyrightFile(this);
				copyrightHolders.add(newCopyrightHolder);
			}
		}
		return copyrightHolders;
	}

	/**
	 * Checks if the file contains any copyright holders.
	 * 
	 * @return
	 */
	public boolean hasCopyrightHolders() {
		if (this.copyrightHolders.isEmpty()) 
			return false;
		else 
			return true;
	}

	/**
	 * Returns the copyright holders of the file. Doesn't remove duplicates.
	 * 
	 * @return
	 */
	public ArrayList<CopyrightHolder> getCopyrightHolders() {
		return this.copyrightHolders;
	}
}
// Reviewed 14.11.2008 by mkupsu
