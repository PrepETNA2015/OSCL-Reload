/**
 * 
 *   Copyright (C) 2008 Johannes Heikkinen
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
 */

package checker.gui.table;

import checker.license.License;
import checker.license.LicenseDatabase;
import checker.localization.Locale;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for selecting licenses
 * 
 * @author Eero Kurkela
 *
 */
public class SelectLicensesTableModel extends AbstractTableModel {
	private Locale loc = new Locale();
	
    private ArrayList<SelectLicensesTableRow> data;
	private String columnNames[] = {"", loc.lc("License"), ""};
	
	public SelectLicensesTableModel() {
        LicenseDatabase ldb = new LicenseDatabase("licenses");
        ldb.buildLicenseDatabase();
        ArrayList<License> allLicenses = new ArrayList<License>(ldb.getLicenses());
        allLicenses.addAll(ldb.getForbiddenPhrases());
        data = new ArrayList<SelectLicensesTableRow>();
        for(int i=0; i<allLicenses.size(); i++) {
            data.add(new SelectLicensesTableRow(allLicenses.get(i), allLicenses.get(i).getScreenName(), false));
        }
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
        return data.size();
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
	}
	public String getColumnName(int col) {
		return columnNames[col];
	}

    public void addRow(SelectLicensesTableRow r) {
		data.add(r);
	}

    public License getLicenseAt(int row) {
        return data.get(row).getLicense();
    }

    public Object getValueAt(int row, int col) {
    	SelectLicensesTableRow r = data.get(row);
    	Object value;

    	switch (col) {
		case 0:
			value = r.getLicense();
			break;
		case 1:
			value = r.getSreenName();
			break;
 		case 2:
            value = r.getSelected();
			break;
        default:
			value = null;
			break;
    	}

    	return value;
    }

    public void setValueAt(Object value, int row, int col) {
        if(col == 2) {
            data.get(row).setSelected(value.equals(true));
        }
        fireTableCellUpdated(row, col);
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
