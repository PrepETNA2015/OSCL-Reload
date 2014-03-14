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
import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;

import checker.CommentLine;
import checker.FileID;
import checker.LicenseChecker;
import checker.license.ForbiddenPhrase;
import checker.license.License;
import checker.license.LicenseDatabase;
import checker.license.Tag;

/**
 * Reads extracted comments from a source file and returns any matching
 * licenses.
 */
public class LicenseMatcher {
	
	/**
	 * Minimum number of words per matched segment.
	 * Segments with fewer words are filtered out.
	 */
	private static final int MIN_MATCH_LENGTH_WORDS = 10;
	
	/**
	 * Variable that contains the current value of
	 * MIN_MATCH_LENGTH_WORDS.
	 */
	private static int minMatchLengthWords;
	
	/**
	 * Run garbage collector every gcInterval matchings.
	 */
	private static final int gcInterval = 2000; 

	/**
	 * List of match algorithms.
	 *
	 */
	public enum MatchAlgorithm { EXACT,PARTIAL };
	
	/**
	 * License->WordList cache
	 */
	static Map<License, WordList> licenseCache = null;

	/**
	 * Load value of MIN_MATCH_LENGTH_WORDS from settings
	 */
	private static void loadMinMatchLengthWords() {
		String s;
		try {
			/* this might throw */
			s = (String) LicenseChecker.getSetting("minMatchLengthWords");
			minMatchLengthWords = Integer.valueOf(s);
			
		} catch (Exception e) {
		}
		
		/* reset invalid values to default */
		LicenseChecker.setSetting("minMatchLengthWords",
				new Integer(MIN_MATCH_LENGTH_WORDS).toString());
		minMatchLengthWords = MIN_MATCH_LENGTH_WORDS;
	}

	/**
	 * Convert given License to WordList and cache the results.
	 * 
	 * @param license License to convert
	 * @return WordList containing all the words from the license
	 */
	static private WordList cacheLicense(License license) {
		if (licenseCache == null) {
			licenseCache = new HashMap<License, WordList>();
		}
		
		WordList licWords = licenseCache.get(license);
		if (licWords == null) {
			/* generate WordList and put it in cache */
			licWords = WordList.licenseToWords(license);
			licenseCache.put(license, licWords);
		}
		
		return licWords;
	}

	/**
	 * WordList->String[] cache
	 */
	static Map<WordList, String[]> licenseStringCache = null;
	
	/**
	 * Convert given WordList to String[] and cache the results.
	 * 
	 * @param license WordList to convert
	 * @return Words from WordList stored in a String[]
	 */
	static private String[] cacheLicenseStrings(WordList license) {
		if (licenseStringCache == null) {
			licenseStringCache = new HashMap<WordList, String[]>();
		}
		
		String[] licStrings = licenseStringCache.get(license);
		if (licStrings == null) {
			/* generate WordList and put it in cache */
			licStrings = license.words.toArray(new String[1]);
			licenseStringCache.put(license, licStrings);
		}
		
		return licStrings;
	}
	
	/**
	 * Performs an exact match against given comments.
	 * 
	 * @param comments
	 *            List of comments.
	 * @param database
	 *            License information.
	 * @return Found matches.
	 * @deprecated Use that match() that takes algorithmID
	 */
	public static ArrayList<LicenseMatch> matchExact(
			ArrayList<CommentLine> comments, LicenseDatabase database) {
		// TODO
		return null;
	}

	/**
	 * Performs a partial match (diff) against given comments.
	 * 
	 * @param comments
	 *            List of comments.
	 * @param database
	 *            License information.
	 * @return Found matches. (the format of partial match object is not yet
	 *         determined)
	 * @deprecated Use that match() that takes algorithmID
	 */
	public static ArrayList<LicenseMatch> matchPartial(
			ArrayList<CommentLine> comments, LicenseDatabase database) {
		return null;
	}

	static int gcCount = 0;
	
