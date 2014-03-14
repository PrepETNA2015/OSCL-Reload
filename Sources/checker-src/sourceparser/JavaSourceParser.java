/**
 * 
 *   Copyright (C) <2006> <Mika Rajanen>
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

import java.util.ArrayList;
import checker.FileID;
import checker.CommentLine;
import checker.Reference;
import java.io.File;
import checker.localization.Locale;


/**
 * @Classname 	JavaSourceParser.java	
 * @Version 	1.47 
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */

/**
 * JavaSourceFileParser takes java source file content and parses it for
 * comments and references.
 * 
 */
public class JavaSourceParser implements SourceParser {

    /* Start of the comment line */
    private final static String COMMENT_LINE = "//"; 

    /* Start of the block comment */
    private final static String COMMENT_BEGIN = "/*";  

    /* Length of the comment marks in characters*/
    private final static int COMMENT_LEN = 2; 

    /* Name for reference searching */
    private String parseWord = "import ";

    /* Array list for extracted references, one variable per object */
    private ArrayList<Reference> referenceArray; 

    /* ParserComments object for comment operations */		
    private ParserComments pComments = null;

    /* Package name, unNamed if none found */
    private String packageName;

    /* Package root name, unNamed if none found */
    private String packageRootName;
    
    /* Localization */
    private Locale loc = new Locale();


    public JavaSourceParser() {

	/* Construct JavaSourseParser with reference array
	 * This array stores all references 
	 */
	referenceArray = new ArrayList<Reference>();
    }

    /**
     * getComments returns the source file's comments.
     */

    public ArrayList<CommentLine> getComments()  {

	/* Array list for extracted comments */
	ArrayList<CommentLine> commentArray; 

	commentArray = pComments.getComments();
	return commentArray;
    }

    /**
     * getReferences return the detected references.
     */

    public ArrayList<Reference> getReferences() {
	return referenceArray;

    }

    /**
     * This private method is for writing reference to arraylist 
     * 
     * @param FileID
     * 		sourceFile is source code file object
     * 
     * 
     * @param SourceParserLine
     *            pLine is code line object that includes line and positions
     *            
     */

    private void writeToReferenceArray(FileID sourceFile, SourceParserLine pLine) {

	String parsedLine; 				// String for parsed line
	Reference reference;				// Reference info
	FileID targetFile;				// Target of the reference 
	File tFile;					// Temporary File object

	try {
	    /* Initializing variables */
	    targetFile = new FileID(null, null);

	    /* Set Reference from this source file to target file */
	    reference = new Reference(sourceFile, targetFile);

	    /* Check input */
	    if (pLine.startPosition < pLine.charPosition) {

		parsedLine = pLine.line.substring(pLine.startPosition, pLine.charPosition);
		/* Remove leading and trailing spaces */
		parsedLine = parsedLine.trim();
		/* Write reference to the output if parsed substring is not empty */
		if (parsedLine.length() > 0) {

		    /* Set import declaration to the reference */
		    reference.declaration = parsedLine;

		    /* Set reference type */
		    if(parsedLine.startsWith(packageRootName)){

			/* Import found inside the same package */
			reference.referenceType = Reference.ReferenceType.IMPORT;

		    }else if( parsedLine.startsWith("java.") 
			    || parsedLine.startsWith("javax.") ){
			/* Import is java standard package */
			reference.referenceType = Reference.ReferenceType.IMPORT_STANDARD_LIBRARY;
			reference.information= loc.lc("Reference to the java standard library") + " (" 
			    + parsedLine + ")";

		    } else {
			/* Import is not under same package root, set default value */
			reference.referenceType = Reference.ReferenceType.UNPARSABLE;

		    }

		    /* Process any other import but java standard imports */
		    if(reference.referenceType != Reference.ReferenceType.IMPORT_STANDARD_LIBRARY){

			/* Replace all dots with slash */
			parsedLine = parsedLine.replace('.', File.separatorChar);
			/* Assume that all reference files are java files */
			parsedLine = parsedLine + ".java";	

			/* Find proper path for the import file(s) */

			/* If there is source file path and package declaration */
			if( (sourceFile.path != null) && !packageName.equals("unNamed") ){

			    /* Try to find package declaration from the source file path */
			    int packageStartIndex = sourceFile.path.indexOf(packageName, 0);

			    /* packageStartIndex is -1 if packageName not found from source path */
			    if(packageStartIndex >= 0){

				/* Add root folder to the target file path*/
				parsedLine = sourceFile.path.substring(0, packageStartIndex ) 
				+ parsedLine;
				reference.referenceType = Reference.ReferenceType.IMPORT;


			    } else if (packageStartIndex == -1){
				/* Package not found from the path */
				reference.information= loc.lc("Package declaration don't match with source file path");
				/* Set reference type UNPARSABLE */
				reference.referenceType = Reference.ReferenceType.UNPARSABLE;
			    }

			} 
			/* If there is source file path and no package declaration  */
			if( (sourceFile.path != null) && (packageName.equals("unNamed"))  ){
			    /* No package declaration found, 
			     * add root directory to the target file path. Assume that 
			     * reference is below this source file's directory */
			    parsedLine = sourceFile.path + File.separator + parsedLine;
			    reference.referenceType = Reference.ReferenceType.IMPORT;
			    /* Package not found from the path */
			    reference.information= loc.lc("Package declaration missing") ;
			}
			/* If there is no source file path and no package declaration */
			if( (sourceFile.path == null) && (packageName.equals("unNamed")) ){
			    /* No package declaration 
			     * or source path found. Assume that this file is in the 
			     * root directory and reference is below this directory */
			    reference.referenceType = Reference.ReferenceType.IMPORT;
			    reference.information= loc.lc("Package declaration missing") ;

			}	
			/* If there is no source file path and package declaration */
			if( (sourceFile.path == null) && (!packageName.equals("unNamed")) ){
			    /* Package declaration found 
			     * and this file is in the root directory */
			    reference.information= loc.lc("Package declaration don't match with source file path");
			    /* Set reference type UNPARSABLE */
			    reference.referenceType = Reference.ReferenceType.UNPARSABLE;
			}

			/* Path processing ready.
			 * Create File object for easier path separation. 
			 * Path would be OS independent
			 */
			tFile = new File(parsedLine);

			/* Get path and file name from File object for the reference */
			reference.targetFile.path = tFile.getParent();
			reference.targetFile.name = tFile.getName();

		    }

		    /* Add reference object to the array */
		    referenceArray.add(reference);

		} else { /* Parsed pLine.line was too short - ambigious case */

		    /* Set reference type UNPARSABLE */
		    reference.referenceType = Reference.ReferenceType.UNPARSABLE;
		    reference.information= loc.lc("Empty import declaration");
		}
	    }
	} catch (RuntimeException e) {
	    throw e;
	}

    }

