/**
 * 
 *   Copyright (C) <2008> <Mikko Kupsu>
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

package checker.localization;

import java.io.FileReader;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Locale.java.
 * 
 * "Dictionary" which is used to use different languages with OSLC.
 */

public class Locale {
	/* Dictionary for the words */
	private HashMap<String, String> words = new HashMap<String, String>();

	/**
	 * Constructor.
	 * 
	 * Reads the language configuration file and uses language specified there.
	 * 
	 * @param char which is unique to every class
	 */
	public Locale() {
		String file = "";
		try {
			FileReader input = new FileReader("conf.txt");
			BufferedReader bufRead = new BufferedReader(input);
	        	file = bufRead.readLine();
			File tmp = new File(file);
			bufRead.close();
			if (!tmp.exists())
				throw new IOException();
		}
		catch (IOException e) {
			//System.out.println(e.toString());
			file = "languages/language-english.txt";
		}
		
		readLocale(file);
	}
	
	/**
	 * Returns the value which is tied to the key.
	 * 
	 * Not fool proof!
	 * 
	 * @param key
	 * @return value
	 */
	public String lc(String key) {
		String value = words.get(key);
		if(value == null) {
			//System.out.println(key);
			return key;
		}
		else 
			return value; 
	}
	
	/**
	 * Reads language file and saves to the HashMap the required Strings.
	 * 
	 * If reading fails there is no backup.
	 * 
	 * @param Path to the language file
	 * @param Unique char of class requested the reading
	 */
	private void readLocale(String pathToFile) {
		try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream (pathToFile), Charset.forName("UTF-8"))); 

			String temp;
			while ((temp = br.readLine()) != null) {
				int location = temp.indexOf(":");
				if (location != -1) {
					// Parses the String if it's not comment
					
					String begin = temp.substring(0, location);
					String end = temp.substring(location + 1, temp.length());
                    if (end.equals(""))
                        end = begin;
					words.put(begin, end);
				}
			}
		} catch (IOException e) {
			
		}
	}
}
