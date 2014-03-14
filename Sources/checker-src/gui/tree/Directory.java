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
import java.util.TreeMap;

import checker.gui.filter.Criteria;

/**
 * A class that represents a directory in a tree view.
 * It can contain all file types.
 * 
 * @author Veli-Jussi Raitila
 *
 */
public class Directory extends LicenseTreeNode {

	// Files contained in this directory
	private TreeMap<String, LicenseTreeNode> files;
	
	/**
	 * Constructs a normal directory.
	 * @param file
	 */
	public Directory(String n) {
		super(n);
		files = new TreeMap<String, LicenseTreeNode>();
	}

	/**
	 * Adds a new node under this directory.
	 * @param node
	 */
	public void addFile(LicenseTreeNode node) {
		node.parent = this;
		files.put(node.name, node);
	}
 
	/**
	 * Checks whether this directory contains a node.
	 * @param name
	 */
	public boolean hasFile(String name) {
		return files.containsKey(name); 
	}
	
	/**
	 * Retrieves a node from this directory.
	 * @param name
	 */
	public LicenseTreeNode getFile(String name) {
		return files.get(name);
	}
	
	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Collection<? extends LicenseTreeNode> getChildren() {
		return files.values();
	}
	
	@Override
	public boolean isLeaf() {
		if (files.size() == 0) return true; else return false;
	}
	
	@Override
	public boolean isViewable() {
		return false;
	}

	@Override
	public boolean hasConflict() {
		for (LicenseTreeNode node : files.values()) {
			if (node.hasConflict()) return true;
		}
		
		return false;
	}

	@Override
	public boolean hasLicense() {
		for (LicenseTreeNode node : files.values()) {
			if (node.hasLicense()) return true;
		}
		
		return false;
	}

	@Override
	public boolean missLicense() {
		for (LicenseTreeNode node : files.values()) {
			if (node.missLicense()) return true;
		}
		
		return false;
	}

	@Override
	public boolean meetsCriteria(Criteria c) {
		for (LicenseTreeNode node : files.values()) {
			if (node.meetsCriteria(c)) return true;
		}
		
		return false;
	}

}
