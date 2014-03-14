/**
 * 
 *   Copyright (C) 2006 Jussi Sirpoma
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
package checker.filepackage;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarInputStream;
import java.util.Iterator;

import checker.FileID;
import checker.filepackage.ZipIterator;


/**
 * Jar package type.
 */
public class JarFilePackage extends ZipFilePackage {

	public JarFilePackage(File pf) {
        super(pf);
	}

    public Iterator<PackageFile> iteratator()  throws Exception
    {
        return new ZipIterator(new JarInputStream(new FileInputStream(packageFile)));
    }
	
}
