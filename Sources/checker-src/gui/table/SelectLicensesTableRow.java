/**
 * 
 *   Copyright (C) <2009> <Eero Kurkela>
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

import checker.license.License;
import checker.localization.Locale;

/**
 * Select license row in a JTable
 *  
 * @author Eero Kurkela
 * 
 */
public class SelectLicensesTableRow {
	/* Localization */
	private Locale loc = new Locale();

    private License license;
    private String sreenName;
    private boolean selected;
	
	public SelectLicensesTableRow(License l, String n, boolean s) {
        this.license = l;
        this.sreenName = n;
        this.selected = s;
	}

    public License getLicense() {
        return license;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean s) {
        selected = s;
    }

    public String getSreenName() {
        return sreenName;
    }
}
