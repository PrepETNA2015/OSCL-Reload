/**
 * 
 *   Copyright (C) <2008> <Eero Kurkela>
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

package checker.gui.table;

import checker.copyright.CopyrightFile;
import checker.copyright.CopyrightHolder;
import checker.localization.Locale;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Table model for the copyrighted file details
 *  
 * @author Eero Kurkela
 * 
 */
public class CopyrightedFileTableModel extends AbstractTableModel {
	private Locale loc = new Locale();

    private ArrayList<CopyrightedFileTableRow> data;
    private String[] columnNames = {
    		loc.lc("File name"),
    		loc.lc("Directory"),
    		loc.lc("Copyright holder")};
    
    public CopyrightedFileTableModel(ArrayList<CopyrightFile> copyrightFiles) {
        this(copyrightFiles, false);
    }

    public CopyrightedFileTableModel(ArrayList<CopyrightFile> copyrightFiles, boolean addNonCopyrightedFiles) {
        data = new ArrayList<CopyrightedFileTableRow>();
        
        for (CopyrightFile thisCopyrightFile : copyrightFiles) {
            ArrayList<CopyrightHolder> copyrightHoldersOfCopyrightFile = thisCopyrightFile.getCopyrightHolders();
            if(copyrightHoldersOfCopyrightFile.isEmpty() && addNonCopyrightedFiles == true) {
                this.addRow(new CopyrightedFileTableRow(thisCopyrightFile.name, thisCopyrightFile.path, "-"));
            }
            else {
                for (CopyrightHolder thisCopyrightHolder : copyrightHoldersOfCopyrightFile) {
                   this.addRow(new CopyrightedFileTableRow(thisCopyrightFile.name, thisCopyrightFile.path, thisCopyrightHolder.name));
                }
            }
        }        
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
    	CopyrightedFileTableRow r = data.get(row);
    	Object value;
    	
    	switch (col) {
		case 0:
			value = r.getFileName();
			break;
		case 1:
			value = r.getFilePath();
			break;
		case 2:
			value = r.getCopyrightHolder();
			break;
                default:
			value = null;
			break;
    	}
    	
    	return value;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void addRow(CopyrightedFileTableRow r) {
        data.add(r);
    }
}
