/**
 * 
 *   Copyright (C) 2006 Sakari K��ri�inen, Lauri Koponen
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

package checker;

/**
 * Pair of objects.
 */
public class Pair<T1, T2> {

	public T1 e1;

	public T2 e2;

	public Pair(T1 e1, T2 e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	
	public boolean equals(Pair<T1, T2> o) {
		return o.e1.equals(e1) && o.e2.equals(e2);
	}

	// If this object is to be a key in hashtable, equals and hashCode methods
	// must be implemented
}
