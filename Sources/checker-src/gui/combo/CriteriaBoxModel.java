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

package checker.gui.combo;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import checker.gui.filter.Criteria;

/**
 * Model for filtering criteria
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class CriteriaBoxModel extends AbstractListModel implements ComboBoxModel {

	ArrayList<Criteria> criteria;
	Criteria selected;
	
	public CriteriaBoxModel() {
		criteria = new ArrayList<Criteria>();
		criteria.add(Criteria.ALL);
		selected = criteria.get(0);
	}
	
	public void addCriteria(Criteria c) {
		criteria.add(c);
	}
	
	public Criteria getSelectedItem() {
		return selected;
	}

	public void setSelectedItem(Object l) {
		selected = (Criteria)l;
	}

	public Criteria getElementAt(int i) {
		return criteria.get(i);
	}

	public int getSize() {
		return criteria.size();
	}
	
}