	/**
	 * Perform exact string matching for given comments.
	 * Arguments l, license and licenseWords must be the same License.
	 * They are separated only to allow proper caching.
	 * Same with comments and commentsWords.
	 * 
	 * @param l License (forbidden phrase) to compare
	 * @param comments Source file comments in a WordList
	 * @param license License words in a WordList
	 * @param commentWords Source file comment words in a String[]
	 * @param licenseWords License words in a String[]
	 * 
	 * @return List of found matches
	 */
	private static ArrayList<LicenseMatch> matchLicenseExact(
			License l,
			WordList comments,
			WordList license) {
		
		ArrayList<LicenseMatch> list = null;
		
		/* this is slow */
		for (int i = 0; i < comments.words.size() - license.words.size() + 1; i++) {
			int j = 0; 
			for (; j < license.words.size(); j++) {
				if (!comments.words.get(i + j).equals(license.words.get(j)))
					break;
			}
			
			if (j == license.words.size()) {
				/* all words matched! */
				
				MatchPosition mp = 
						new MatchPosition(
								comments.row.get(i), comments.col.get(i),
								comments.row.get(i + j - 1), comments.col.get(i + j - 1) + comments.len.get(i + j - 1) - 1,
								license.row.get(0), license.col.get(0),
								license.row.get(j - 1), license.col.get(j - 1) + license.len.get(j - 1),
								0, j - 1);
				mp.setOrigMatchLength(j - 1);
				LicenseMatch m = new ForbiddenPhraseMatch(l, mp);
				
				if (list == null)
					list = new ArrayList<LicenseMatch>();
				
				list.add(m);
				
				/* return m;*/
				i += j - 1;
			}
		}
		
		return list;
	}

	/**
	 * Perform Diff and DiffAnalysis for given comments.
	 * 
	 * Arguments l, license and licenseWords must be the same License.
	 * They are separated only to allow proper caching.
	 * Same with comments and commentsWords.
	 * 
	 * @param l License to compare 
	 * @param comments Source file comments in a WordList
	 * @param license License words in a WordList
	 * @param commentsWords Source file comment words in a String[]
	 * @param licenseWords License words in a String[]
	 * @param minMatch Minimum match percentage (0.0 .. 1.0)
	 * @return Found match or null
	 */
	private static LicenseMatch matchLicenseDiff(
			License l,
			WordList comments,
			WordList license,
			String[] commentsWords,
			String[] licenseWords,
			double minMatch) {

		/* diff and analysis */
		Diff d = new Diff(licenseWords, commentsWords);
		DiffAnalysis a = new DiffAnalysis(d, license, comments,
				minMatch, l.getTags());
		
		/* get analysis results */
		ArrayList<MatchPosition> positions = a.getPositions();
		
		/* put results in a LicenseMatch */
		LicenseMatch match = new LicenseMatch(l, positions);
		
		if (!a.tags.isEmpty()) {
			for (Tag t : l.getTags()) {
				String value = a.tags.get(t.getId());
				if (value != null)
					match.addTag(t, value, a.tagStart.get(t.getId()));
			}
		}
		match.setMatchPr(a.getMatchPr());

		/* drop diff and analysis objects */
		d = null;
		a = null;
		
		gcCount++;
		if (gcCount > gcInterval) {
			gcCount = 0;
			System.gc();
		}

		return match;
	}
			
