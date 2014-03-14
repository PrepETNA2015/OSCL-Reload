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
 * @Classname 	CppSourceParser.java	
 * @Version 	1.23 
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */


/**
 * CppSourceFileParsing takes c/c++ source file content and parses it for
 * comments and references.
 * 
 */
public class CppSourceParser implements SourceParser {

    /*	 Start of the comment line*/
    private final static String COMMENT_LINE = "//"; 

    /*	 Start of the block comment */
    private final static String COMMENT_BEGIN = "/*"; 

    /* Length of the comment marks in characters*/
    private final static int COMMENT_LEN = 2;  

    /* Name for reference searching */
    private String parseWord = "#include ";

    /* Array list for extracted references, one variable per object */
    private ArrayList<Reference> referenceArray; 

    /* ParserComments object for comment operations */		
    private ParserComments pComments = null;

    /* ReferenecProcessing object for reference path processing */
    private ReferenceProcessing refProc;

	/* Localization */
	private Locale loc = new Locale();

    public CppSourceParser() {

	/* Construct CppSourseParser with reference array
	 * This array stores all references 
	 */
	referenceArray = new ArrayList<Reference>();

	refProc = new ReferenceProcessing();
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

		    /* Set raw declaration to the reference */
		    reference.declaration = parsedLine;

		    /* Set reference type, this may change during processing */
		    reference.referenceType = Reference.ReferenceType.STATIC_INCLUDE;


		    /* Process path in reference */
		    refProc.setReference(reference);

		    parsedLine = refProc.processPath(parsedLine, sourceFile.path );

		    reference = refProc.getReference();

		    /* Path processing ready.
		     * Create File object for easier path separation. 
		     * Path would be OS independent
		     */
		    tFile = new File(parsedLine);

		    /* Get path and file name from File object for the reference */
		    reference.targetFile.path = tFile.getParent();
		    reference.targetFile.name = tFile.getName();

		    /* Add reference object to the array */
		    referenceArray.add(reference);

		} else { /* Parsed line was too short - ambigious case */

		    /* Set reference type UNPARSABLE */
		    reference.referenceType = Reference.ReferenceType.UNPARSABLE;
		    reference.information= loc.lc("Empty include declaration");
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

    private SourceParserLine parseCppRefenceFromLine(SourceParserLine pLine, 
	    FileID sourceFile)throws Exception{

	/* Last substring of the line for comparing */
	String lastString; 

	/* Reference end mark */
	String referenceEndMark = "";


	if (  pLine.charPosition <= (pLine.line.length() 
		- parseWord.length() ) ){

	    try{
		lastString = pLine.line.substring(pLine.charPosition,
			(pLine.charPosition + parseWord.length() ) );

		/* Compare substring */
		if (lastString.equals(parseWord)) {
		    /* Reference found */

		    pLine.charPosition = pLine.charPosition + parseWord.length();

		    /* Find and select reference start mark */
		    if( pLine.line.indexOf("\"", pLine.charPosition)
			    >= pLine.charPosition){

			pLine.charPosition = pLine.line.indexOf("\"", 
				pLine.charPosition);
			referenceEndMark = "\"";

		    } else if( pLine.line.indexOf("<", pLine.charPosition)
			    >= pLine.charPosition){

			pLine.charPosition = pLine.line.indexOf("<", 
				pLine.charPosition);
			referenceEndMark = ">";

		    }

		    if( referenceEndMark.equals(">") || referenceEndMark.equals("\"") ){

			pLine.startPosition = pLine.charPosition + 1;

			/* Search reference end mark */
			if( pLine.line.indexOf(referenceEndMark, pLine.startPosition)
				>= pLine.startPosition){

			    pLine.charPosition = pLine.line.indexOf(referenceEndMark, 
				    pLine.startPosition);

			    /* Write reference to the reference array */
			    writeToReferenceArray(sourceFile, pLine);

			}
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
     * scanFile extracts comments and references from given source file.
     * 
     * See SourceParser interface
     * 
     */

    public void scanFile(FileID sourceFile, ArrayList<String> fileContent) throws Exception{

	/* Initialize new ParserComments object */
	pComments = new ParserComments();

	/* Create object containing line information */
	SourceParserLine pLine;

	pLine = new SourceParserLine();


	try {
	    /* Read one object (source code pLine.line) at the time */
	    for (int objectNumber = 0; objectNumber < fileContent.size(); objectNumber++) {
		/* Get source code line from array */
		pLine.line = fileContent.get(objectNumber);

		if( ( ( pLine.line.contains(COMMENT_BEGIN) 
			|| pLine.line.contains(COMMENT_LINE) )
			|| pLine.commentSection ) 
			|| pLine.line.contains(parseWord) ) {

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
			    if(pLine.line.contains(parseWord) && !pLine.commentSection){

				/* Parse and write references from the line */
				pLine = parseCppRefenceFromLine(pLine, sourceFile);

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
	
	if( fName.endsWith(".cpp") 
		|| fName.endsWith(".c")
		|| fName.endsWith(".cc")
		|| fName.endsWith(".h")
		|| fName.endsWith(".hpp")
		|| fName.endsWith(".cxx")
		|| fName.endsWith(".hxx")
		|| fName.endsWith(".hc")){ 
	    /* Found supported source file */
	    return true;

	}else {
	    return false;
	}
    }

}
