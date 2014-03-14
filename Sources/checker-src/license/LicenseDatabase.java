/**
 * Copyright (C) 2007 Xie Xiaolei
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General 
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package checker.license;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Arrays;
import java.io.*;

import checker.ErrorManager;
import checker.Log;
import checker.LogEntry;
import checker.Reference;
import checker.Pair;
import checker.localization.Locale;

/**
 * Container for all license information. Also handles loading of license data
 * files.
 */
public class LicenseDatabase {
	/* Localization */
	private Locale loc = new Locale();

    private HashSet<License> licenses;

    private HashSet<ForbiddenPhrase> forbiddenPhrases;

    private String licenseDirectory;
    File licenseDirectoryFile; //jpkheikk
    private File[] licenseFiles; //jpkheikk
    

    /**
     * 
     * @param licenseDirectory
     *            Directory where the license data files are located.
     */
    public LicenseDatabase(String licenseDirectory) {
        licenses = new HashSet<License>();
        forbiddenPhrases = new HashSet<ForbiddenPhrase>();
        this.licenseDirectory = licenseDirectory;
        licenseDirectoryFile = new File(licenseDirectory); //jpkheikk
        Log.log(LogEntry.VERBOSE, "licenseDirectory: " + licenseDirectory);        
    }

    /**
     *
     * @param value The String to be parsed. Items in the String are expected to be separated by commas or white spaces.
     *              White spaces around a comma are skipped.
     *       
     */
    private ArrayList<String> parseValue(String value) {
	int commaIndex;
	int whiteSpaceIndex;

	int lowerIndex;
	int higherIndex;
	ArrayList<String> items;

	int i, len;
	items = new ArrayList<String>();


        value = value.replace('\t', ' ');
	for (i = 0, len = value.length(); i < len;) {

	    commaIndex = value.indexOf(",", i);
	    whiteSpaceIndex = value.indexOf(" ", i);

	    if (commaIndex < whiteSpaceIndex) {
		lowerIndex = commaIndex;
		higherIndex = whiteSpaceIndex;
	    } else {
		lowerIndex = whiteSpaceIndex;
		higherIndex = commaIndex;
	    }

	    if (lowerIndex < 0 && higherIndex < 0) {
		items.add(value.substring(i));
		i = len;
		return items;

	    } else if (lowerIndex < 0) {
		items.add(value.substring(i, higherIndex));
		i = higherIndex;

	    } else {
		items.add(value.substring(i, lowerIndex));
		i = lowerIndex;
	    }

	    while ((i < len) && (value.charAt(i) == ',' || value.charAt(i) == ' ')) i++;
	}
	return items;
    }


    /**
     * Adds a new license to the database.
     * 
     * @param license
     */
    private void addLicense(License license) {
        if (!licenses.contains(license))
            licenses.add(license);
    }

    public License getItem(HashSet<? extends License> set, String itemId) {
	Iterator<? extends License> it;
	License license;

	for (it = set.iterator(); it.hasNext();) {
	    license = it.next();

	    if (license.getId().equals(itemId)) {
		return license;
	    }
	}
	return null;
    }

    public License getLicense(String licenseId) {
        return getItem(licenses, licenseId);
    }

    public License getModuleLicense(String ModuleLicenseId) {
        // TODO: This is hard coded due to late requirement. The kernel
        // MODULE_LICENSE abreviations should be stored in the lincense
        // database files.

        License license = null;
        
        Log.log(LogEntry.DEBUG, "Mod lic abreviation: " + ModuleLicenseId);                    
        if(ModuleLicenseId.equals("GPL")) {
            license = getItem(licenses, "gpl-2.0-s");
        }
        else if(ModuleLicenseId.equals("GPL v2"))
            license = getItem(licenses, "gpl-2.0-only-s");
        else if(ModuleLicenseId.equals("GPL and additional rights"))
            license = getItem(forbiddenPhrases, "gpl-2.0-LK-+-k");
        else if(ModuleLicenseId.equals("Dual BSD/GPL"))
        {
            license = getItem(forbiddenPhrases, "dual-bsd-gpl-k");
        }
        else if(ModuleLicenseId.equals("Dual MPL/GPL"))
        {
            license = getItem(forbiddenPhrases, "dual-mpl-gpl-k");
        }
        else
        {
            Log.log(LogEntry.DEBUG, loc.lc("Abreviation not found."));                    
            license = getItem(forbiddenPhrases, "module_license-f");
        }        
        
        return license;
    }

    public ForbiddenPhrase getForbiddenPhrase(String phraseId) {
        return (ForbiddenPhrase)getItem(forbiddenPhrases, phraseId);
    }

    public LicenseException getLicenseException(String exceptionId) {
        return (LicenseException)getItem(licenses, exceptionId);
    }

    /**
     * Gets the licenses in the database.
     * 
     * @return
     */
    public HashSet<License> getLicenses() {
        return licenses;
    }

