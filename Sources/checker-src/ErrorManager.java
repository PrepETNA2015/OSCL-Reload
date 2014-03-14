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

/**
 * Manages fatal errors.
 */
public class ErrorManager {

	/**
	 * Prints the error description and the stacktrace. Ends the program.
	 * 
	 * @param errorDescription
	 */
	public static void error(String errorDescription) {
		Log.log(LogEntry.ERROR, errorDescription);
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			Log.log(LogEntry.ERROR, e.toString());
		}
		System.exit(0);
	}
	
	public static void error(String errorDescription,Exception ex) {
		if((errorDescription != null)
				&& (errorDescription.length() > 0)) {
			
			Log.log(LogEntry.ERROR, errorDescription);
		}
		
		Log.log(LogEntry.ERROR, ex.toString());
		for (StackTraceElement e : ex.getStackTrace()) {
			Log.log(LogEntry.ERROR, e.toString());
		}
		System.exit(0);
	}
	
	public static void error(Exception ex) {
		error("",ex);
	}
}
