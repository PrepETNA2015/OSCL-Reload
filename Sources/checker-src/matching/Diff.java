/**
 * 
 *   Copyright (C) 2007 Lauri Koponen
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

package checker.matching;

import java.lang.*;
import java.util.*;

/**
 * Produces a diff of two texts.
 * <p>
 * P. Heckel. A technique for Isolating Differences Between Files.<br>
 * _Communications of the ACM_ 21(4), April 1978, pp. 264-268.
 * 
 * @author Lauri Koponen
 */
public class Diff {

	/**
	 * Entry for diff symbol table.
	 */
	class SymbolTableEntry {

		/** number of copies of this symbol in "old" data */
		public int oc;

		/** number of copies of this symbol in "new" data */
		public int nc;

		/** line number in "old" data */
		public int olno;

		/** line number in "new" data */
		public int nwno;
	};

	/**
	 * Symbol table that contains an entry for each word.
	 */
	Dictionary<String, SymbolTableEntry> symbolTable;

	/**
	 * Original old text (template)
	 */
	String[] O;

	/**
	 * Original new text (text to be matched)
	 */
	String[] N;

	/**
	 * OA[i] contains information attached to O[i], either a SymbolTableEntry or
	 * an Integer index to N[].
	 */
	Object[] OA;

	/**
	 * NA[i] contains information attached to N[i], either a SymbolTableEntry or
	 * an Integer index to O[].
	 */
	Object[] NA;
	
	/**
	 * foundNoneUnique is true if and only if a word was found for
	 * which oc=1 and nc>1 AND no words were found for which oc=1 and nc=1.    
	 */
	boolean foundNoneUnique;
	
	/**
	 * Class constructor that produces the diff immediately
	 * 
	 * @param O
	 *            old text (template)
	 * @param N
	 *            new text (text to be matched)
	 */
	Diff(String[] O, String[] N) {
		diff(O, N);
	}

	/**
	 * Produce a diff of two texts. This function creates the symbol table and
	 * fills the arrays OA and NA and creates the symbol table.
	 * 
	 * @param O
	 *            old text (template)
	 * @param N
	 *            new text (text to be matched)
	 */
	private void diff(String[] O, String[] N) {
		int i;

		this.O = O;
		this.N = N;
		symbolTable = new Hashtable<String, SymbolTableEntry>();
		OA = new Object[O.length];
		NA = new Object[N.length];

		/* Pass 1: create symbols for N */

		for (i = 0; i < N.length; i++) {

			/* Create a new symbolTable entry if one doesn't exist */
			SymbolTableEntry symbol = (SymbolTableEntry) symbolTable.get(N[i]);
			if (symbol == null) {
				symbol = new SymbolTableEntry();
				symbolTable.put(N[i], symbol);
				symbol.nwno = i;
			}

			symbol.nc++;
			NA[i] = symbol;
		}

		/* Pass 2: Create symbols for O */

		for (i = 0; i < O.length; i++) {

			/* Create a new symbolTable entry if one doesn't exist */
			SymbolTableEntry symbol = (SymbolTableEntry) symbolTable.get(O[i]);
			if (symbol == null) {
				symbol = new SymbolTableEntry();
				symbolTable.put(O[i], symbol);
			}

			symbol.oc++;
			symbol.olno = i;
			OA[i] = symbol;
		}

		/* Pass 3: Match unique lines */

		/*
		 * we could get rid of symbol.nwno by scanning through NA instead of
		 * symbolTable. 
		 */
		
		foundNoneUnique = false;
		boolean foundUnique = false;

		for (Enumeration<SymbolTableEntry> e = symbolTable.elements(); e
				.hasMoreElements();) {
			SymbolTableEntry symbol = e.nextElement();

			if ((symbol.nc == 1) && (symbol.oc == 1)) {
				OA[symbol.olno] = new Integer(symbol.nwno);
				NA[symbol.nwno] = new Integer(symbol.olno);
				foundUnique = true;
			} else if ((symbol.nc > 1) && (symbol.oc == 1)) {
				foundNoneUnique = true;
			}
		}
		if(foundUnique) foundNoneUnique = false;

		/* Pass 4: Extend common lines forwards */

		for (i = 0; i < N.length - 1; i++) {
			Object naEntry = NA[i];

			/* If an index to O */
			if (naEntry instanceof Integer) {
				int j = ((Integer) naEntry).intValue();

				/* Must not exceed array bounds */
				if (j + 1 >= O.length) {
					break;
				}

				/*
				 * Are the symbols the same? same integer cannot be in both
				 * arrays.
				 */
				if (NA[i + 1] == OA[j + 1]) {
					OA[j + 1] = new Integer(i + 1);
					NA[i + 1] = new Integer(j + 1);
				}
			}
		}

		/* Pass 5: Extend common lines backwards */

		for (i = N.length - 1; i >= 1; i--) {
			Object naEntry = NA[i];

			/* If an index to O */
			if (naEntry instanceof Integer) {
				int j = ((Integer) naEntry).intValue();

				/* Must not exceed array bounds */
				if (j - 1 < 0) {
					break;
				}

				/*
				 * Are the symbols the same? same integer cannot be in both
				 * arrays.
				 */
				if (NA[i - 1] == OA[j - 1]) {
					OA[j - 1] = new Integer(i - 1);
					NA[i - 1] = new Integer(j - 1);
				}
			}
		}
	}
}
