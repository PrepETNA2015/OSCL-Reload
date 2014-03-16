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

import java.io.File;
import java.util.ArrayList;
import checker.FileID;
import checker.CommentLine;
import checker.Reference;
import checker.localization.Locale;

/**
 * 
 * 
 * @Classname 	PHPSourceParser.java	
 * @Version 	1.33 
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */


/**
 * PHPSourceFileParsing takes php source file content 
 * and parses php code sections for comments and references.
 * 
 * 
 */

public class PHPSourceParser implements SourceParser {

    /* Start of the comment line*/
    private final static String COMMENT_LINE = "//"; 

    /* Start of the unix-style comment 
     * This comment either ends when line ends or block ends (?>)
     */ 
    private final static char COMMENT_MARK = '#'; 

    /* Start of the block comment */
    private final static String COMMENT_BEGIN = "/*"; 

    /* Length of the comment marks in characters*/
    private final static int COMMENT_LEN = 2; 
    
    /* Localization */
    private Locale loc = new Locale();

    /*
     * PHP code section marks: (note that ASP-style is not supported)
     * 
    preferred format 		<?php ... ?> 	
    verbose 			<script language="php"> ... </script>

    Less portable format that may be disabled in php.ini:
    short-form 			<? .. ?>
    short-form expression 	<?=expr?>
    ASP-style 			<% ... %>
    ASP-style expression 	<%=expr%>
     */

    /* All php code start marks for searching */
    private String phpStartArray[] = {"<?php", "<script language=\"php\">", "<?"};
    private String phpStart;

    /* All php code end marks for searching
     * Note: These must equal order in the phpStartArray  */
    private String phpEndArray[] = {"?>", "</script>", "?>"};
    private String phpEnd;

    /* All references for searching */
    private String parseWordsArray[] = {"include", "require"};

    /* Name for reference searching from the line */
    private String parseWord;


    /* Array list for extracted references, one variable per object */
    private static ArrayList<Reference> referenceArray; 

    /* ParserComments object for comment operations */		
    private ParserComments pComments = null;

    /* ReferenecProcessing object for reference path processing */
    private ReferenceProcessing refProc;


    public PHPSourceParser() {

	/* Construct PHPSourseParser with reference array
	 * This array stores all references 
	 */
	referenceArray = new ArrayList<Reference>();

	refProc = new ReferenceProcessing();
    }

