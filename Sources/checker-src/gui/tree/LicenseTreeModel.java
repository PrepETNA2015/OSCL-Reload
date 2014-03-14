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

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import checker.gui.filter.Criteria;

/**
 * Represent the tree contents and handles
 * changes in the view (filtering etc.)
 * 
 * @author Veli-Jussi Raitila
 *
 */
public class LicenseTreeModel implements TreeModel {

    // Registered tree listeners
	private Vector<TreeModelListener> treeModelListeners =
        new Vector<TreeModelListener>();
	// The root node
    private LicenseTreeNode rootNode;
    // Filter being applied to the tree
    private FilterType filter;
    // Filter criteria (for example a specific license) 
    private Criteria criteria;
    // Toggle showing references
    private boolean showReferences;
    // Possible filter types
    public enum FilterType { ALL, CONFLICTS, LICENSED, MISSING, UNCERTAIN };
    
    public LicenseTreeModel(LicenseTreeNode root) {
        rootNode = root;
        filter = FilterType.ALL;
        criteria = null;
        showReferences = false;
    }
    
    /**
     * Used to toggle between show/hide references and
     * to change the root of the tree.
     * 
     * @param b
     * @param newRoot
     */
    public void showReferences(boolean b, Object newRoot) {
        showReferences = b;
        LicenseTreeNode oldRoot = rootNode;
        /*
        if (newRoot != null) {
           rootNode = (LicenseTreeNode)newRoot;
        }
        */
        fireTreeStructureChanged(oldRoot);
    }
    
    /**
     * Used to apply a filter to the tree
     * 
     * @param f
     * @param c
     * @param newRoot
     */
    public void applyFilter(FilterType f, Criteria c, Object newRoot) {
    	filter = f;
    	criteria = c;
        LicenseTreeNode oldRoot = rootNode;

    	fireTreeStructureChanged(oldRoot);
    }

    /**
     * The only event raised by this model is TreeStructureChanged with the
     * root as path, i.e. the whole tree has changed.
     * 
     * @param oldRoot
     */
    protected void fireTreeStructureChanged(LicenseTreeNode oldRoot) {
        TreeModelEvent e = new TreeModelEvent(this, 
                                              new Object[] {oldRoot});
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }
    
    public void addTreeModelListener(TreeModelListener l) {
    	treeModelListeners.addElement(l);
	}

	public Object getChild(Object parent, int index) {
		LicenseTreeNode node = (LicenseTreeNode) parent;
		Object child = null;
		
		/* Source file has no children except references */
		if (node instanceof FileSource && !showReferences) return null;

		int i = 0;
		for (LicenseTreeNode c : node.getChildren()) {
			switch(filter) {
			case ALL:
				if (i == index) child = c;
				i++; 
				break;
			case CONFLICTS:
				if (c.hasConflict()) {
					if (i == index) child = c;
					i++;
				}
				break;
			case LICENSED:
			case UNCERTAIN:
				if (criteria != Criteria.ALL) {
					if (c.hasLicense() && c.meetsCriteria(criteria)) {
						if (i == index) child = c;
						i++;
					}
				} else {
					if (c.hasLicense()) {
						if (i == index) child = c;
						i++;
					}
				}
				break;
			case MISSING:
				if (c.missLicense()) {
					if (i == index) child = c;
					i++;
				}
				break;
			default:
				child = null;
				break;
			}
		}
		
		return child;
	}

	public int getChildCount(Object parent) {
		LicenseTreeNode node = (LicenseTreeNode) parent;
		int i, count;
		
		/* Source file has no children except references */
		if (node instanceof FileSource && !showReferences) return 0;

		/* Source file has no children except references */
		switch(filter) {
		case ALL:
			count = node.getCount(); 
			break;
		case CONFLICTS:
			i = 0;
			for (LicenseTreeNode c : node.getChildren()) {
				if (c.hasConflict()) i++;
			}
			count = i;
			break;
		case LICENSED:
		case UNCERTAIN:
			if (criteria != Criteria.ALL) {
				i = 0;
				for (LicenseTreeNode c : node.getChildren()) {
					if (c.hasLicense() && c.meetsCriteria(criteria)) i++;
				}
				count = i;
			} else {
				i = 0;
				for (LicenseTreeNode c : node.getChildren()) {
					if (c.hasLicense()) i++;
				}
				count = i;
			}
			break;
		case MISSING:
			i = 0;
			for (LicenseTreeNode c : node.getChildren()) {
				if (c.missLicense()) i++;
			}
			count = i;
			break;
		default:
			count = 0;
			break;
		}

		return count;
	}

	public int getIndexOfChild(Object parent, Object child) {
		LicenseTreeNode node = (LicenseTreeNode) parent;
		int index = -1;
		
		/* Source file has no children except references */
		if (node instanceof FileSource && !showReferences) return -1;

		int i = 0;
		for (LicenseTreeNode c : node.getChildren()) {
			switch(filter) {
			case ALL:
				if (c == child) index = i;
				i++; 
				break;
			case CONFLICTS:
				if (c.hasConflict()) {
					if (c == child) index = i;
					i++;
				}
				break;
			case LICENSED:
			case UNCERTAIN:
				if (criteria != Criteria.ALL) {
					if (c.hasLicense() && c.meetsCriteria(criteria)) {
						if (c == child) index = i;
						i++;
					}
				} else {
					if (c.hasLicense()) {
						if (c == child) index = i;
						i++;
					}
				}
				break;
			case MISSING:
				if (c.missLicense()) {
					if (c == child) index = i;
					i++;
				}
				break;
			default:
				index = -1;
				break;
			}
		}
		
		return index;
	}

	public Object getRoot() {
		return rootNode;
	}

	public boolean isLeaf(Object n) {
		LicenseTreeNode node = (LicenseTreeNode) n;

		/* Source file has no children except references */
		if (node instanceof FileSource && !showReferences) return true;
		
		return node.isLeaf();
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.removeElement(l);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}
	
}
