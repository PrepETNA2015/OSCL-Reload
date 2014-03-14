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

import java.util.Collection;

import checker.gui.filter.Criteria;

/**
 * Superclass of all files, references and directories.
 * These are placed inside a tree structure in the GUI.
 * 
 * @author Veli-Jussi Raitila
 * 
 */
public abstract class LicenseTreeNode {

	// The name of the file that this node represents
	protected String name;
	// The parent of this node
	protected LicenseTreeNode parent;
	
	/**
	 * Creates a new node of the source tree.
	 * @param file
	 */
	public LicenseTreeNode(String n) {
		name = n;
		parent = this;
	}

	public String toString() {
		return name;
	}
	
	/**
	 * Returns the parent of this node.
	 * @return
	 */
	public LicenseTreeNode getParent() {
		return parent;
	}
	
	/**
	 * Retrieves the numbers of nodes in this directory.
	 * @param name
	 */
	public abstract int getCount();

	/**
	 * Returns the children of this node.
	 * @return
	 */
	public abstract Collection<? extends LicenseTreeNode> getChildren();

	/**
	 * Returns true is this node is a leaf.
	 * @return
	 */
	public abstract boolean isLeaf();
	
	/**
	 * Returns true if this node is viewable by the user.
	 * @return
	 */
	public abstract boolean isViewable();

	/**
	 * Returns true if this node has conflicts.
	 * @return
	 */
	public abstract boolean hasConflict();
	
	public abstract boolean hasLicense();
	
	public abstract boolean missLicense();

	public abstract boolean meetsCriteria(Criteria c);

}