    /**
     * getComments returns the source file's comments.
     * 
     */
    public ArrayList<CommentLine> getComments() {

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
     * This private method is for searching unix style comment from the line
     *  
     *  @param SourceParserLine
     *  		pLine is code line object that includes line and positions
     *  
     *  @return SourceParserLine
     * 		  pLine with new values
     */

    private SourceParserLine scanCommentMark(SourceParserLine pLine) throws Exception{

	try{

	    if(pLine.line.charAt(pLine.charPosition) == COMMENT_MARK){

		/* Found unix style comment */

		pLine.startPosition = pLine.charPosition + 1;
		pLine.charPosition = pLine.line.length();

		pComments.writeToCommentArray( pLine );
		pLine.charPosition = pLine.line.length() - 1;

	    }

	} catch (RuntimeException e) {
	    throw e;
	} 

	return pLine;

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
	String lastString;				// Help string
	int refStart = 0;				// Help int
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

		parsedLine = pLine.line.substring(pLine.startPosition, 
			pLine.charPosition);

		/* Remove leading and trailing spaces */
		parsedLine = parsedLine.trim();

		/* Write reference to the output 
		 * if parsed substring is not empty */
		if (parsedLine.length() > 0) {

		    /* Set raw declaration to the reference */
		    reference.declaration = parsedLine;

		    if(parsedLine.startsWith("http://")){

			reference.information = loc.lc("Reference to www document");
			reference.referenceType = Reference
			.ReferenceType.UNPARSABLE;

		    } else if (parsedLine.contains("$")){
			/* There was variable inside parsed reference */
			reference.information = loc.lc("Reference includes variable");
			reference.referenceType = Reference
			.ReferenceType.UNPARSABLE;

		    } else { /* Normal reference */

			/* Find out what was inside reference declaration,
			 * especially before parsed line
			 */
			refStart = pLine.line.indexOf( parseWord, 0);
			refStart = refStart + parseWord.length();

			lastString = pLine.line.substring(refStart,
				pLine.startPosition-1);
			lastString = lastString.trim();
			if( lastString.length() < 1 ){
			    /* There was nothing, e.g. variables before 
			     * reference file.
			     * Assume this parsable reference.
			     */
			    reference.referenceType = Reference
			    .ReferenceType.STATIC_INCLUDE; 

			} else {
			    /* There was something before reference */
			    reference.referenceType = Reference
			    .ReferenceType.UNPARSABLE; 
			    reference.information = loc.lc("Reference include variables e.g. path");
			}

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

		    }

		    /* Add reference object to the array */
		    referenceArray.add(reference);

		} else { /* Parsed line was too short - ambigious case */

		    /* Set reference type UNPARSABLE */
		    reference.referenceType = Reference.ReferenceType.UNPARSABLE;
		    reference.information= loc.lc("Empty declaration");
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
     * 
     * @param SourceParserLine
     *            pLine is code line object that includes line and positions
     *            
     * @return SourceParserLine
     * 		  pLine with new values
     *            
     */

    private SourceParserLine parsePHPReferenceFromLine(SourceParserLine pLine, 
	    FileID sourceFile)throws Exception{

	/* Last substring of the line for comparing */
	String lastString; 
	/* Initialize position variables */
	int lastApo = -1;
	int lastQuo = -1;
	int firstMark = -1;
	String endMark = "";

	boolean parseWordIsOK = true;


	/* Add suitable postfix for the reference */
	if( pLine.line.contains(parseWord + " ") ){

	    parseWord = parseWord + " ";

	} else if( pLine.line.contains(parseWord + "(") ){

	    parseWord = parseWord + "(";

	} else if( pLine.line.contains(parseWord + "_once ") ){

	    parseWord = parseWord + "_once ";

	} else if( pLine.line.contains(parseWord + "_once(") ){

	    parseWord = parseWord + "_once(";

	} else { /* Other than supported format */
	    parseWordIsOK = false;
	}

	if(parseWordIsOK){
	    if (  pLine.charPosition 
		    <= (pLine.line.length() 
			    - parseWord.length() ) ){

		try{
		    lastString = pLine.line.substring(pLine.charPosition,
			    (pLine.charPosition + parseWord.length() ) );

		    /* Compare substring */
		    if (lastString.equals(parseWord)) {
			/* Reference found */

			pLine.startPosition = pLine.charPosition + parseWord.length();

			if( pLine.line.indexOf(";", pLine.startPosition)
				>= pLine.startPosition ){
			    /* Find declaration end mark */

			    pLine.charPosition = pLine.line.
			    indexOf(";", pLine.startPosition);

			    lastApo = pLine.line.lastIndexOf("'", pLine.charPosition);
			    lastQuo = pLine.line.lastIndexOf("\"", pLine.charPosition);

			    if(lastApo > lastQuo){
				/* The closest mark to semicolon is apostrophe */
				endMark = "'";				

			    }
			    else if(lastApo < lastQuo){
				/* The closest mark to semicolon is quotation */
				endMark = "\"";

			    } 
			    if( !(lastApo==lastQuo) ){
				/* Either apostrophes or quotation found */

				/* Set charPosition to get substring */
				pLine.charPosition = pLine.line.
				lastIndexOf(endMark, pLine.charPosition);
				lastString = pLine.line.
				substring(pLine.startPosition, pLine.charPosition);

				firstMark = lastString.lastIndexOf(endMark);

				if( (firstMark > -1) && lastString.endsWith(".php") ){
				    /* First mark found and this is php file
				     * set new startPosition */
				    pLine.startPosition = pLine.startPosition + firstMark + 1 ;
				    /* Write reference to the reference array */
				    writeToReferenceArray(sourceFile, pLine);

				}
			    }
			    /* Set new char position from reference end mark */
			    pLine.charPosition = pLine.line.indexOf(";", pLine.charPosition);

			}  else{
			    /* This declaration does not end in this row */
			    pLine.charPosition = pLine.charPosition + parseWord.length();
			}

		    }

		} catch (RuntimeException e) {
		    throw e;
		} 
	    }
	}
	/* Return pLine with new values */	
	return pLine;
    }

    /**
     * This private method is for searching php code section 
     * 
     * @param SourceParserLine
     *            pLine is code line object that includes line and positions
     *            
     * @param int
     * 		lineIndex is a start index of the line 
     *            
     * @return SourceParserLine
     * 		  pLine with new values
     *            
     */

    private SourceParserLine findPHPSection(SourceParserLine pLine, int lineIndex ){

	/* Reset code section marks */
	phpStart = "<NotSetYet>";
	phpEnd = "<NotSetYet>";

	pLine.charPosition = 0;

	/* Find PHP code section start mark from the line */
	for(int i=0; i < phpStartArray.length; i++){

	    if( pLine.line.indexOf( phpStartArray[i], lineIndex ) 
		    >= lineIndex ){
		/* There is php start mark after lineIndex */

		/* Set start mark */
		phpStart = phpStartArray[i];
		/* Set end mark according to start mark */
		phpEnd = phpEndArray[i];   

		/* Cut line to start from code start mark */
		pLine.charPosition = pLine.line.indexOf(phpStart, lineIndex);

	    }

	}

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

	/* Set php section */
	boolean phpSection = false;

	/* This indicates line which is cut because of end mark */
	boolean cutLine = false;

	/* This is index mark for cut line */
	int cutLineIndex = 0;

	/* This sets character position */
	int lineIndex = 0;


	pLine = new SourceParserLine();

	try {
	    /* Read one object (source code pLine.line) at the time */
	    for (int objectNumber = 0; objectNumber < fileContent.size(); 
	    objectNumber++) {

		if(!phpSection && cutLine ){
		    /* PHP code section ended in last row,
		     * move rest of the row to the next line
		     */
		    objectNumber--;
		    pLine.lineNumber--;
		    cutLine = false;
		    /* Set new character position */
		    lineIndex = cutLineIndex;
		    cutLineIndex = 0;
		    pLine.line = fileContent.get(objectNumber);

		    /* Reset sections */
		    pLine.quotationSection = false;
		    pLine.apostropheSection = false;
		    pLine.commentSection = false;

		} else { /* Normal case */

		    /* Get source code line from array */
		    pLine.line = fileContent.get(objectNumber);
		    /* Reset index of the cut line */
		    lineIndex = 0;

		}

		parseWord = "<nothingFoundSoFar>";	/* Reset parseWord */

		/* Try find reference from the line */
		for(int i=0; i < parseWordsArray.length; i++){
		    if( pLine.line.contains(parseWordsArray[i]) ){
			parseWord = parseWordsArray[i];
		    }
		}

		if( !phpSection ){
		    /* Find PHP code section start mark from the line */
		    pLine = findPHPSection( pLine, lineIndex );
		    lineIndex = pLine.charPosition;

		    /* Find end mark from lines that are left in the file */
		    for (int oNumber = objectNumber; oNumber < fileContent.size(); 
		    oNumber++){

			if( fileContent.get(oNumber).contains(phpEnd) ){
			    /* PHP code section end mark found. */
			    /* Approve PHP code section 
			     * and set section started */
			    phpSection = true;
			    oNumber = fileContent.size();
			}

		    }

		}

		if( phpSection ){
		    /* This in the PHP code section */

		    if( pLine.line.contains(phpEnd) ){
			/* PHP code section will end in this row */


			/* Move rest of the line to the next line 
			 * if line is continues after end mark */
			if( ( pLine.line.indexOf(phpEnd, lineIndex)
				< ( pLine.line.length() - phpEnd.length() ) ) 
				&& ( pLine.line.indexOf(phpEnd, lineIndex) > -1)  ){

			    phpSection = false;
			    cutLine = true;
			    cutLineIndex = pLine.line.indexOf(phpEnd, lineIndex);
			    /* Cut line from code end mark */
			    pLine.line = pLine.line.
			    substring(0, pLine.line.indexOf(phpEnd, lineIndex));

			}

		    }


		    if( ( ( ( pLine.line.contains(COMMENT_BEGIN) 
			    || pLine.line.contains(COMMENT_LINE) )
			    || pLine.line.contains("#") )
			    || pLine.commentSection ) 
			    || ( pLine.quotationSection || pLine.apostropheSection )
			    || pLine.line.contains(parseWord) ) {

			/* Line contains comments or references
			 * or some of the sections is set,
			 * parse source code line */


			for (pLine.charPosition = lineIndex; 
			pLine.charPosition < (pLine.line.length()); 
			pLine.charPosition++) {

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

			    /* Nothing found in this line, try to find comment marks 
			     * of references */
			    if ( ( (!pLine.commentSection 
				    && !pLine.quotationSection) 
				    && !pLine.apostropheSection)
				    && (pLine.charPosition 
					    <= (pLine.line.length() - COMMENT_LEN))) {

				/* Scan line for comments */
				pLine = scanCommentMark( pLine );

				pLine = pComments.scanLine( pLine );

				if(pLine.line.contains(parseWord) 
					&& !pLine.commentSection){

				    /* Search and write reference to the array */
				    pLine = parsePHPReferenceFromLine(pLine, 
					    sourceFile);

				}

			    } 

			    /* Search quotation and apostrophe second mark.
			     * If either is found, section is ended and reset to false
			     */
			    pLine = pComments.endVariableSection( pLine);

			}

		    }

		}
		/* Update pLine.line number, position is needed for Comment object */
		pLine.lineNumber++; 	
		/* move comment start position back to start of the line */
		pLine.startPosition = 0; 	

	    }

	} catch (Exception e) {
	    throw e;

	} 

    }

    /**
     * See SourceParser interface 
     * 
     */

    public boolean isSourceFile(FileID file) {

	/* Make sure that all letters are lower case */
	String fName = file.name.toLowerCase();

	if( fName.endsWith(".php") ){
	    return true;
	}
	else {
	    return false;
	}

    }

}
