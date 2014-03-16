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
import java.util.Collection;

import checker.FileID;
import checker.gui.filter.Criteria;
import checker.license.License;
import checker.matching.LicenseMatch;

/**
 * This class represents an unknown file in a tree.
 * 
 * @author Veli-Jussi Raitila
 *
 */
public class FileUnknown extends FileAbstract {

	public FileUnknown(FileID f) {
		super(f);
	}

	@Override
	public ArrayList<LicenseMatch> getMatches() {
		return null;
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Collection<? extends LicenseTreeNode> getChildren() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public boolean isViewable() {
		return true;
	}

	@Override
	public boolean hasConflict() {
		return false;
	}
	
	@Override
	public boolean hasLicense() {
		return false;
	}

	@Override
	public boolean missLicense() {
		return false;
	}

	@Override
	public boolean meetsCriteria(Criteria c) {
		return false;
	}

}
