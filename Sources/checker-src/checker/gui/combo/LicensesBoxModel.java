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

package checker.gui.combo;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import checker.license.License;
import checker.license.LicenseDatabase;
import java.util.Iterator;

/**
 * Model for selecting license
 *  
 * @author Eero Kurkela
 * 
 */
public class LicensesBoxModel extends AbstractListModel implements ComboBoxModel {

	ArrayList<License> items;
	License selected;
	
	public LicensesBoxModel(boolean onlyUserMade) {
        /* fill items with licenses in the database. if onlyUserMade is true,
         * use only licenses added by user */
		items = new ArrayList<License>();
        LicenseDatabase ldb = new LicenseDatabase("licenses");
        ldb.buildLicenseDatabase();
        ArrayList<License> allLicenses = new ArrayList<License>(ldb.getLicenses());
        allLicenses.addAll(ldb.getForbiddenPhrases());
        License license;
        for(Iterator<License> iter = allLicenses.iterator(); iter.hasNext();) {
            license = iter.next();
            if(onlyUserMade) {
                if(license.isUserMade()) {
                    items.add(license);
                }
            }
            else {
                items.add(license);
            }
        }

        // select first item
        // handle empty list
        try {
        	selected = items.get(0);
        }
        catch(Exception e) {
        	
        }
	}

    public LicensesBoxModel() {
        this(false);
    }
	
	public void addItem(License c) {
		items.add(c);
	}
	
	public License getSelectedItem() {
		return selected;
	}

	public void setSelectedItem(Object l) {
		selected = (License)l;
	}

	public License getElementAt(int i) {
		return items.get(i);
	}

	public int getSize() {
		return items.size();
	}
}
