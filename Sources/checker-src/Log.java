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

import java.util.Iterator;
import java.util.ArrayList;

/**
 * Log for logging program execution.
 * <p> 
 * Log is filled in during program execution by calling Log.log(). User
 * interface may read log contents using Log.iterator(), or set output
 * verbosity level with setVerbosity() to directly output log entries to
 * standard output.  
 * 
 * @author Lauri Koponen
 */
public class Log {
	
	/* Log entries in list */
	private static ArrayList<LogEntry> logTexts = new ArrayList<LogEntry>();

	/* selected output verbosity */ 
	private static int outputVerbosity = 0;
	
	
	/**
	 * Get output verbosity. Log entries up to and including the set
	 * verbosity will be printed directly to System.out.
	 * 
	 * @return LogEntry.ERROR, LogEntry.VERBOSE, LogEntry.DEBUG or 0.
	 */
	public static int getVerbosity() {
		return outputVerbosity;
	}

	/**
	 * Set output verbosity. Log entries up to and including the set
	 * verbosity will be printed directly to System.out.
	 * 
	 * @param verbosity LogEntry.ERROR, LogEntry.VERBOSE, or LogEntry.DEBUG
	 */
	public static void setVerbosity(int verbosity) {
		outputVerbosity = verbosity;
	}

	/**
	 * Write to log.
	 * 
	 * @param priority Log entry priority: LogEntry.ERROR, LogEntry.VERBOSE,
	 *             or LogEntry.DEBUG
	 * @param text Log entry string
	 */
	public static void log(int priority, String text) {
		
		/* don't log debug messages if not requested */
		if((priority >= LogEntry.DEBUG) && (outputVerbosity < LogEntry.DEBUG)) {
			return;
		}
		
		LogEntry le = new LogEntry(priority, text); 
		
		logTexts.add(le);
		
		if(outputVerbosity >= priority) {
			System.out.println(le);
		}
	}

	/**
	 * Clear log.
	 */
	public static void clear() {
		logTexts = new ArrayList<LogEntry>();
	}

	/**
	 * Get the number of entries in log.
	 * 
	 * @return number of entries in log
	 */
	public static int size() {
		return logTexts.size();
	}
	
	/**
	 * Iterate through log entries.
	 * 
	 * @return log iterator
	 */
	public static Iterator iterator() {
		return logTexts.iterator();
	}
}