	/**
	 * Cut fragment 'cut' away from fragment 'match'. Word list is needed
	 * for counting word positions.
	 * <p>
	 * This implementation is really ugly, feel free to clean it up though.
	 * 
	 * @param match
	 * @param cut
	 * @param commentWords
	 * @return
	 */
	static LicenseMatch cutMatch(LicenseMatch match,
			LicenseMatch cut, WordList commentWords)
	{
		
		
		/* known bug: word count calculations will be off if
		 * the cut includes freeform fields. */
		
		/* Convert License to WordList */
		WordList licenseWords = cacheLicense(match.getLicense());
	
		/* never cut ForbiddenPhraseMatches */
		if (match instanceof ForbiddenPhraseMatch)
			return match;
		
		ArrayList<MatchPosition> newPosList = match.getMatchPositions();
		
		for (MatchPosition cc : cut.getMatchPositions()) {
			ArrayList<MatchPosition> newPosListTmp = new ArrayList<MatchPosition>();
			
			for (MatchPosition mm : newPosList) {
				
				if (mm.getNumWords() < minMatchLengthWords) 
					continue;
				
				/*
				 *   mmmmm
				 *     cccccc
				 */
				if ((mm.getStartLine() < cc.getStartLine()) ||
						(mm.getStartLine() == cc.getStartLine() &&
						mm.getStartCol() < cc.getStartCol())) {
					
					if ((mm.getEndLine() > cc.getStartLine()) ||
						(mm.getEndLine() == cc.getStartLine() &&	
							mm.getEndCol() > cc.getStartCol())){
						
						
						int templstart_i =
							licenseWords.findWordAt(mm.getTemplateStartLine(),
									mm.getTemplateStartCol());
						int c_start_i =
							commentWords.findWordAt(cc.getStartLine(), cc.getStartCol());
					
						int endrow = commentWords.row.get(c_start_i - 1);
						int endcol = commentWords.col.get(c_start_i - 1) + commentWords.len.get(c_start_i -1) - 1;
						
						int new_word_count = commentWords.countWordsBetween(
								mm.getStartLine(), mm.getStartCol(), endrow, endcol + 1);
						
						int end_word_i = templstart_i + new_word_count - 1;
						int templ_end_row = licenseWords.row.get(end_word_i);
						int templ_end_col = licenseWords.col.get(end_word_i) + licenseWords.len.get(end_word_i) - 1;
						
						MatchPosition newpos = new MatchPosition(
								mm.getStartLine(), mm.getStartCol(),
								endrow,
								endcol,
								mm.getTemplateStartLine(), mm.getTemplateStartCol(),
								templ_end_row, templ_end_col,
								mm.getCommentStartCol(), new_word_count);
						newpos.setOrigMatchLength(mm.getOrigMatchLength());
						
						
						
						if (new_word_count >= minMatchLengthWords) {
							newPosListTmp.add(newpos);
						}
						
						
						
					}
					
				}
				
				/*
				 *      mmmmmm
				 *  ccccccc
				 */
				if ((mm.getStartLine() < cc.getEndLine()) ||
					(mm.getStartLine() == cc.getEndLine() &&	
						mm.getStartCol() < cc.getEndCol())) {
					
					if ((mm.getEndLine() > cc.getEndLine()) ||
							(mm.getEndLine() == cc.getEndLine() && 
							 mm.getEndCol() > cc.getEndCol())) {
						
						int m_start_i = commentWords.findWordAt(mm.getStartLine(), mm.getStartCol());
						int c_end_i =
							commentWords.findWordAt(cc.getEndLine(), cc.getEndCol());
						
						
						int startrow = commentWords.row.get(c_end_i);
						int startcol = commentWords.col.get(c_end_i);
					
						int new_word_count =
							commentWords.countWordsBetween(
									startrow, startcol,
									mm.getEndLine(), mm.getEndCol());
						int templstart_i =
							licenseWords.findWordAt(mm.getTemplateStartLine(),
									mm.getTemplateStartCol());
						int start_word_i = templstart_i + c_end_i - m_start_i;
						
						int templ_start_row = licenseWords.row.get(start_word_i);
						int templ_start_col = licenseWords.col.get(start_word_i);
						
						
						MatchPosition newpos = new MatchPosition(
								startrow, startcol,
								mm.getEndLine(),
								mm.getEndCol(),
								templ_start_row, templ_start_col,
								mm.getTemplateEndLine(), mm.getTemplateEndCol(),
								mm.getCommentStartCol(), new_word_count);
						newpos.setOrigMatchLength(mm.getOrigMatchLength());
					
						
						
						if (new_word_count >= minMatchLengthWords) {
							newPosListTmp.add(newpos);
						}
						
						
					}
				}
				
			   /*
			    *   mmmmmmmmm
			    *     cccc
			    *   (This situation is handled by above two situations.
			    *   
			    */
				
				
			   /*
			    *    mmm
			    *  ccccccc or
			    *  
			    *  mmmm
			    *  cccc 
			    * (total cut)
			    */
				
				
				/*
				 *  mmmm
				 *       cccccc
				 */
				if ((mm.getEndLine() < cc.getStartLine())
					|| ((mm.getEndLine() == cc.getStartLine())
						&& (mm.getEndCol() < cc.getStartCol()))) {
					
					newPosListTmp.add(mm);
				}

				/*
				 *         mmmm
				 *  cccccc
				 */
				if ((mm.getStartLine() > cc.getEndLine())
					|| ((mm.getStartLine() == cc.getEndLine())
						&& (mm.getStartCol() > cc.getEndCol()))) {
					
					newPosListTmp.add(mm);
				}
				
			}
			newPosList = newPosListTmp;
		
		
		}
	
		int best_length = 0;
		for (MatchPosition pos : newPosList) {
			if (pos.getNumWords()> best_length)
				best_length = pos.getNumWords();
		}
		match.setMatchPr((float)best_length /(float)licenseWords.words.size());
		match.setMatchPositions(newPosList);
		return match;
	}

