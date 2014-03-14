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
import java.util.HashMap;

import checker.Log;
import checker.LogEntry;
import checker.license.Tag;

/**
 * License match analysis. This class analyzes the results of matching comments
 * to a license template with the Diff class.
 * 
 * @author Lauri Koponen
 */
public class DiffAnalysis {

	protected static final int MAX_TAG_LENGTH = 20;
	
	/**
	 * The Diff that is being analyzed.
	 */
	private Diff diff;

	/**
	 * Words from the license template.
	 */
	private WordList templatewords;

	/**
	 * Words from the comments that are being matched.
	 */
	private WordList textwords;
	
	/**
	 * List of tags from license.
	 */
	private ArrayList<Tag> licenseTags;

	/**
	 * Match percentage, in range 0.0 .. 1.0.
	 */
	private float match;
	
	ArrayList<MatchPosition> foundPositions = null;

	/**
	 * Get the match percentage.
	 * 
	 * @return match percentage, in range 0.0 .. 1.0.
	 */
	public float getMatchPr() {
		return match;
	}

	/**
	 * Get the found matches.
	 * 
	 * @return list of found matches
	 */
	public ArrayList<MatchPosition> getPositions() {
		return foundPositions;
	}

	
	/**
	 * List of found tags.
	 */
	public HashMap<String, String> tags;
	
	/**
	 * List of tag starting word indices.
	 */
	public HashMap<String, Integer> tagStart;
	
	
	
	/**
	 * Class constructor that analyzes the Diff result. Results are stored in
	 * the DiffAnalysis instance and can be retrieved with member methods.
	 * 
	 * @param diff
	 *            Diff to be analysed
	 * @param template
	 *            license template used in matching
	 * @param text
	 *            comments used in matching
	 * @param minMatch
	 *            Minimum matched substring length,
	 *            percentage of total length in range 0.0 .. 1.0
	 */
	public DiffAnalysis(Diff diff, WordList template, WordList text,
			double minMatch, ArrayList<Tag> tags) {
		this.diff = diff;
		this.templatewords = template;
		this.textwords = text;
		
		this.licenseTags = tags;
		this.tags = new HashMap<String, String>();
		this.tagStart = new HashMap<String, Integer>();

		analysis(minMatch);
	}
	
	/**
	 * Save tag in tag list.
	 * 
	 * @param tag Name of tag
	 * @param value Value of tag
	 * @param i Index of first tag word
	 */
	private void storeTag(String tag, String value, int i) {
		tags.put(tag, value);
		tagStart.put(tag, new Integer(i));
		Log.log(LogEntry.DEBUG, "Found tag '" + tag + "': " + value);
	}
	
	/**
	 * Scan text backwards to the beginning of a tag.
	 * 
	 * @param i Position in source text
	 * @param oi Position in license template
	 * @return New position in source text or -1 if no tag was found
	 */
	private int scanTagBackward(int i, int oi)
	{
		/* diff.O[oi] is the "<tag>" and diff.N[i] is the last word of the tag value */
		
		String tag = diff.O[oi];
		StringBuffer sb = new StringBuffer();
		int length = 0;

		/* check for overflow */
		if(oi - 1 < 0) return -1;
		
		while((i >= 0) && (length < MAX_TAG_LENGTH)) {
			if (diff.N[i].equals(diff.O[oi - 1])) {
				storeTag(tag, sb.toString(), i + 1);
				
				/* scanned one too far in loop */
				return i + 1;
			}
			
			/* add this word to the tag */
			if(length != 0)
				sb.insert(0, ' '); /* words separated by spaces */
			sb.insert(0, diff.N[i]);
			i--;
			length++;
		}
		
		/* could not find tag start */
		return -1;
	}

	/**
	 * Scan text forwards to the end of a tag.
	 * 
	 * @param i Position in source text
	 * @param oi Position in license template
	 * @return New position in source text or -1 if no tag was found
	 */
	private int scanTagForward(int i, int oi)
	{
		/* diff.O[oi] is the "<tag>" and diff.N[i] is the first word of the tag value */
		
		int si = i; /* tag start index */
		String tag = diff.O[oi];
		StringBuffer sb = new StringBuffer();
		int length = 0;
		
		/* check for overflow */
		if(oi + 1 >= diff.O.length) return -1;
		
		while((i < diff.N.length) && (length < MAX_TAG_LENGTH)) {
			if (diff.N[i].equals(diff.O[oi + 1])) {
				storeTag(tag, sb.toString(), si);
				
				/* scanned one too far in loop */
				return i - 1;
			}
			
			/* add this word to the tag */
			if(length != 0)
				sb.append(' '); /* words separated by spaces */
			sb.append(diff.N[i]);
			i++;
			length++;
		}
		
		/* could not find tag end */
		return -1;
	}
	
