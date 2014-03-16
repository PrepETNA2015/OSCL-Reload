/**
 * 
 *   Copyright (C) 2006 Lauri Koponen
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

import checker.localization.Locale;

public class LogEntry {
	
	private static Locale loc = new Locale();
	/* log entry priorities */
	public final static int ERROR = 1;
	public final static int VERBOSE = 2;
	public final static int DEBUG = 3;
	
	private final static String levelNames[] = { "", loc.lc("ERROR"), "VERBOSE", "DEBUG" };

	/* priority for this log entry */
	private int priority;

	/* description for this log entry */
	private String text;

	/**
	 * Class constructor. Log entry priority and description must be supplied.
	 * 
	 * @param priority  Priority for this log entry (ERROR, VERBOSE or DEBUG)
	 * @param text      Log entry text
	 */
	public LogEntry(int priority, String text) {
		this.priority = priority;
		this.text = text;
	}
	
	/**
	 * @return Log entry priority
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * @return Log entry text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Log entry as a text string
	 */
	public String toString() {
		return levelNames[priority] + ": " + text;  
	}
}