    /**
     * Gets the list of forbidden phrases. (common to all licenses)
     * 
     * @return
     */
    public HashSet<ForbiddenPhrase> getForbiddenPhrases() {
        return forbiddenPhrases;
    }

    public HashSet<LicenseException> getLicenseExceptions() {
        HashSet<LicenseException> licenseExceptions = new HashSet<LicenseException>();
        Iterator<License> it;
        License license;

        for (it = licenses.iterator(); it.hasNext();) {
            if ((license = it.next()).getId().indexOf("-m-") >= 0) {
                licenseExceptions.add((LicenseException)license);
            }
        }
        return licenseExceptions;
    }

    private void linkSisterLicenses() {
        Iterator<License> iterator;
        String licenseId;
        License license;

        for (iterator = licenses.iterator(); iterator.hasNext();) {
            license = iterator.next();
            licenseId = license.getId();

            if (license.isTheLongerVersion()) {
            	license.setSisterLicense(null);
            } else {
                license.setSisterLicense(getLicense(licenseId.substring(0, licenseId.lastIndexOf("-s")) + "-l"));
            }
        }
    }

    private void linkExceptionsAndTheirParentLicenses() {
        Iterator<License> i, j;
        License license, anotherLicense;
        String str;
        int index;

        for (i = licenses.iterator(); i.hasNext();) {
            if ((index = (license = i.next()).getId().indexOf("-m-")) >= 0) {
                str = license.getId();
                for (j = licenses.iterator(); j.hasNext();) {
                    anotherLicense = j.next();
                    if (anotherLicense == license) {
                        continue;
                    }
                    if (anotherLicense.getId().indexOf(str.substring(0, index)) == 0) {
                        ((LicenseException)license).addParentLicense(anotherLicense);
                    }
                }
            }
        }
    }
    /**
     * @author jpkheikk
     * 
     * Returns the license names from the licenses file directory 
     * in a String table from which license names can be taken.
     * The table has only the .txt filenames without the ending ".txt".
     * 
     * @return licenseNames2[]
     */
    public String[] getLicenseNames() {
	    String str;    
	    int i, j=0, counter = 0;
	    licenseFiles = licenseDirectoryFile.listFiles();
	    String[] licenseNames = new String[licenseFiles.length];
	    // loop to put the licensefilenames to a licenseNames table for later use
	    // accepts only files that end with .txt leaves null values where other files exist
	    // TODO simplify ie. insert a second counter for this for loop so that null values are not written.
	    for (i = 0; i < licenseFiles.length; i++) {	    
		    if (licenseFiles[i].isDirectory() == true) continue;
	            if (!licenseFiles[i].getName().endsWith(".txt") == true) continue;
		    str = licenseFiles[i].getName();		    
		    str = str.substring(0, str.lastIndexOf("."));
		    //System.out.println("getLicenseNames kohta 1: " + str);
		    licenseNames[i] = str;
		    
	    }
	    // counting how many licenses there are
	    for (i = 0; i < licenseFiles.length; i++) {
		    if (licenseNames[i] != null) {
			    counter++;
		    }
	    }
	    // inserting the license names to new table which is of the right size
	    String[] licenseNames2 = new String[counter];
	    for (i = 0; i < licenseFiles.length; i++) {
		    if (licenseNames[i] != null) {
			    licenseNames2[j] = licenseNames[i];
			    j++;
		    }
	    }
	    //System.out.println("viiminen olio taulussa: " + licenseNames2[counter-1]);
	    return licenseNames2;
    }
    
