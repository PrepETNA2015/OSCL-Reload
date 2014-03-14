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

package checker.license;

import javax.swing.table.AbstractTableModel;
import checker.localization.Locale;

/**
 * A Class for a table model to be used in listing the licenses with a check box to select multiple
 * licenses.
 * 
 * @author Johannes Heikkinen
 *
 */
public class ListLicenseTableModel extends AbstractTableModel {
	
	private static Locale loc = new Locale();
	private String licenseName = "";
	private int licenseCount = 0;
	Object[][] data;
	String columnNames[] = {"Number", loc.lc("License")};
	
	/**
	 * Class constructor.
	 * 
	 * @param licenseCount
	 */
	
	public ListLicenseTableModel(int licenseCount) {
		//this.licenseName = licenseName;
		this.licenseCount = licenseCount;
		data = new Object[licenseCount][2];
		for (int i = 0; i < this.data.length; i++) {
			this.setValueAt(""+i, i, 0);
			this.setValueAt("", i, 1);
		}
	}
	
	public String getName() {
		return this.licenseName;
	}
	
	public int getColumnCount() {	
		return columnNames.length;
	}

	public String getColumnName(int col) {
	        return columnNames[col];
	}
	public int getRowCount() {	
		return data.length;
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	/* public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
		int luku = 0;
		this.setValueAt(this.licenseName, luku, 0);
	}
	*/
	public Object getValueAt(int row, int column) {
		
		return data[row][column]; //returns the value from a given cell.
	}
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}
	/* public boolean isSelected() {
		boolean selected = false;
		for (int i=0; i<this.getRowCount();i++) {
			if (this.getValueAt(i, 1) == true) {
				return true;
			} else return false;
		} 
	} */	
	public Class getColumnClass(int columnIndex) {
		return data[0][columnIndex].getClass();
	}
	


}