	/**
	 * Filter found License matches. Remove overlapping matches and too
	 * short fragments.
	 *  
	 * @param matchList Input, list of matches
	 * @param commentWords Source data for calculating match locations
	 * @return List of matches after filtering
	 */
	static ArrayList<LicenseMatch> filterMatches(
			ArrayList<LicenseMatch> matchList,
			WordList commentWords)
	{
		//if (true) return matchList;
		if (matchList == null) return null;
		
		/* get setting */
		loadMinMatchLengthWords();
		
		ArrayList<LicenseMatch> filtered = new ArrayList<LicenseMatch>();
	
		
		while (!matchList.isEmpty()) {
			ArrayList<LicenseMatch> matchListTmp = new ArrayList<LicenseMatch>();
			
			/* find 'best' match */
			LicenseMatch bestMatch = null;
			int bestMatchLen = 0;
			for (LicenseMatch m : matchList) {
				if (bestMatch == null) {
					bestMatch = m;
					bestMatchLen = m.getLongestLength();
				} else {
					int mlen = m.getLongestLength();
					
					if(bestMatchLen < mlen) {
						bestMatch = m;
						bestMatchLen = mlen;
					} else if (bestMatchLen == mlen) {
						if (bestMatch.getOrigLength() < m.getOrigLength()) {
							bestMatch = m;
							bestMatchLen = mlen;
						}
					}
				}
			}
			
			matchList.remove(bestMatch); /* TBD: use index instead */
			filtered.add(bestMatch);
			
			/* cut best match from the other matches
			 * and filter out short fragments */
			for (LicenseMatch m : matchList) {
				LicenseMatch f = cutMatch(m, bestMatch, commentWords);
				if (f != null) {
					if (f instanceof ForbiddenPhraseMatch) {
						matchListTmp.add(f);
					} else if (f.getLongestLength() > minMatchLengthWords) {
						matchListTmp.add(f);
					}
				}
			}
			
			/* swap lists */
			matchList = matchListTmp;
		}
		
		/* insert first of each license in 'matches', ignore duplicate
		 * licenses */
		ArrayList<LicenseMatch> matches = new ArrayList<LicenseMatch>();
		for (LicenseMatch m : filtered) {
			boolean foundAlready = false;
			for (LicenseMatch i : matches) {
				if (i.getLicense().equals(m.getLicense())) {
					/* ignore this license */
					foundAlready = true;
					break;
				}
			}
			if (!foundAlready) {
				
				matches.add(m);
			}
		}
		return matches;
	}
	
