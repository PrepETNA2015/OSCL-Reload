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

import java.util.ArrayList;
import java.util.Collection;

import checker.FileID;
import checker.gui.filter.Criteria;
import checker.gui.filter.Criterion;
import checker.gui.filter.LicenseCriterion;
import checker.gui.filter.MatchCriterion;
import checker.license.License;
import checker.matching.LicenseMatch;

/**
 * This class represents a license file in a tree.
 * 
 * @author Veli-Jussi Raitila
 *
 */
public class FileLicense extends FileAbstract {
	
	// Matches contained in this license file
	private ArrayList<LicenseMatch> matches;

	public FileLicense(FileID f, ArrayList<LicenseMatch> l) {
		super(f);
		matches = l;
	}

	@Override
	public ArrayList<LicenseMatch> getMatches() {
		return matches;
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Collection<? extends LicenseTreeNode> getChildren() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public boolean isViewable() {
		return true;
	}

	@Override
	public boolean hasConflict() {
		return false;
	}

	@Override
	public boolean hasLicense() {
		return !matches.isEmpty();
	}

	@Override
	public boolean missLicense() {
		return matches.isEmpty();
	}

	@Override
	public boolean meetsCriteria(Criteria c) {
		Criterion con = c.getCriterion(); 
		
		if (con instanceof LicenseCriterion) {
			for (LicenseMatch match : matches) {
				if (match.getLicense() == (License)con.getValue()) return true; 
			}
		} else if(con instanceof MatchCriterion) {
			for (LicenseMatch match : matches) {
				if (match.getMatchPr() <= (Float)con.getValue()) return true; 
			}
		}
		
		return false;
	}

}
