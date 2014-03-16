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
import java.util.Iterator;


import checker.Pair;
import checker.Reference;

/**
 * Information about single license.
 */
public class License implements Comparable {

    /**
     * License name.
     * 
     * (NOTE: I changed this to 'public' for testing, TBD: WRITE A GETTER. -Lauri
     */
    public String name;

    /**
     * Full name of the license.
     */
    private String fullName;

    /**
     * Unique identifier. (probably acronym of the name)
     */
    private String id;

    /**
     * List of key phrases that can be used in exact matching as alternative to
     * whole text matching.
     */
    private ArrayList<String> keyPhrases;

    /**
     * Free-form fields. (like name and year)
     */
    private ArrayList<Tag> tags;

    /**
     * List of forbidden phrases that are allowed in this license.
     */
    private ArrayList<ForbiddenPhrase> allowedForbiddenPhrases;

    /**
     * List of licenses that are compatible with this license when a single file contains multiple licenses.
     */
    private ArrayList<License> compatibleWithinFile;

    /**
     * List of licenses that are compatible with this license when the file is referenced.
     */
    private ArrayList<Pair<Reference.ReferenceType,License>> compatibleReference;

    /**
     * List of exceptions of this license.
     */
    private ArrayList<LicenseException> exceptions;

    /**
     * Text of the license (each element is a single line).
     * 
     * (NOTE: I changed this to 'public' for testing, TBD: WRITE A GETTER. -Lauri
     */
    private ArrayList<String> licenseText;

    private License sisterLicense;

    // TODO create get/set for all private variables

    public void setLicenseText(ArrayList<String> licenseText) {
        this.licenseText = licenseText;
    }

    public ArrayList<String> getLicenseText() {
	return licenseText;
    }

    /**
     * Returns true if this license and the argument license are
     * compatible.
     * @param referenceType Type of the reference between files (null if within single file)
     */
    public boolean isCompatible(License anotherLicense, Reference.ReferenceType referenceType) {
        Iterator<Pair<Reference.ReferenceType,License>> iterator;
        Pair<Reference.ReferenceType,License> pair;
        
        if(compatibleReference == null) return false;

        for (iterator = compatibleReference.iterator(); iterator.hasNext();) {
            pair = iterator.next();

            //referenceType will be taken into account later.
            if (pair.e2.equals(anotherLicense)) {
                return true;
            }
        }

        return false;
    }
    
    public String toString() {
    	return "id:"+id+"\tname:"+name;
    }

    public ArrayList<ForbiddenPhrase> getAllowedForbiddenPhrases() {
	return allowedForbiddenPhrases;
    }

    public ArrayList<Pair<Reference.ReferenceType, License>> getCompatibleReference() {
	return compatibleReference;
    }

    public ArrayList<License> getCompatibleWithinFile() {
	return compatibleWithinFile;
    }

    public ArrayList<LicenseException> getExceptions() {
	return exceptions;
    }

    public String getId() {
	return id;
    }

    public ArrayList<String> getKeyPhrases() {
	return keyPhrases;
    }

    public String getName() {
	return name;
    }

    public String getFullName() {
        return this.fullName;
    }

    public ArrayList<Tag> getTags() {
	return tags;
    }

    /* Returns human-interpretable name of the license
     *
     * ekurkela */
    public String getScreenName() {
        String name = "";

        // choose the name that is the easiest to read
        if(this.fullName != null) {
            name = this.fullName;
        }
        else if(this.name != null) {
            name = this.name;
        }
        else {
            name = this.id;
        }

        // distinguish user-added licenses
        if(this.isUserMade()) {
            name += " (USER)";
        }

        return name;
    }
    /* end */

    public boolean isTheLongerVersion() {
        return !id.endsWith("-s");
    }

    /* ekurkela */
    // different from isTheLongerVersion: specifically requires ending "-l"
    public boolean isLongVersion() {
        return id.endsWith("-l");
    }

    public boolean isShortVersion() {
        return id.endsWith("-s");
    }

    public boolean isForbiddenPhrase() {
        return id.endsWith("-f");
    }

    public boolean isUserMade() {
        return id.startsWith("user_");
    }
    /* end */

    public int compareTo(Object license) {
	if (!(license instanceof License)) {
	    return this.id.compareTo(license.toString());
	}
	return this.id.compareTo(((License)license).id);
    }

    public License getSisterLicense() {
        return sisterLicense;
    }

    protected void setAllowedForbiddenPhrases(
					      ArrayList<ForbiddenPhrase> allowedForbiddenPhrases) {
	    this.allowedForbiddenPhrases = allowedForbiddenPhrases;
    }

    protected void setCompatibleReference(
					  ArrayList<Pair<Reference.ReferenceType, License>> compatibleReference) {
    	this.compatibleReference = compatibleReference;
    }

    protected void setCompatibleWithinFile(ArrayList<License> compatibleWithinFile) {
	    this.compatibleWithinFile = compatibleWithinFile;
    }

    protected void setExceptions(ArrayList<LicenseException> exceptions) {
    	this.exceptions = exceptions;
    }

    protected void setId(String id) {
	    this.id = id;
    }

    protected void setKeyPhrases(ArrayList<String> keyPhrases) {
    	this.keyPhrases = keyPhrases;
    }

    protected void setName(String name) {
	    this.name = name;
    }

    protected void setFullName(String fullName) {
        this.fullName = fullName;
    }

    protected void setTags(ArrayList<Tag> tags) {
    	this.tags = tags;
    }

    protected void setSisterLicense(License sisterLicense) {
        this.sisterLicense = sisterLicense;
    }

    void addCompatibleReference(Pair<Reference.ReferenceType, License> pair) {
        if (compatibleReference == null) {
            compatibleReference = new ArrayList<Pair<Reference.ReferenceType, License>>();
        }
	compatibleReference.add(pair);
    }
}
