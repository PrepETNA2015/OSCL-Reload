/**
 * 
 *   Copyright (C) 2006 Sakari K��ri�inen, Lauri Koponen
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

package checker;

/**
 * Comment line of a source code without any surrounding characters. <br>
 * Never contains a newline character. If single source code line has multiple
 * comments, create a new CommentLine object for each comment.
 */
public class CommentLine {

	private String content;

	private int sourceLineNumber;

	private int commentStartColumn;

	/**
	 * 
	 * @param line
	 *            Comment line without surrounding characters.
	 * @param sourceLineNumber
	 *            Line number in the source file.
	 * @param commentStartColumn
	 *            Column in the file where the comments start (number of
	 *            characters that have been stripped from the beginning of the
	 *            line)
	 */
	public CommentLine(String line, int sourceLineNumber, int commentStartColumn) {
		this.content = line;
		this.sourceLineNumber = sourceLineNumber;
		this.commentStartColumn = commentStartColumn;
	}

	/**
	 * Constructor for lines of license files. (commentStartColumn will be 0)
	 * @param line Normal line of the license file.  
	 * @param lineNumber Line number in the license file.
	 */
	public CommentLine(String line,int lineNumber) {
		this.content = line;
		sourceLineNumber = lineNumber;
		commentStartColumn = 0;
	}
	
	/**
	 * Gets the comment line content.
	 * 
	 * @return content
	 * 			
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Gets the line number in the source file.
	 * 
	 * @return Line number
	 */
	public int getSourceLineNumber() {
		return sourceLineNumber;
	}

	/**
	 * Gets the column number where comment start.
	 * 
	 * @return column number
	 */
	public int getCommentStartColumn() {
		return commentStartColumn;
	}
}
