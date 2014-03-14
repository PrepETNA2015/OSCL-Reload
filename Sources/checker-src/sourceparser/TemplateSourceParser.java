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
import java.util.ArrayList;

import checker.CommentLine;
import checker.FileID;
import checker.Reference;
import checker.localization.Locale;

/**
 * @Classname 	TemplateSourceParser.java	
 * @Version 	1.5
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */


/**
 * This a template for new source code parser.
 * Add new parser support in SourceParserFactory also.
 * 
 */

public class TemplateSourceParser {

    /* Example: Start of the comment line*/
    private final static String COMMENT_LINE = "//"; 

    /* Example: Start of the block comment */
    private final static String COMMENT_BEGIN = "/*"; 

    /* Length of the comment marks in characters, 
     * you must declare length of the comment mark here*/
    private final static int COMMENT_LEN = 2;  

    /* Name for reference searching, add your reference key word here
     * Tip: You can leave space after keyword 
     *  */
    private String parseWord = "AddYourOwnReference ";

    /* Array list for extracted references, one variable per object */
    private ArrayList<Reference> referenceArray; 

    /* ParserComments object for comment operations and storing */		
    private ParserComments pComments = null;
    
    /* Localization */
    private Locale loc = new Locale();


    public TemplateSourceParser() {

	/* Construct SourceParser with reference array
	 * This array stores all references 
	 * Note: This object is created only once
	 */
	referenceArray = new ArrayList<Reference>();

	/*
	 * Note: Change SourceParserFactory to include new source parser
	 * 
	 */
    }

    /**
     * getComments returns the source file's comments.
     */

    public ArrayList<CommentLine> getComments()  {

	/* Array list for extracted comments */
	ArrayList<CommentLine> commentArray;

	/* Get array from the parserComments object 
	 * Note: This is called after each source file is parsed
	 * */
	commentArray = pComments.getComments();
	return commentArray;
    }

    /**
     * getReferences return the detected references.
     */

    public ArrayList<Reference> getReferences() {

	/* Return all references at once.
	 * Note: This is called once after all source file are parsed
	 */
	return referenceArray;

    }

    /**
     * This private method is for writing reference to arraylist 
     * 
     * @param FileID
     * 		sourceFile is source code file object
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
	    /* Initializing variables for Refrence object */
	    targetFile = new FileID(null, null);

	    /* Set Reference object from this source file to target file */
	    reference = new Reference(sourceFile, targetFile);

