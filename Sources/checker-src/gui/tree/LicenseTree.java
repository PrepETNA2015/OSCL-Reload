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

import javax.swing.JTree;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import checker.localization.Locale;

import checker.FileID;
import checker.Reference;
import checker.gui.LicenseMain;
import checker.gui.filter.Criteria;
import checker.gui.tree.LicenseTreeModel.FilterType;

/**
 * A tree that represents all the files processed
 * as well as their references.
 *
 * @author Veli-Jussi Raitila
 */
public class LicenseTree extends JTree {
	
	/* Localization */
	private Locale loc = new Locale();
	
    public LicenseTree() {
        /* User can select only one node at a time */
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        /* Set the renderer to apply icons to different nodes */
        setCellRenderer(new LicenseCellRenderer());

        /* Set the indentation of the tree nodes */
        ComponentUI ui = getUI();
        if (ui instanceof BasicTreeUI) {
        	((BasicTreeUI)ui).setLeftChildIndent(4);
        	((BasicTreeUI)ui).setRightChildIndent(10);
        }
        
        /* Set the node expansion click count to 3 */
        setToggleClickCount(3);
        
        reset();
    }
    
    public void reset() {
        setModel(new LicenseTreeModel(new Directory(this.loc.lc("No files"))));
    }
    
    private void printNode(LicenseTreeNode node, String indent) {
        //System.out.println(indent + "`" + node.name);
        if(node instanceof Directory) {
            //System.out.println(indent + "\\__");
    		for (LicenseTreeNode c : node.getChildren()) {
    			printNode(c, indent + "   ");
    		}
        }
    }
    
    public void printTree() {
    	LicenseTreeNode root = (LicenseTreeNode) getModel().getRoot();
    	printNode(root, "");
    }

    /**
     * Jumps to the referenced file in the tree and return
     * the destination node.
     * 
     * @param node
     * @return
     * @throws Exception
     */
    public LicenseTreeNode findNode(LicenseTreeNode node) throws Exception {

    	ArrayList<Object> path = new ArrayList<Object>();
    	FileID file;
    	
    	if (node instanceof FileReference) {
    		Reference ref = ((FileReference)node).getReference();
    		file = ref.targetFile;
    	} else if (node instanceof FileAbstract) {
    		file = ((FileAbstract)node).getFileID();
    	} else {
           	/* TODO Might jump to a directory as well (java.*) */
    		file = null;
    	}

		LicenseTreeNode root = (LicenseTreeNode) getModel().getRoot();
		LicenseTreeNode fileref = root;
		path.add(fileref);
		
		/* Root is a directory, walk through it. 
		 * Otherwise return the root itself
		 */
		if (root instanceof Directory) {
			Directory curdir = (Directory) root;
	
			if (file.path != null) {
	    		
				String[] dirs = file.path.split(LicenseMain.fileSeparator);
				for (String dir : dirs) {
					if(curdir.hasFile(dir)) {
						curdir = (Directory)curdir.getFile(dir);
						path.add(curdir);
					}
				}
			}
			fileref = curdir.getFile(file.name);
			path.add(fileref);
		}

		TreePath tp = new TreePath(path.toArray());
		scrollPathToVisible(tp);
		setSelectionPath(tp);
		return fileref;
    }
    
    /**
     * Toggle whether references are shown in the tree.
     * 
     * @param b
     */
    public void showReferences(boolean b) {
        Object newRoot = null;
        TreePath path = getSelectionModel().getSelectionPath();
        if (path != null) {
            newRoot = path.getLastPathComponent();
        }
        TreeModel model = getModel();
        if (model instanceof LicenseTreeModel)
        	((LicenseTreeModel)model).showReferences(b, newRoot);
    }

    /**
     * Apply a filter to a tree.
     * 
     * @param f
     * @param c
     */
    public void applyFilter(FilterType f, Criteria c) {
        Object newRoot = null;
        TreePath path = getSelectionModel().getSelectionPath();
        if (path != null) {
            newRoot = path.getLastPathComponent();
        }
        TreeModel model = getModel();
        if (model instanceof LicenseTreeModel)
        	((LicenseTreeModel)model).applyFilter(f, c, newRoot);
    }

}
