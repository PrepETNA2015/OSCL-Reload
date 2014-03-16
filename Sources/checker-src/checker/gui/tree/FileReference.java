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

import checker.Pair;
import checker.Reference;
import checker.gui.filter.Criteria;
import checker.license.License;

/**
 * This class represents a file reference in a tree.
 *
 * @author Veli-Jussi Raitila
 * 
 */
public class FileReference extends LicenseTreeNode {

	// Reference this node represents
	private Reference reference;
	// Conflicts in this reference
	ArrayList<Pair<License, License>> conflicts;
	
	public FileReference(Reference ref, ArrayList<Pair<License, License>> c) {
		super(ref.targetFile.name);
		reference = ref;
		conflicts = c;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public boolean isSelf() {
		if (reference.sourceFile == reference.targetFile) {
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<Pair<License, License>> getConflicts() {
		return conflicts;
	}
	
	@Override
	public String toString() {
		return reference.declaration; 
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
		switch (reference.referenceType) {
		case IMPORT:
			return true;
		case STATIC_INCLUDE:
			return true;
		case IMPORT_STANDARD_LIBRARY:
			return false;
		case UNPARSABLE:
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean hasConflict() {
		if (conflicts != null) return true;
		else return false;
	}

	@Override
	public boolean hasLicense() {
		return true;
	}
	
	@Override
	public boolean missLicense() {
		return true;
	}

	@Override
	public boolean meetsCriteria(Criteria c) {
		return true;
	}

}