	/**
	 * Performs a match for multiple licenses agains given comments.
	 * 
	 * @param comments
	 *            List of comments.
	 * @param licenses
	 *            Licenses that the match will be done against.
	 * @param algorithmID
	 *            The algorithm that will be used (ignored)
	 * @param minMatch
	 *            Minimum match percentage to include in matches,
	 *            in range 0.0 .. 1.0 
	 * @return Found matches, never null.
	 */
	public static ArrayList<LicenseMatch> match(
			ArrayList<CommentLine> comments,
			AbstractCollection<License> licenses,
			MatchAlgorithm algorithmID,
			double minMatch) {
		
		ArrayList<LicenseMatch> matches = new ArrayList<LicenseMatch>();
		
		/* convert inputs to WordLists */
		WordList commentWords = WordList.commentsToWords(comments);
		String[] commentStrings = commentWords.words.toArray(new String[1]);

		/* check if comments are empty */
		if (commentWords.words.size() < 1) return matches;
		
		LicenseMatch bestMatch = null;
		for (License license : licenses) {

			/* Convert License to WordList */
			WordList licenseWords = cacheLicense(license);
		
			/* get words as a String array from WordLists */
			String[] licenseStrings = cacheLicenseStrings(licenseWords);
			
			if (license instanceof ForbiddenPhrase) {
				
				ArrayList<LicenseMatch> fpmatch = matchLicenseExact(license,
						commentWords, licenseWords);
				
				if(fpmatch != null)
					matches.addAll(fpmatch);
				
			} else { 
			
				/* analysis */
				LicenseMatch match = matchLicenseDiff(license,
						commentWords, licenseWords, commentStrings, licenseStrings,
						minMatch);
				if (match.getMatchPositions().size() > 0) {
					if (bestMatch == null
							|| (match.getMatchPr() > bestMatch.getMatchPr()))
						bestMatch = match;
					
					matches.add(match);
				}
			}
		}
		
		ArrayList<LicenseMatch> filtered = filterMatches(matches, commentWords);
		ArrayList<LicenseMatch> newMatches = new ArrayList<LicenseMatch>();
		
		/* re-check tags for forbidden phrases */
		for (LicenseMatch match : filtered) {
			HashMap<Tag, String> tags = match.getTags();
			if (tags == null) continue;
			
			for (Tag t : tags.keySet()) {
				String value = tags.get(t);
				
				/* split tag to get number of words */
				WordList tagTempWords = WordList.splitWords(value, 0, 0);

				WordList tagWords = commentWords.substring(match.getTagStart(t), tagTempWords.words.size());
				
				/* check forbidden phrases */
				for (License license : licenses) {
					if (license instanceof ForbiddenPhrase) {

						/* Convert License to WordList */
						WordList licenseWords = cacheLicense(license);
					
						ArrayList<LicenseMatch> fpmatch = matchLicenseExact(license,
								commentWords, licenseWords);
						
						if(fpmatch != null) {
							/* if this forbidden phrase was already
							 * found, ignore it */
							
							boolean alreadyFound = false;
							for(LicenseMatch m : filtered) {
								if (m.getLicense().equals(license)) {
									alreadyFound = true;
									break;
								}
							}
							
							if (!alreadyFound)
								newMatches.addAll(fpmatch);
						}
					}
				}
			}
		}
		
		filtered.addAll(newMatches);
		
		return filtered;
	}

	/**
	 * Performs a match agains given comments.
	 * 
	 * @param comments
	 *            List of comments.
	 * @param license
	 *            License that the match will be done agains.
	 * @param algorithmID
	 *            The algorithm that will be used
	 * @return Found matches, null if comments contain no words.
	 */
	public static LicenseMatch match(
			ArrayList<CommentLine> comments,
			License license,
			MatchAlgorithm algorithmID) {
		
		/* make an AbstractCollection that holds the license */
		HashSet<License> llist = new HashSet<License>();
		llist.add(license);
		
		ArrayList<LicenseMatch> c = match(comments, 
			llist,
			algorithmID,
			0.05);
		
		if (c == null) return null;
		if (c.isEmpty()) return null;
		
		return c.get(0);
	}
	
	
	/**
	 * Scans the comments for forbidden phrases. (interaction with
	 * allowedForbiddenPhrases to be determined)
	 * 
	 * @param comments List of comments.
	 * @param forbiddenPhrases List of ForbiddenPhrase:s
	 * @return
	 */
	public static ArrayList<ForbiddenPhraseMatch> matchForbiddenPhrases(
			ArrayList<CommentLine> comments,
			ArrayList<ForbiddenPhrase> forbiddenPhrases) {
		// TODO
		return null;
	}
}
