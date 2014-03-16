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

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import checker.localization.Locale;


/**
 * Data model for the license match details
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class MatchTableModel extends AbstractTableModel {
	/* Localization */
	private Locale loc = new Locale();

	private ArrayList<MatchTableRow> data;
    private String[] columnNames = {
    		this.loc.lc("Legend"),
            this.loc.lc("License"),
            this.loc.lc("Match%") };
    
    public MatchTableModel() {
    	data = new ArrayList<MatchTableRow>();
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
    	MatchTableRow r = data.get(row);
    	Object value;
    	
    	switch (col) {
		case 0:
			value = r.getColor();
			break;
		case 1:
			value = r.getLicense();
			// value = m.getLicense().getId();
			break;
		case 2:
			value = r.getMatch();
			// value = String.valueOf(Math.floor(m.getMatchPr() * 100.0));
			break;
		default:
			value = null;
			break;
    	}
    	
    	return value;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void addRow(MatchTableRow r) {
		data.add(r);
	}
}