	/**
	 * Scan backwards from a found it at diff.N[i] and locate the real
	 * start of the matching substring.
	 * 
	 * @param i  Found location of a match. diff.NA[i] must be an Integer
	 * @param oi Index in diff.O[] that matches diff.NA[i]
	 * @return
	 */
	private int[] scanBackwards(int i, int oi) {
		
		boolean dobreak = false;

		/* index in diff.O[] */
		//int oi = ((Integer) diff.NA[i]).intValue();
		
		do {
		
			/* check we're not going out of bounds */
			if((i == 0) || (oi == 0)) break;
			
			if(isTag(diff.O[oi - 1])) {
				int tag_start = scanTagBackward(i - 1, oi - 1);
				if(tag_start < 0) {
					/* tag scan failed */
					break;
				}
				
				/* go backwards past the tag */
				i = tag_start - 1;
				oi -= 2;
			} else {
				dobreak = true;
			}

			/* check again we're not going out of bounds */
			if((i == 0) || (oi == 0)) break;
			
			/* scan as long as words match */
			while (diff.O[oi - 1].equals(diff.N[i - 1])) {
	
				/* scan backwards */
				oi--;
				i--;
				
				dobreak = false;
				
				if((oi == 0) || (i == 0)) {
					/* can't go past beginning of text */
					dobreak = true;
					break;
				}
			}
		} while(!dobreak);
		
		int result[] = new int[2];
		result[0] = i;
		result[1] = oi;
		return result;
	}