    /**
     * Reads the license directory and creates a database of licenses.
     * 
     */
    public void buildLicenseDatabase() {
    	
    	Log.log(LogEntry.DEBUG, "buildLicenseDatabase");
    	
        // TODO Read the files, parse contents and create a License object for
        // each file.
        /**
         * Change for oslcv3 by jpkheikk (15.11.2008)
         * File licenseDirectoryFile = new File(licenseDirectory);
         * File[] licenseFiles;
         *       
	 */
    	
        int i;
        String str;
        String licenseID;
            
        if (!licenseDirectoryFile.isDirectory()) {
	    ErrorManager.error(loc.lc("License directory file not found"));
        }

        licenseFiles = licenseDirectoryFile.listFiles();
        Arrays.sort(licenseFiles);

        for (i = 0; i < licenseFiles.length; i++) {
            License license;

            if (licenseFiles[i].isDirectory()) continue;
            if (!(str = licenseFiles[i].getName()).endsWith(".meta")) continue;

            licenseID = str.substring(0,str.indexOf(".meta"));
            Log.log(LogEntry.DEBUG, loc.lc("Found license meta file") + ": "+licenseFiles[i]);
            
            if (licenseID.endsWith("-f")) { // A forbidden phrase
                license = new ForbiddenPhrase();
            } else if (licenseID.endsWith("-k")) { // kernel type license
                license = new ForbiddenPhrase(true); 
            } else if (licenseID.indexOf("-m-") >= 0) { // An exception
                license = new LicenseException();
            } else {
                license = new License();
            }
            license.setId(licenseID);
            ArrayList<String> text = new ArrayList<String>();

            /**
             * Now open the txt file containing the license text.
             */
            File licenceTextFile = new File(licenseDirectoryFile, licenseID + ".txt");
            
            Log.log(LogEntry.DEBUG, loc.lc("Loading license text file") + ":" + licenceTextFile);
            if (licenceTextFile.exists()) {
                try {
                    BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(licenceTextFile)));
                    while ((str = reader.readLine()) != null) {
                        text.add(str);
                    }
                    reader.close();
                    license.setLicenseText(text);
                } catch (Exception ex) {
		    ErrorManager.error(loc.lc("Error while opening license text file") + ":"+licenceTextFile,ex);
                }

                Log.log(LogEntry.DEBUG, loc.lc("Loaded license")+":" + license);

                if (license instanceof ForbiddenPhrase) {
                    forbiddenPhrases.add((ForbiddenPhrase)license);
                } else {
                    licenses.add(license);
                }
            } else {
            	ErrorManager.error(loc.lc("License text file")+":"+licenceTextFile+" "+loc.lc("doesn't exist"));
            }
        }

        linkSisterLicenses();

	/**
	 * Parse the meta files.
	 */
        for (i = 0; i < licenseFiles.length; i++) {
    	    if (licenseFiles[i].isDirectory()) continue;
            if (!licenseFiles[i].getName().endsWith(".meta")) continue;

            Properties properties = new Properties();
            Enumeration<?> propertyNames;
	        License license;

            str = licenseFiles[i].getName();
            str = str.substring(0, str.lastIndexOf("."));

            if (str.endsWith("-f") || str.endsWith("-k")) {
                license = getForbiddenPhrase(str);
            } else {
                license = getLicense(str);
            }

	        if (license == null) {
		        ErrorManager.error(str + loc.lc("is null while its meta exists."));
        		continue;
	        }

            try {
                properties.load(new FileInputStream(licenseFiles[i]));
            } catch (Exception ex) {
            	ErrorManager.error(loc.lc("Error while loading license file")+": "+licenseFiles[i],ex);
            }

    	    propertyNames = properties.propertyNames();

	        while(propertyNames.hasMoreElements()) {
                ArrayList<String> parsedValue;
        		Pair<Reference.ReferenceType, License> compatibleReference;

		        String propertyValue;
        		String propertyName = (String)propertyNames.nextElement();
		        int j, itemNumber;
        		License anotherLicense;

		        propertyValue = properties.getProperty(propertyName);
        		parsedValue = parseValue(propertyValue);

		        if (propertyName.equals("isCompatible")) {
        		    for (j = 0, itemNumber = parsedValue.size(); j < itemNumber; j++) {
    		        	anotherLicense = getLicense(parsedValue.get(j));

            			if(anotherLicense == null) {
                            anotherLicense = getForbiddenPhrase(parsedValue.get(j));
                        }

            			if (anotherLicense != null) {
                            compatibleReference = new Pair<Reference.ReferenceType, License>(null, anotherLicense);
                            license.addCompatibleReference(compatibleReference);
            			}
		            }
        		} else if(propertyName.equals("tags")) {
                    Tag tag;
                    ArrayList<Tag> tags = new ArrayList<Tag>();

                    for (j = 0, itemNumber = parsedValue.size(); j < itemNumber; j++) {
                        tag = new Tag(parsedValue.get(j), license);
                        tags.add(tag);
                    }

                    license.setTags(tags);
        	    } else if (propertyName.equals("fullName")) {
                    license.setFullName(propertyValue);
                }
            }
    	}

        // fill license compatibility matrix (matrix is symmetric)
        if (true) {
        	for (License license: licenses) {
        		for (Pair<Reference.ReferenceType,License> reference : license.getCompatibleReference()) {
        			License anotherLicense = reference.e2;
        			Reference.ReferenceType refType = reference.e1;
        			if (!anotherLicense.isCompatible(license, refType))
        				anotherLicense.addCompatibleReference(new Pair<Reference.ReferenceType,License>(refType,license));
        		}
        	}
            
        	for (License license: forbiddenPhrases) {
        		ArrayList<Pair<Reference.ReferenceType, License>> refs = license.getCompatibleReference();
        		if(refs != null) {
                    for (Pair<Reference.ReferenceType,License> reference : refs) {
                        License anotherLicense = reference.e2;
                        Reference.ReferenceType refType = reference.e1;
                        if (!anotherLicense.isCompatible(license, refType))
                            anotherLicense.addCompatibleReference(new Pair<Reference.ReferenceType,License>(refType,license));
                    }
        		}
        	}
        }
        
        linkExceptionsAndTheirParentLicenses();
        Log.log(LogEntry.VERBOSE, loc.lc("License database loaded"));
    }
}
