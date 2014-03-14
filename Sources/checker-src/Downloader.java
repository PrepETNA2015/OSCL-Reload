/**
 * 
 *   Copyright (C) 2009 Mikko Kupsu
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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class Downloader {
    public boolean unzip() {
        File file = new File("temp.zip");
        File folder = new File("licenses");
        if (!folder.exists())
            folder.mkdir();
        if (file.exists()) {
            //System.err.println("Found");
            try {
                ZipFile zip = new ZipFile(file);
                Enumeration entries = zip.entries();
                while(entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry)entries.nextElement();
                    if(entry.isDirectory()) {
                        //System.err.println("Folder found");
                    }
                    else {
                        //System.err.println("Extracting file: " + entry.getName());

                        InputStream in = zip.getInputStream(entry);
                        OutputStream out =  new BufferedOutputStream(new FileOutputStream(folder.getAbsolutePath()+"/"+entry.getName()));

                        byte[] buffer = new byte[1024];
                        int len;

                        while((len = in.read(buffer)) >= 0)
                            out.write(buffer, 0, len);
                        out.flush();
                        in.close();
                        out.close();
                    }
                }
                zip.close();
            } catch (IOException ioe) {
                //System.err.println("Unhandled exception:"+ioe.toString());
            }
        }
        else {
            //System.err.println("Not found");
            return false;
        }
        remove("temp.zip");
        return true;
    }

    public void download_file(String location) {
        // remove any previous file
        remove("temp.zip");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            URL url = new URL( location );
            URLConnection urlc = url.openConnection();

            bis = new BufferedInputStream( urlc.getInputStream() );
            bos = new BufferedOutputStream( new FileOutputStream("temp.zip") );

            int i;
            while ((i = bis.read()) != -1)
                bos.write( i );

        }
        catch (IOException e) {
            //System.err.println(e.toString());
        }
        finally
        {
            if (bis != null)
                try
            {
                    bis.close();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            if (bos != null)
                try
            {
                    bos.close();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    public void pack_database() {
        int BUFFER = 1024;
        File inFolder = new File("licenses");
        File outFile = new File("database.zip");
        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
            BufferedInputStream in = null;
            byte[] data    = new byte[BUFFER];
            String files[] = inFolder.list();

            for (int i=0; i<files.length; i++) {
                //System.out.println("Adding: " + files[i]);
                in = new BufferedInputStream(new FileInputStream(inFolder.getPath() + "/" + files[i]), BUFFER);
                out.putNextEntry(new ZipEntry(files[i]));
                int count;
                while((count = in.read(data,0,BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                out.closeEntry();
            }
            out.flush();
            out.close();
            in.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void remove(String file) {
        File rem = new File(file);
        rem.delete();
    }
}
