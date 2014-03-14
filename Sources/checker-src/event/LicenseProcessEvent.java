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

import checker.FileID;

/**
 * A processing event for LicenseChecker.
 * Used to track processing events and notify the GUI about them
 * (ie. progress indicator).
 * 
 * @author Veli-Jussi Raitila
 *
 */
public class LicenseProcessEvent {
	// File being processed
	private FileID file;
	// Index of the file being processed
	private int fileIndex;
	// Total amount of files to be processed
	private int fileCount;
	
	public LicenseProcessEvent(FileID f, int fi, int fc) {
		file = f;
		fileIndex = fi;
		fileCount = fc;
	}
	public FileID getFile() {
		return file;
	}
	public int getFileCount() {
		return fileCount;
	}
	public int getFileIndex() {
		return fileIndex;
	}
}