    /**
     * This private method is for searching reference from the line 
     * 
     * @param FileID
     * 		sourceFile is source code file object
     * 
     * @param SourceParserLine
     *            pLine is code line object that includes line and positions
     *            
     * @return SourceParserLine
     * 		  pLine with new values
     *            
     */

    private SourceParserLine parseJavaRefenceFromLine(SourceParserLine pLine, 
	    FileID sourceFile)throws Exception{

	/* Last substring of the line for comparing */
	String lastString; 

	/* Reference end mark */
	String referenceEndMark = ";";

	if ( pLine.charPosition 
		<= ( pLine.line.length() - parseWord.length() ) ) {

	    try{
		lastString = pLine.line.substring(pLine.charPosition,
			(pLine.charPosition + parseWord.length() ));

		/* Compare substring */
		if (lastString.equals(parseWord)) {
		    /* Reference found 
		     * set starting point*/

		    /* Check for import static */
		    if( (pLine.line.indexOf(" static ", pLine.charPosition)
			    > -1 )
			    && ( pLine.line.indexOf(" static ", pLine.charPosition)
				    < pLine.line.indexOf(";", pLine.charPosition) ) ){

			/* There is "import static foo;" declaration, exclude static */
			pLine.startPosition = pLine.charPosition + 7;
			pLine.charPosition = pLine.startPosition - 1;

		    } else{ /* Regular case without static */

			pLine.startPosition = pLine.charPosition + parseWord.length();
		    }

		    /* Find reference end mark */
		    if( pLine.line.indexOf(referenceEndMark, pLine.startPosition)
			    >= pLine.startPosition){

			pLine.charPosition = pLine.line.indexOf(referenceEndMark, 
				pLine.startPosition);

			/* Write reference to the array */
			writeToReferenceArray(sourceFile, pLine);

		    } 

		}          	    

	    } catch (RuntimeException e) {
		throw e;
	    } 

	}

	/* Return pLine with new values */	
	return pLine;
    }

    /**
     * This private method is for extracting package declaration 
     * 
     * 
     * @param SourceParserLine
     *            pLine is code line object that includes line and positions
     *            
     * @return SourceParserLine
     * 		  pLine with new values
     *            
     */