	/**
	 * Check if a word is a tag. If the word starts with a "&lt;" and ends
	 * with "&gt;", it is assumed to be a tag.
	 * 
	 * @param word Word to check
	 * @return True if the word is a tag, false otherwise
	 */
	private boolean isTag(String word) {
		
		/* TODO: add tag checking from license info */
		if (word.startsWith("<") && word.endsWith(">")) {
			
			for (Tag t : licenseTags) {
				if (t.getId().equals(word)) {
					return true;
				}
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Find match positions, fill foundPositions.
	 * 
	 * @param minMatch Minimum match percentage, in range 0..1
	 */
	private void findPositions(double minMatch) {
		int i;

		ArrayList<MatchPosition> pos = new ArrayList<MatchPosition>();
		
		/* position in source code */
		int start_row = -1, start_col = 0;
		int end_row = 0, end_col = 0;

		/* position in template */
		int t_start_row = -1, t_start_col = 0;
		int t_end_row = 0, t_end_col = 0;
		
		/* found match length in words */
		int match_length = 0;
		int best_length = 0;
		int total_length = 0; /* total number of matched words */
		MatchPosition bestMatch = null;
		
		boolean continuous = false;
		int oi = 0; /* found index in diff.O[] */

		for (i = 0; i < diff.N.length - 1; i++) {
			
			/* if two sequential words are indices to sequential
			 * word in template, found a match.
			 */ 
			
			if (diff.NA[i] instanceof Integer
					&& (diff.NA[i + 1] instanceof Integer)) {

				int oi1 = ((Integer) diff.NA[i]).intValue();
				int oi2 = ((Integer) diff.NA[i + 1]).intValue();

				if ((oi1 + 1) == oi2) {
					/* words are sequential in template */

					if (start_row == -1) {
						int start[] = scanBackwards(i, oi1);
						/* start a new match */
						start_row = textwords.row.get(start[0]);
						start_col = textwords.col.get(start[0]);
						t_start_row = templatewords.row.get(start[1]);
						t_start_col = templatewords.col.get(start[1]);
						
						/* count the words already found in new match */
						/* using the template is safer for counting words */
						match_length = (oi1 + 1 - start[1]); 
					}
					
					/* current found match ends at the next word */
					
					match_length++;
					
					continuous = true;
					oi = oi2;
					
				} else {
					/* this and next word not sequential in template,
					 * match ends.
					 */
					
					continuous = false;
				}

			}
			
			/* or if foundNoneUnique is set, also consider first instance of each word */
			else if (diff.foundNoneUnique
				&& ((diff.NA[i] instanceof Diff.SymbolTableEntry)
						&& (((Diff.SymbolTableEntry)diff.NA[i]).nwno == i)
						&& (diff.NA[i + 1] instanceof Diff.SymbolTableEntry)
						&& (((Diff.SymbolTableEntry)diff.NA[i + 1])).nwno == i + 1)) {

				int oi1 =((Diff.SymbolTableEntry)diff.NA[i]).olno; 
				int oi2 =((Diff.SymbolTableEntry)diff.NA[i + 1]).olno;

				if ((oi1 + 1) == oi2) {
					/* words are sequential in template */
				
					if (start_row == -1) {
						int start[] = scanBackwards(i, oi1);
						/* start a new match */
						start_row = textwords.row.get(start[0]);
						start_col = textwords.col.get(start[0]);
						t_start_row = templatewords.row.get(start[1]);
						t_start_col = templatewords.col.get(start[1]);
						
						/* count the words already found in new match */
						/* using the template is safer for counting words */
						match_length = (oi1 + 1 - start[1]); 
					}
					
					/* current found match ends at the next word */
					
					match_length++;
					
					continuous = true;
					oi = oi2;
				} else {
					/* this and next word not sequential in template,
					 * match ends.
					 */
					
					continuous = false;
				}
				
			} else if(continuous && (oi + 1 < diff.O.length) && isTag(diff.O[oi + 1])) {
				/* continous match and the template expects a tag */
				
				int new_i = scanTagForward(i + 1, oi + 1);
				if(new_i == -1) {
					/* tag search failed, match ends */
					
					continuous = false;
				} else {
					i = new_i;
					oi += 2;
					
					/* current found match ends at the next word */
					
					match_length += 2;
					continuous = true;
				}
				
			} else if(continuous && (diff.NA[i + 1] instanceof Diff.SymbolTableEntry)) {
				/* found a word that has more than one hit in either the
				 * source text or the template.
				 */

				/* if the words match, continue found hit */
				if((oi + 1 < diff.O.length) && diff.N[i + 1].equals(diff.O[oi + 1])) {
					/* words match */
					
					oi++;
					match_length++;
					
				} else {
					/* words don't match */
					continuous = false;
				}
			}
			
			if(!continuous) {
				if (start_row != -1) {
					/* found match ends */
					
					end_row = textwords.row.get(i);
					end_col = textwords.col.get(i)
						+ textwords.len.get(i) - 1;

					t_end_row = templatewords.row.get(oi);
					t_end_col = templatewords.col.get(oi)
						+ templatewords.len.get(oi) - 1;
					
					total_length += match_length;
					if (((double)match_length / (double)diff.O.length)
							>= minMatch) {
						int findInd = -1;
						int ii = 0;
						for (MatchPosition m : pos) {
							if (m.getStartLine() == start_row && 
									m.getStartCol() == start_col) {
								findInd = ii;
							}
							ii++;
								
						}
						if (findInd != -1) {
							total_length -= pos.get(findInd).getNumWords();
							pos.set(findInd, new MatchPosition(start_row, start_col,
									end_row, end_col, t_start_row, t_start_col,
									t_end_row, t_end_col, 0, match_length));
						}
						else {
    						pos.add(new MatchPosition(start_row, start_col,
    							end_row, end_col, t_start_row, t_start_col,
    							t_end_row, t_end_col, 0, match_length));
						}
					}
					if(match_length > best_length) {
						best_length = match_length;
						bestMatch = new MatchPosition(start_row, start_col,
								end_row, end_col, t_start_row, t_start_col,
								t_end_row, t_end_col, 0, match_length); 
					}
					start_row = -1;
				}
			}
		}

		/* check if a match is still open */
		if (start_row != -1) {
			
			end_row = textwords.row.get(i);
			end_col = textwords.col.get(i)
				+ textwords.len.get(i) - 1;

			t_end_row = templatewords.row.get(oi);
			t_end_col = templatewords.col.get(oi)
				+ templatewords.len.get(oi) - 1;
			if (((double)match_length / (double)diff.O.length)
					>= minMatch) {
				int findInd = -1;
				int ii = 0;
				for (MatchPosition m : pos) {
					if (m.getStartLine() == start_row && 
							m.getStartCol() == start_col) {
						findInd = ii;
					}
					ii++;
						
				}
				if (findInd != -1) {
					total_length -= pos.get(findInd).getNumWords();
					pos.set(findInd, new MatchPosition(start_row, start_col,
							end_row, end_col, t_start_row, t_start_col,
							t_end_row, t_end_col, 0, match_length));
				}
				else {
					pos.add(new MatchPosition(start_row, start_col,
						end_row, end_col, t_start_row, t_start_col,
						t_end_row, t_end_col, 0, match_length));
					
				}
			}
			total_length += match_length;
			if(match_length > best_length) 
				best_length = match_length;
				
			
		}
		
		foundPositions = pos;
	}

	/**
	 * Perform analysis on the produced diff. This function calculates the
	 * results of the matching.
	 * 
	 * @param minMatch Minimum match percentage in range 0..1.
	 */
	private void analysis(double minMatch) {

		findPositions(minMatch);
		
		/* Calculate match% = (length of longest match) / (length of template) */

		int maxpts = diff.O.length;
		int best = 0;
		int total_length = 0;
		for (MatchPosition pos : foundPositions) {
			if(pos.getNumWords() > best)
				best = pos.getNumWords();
			
			total_length += pos.getNumWords();
			pos.setOrigMatchLength(pos.getNumWords());
		}

		/* match% is the length of the best match compared to maximum length */
		match = (float) best / (float) maxpts;
	}
	
	
}
