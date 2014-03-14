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

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Stack;
import java.util.NoSuchElementException;

import checker.FileID;
import checker.Log;
import checker.LogEntry;

class FSPackageFile implements PackageFile
{
    FileID id;
    FSIterator iterator; // for getting the contents if necessary
    
	public FSPackageFile(FileID id2, FSIterator iterator) {
		id = id2;
		this.iterator = iterator;
	}

	public FileID getFileID()
    {
        return id;
    }
    
	public ArrayList<String> getContents() throws Exception
    {
		return iterator.getContents(id);
    }
}

class FSIterator implements Iterator<PackageFile>
{
    private File curr;
    private File next;
	private String rootDir;

    private Stack<File> dirs;
    private Stack<File> files;

    FSIterator(File root)
    {
        Log.log(LogEntry.DEBUG, "Starting FSIterator from: "+root.getName());
        files = new Stack<File>();
        dirs = new Stack<File>();
        
		if(root.isDirectory())
			rootDir = root.getPath();
		else
			rootDir = root.getParent();
		
		if(rootDir == null)
            rootDir = "";

        if(root.isDirectory())
            dirs.push(root);
        else
            files.push(root);

        curr = null;
        next = getNext();
    }
    
	public boolean hasNext()
	{
		return next != null;
	}

    private File getNext()
    {
        do {
            if(files.empty())
            {
                if(!dirs.empty())
                {
                    File d = dirs.pop();
                    
                    File[] flist = d.listFiles();
                    for(File f : flist) {
                        if(f.isDirectory())
                            dirs.push(f);
                        else
                            files.push(f);
                    }
                }
            }
        }
        while(files.empty() && !dirs.empty());

        if(!files.empty())
            return files.pop();
        else
            return null;
        
    }
    
    public PackageFile next()
	{
        if(next == null)
            throw new NoSuchElementException();

        curr = next;
        next = getNext();

        Log.log(LogEntry.DEBUG, "Next file: "+curr.getPath());
        String p = curr.getParent();
        if(p != null) {
            if(p.startsWith(rootDir))
                p = p.substring(rootDir.length());

            while(p.startsWith(File.separator))
                p = p.substring(1);
			
            if(p.length() == 0)
                p = null;
        }

        FileID id = new FileID(p, curr.getName());
        return new FSPackageFile(id, this);
    }
    
        
	protected ArrayList<String> getContents(FileID file) throws Exception
    {
        ArrayList<String> rows = new ArrayList<String>();
        try {
        	String fname = file.toString();
        	if((rootDir != null) && (rootDir.length() > 0)) {
        		fname = rootDir + File.separator + fname;
        	}
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            String row;
            while ((row = reader.readLine()) != null) {
                rows.add(row);
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }
    
    
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}



/**
 * Normal file system package type.
 */
public class FileSystemFilePackage implements
		checker.filepackage.FilePackage {

	// This class currently supports only reading single file!
		
	private File packageFile;
	
	/**
	 * Constructor.
	 * @param packageFile File or directory that will be scanned 
	 */
	public FileSystemFilePackage(File packageFile) {
		this.packageFile = packageFile;
	}

	public ArrayList<String> getFileExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPackageTypeID() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getRootFile() {
		return packageFile;
	}

    public Iterator<PackageFile> iterator()
    {
        return new FSIterator(packageFile);
    }
	    
    public ArrayList<String> readFile(FileID file) throws Exception
    {
    	// FSIterator.getContents can get any file's data. This is a
    	// bit unsafe but fast.
    	FSIterator i = new FSIterator(packageFile);
        return i.getContents(file);
    }
}