    private SourceParserLine scanPackageFromLine(SourceParserLine pLine){

	/* Last substring of the line for comparing */
	String lastString; 

	lastString = pLine.line.substring(pLine.charPosition,
		(pLine.charPosition + 8));

	/* Compare substring */
	if (lastString.equals("package ")) {
	    /* Package found */

	    pLine.startPosition = pLine.charPosition + 8;
	    if( (pLine.line.indexOf(";", pLine.startPosition) ) 
		    >= pLine.startPosition ){

		pLine.charPosition = pLine.line.indexOf(";", 
			pLine.startPosition);

		if( packageName.equals("unNamed") ){
		    /* Note: Only one package declaration is allowed */
		    /* Write package name */
		    packageName = pLine.line.substring(pLine.startPosition, 
			    pLine.charPosition);
		    packageName = packageName.trim();

		    if( packageName.contains(".") ){
			/* There is path before package 
			 * Search leftmost dot to separate root path*/
			packageRootName = packageName.substring
			(0, packageName.indexOf(".", 0) );

			/* Replace all dots with slash in the package path*/
			packageName = packageName.replace('.', 
				File.separatorChar);
		    }					

		}

	    }    					

	}

	/* Return pLine with new values */	
	return pLine;

    }

    /**
     * scanFile extracts comments and references from given source file.
     * 
     * See SourceParser interface
     * 
     */

    public void scanFile(FileID sourceFile, ArrayList<String> fileContent) throws Exception{

	/* Initialize new ParserComments object */
	pComments = new ParserComments();

	/* Package name, unNamed if none found */
	packageName = "unNamed";

	/* Package root name, unNamed if none found */
	packageRootName = "unNamed";

	/* Create object containing line information */
	SourceParserLine pLine;

	pLine = new SourceParserLine();


	try {
	    /* Read one object (source code pLine.line) at the time */
	    for (int objectNumber = 0; objectNumber < fileContent.size(); objectNumber++) {
		/* Get source code line from array */
		pLine.line = fileContent.get(objectNumber);

		if( ( ( ( pLine.line.contains(COMMENT_BEGIN) 
			|| pLine.line.contains(COMMENT_LINE) )
			|| pLine.commentSection ) 
			|| pLine.line.contains(parseWord) )
			|| pLine.line.contains("package ") ){

		    /* Line contains comments or references, parse source code line */

		    for (pLine.charPosition = 0; 
		    pLine.charPosition < (pLine.line.length()); pLine.charPosition++) {

			/* Search quotation and apostrophe marks.
			 * If either is found, section is set to true
			 * and no comments or references are parsed
			 */
			pLine = pComments.findVariableSection(pLine);

			if ( ( (  pLine.commentSection 
				&& !pLine.quotationSection) 
				&& !pLine.apostropheSection)
				&& (pLine.charPosition < pLine.line.length() ) ) {

			    /* Scan comment section */
			    pLine = pComments.scanCommentSection(pLine);
			}


			/* Nothing found in this line, try to find comment marks */
			if ( ( (!pLine.commentSection 
				&& !pLine.quotationSection) 
				&& !pLine.apostropheSection)
				&& (pLine.charPosition <= (pLine.line.length() - COMMENT_LEN))) {

			    /* Scan line for comments */
			    pLine = pComments.scanLine( pLine );

			    /* Try to find given refences */
			    if(pLine.line.contains(parseWord) && !pLine.commentSection ){

				/* Parse and write references from the line */
				pLine = parseJavaRefenceFromLine(pLine, sourceFile);

			    }
			    /* Try to find package declaration  */
			    if(pLine.line.contains("package ") && packageName.equals("unNamed") ){

				if ( ( pLine.charPosition <= (pLine.line.length() - 8) )
					&& !pLine.commentSection ){

				    /* Parse and store package declaration */
				    pLine = scanPackageFromLine(pLine);

				}

			    }
			}

			/* Search quotation and apostrophe second mark.
			 * If either is found, section is ended and reset to false
			 */
			pLine = pComments.endVariableSection( pLine);

		    }
		}
		/* Update pLine.line number, position is needed for Comment object */
		pLine.lineNumber++; 	
		/* move comment start position back to start of the line */
		pLine.startPosition = 0; 
		/* Set sections false */
		pLine.quotationSection = false; 
		pLine.apostropheSection = false;

	    }

	} catch (Exception e) {
	    throw e;
	} 

    }

    /** Method isSourceFile
     * See SourceParser interface 
     * 
     */

    public boolean isSourceFile(FileID file) {

	/* Make sure that all letters are lower case */
	String fName = file.name.toLowerCase();

	if( fName.endsWith(".java") ){
	    return true;
	}else {
	    return false;
	}
    }

}
