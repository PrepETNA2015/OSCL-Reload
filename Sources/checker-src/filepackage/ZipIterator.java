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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;

import checker.FileID;
import checker.Log;
import checker.LogEntry;

class ZipPackageFile implements PackageFile
{
    FileID id;
    ArrayList<String> contents;
    
    ZipPackageFile(FileID i, ArrayList<String> cont)
    {
        id = i;
        contents = cont;
    }
    
    public FileID getFileID()
    {
        return id;
    }
    
	public ArrayList<String> getContents() throws Exception
    {
		return contents;
    }
}


class ZipIterator implements Iterator<PackageFile>
{
	ZipInputStream zis;
	ZipEntry curr;
    ZipEntry next;
    
	ZipIterator(ZipInputStream z)
	{
        if(z == null)
        {
            curr = null;
            next = null;
        }
        else
        {
            zis = z;
            curr = null;
            next = getNext();
        }
        
	}
	
	public boolean hasNext()
	{
		return next != null;
	}

    private ZipEntry getNext()
    {
        ZipEntry n = null;
        try
        {
        	do
        	{
        		n = zis.getNextEntry();
        	} while (n != null && n.isDirectory());
        }
        catch(Exception e)
        {}
        return n;
    }
    
    public PackageFile next()
	{
        if(next == null)
            throw new NoSuchElementException();

        curr = next;
        ArrayList<String> contents = getContents();

        String path = curr.getName();
        String name;
			
        int pathsplit = path.lastIndexOf('/');
        if(pathsplit == -1) {
            name = path;
            path = null;
        } else {
            name = path.substring(pathsplit + 1);
            path = path.substring(0, pathsplit);
            path = path.replace('/', File.separatorChar);
        }
            
        FileID id = new FileID(path, name);

        // Move forward in the zip file
        next = getNext();
        
		return new ZipPackageFile(id, contents);
	}

	private ArrayList<String> getContents()
    {
        ArrayList<String> rows = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
        try {
        	String row;
        	while ((row = reader.readLine()) != null) {
        		rows.add(row);
        	}
        } 
        catch(Exception e)
        {
            // The exception cannot be reported from next anyway so we will catch it here.
            Log.log(LogEntry.ERROR, "Error reading contents of : "+curr.getName());
        }
        return rows;
    }    
    
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
