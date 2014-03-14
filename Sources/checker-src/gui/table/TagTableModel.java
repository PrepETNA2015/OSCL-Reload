/**
 * 
 *   Copyright (C) <2007> <Veli-Jussi Raitila>
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

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import checker.localization.Locale;

import checker.license.Tag;

/**
 * Data model for the free-form fields
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class TagTableModel extends AbstractTableModel {
	/* Localization */
	private Locale loc = new Locale();
	
	private HashMap<Tag, String> data;
    private String[] columnNames = {
    		this.loc.lc("License"),
            this.loc.lc("Tag"),
            this.loc.lc("Value") };

    public TagTableModel() {
    	data = new HashMap<Tag, String>(0);
    }

    public void putAll(HashMap<Tag, String> t) {
    	data.putAll(t);
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
		Object value = null;
		
		int i = 0;
		for (Tag tag : data.keySet()) {
			if (i == row) {
				switch (col) {
				case 0:
					value = tag.getLicense().getScreenName();
					break;
				case 1:
					value = tag.getId();
					break;
				default:
					value = data.get(tag);
					break;
				}
			}
			i++;
		}
		
		return value;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
