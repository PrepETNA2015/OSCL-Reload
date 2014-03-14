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
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import checker.localization.Locale;

import checker.license.License;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Data model for the license counts
 * to be represented in a JTable
 *  
 * @author Veli-Jussi Raitila
 *
 * modified / ekurkela
 */
public class CountTableModel extends AbstractTableModel {
	/* Localization */
	private Locale loc = new Locale();

	private Map<License, Integer> counts;
    private HashMap<License,Float> matchps;

    private String[] columnNames = {
    		this.loc.lc("License"),
            this.loc.lc("Count"),
            this.loc.lc("Max %") };
    

    public CountTableModel(Map<License, Integer> d, HashMap<License, Float> m) {
    	counts = d;
    	matchps = m;
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }

	public int getRowCount() {
		return counts.size();
	}

    public String getColumnName(int col) {
        return columnNames[col];
    }

	public Object getValueAt(int row, int col) {
		Object value = null;
	
		int i = 0;
		for (License lic : counts.keySet()) {
			if (i == row) {
				switch (col) {
				case 0:
					value = lic.getScreenName();
					break;
				case 1:
					value = counts.get(lic);
					break;
				case 2:
					value = Math.floor(matchps.get(lic) * 100.0 * 10)/10;
					break;
				default:
					value = null;
					break;
				}
			}
			i++;
		}
		
		return value;
	}

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
