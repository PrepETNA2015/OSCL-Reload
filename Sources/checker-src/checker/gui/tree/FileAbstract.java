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

package checker.gui.tree;

import java.util.ArrayList;

import checker.FileID;
import checker.matching.LicenseMatch;

/**
 * An abstract superclass of all files that can be inserted into a tree.
 * Programmer has to specify the file type explicitly.
 *
 * @author Veli-Jussi Raitila
 *
 */
public abstract class FileAbstract extends LicenseTreeNode {

	protected FileID file;
	
	public FileAbstract(FileID f) {
		super(f.name);
		file = f;
	}
	
	public FileID getFileID() {
		return file;
	}

	public abstract ArrayList<LicenseMatch> getMatches();

}
