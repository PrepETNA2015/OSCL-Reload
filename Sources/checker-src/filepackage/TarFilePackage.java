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
import java.util.zip.GZIPInputStream;
import java.util.Iterator;
import java.util.ArrayList;

import checker.FileID;
import com.ice.tar.TarInputStream;

/**
 * Tar package type.
 */
public class TarFilePackage implements FilePackage {

    File packageFile;

    TarFilePackage(File pf)
    {
        packageFile = pf;

    }
    
    
	public ArrayList<String> getFileExtensions()
    {
		ArrayList<String> exts = new ArrayList<String>();
		exts.add(".tar");
		exts.add(".tar.gz");
		exts.add(".tgz");
		return exts;
    }
    
	public String getPackageTypeID()
    {
        return "";
    }
    
	public File getRootFile()
    {
        return packageFile;
    }

    public Iterator<PackageFile> iterator()
    {
    	Iterator<PackageFile> it;
    	try
    	{
            if(packageFile.getName().toUpperCase().endsWith(".TAR.GZ") ||
               packageFile.getName().toUpperCase().endsWith(".TGZ"))
            {
                it = new TarIterator(new TarInputStream(new GZIPInputStream(new FileInputStream(packageFile))));
            }
            else
            {
                it = new TarIterator(new TarInputStream(new FileInputStream(packageFile)));
            }
            
    	}
    	catch(Exception e)
    	{
    		it = new TarIterator(null);
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

