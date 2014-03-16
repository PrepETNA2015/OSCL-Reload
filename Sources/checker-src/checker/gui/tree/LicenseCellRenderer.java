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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renderer for the tree cells. Displays appropriate
 * icons for different types of nodes.
 * 
 * @author Veli-Jussi Raitila
 *
 */
public class LicenseCellRenderer extends DefaultTreeCellRenderer {

	// Folder icon (closed)
	private ImageIcon folderIcon;
	// Folder icon (open)
	private ImageIcon foopenIcon;
	// Folder that has conflicts
	private ImageIcon foconfIcon;
	// Source file icon
	private ImageIcon sourceIcon;
	// License file icon
	private ImageIcon licenseIcon;
	// Unknown file icon
	private ImageIcon unknownIcon;
	// Reference icon (valid)
	private ImageIcon referenceIcon;
	// Reference icon (conflicting)
	private ImageIcon referconfIcon;
	// Reference icon (self)
	private ImageIcon referselfIcon;
	// Reference icon (invalid)
	private ImageIcon invalidIcon;
	// Source file icon (conflicts)
	private ImageIcon conflictIcon;
	
	public LicenseCellRenderer() {
		super();
		folderIcon = new ImageIcon(getClass().getResource("/resources/folder.png"));
		foopenIcon = folderIcon;
		foconfIcon = new ImageIcon(getClass().getResource("/resources/folder_delete.png"));
		sourceIcon = new ImageIcon(getClass().getResource("/resources/page_white_code.png"));
		licenseIcon = new ImageIcon(getClass().getResource("/resources/page_white_text.png"));
		unknownIcon = new ImageIcon(getClass().getResource("/resources/page_white.png"));
		referenceIcon = new ImageIcon(getClass().getResource("/resources/arrow_right.png"));
		referconfIcon = new ImageIcon(getClass().getResource("/resources/red_arrow_right.png"));
		referselfIcon = new ImageIcon(getClass().getResource("/resources/red_arrow_undo.png"));
		invalidIcon = new ImageIcon(getClass().getResource("/resources/yellow_arrow_right.png"));
		conflictIcon = new ImageIcon(getClass().getResource("/resources/page_white_delete.png"));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
				hasFocus);
		
		// Rendering a directory
		if (value instanceof Directory)
		{
			Directory dir = (Directory) value;
			if (dir.hasConflict()) {
				if (expanded) setIcon(foconfIcon); 
				else setIcon(foconfIcon);
			} else {
				if (expanded) setIcon(foopenIcon); 
				else setIcon(folderIcon);
			}
		}
		// Rendering a source file
		else if (value instanceof FileSource)
		{
			FileSource file = (FileSource) value; 
			if (file.hasConflict())
				setIcon(conflictIcon);
			else
				setIcon(sourceIcon);
		}
		// Rendering a license file
		else if (value instanceof FileLicense)
			setIcon(licenseIcon);
		// Rendering an unknown file
		else if (value instanceof FileUnknown)
			setIcon(unknownIcon);
		// Rendering a reference
		else if (value instanceof FileReference)
		{
			// FIXME Not very intuitive, mabby add some icons?
			FileReference file = (FileReference) value; 
			if (file.isViewable()) {
				if (file.hasConflict()) {
					if (file.isSelf()) setIcon(referselfIcon);
					else setIcon(referconfIcon);
				}
				else setIcon(referenceIcon);
			}
			else setIcon(invalidIcon);
		}
		else setIcon(null);
		
		return this;
	}

}
