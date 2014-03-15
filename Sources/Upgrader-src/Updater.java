/*
 * 
 * Copyright (C) 2014 FLOREA Gheorghe,LE MOIGNE Adrien,WIESER Frank
 * 
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package updater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
/**
 *
 * @author floreag
 */
public class Updater {
    private final static String new_verURL = "http://oslc.bravesites.com/version";
    
    public static String getLatestVersion() throws Exception
    {
        String data = getData("file://"+new File(".").getAbsolutePath()+"/version.html");
        
        return data.substring(data.indexOf("[version]")+9,data.indexOf("[/version]"));
    }
    public static String getWhatsNew() throws Exception
    {
        String data = getData(new_verURL);
        return data.substring(data.indexOf("[version]")+9,data.indexOf("[/version]"));
    }
    private static String getData(String address)throws Exception
    {
        URL url = new URL(address);
        
        InputStream html = null;

        html = url.openStream();
        
        int c = 0;
        StringBuffer buffer = new StringBuffer("");

        while(c != -1) {
            c = html.read();
            
        buffer.append((char)c);
        }
        html.close();
        return buffer.toString();
    }
}
