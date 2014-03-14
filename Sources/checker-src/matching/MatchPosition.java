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

import checker.CommentLine;

/**
 * Location of a match inside a source file.
 */
public class MatchPosition {

	/**
	 * Source file line number where the match starts.
	 */
	private int startLine;

	/**
	 * (character) Column where the match starts.
	 */
	private int startCol;

	/**
	 * Source file line number where the match ends.
	 */
	private int endLine;

	/**
	 * (character) Column where the match ends. (inclusive)
	 */
	private int endCol;

	/**
	 * Template line number where the match starts.
	 */
	private int startLineTemplate;

	/**
	 * (character) Column where the match starts in template.
	 */
	private int startColTemplate;

	/**
	 * Template line number where the match ends.
	 */
	private int endLineTemplate;

	/**
	 * (character) Column where the match ends in template. (inclusive)
	 */
	private int endColTemplate;
	
	/**
	 * Column in the file where the comments start (number of characters that
	 * have been stripped from the beginning of the line). This is usefull if
	 * the file is displayed to the user with only comments.
	 */
	private int commentStartCol;
	
	/** Number of matching words in this match. */
	private int numWords;
	
	
	/** Total number of matching words in the original match. This is a bit
	 * of a hack to choose the longer match during filtering. */
	private int origMatchLength;

	/**
	 * Creates a new match position. Column references are to source code files,
	 * this is so that the CommentLine is not needed after the match and so that
	 * same MatchPosition can be used both for source files and license files.
	 * 
	 * @param startLine
	 *            Source file line number where the match starts 
	 * @param startCol
	 *            (character) Column where the match starts
	 * @param endLine
	 *            Source file line number where the match ends
	 * @param endCol
	 *            (character) Column where the match ends (inclusive)
	 * @param startLineTemplate
	 *            Template line number where the match starts 
	 * @param startColTemplate
	 *            (character) Column where the match starts in template
	 * @param endLineTemplate
	 *            Template line number where the match ends
	 * @param endColTemplate
	 *            (character) Column where the match ends in template (inclusive)
	 * @param commentStartCol
	 *            Column in the file where the comments start (number of
	 *            characters that have been stripped from the beginning of the
	 *            line). This is usefull if the file is displayed to the user
	 *            with only comments.
	 * 
	 */
	public MatchPosition(int startLine, int startCol,
			int endLine, int endCol,
			int startLineTemplate, int startColTemplate,
			int endLineTemplate, int endColTemplate,
			int commentStartCol, int numWords) {
		this.startLine = startLine;
		this.startCol = startCol;
		this.endLine = endLine;
		this.endCol = endCol;
		this.startLineTemplate = startLineTemplate;
		this.startColTemplate = startColTemplate;
		this.endLineTemplate = endLineTemplate;
		this.endColTemplate = endColTemplate;
		this.commentStartCol = commentStartCol;
		this.numWords = numWords;
		
		this.origMatchLength = 0;
		
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartCol() {
		return startCol;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndCol() {
		return endCol;
	}

	public int getTemplateStartLine() {
		return startLineTemplate;
	}

	public int getTemplateStartCol() {
		return startColTemplate;
	}

	public int getTemplateEndLine() {
		return endLineTemplate;
	}

	public int getTemplateEndCol() {
		return endColTemplate;
	}
	
	public int getCommentStartCol() {
		return commentStartCol;
	}

	public int getNumWords() {
		return numWords;
	}
	
	public int getOrigMatchLength() {
		return origMatchLength;
	}
	
	public void setOrigMatchLength(int length) {
		origMatchLength = length;
	}

	
	public boolean equals(MatchPosition match) {
		if ((startLine == match.startLine)
				&& (startCol == match.startCol)
				&& (endLine == match.endLine)
				&& (endCol == match.endCol)
				&& (startLineTemplate == match.startLineTemplate)
				&& (startColTemplate == match.startColTemplate)
				&& (endLineTemplate == match.endLineTemplate)
				&& (endColTemplate == match.endColTemplate)				
				&& (numWords == match.numWords)				
				&& (commentStartCol == match.commentStartCol)) {
			
			return true;
		}
		
		return false;
	}
	
	public String toString() {
		/* adjust positions: internally lines and columns start at
		 * index 0, but users expect them to start at index 1.
		 */
		return "(" + (startLine + 1) + ", "
		 	+ (startCol + 1) + ") -> ("
			+ (endLine + 1) + ", " + (endCol + 1) + ") "
			+ "[" + (startLineTemplate + 1) + ", "
			+ (startColTemplate + 1) + "] -> ["
			+ (endLineTemplate + 1) + ", "
			+ (endColTemplate + 1) + "] ";
	}
}