	    /* Check input */
	    if (pLine.startPosition < pLine.charPosition) {

		parsedLine = pLine.line.substring(pLine.startPosition, pLine.charPosition);

		/* Remove leading and trailing spaces */
		parsedLine = parsedLine.trim();

		/* Write reference to the output if parsed substring is not empty */
		if (parsedLine.length() > 0) {

		    /* Set raw declaration to the reference */
		    reference.declaration = parsedLine;

		    /* Construct path for target file as needed 
		     * Check ReferenceProcessing class too.
		     * Put your code here
		     * */

		    /* Path processing ready. 
		     * Create File object for easier path separation. 
		     * Path would be OS independent
		     */
		    tFile = new File(parsedLine);

		    /* Get path and file name from File object for the reference */
		    reference.targetFile.path = tFile.getParent();
		    reference.targetFile.name = tFile.getName();

		    /* Set reference type, like
		     * 
		     * reference.referenceType = Reference.ReferenceType.STATIC_INCLUDE;
		     * 
		     * Put your code here
		     * 
		     */

		    /* Add reference object to the array */
		    referenceArray.add(reference);

		} else { /* Parsed line was too short - ambigious case */

		    /* Set reference type UNPARSABLE */
		    reference.referenceType = Reference.ReferenceType.UNPARSABLE;
		    reference.information=loc.lc("Empty declaration");
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

    private SourceParserLine parseRefenceFromLine(SourceParserLine pLine, 
	    FileID sourceFile)throws Exception{

	/* Last substring of the line for comparing */
	String lastString; 

	/* Reference end mark */
	String referenceEndMark;

	/* Check that there is enough space to substring reference key word */
	if (  pLine.charPosition <= (pLine.line.length() 
		- parseWord.length() ) ){

	    try{
		lastString = pLine.line.substring(pLine.charPosition,
			(pLine.charPosition + parseWord.length() ) );

		/* Compare substring, quit if not matched */
		if (lastString.equals(parseWord)) {
		    /* Reference found 
		     * 
		     * Example:
		     * #include "foo" found from the line
		     * parseWord is "#include "
		     * 
		     * 1) set charPosition to the position of the first quotation
		     * 
		     * 2) find referenceEndMark, like "
		     * 
		     * 3) set startPosition to the character after the first quotation and
		     * charPosition to the second quotation 
		     * 
		     * string for writing is then foo", you must set charPosition one step more
		     * because of substring method in String class
		     * 
		     * 4) write reference to the array using writeToReferenceArray method
		     * 
		     * */

		    /* Set new position where to start further parsing */
		    pLine.charPosition = pLine.charPosition + parseWord.length();

		    /* Find and select reference start mark 
		     * 
		     * Put your code here
		     * 
		     * and set referenceEndMark
		     * 
		     */ 

		    /* Find then reference end mark
		     * 
		     * Put your code here
		     */ 


		    /* Set reference declaration start and end position
		     * and
		     * write reference to the reference array */
		    writeToReferenceArray(sourceFile, pLine);       	

		}          	    

	    } catch (RuntimeException e) {
		throw e;
	    } 
	}

	/* Return pLine with new values */	
	return pLine;
    }



    /**
     * scanFile extracts comments and references from given source file content
     * 
     * See SourceParser interface
     * 
     */

    public void scanFile(FileID sourceFile, ArrayList<String> fileContent) throws Exception{

	/* Initialize new ParserComments object */
	pComments = new ParserComments();

	/* Create object containing line information */
	SourceParserLine pLine;

	/* Create new source parser line object to store line and positions */
	pLine = new SourceParserLine();


	try {
	    /* Read one object (source code line) at the time */
	    for (int objectNumber = 0; objectNumber < fileContent.size(); objectNumber++) {
		/* Get source code line from array */
		pLine.line = fileContent.get(objectNumber);


		/* Search comment start marks and references from the line and check
		 * if this line is in the comment section set earlier
		 * 
		 * This pre-check speed up parsing because only lines that are needed to parse
		 * are parsed! Set your own key words if needed.
		 * 
		 */
		if( ( ( pLine.line.contains(COMMENT_BEGIN) 
			|| pLine.line.contains(COMMENT_LINE) )
			|| pLine.commentSection ) 
			|| pLine.line.contains(parseWord) ) {

		    /* Line contains comments or references, parse source code line */

		    for (pLine.charPosition = 0; 
		    pLine.charPosition < (pLine.line.length()); pLine.charPosition++) {

			/* Parse every character to make sure that reference key words or 
			 * comment marks are not in the quotations or apostrophes
			 * 
			 * Position in the line is stored to the pLine.charPosition
			 * */

			/* Search quotation and apostrophe marks.
			 * If either is found, section is set to true
			 * and no comments or references are parsed
			 */
			pLine = pComments.findVariableSection(pLine);


			if ( ( (  pLine.commentSection 
				&& !pLine.quotationSection) 
				&& !pLine.apostropheSection)
				&& (pLine.charPosition < pLine.line.length() ) ) {

			    /* If we are in the comment section, scan comment section 
			     * Method receive line of code (pLine) with positions.
			     * Method parses comments and write them to array
			     * 
			     * Note: Check that all comment marks are included, if there
			     * is additional comment style, create new method for pre-parsing
			     * and send pLine after that to scanCommentSection if needed
			     * */
			    pLine = pComments.scanCommentSection( pLine );
			}


			if ( ( (!pLine.commentSection 
				&& !pLine.quotationSection) 
				&& !pLine.apostropheSection)
				&& (pLine.charPosition <= (pLine.line.length() - COMMENT_LEN))) {

			    /* Nothing found in this line, try to find comment marks or references */
			    /* Scan line for starting comments 
			     * 
			     * Note: Check ParserComments which comment marks are supported
			     * scanLine method tries to find comment section and writes
			     * one-line comments immediately to the comment array
			     * */

			    pLine = pComments.scanLine( pLine );

			    /* Try to find given refences if comment section is not set before */
			    if(pLine.line.contains(parseWord) && !pLine.commentSection){

				/* Parse and write references from the line */
				pLine = parseRefenceFromLine(pLine, sourceFile);

			    }

			}

			/* Search quotation and apostrophe second mark.
			 * If either is found, section is ended and reset to false
			 */
			pLine = pComments.endVariableSection( pLine );

		    }
		}
		/* Update pLine.line number, position is needed for Comment object */
		pLine.lineNumber++; 	
		/* Move start position back to start of the line */
		pLine.startPosition = 0; 	

		/* Set sections false, if language does not support 
		 * multiline quotation or apostrophes
		pLine.quotationSection = false; 
		pLine.apostropheSection = false;
		 */
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

	if( fName.endsWith(".sourceFileExtension") ) { 
	    /* Found supported source file */
	    return true;

	}else {
	    return false;
	}
    }

}
