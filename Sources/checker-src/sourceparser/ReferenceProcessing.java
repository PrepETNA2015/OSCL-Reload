/**
 * 
 *   Copyright (C) <2007> <Mika Rajanen>
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

package checker.sourceparser;

import java.io.File;

import checker.Reference;
import checker.localization.Locale;


/**
 * 
 * @Classname 	ReferenceProcessing.java	
 * @Version 	1.2
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */

/**
 * This class is for processing references.
 * Only on object of this class is needed for one parser.
 * 
 * Note: cpp and php parser use this class
 * 
 *            
 */

public class ReferenceProcessing {

    /* This stores given reference */
    private Reference reference;
    
    /* Localization */
    private Locale loc = new Locale();


    ReferenceProcessing(){

    }

    /**
     * This method is for getting reference 
     * This must be set before processPath()
     * 
     * @param Reference
     *            reference includes reference's type
     *            
     */

    protected void setReference(Reference reference){

	this.reference = reference;

    }

    /**
     * This method is for getting reference 
     * This must be called after processPath()
     * 
     * @return Reference
     *            reference object that includes reference's type
     *            
     */

    protected Reference getReference(){

	return reference;

    }

    /**
     * This method is for processing path in the given reference.
     * Method takes leading ../ strings away from parsedLine until
     * source file's path is in the root directory.
     * If there is ../ in the root, reference is se to unparsable
     * 
     * @param String
     *            parsedLine is a parsed reference without quotation
     *            or apostrophe marks
     * @param String
     *            sourcePath is a source file path (FileID.path)
     * 
     * @return String
     *            parsedLine is a processed reference
     *            
     */

    protected String processPath(String parsedLine, String sourcePath){

	/* Set path to OS independent form */

	/* Replace all slashes with file separator */
	parsedLine = parsedLine.replace('/', File.separatorChar);

	/* Replace all backslashes with file separator */
	parsedLine = parsedLine.replace('\\', File.separatorChar);

	if( parsedLine.startsWith(".." + File.separator) ){
	    /* Reference starts with ../ */

	    if(sourcePath==null){
		/* Source file path is empty */
		sourcePath = "";

		/* Check if there is starting ../ in reference */
		if( parsedLine.startsWith(".." + File.separator)){
		    /* There is more directories than in source path */
		    reference.referenceType = Reference
		    .ReferenceType.UNPARSABLE; 
		    reference.information= loc.lc("Reference points above root directory");

		    while( parsedLine.startsWith(".." + File.separator)){
			/* Remove rest of the ../ */ 
			parsedLine = parsedLine.
			substring( parsedLine.indexOf
				(File.separator)+1, 
				parsedLine.length() );

		    }
		    /* There shouldn't be any directories left after this */
		}

	    }

	    if( sourcePath.contains(File.separator) ){

		while( parsedLine.startsWith(".." + File.separator) 
			&& sourcePath.contains(File.separator)){

		    /* Take next starting  ../ away from parsed line */      				
		    parsedLine = parsedLine.
		    substring( parsedLine.indexOf
			    (File.separator)+1, 
			    parsedLine.length() );

		    /* Take away last directory from source path
		     * including trailing file separator */ 
		    sourcePath = sourcePath.substring( 0, sourcePath.
			    lastIndexOf(File.separator, 
				    sourcePath.length() ) );

		}

		if( sourcePath.contains(File.separator) ){

		    /*  Concatenate paths */
		    parsedLine = sourcePath + File.separator + parsedLine;
		    sourcePath = "";

		} 

	    } 

	    if( !sourcePath.contains(File.separator)
		    && (sourcePath.length() > 0) ){

		/* Source file path does not contain file separator
		 * and target starts with ../. Assume that there is only
		 * one directory in source file path but target may contain
		 * one or more ../.
		 */
		if( parsedLine.startsWith(".." + File.separator)){

		    /* There is one more ../ in the reference */
		    parsedLine = parsedLine.
		    substring( parsedLine.indexOf
			    (File.separator)+1, 
			    parsedLine.length() );
		    /* This is same level as in source root */

		} else{
		    /* Only one directory left in the source path,
		     * concatenate paths
		     */
		    parsedLine = sourcePath + File.separator + parsedLine;
		}

		if( parsedLine.startsWith(".." + File.separator)){
		    /* There is more directories than in source path */
		    reference.referenceType = Reference
		    .ReferenceType.UNPARSABLE; 
		    reference.information= loc.lc("Reference points above root directory");

		    while( parsedLine.startsWith(".." + File.separator)){
			/* Remove rest of the ../ */ 
			parsedLine = parsedLine.
			substring( parsedLine.indexOf
				(File.separator)+1, 
				parsedLine.length() );

		    }
		    /* There shouldn't be any directories left after this point */

		}			    

	    }      			    

	} else if( (sourcePath != null)  
		&& !parsedLine.startsWith(File.separator) ){

	    /* If there is source file path 
	     * and reference is not pointing to root */

	    /* Add root directory to the target file path */
	    parsedLine = sourcePath + File.separator 
	    + parsedLine;

	} else if( parsedLine.startsWith(File.separator) ){
	    /* Take the first file separator away 
	     * Assume this as a root. No source file path is needed
	     * */
	    parsedLine= parsedLine.
	    substring(parsedLine.indexOf(File.separator, 0)+1,
		    parsedLine.length() );

	} 

	return parsedLine;

    }

}
