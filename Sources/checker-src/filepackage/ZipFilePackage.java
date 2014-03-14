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
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import checker.FileID;
import checker.filepackage.ZipIterator;


/**
 * Implementation of Zip package type.
 */
public class ZipFilePackage implements FilePackage {
	
    File packageFile;
    
	public ZipFilePackage(File pf) {
        packageFile = pf;
	}

	
	public ArrayList<String> getFileExtensions() {
		ArrayList<String> exts = new ArrayList<String>();
		exts.add(".zip");
		
		return exts;
	}

	public String getPackageTypeID() {
		return null;
	}

	public File getRootFile() {		
		return packageFile;
	}

    public Iterator<PackageFile> iterator()
    {
    	Iterator<PackageFile> it;
    	try
    	{ 
    		it = new ZipIterator(new ZipInputStream(new FileInputStream(packageFile)));
    	}
    	catch(Exception e)
    	{
    		it = new ZipIterator(null);
    	}
    
        return it;
    }

    public ArrayList<String> readFile(FileID file) throws Exception
    {
        Iterator<PackageFile> i = iterator();
        while(i.hasNext())
        {
            PackageFile pf = i.next();
            if(file.equals(pf.getFileID()))
            	return pf.getContents();
        }
        
        return new ArrayList<String>();
    }
}
