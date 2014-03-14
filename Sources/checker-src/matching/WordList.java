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
import java.util.regex.Pattern;

import checker.CommentLine;
import checker.license.License;

/**
 * WordList holds a list of words and their positions (row and column) in a
 * file. This class is used as a helper class with Diff to be able to do diff
 * operations on word basis.
 * <p>
 * Users of this class expect the words to appear in original order, so row and
 * column information are only used for accessing the original data. They are
 * not used for sorting.
 * 
 * @author Lauri Koponen
 */
public class WordList {
	/**
	 * list of words in WordList
	 */
	public ArrayList<String> words;

	/**
	 * text row the word appears on, one Integer for each word in the same order
	 */
	public ArrayList<Integer> row;

	/**
	 * text column the word begins at, one Integer for each word in the same
	 * order
	 */
	public ArrayList<Integer> col;

	/**
	 * length of each word, one Integer for each word in the same order
	 */
	public ArrayList<Integer> len;
	
	/**
	 * Class constructor.
	 * 
	 */
	public WordList() {
		words = new ArrayList<String>();
		row = new ArrayList<Integer>();
		col = new ArrayList<Integer>();
		len = new ArrayList<Integer>();
	}

	/**
	 * Adds the contents of another word list at the end of this list,
	 * preserving word order.
	 * 
	 * @param wordlist
	 *            a word list to add to this list
	 */
	public void addAll(WordList wordlist) {
		words.addAll(wordlist.words);
		row.addAll(wordlist.row);
		col.addAll(wordlist.col);
		len.addAll(wordlist.len);
	}

	/**
	 * Check if a word is a tag. If the word starts with a "&lt;" and ends
	 * with "&gt;", it is assumed to be a tag.
	 * 
	 * @param word Word to check
	 * @return True if the word is a tag, false otherwise
	 */
	private static boolean isTag(String word) {
		
		/* TODO: add tag checking from license info */
		if(word.startsWith("<") && word.endsWith(">")) return true;
		
		return false;
	}
	
	/**
	 * Clean up word. Removes non-alphanumeric characters if not a tag.
	 * Words identified as tags are not processed.
	 * 
	 * @param word Word to tidy
	 * @return Cleaning result
	 */
	public static String tidy(String word) {
		
		if(isTag(word)) {
			return word;
		}
		
		return word.replaceAll("[^a-zA-Z0-9<>]", "");
	}
	
	/**
	 * Split a String to words and calculate word positions. The given string
	 * appears on line 'row' in the text file and begins at column 'startcol'.
	 * Words with no alphanumeric characters are not included in the word list.
	 * 
	 * @param in
	 *            the string to split
	 * @param row
	 *            line number for this string
	 * @param startcol
	 *            start column for this string
	 * @return WordList containing the words in the string
	 */
	public static WordList splitWords(String in, int row, int startcol) {
		WordList words = new WordList();
		int i;
		int start;
		boolean found = false;
		boolean isword = false;

		start = 0;

		for (i = 0; i < in.length(); i++) {

			char c = in.charAt(i);

			if ((c == ' ') || (c == '\n') || (c == '\t')) {
				/* whitespace */
				if (found) {
					if(isword) {
						String word = in.substring(start, i).toLowerCase();
						word = tidy(word);
						words.words.add(word);
						words.col.add(start + startcol);
						words.row.add(row);
						words.len.add(i - start);
						isword = false;
					}
					found = false;
				}
			} else {
				/* word characters */
				if (!found) {
					start = i;
					found = true;
				}
				if(!isword) {
					if (((c >= 'a') && (c <= 'z'))
							|| ((c >= 'A') && (c <= 'Z'))
							|| ((c >= '0') && (c <= '9'))) {
						
						/* found an alphanumeric character, this is a word */
						isword = true;
					}
				}
			}
		}

		/* handle word at end of string */
		if (found) {
			if(isword) {
				String word = in.substring(start).toLowerCase();
				word = tidy(word);
				words.words.add(word);
				words.col.add(start + startcol);
				words.row.add(row);
				words.len.add(i - start);
			}
		}

		return words;
	}

	/**
	 * Comment an ArrayList<CommentLine> to WordList.
	 * 
	 * @param comments
	 *            the comments to convert
	 * @return WordList containing the words of the comments
	 */
	public static WordList commentsToWords(ArrayList<CommentLine> comments) {
		int i;

		WordList words = new WordList();

		for (i = 0; i < comments.size(); i++) {
			WordList linewords = splitWords(comments.get(i).getContent(),
					comments.get(i).getSourceLineNumber(), comments.get(i)
							.getCommentStartColumn());

			words.addAll(linewords);
			linewords = null;
		}

		return words;
	}

	/**
	 * Convert a License to a WordList.
	 * 
	 * @param license
	 *            the license to convert
	 * @return WordList containing the words of the license
	 */
	public static WordList licenseToWords(License license) {
		int i;

		WordList words = new WordList();
		ArrayList<String> licensetext = license.getLicenseText();

		for (i = 0; i < licensetext.size(); i++) {
			WordList linewords = splitWords(licensetext.get(i), i, 0);

			words.addAll(linewords);
			linewords = null;
		}

		return words;
	}

	/**
	 * Find a word starting at startrow, startcol.
	 * 
	 * @param startrow Word starting row
	 * @param startcol Word starting column
	 * @return Index into words array.
	 */
	public int findWordAt(int startrow, int startcol) {
		int i;
		
		for(i = 0; i < words.size(); i++) {
			if(row.get(i) > startrow
				|| ((row.get(i) == startrow) && (col.get(i) >= startcol))) {
				
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Count the number of words between startrow,startcol and endrow,endcol.
	 * 
	 * @param startrow Starting row
	 * @param startcol Starting column
	 * @param endrow Ending row
	 * @param endcol Ending column
	 * @return Number of words between start and end position
	 */
	public int countWordsBetween(int startrow, int startcol,
		int endrow, int endcol) {

		int i;
		
		int first_i = words.size() - 1;
		int last_i = 0;
		
		/* find first word inside given range */
		for(i = 0; i < words.size(); i++) {
			if(row.get(i) > startrow) {
				first_i = i;
				break;
			} else if((row.get(i) == startrow) && (col.get(i) >= startcol)) {
				first_i = i;
				break;
			}
		}
		
		/* find last word inside given range */
		for(i = words.size() - 1; i >= 0; i--) {
			if(row.get(i) < endrow) {
				last_i = i;
				break;
			} else if((row.get(i) == endrow) && (col.get(i) <= endcol)) {
				last_i = i;
				break;
			}
		}
		
		int count = last_i - first_i + 1;
		
		if(count < 0) count = 0;
		
		return count;
	}

	WordList substring(int start, int length) {
		WordList w = new WordList();
		
		for(int i = start; i < start+length; i++) {
			w.words.add(words.get(i));
			w.row.add(row.get(i));
			w.col.add(col.get(i));
			w.len.add(len.get(i));
		}
		
		return w;
	}
}
