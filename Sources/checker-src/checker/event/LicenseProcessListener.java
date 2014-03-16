/**
 * 
 *   Copyright (C) <2006> <Veli-Jussi Raitila>
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
 *   Also add information on how to contact you by electronic and paper mail.
 *
 */

package checker.event;

import java.util.EventListener;

/**
 * Event interface that enables communication between the GUI
 * and the main program.
 * 
 * @author Veli-Jussi Raitila
 *
 */
public interface LicenseProcessListener extends EventListener {
	/**
	 * LicenseChecker has started processing files
	 * @param e
	 */
	public void processBegun(LicenseEvent e);
	/**
	 * LicenseChecker has finished processing files
	 * @param e
	 */
	public void processEnded(LicenseEvent e);
	/**
	 * User has cancelled the checking process
	 * @param e
	 */
	public void processCancelled(LicenseEvent e);
	/**
	 * LicenseChecker has started retrieving file contents
	 * @param e
	 */
	public void fileOpenBegun(LicenseEvent e);
	/**
	 * LicenseChecker has retrieved file contents
	 * @param e
	 */
	public void fileOpenEnded(LicenseEvent e);
	/**
	 * LicenseChecker has processed one file
	 * @param e
	 */
	public void fileProcessed(LicenseProcessEvent e);
}
