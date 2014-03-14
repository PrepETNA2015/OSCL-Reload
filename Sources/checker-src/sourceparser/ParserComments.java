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

import checker.CommentLine;


/**
 * @Classname 	ParserComments.java	
 * @Version 	1.8
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */

/**
 * ParserComments scans source code comments and writes them to array.
 * This class also provide variable searching from the line.
 * This class is used for all source parsers
 * 
 */

public class ParserComments {

    /*	 Start of the comment line*/
    private final static String COMMENT_LINE = "//"; 

    /*	 Start of the block comment */
    private final static String COMMENT_BEGIN = "/*";  

    /*	 End of the block comment */
    private final static String COMMENT_END = "*/";

    /* Length of the comment end mark in characters*/
    private final static int COMMENT_LEN = 2;  

    /*	 Array list for extracted comments, one line per object */
    private ArrayList<CommentLine> commentArray; 

    /* Initialize object */   
    ParserComments(){

	/* Initialize new array list for comments 
	 * one array per source file */		
	commentArray = new ArrayList<CommentLine>();
    }

    /**
     * getComments returns the source file's comments.
     */

    protected ArrayList<CommentLine> getComments()  {

	return commentArray;

    }

    /**
     * This method is for searching comments in the line 
     *  
     *  @param SourceParserLine
     *  		pLine is code line object that includes line and positions
     *  
     *  @return SourceParserLine
     * 		  pLine with new values
     */

    protected SourceParserLine scanLine(SourceParserLine pLine) throws Exception{

	/* String from the pLine.line for comparing */
	String lastString = "";

	try{
	    if(pLine.charPosition <= (pLine.line.length() - COMMENT_LEN)){

		lastString = pLine.line.substring(pLine.charPosition,
			(pLine.charPosition + COMMENT_LEN));

		/* Case where found comment marks */
		if (lastString.equals(COMMENT_LINE)) {
		    /*
		     * Found comment line, assume that the rest of the line
		     * is comment, exclude comment marks
		     */   
		    pLine.startPosition = pLine.charPosition + COMMENT_LEN;
		    pLine.charPosition = pLine.line.length();

		    writeToCommentArray( pLine );

		    pLine.charPosition = pLine.line.length() - 1;
		}

		/* Comment block found */
		if (lastString.equals(COMMENT_BEGIN)) {
		    // Found /*, this may be multiline comment
		    pLine.commentSection = true;

		    /*
		     * Boundary check, character is more than two char from
		     * end of pLine.line
		     */
		    if (pLine.charPosition < (pLine.line.length() - COMMENT_LEN)) {
			/* Mark where comment starts */
			pLine.startPosition = pLine.charPosition + COMMENT_LEN;
			pLine.charPosition++;

			/*
			 * Special case - if there is a star after
			 * COMMENT_BEGIN, move pLine.startPosition forward but
			 * check that comment would not end at the same time
			 */
			if (pLine.startPosition < (pLine.line.length() - COMMENT_LEN)) {
			    /*
			     * This is normal case, pLine.charPosition is still
			     * more than char from end of pLine.line
			     */
			    if ( (pLine.line.charAt(pLine.startPosition) == '*')
				    && !(pLine.line.charAt(pLine.startPosition + 1) == '/')) {
				pLine.startPosition++;
				pLine.charPosition++;
			    }
			    if ( (pLine.line.charAt(pLine.startPosition) == '*')
				    && (pLine.line.charAt(pLine.startPosition + 1) == '/')){
				/* Comment end */
				pLine.commentSection = false;
				pLine.charPosition++;
			    }
			}
			if (pLine.startPosition == (pLine.line.length() - 1)) {
			    /*
			     * Unusual case - star is a last character in
			     * the pLine.line after comment begin
			     */
			    if (pLine.line.charAt(pLine.startPosition) == '*') {
				pLine.startPosition = 0;
				pLine.charPosition = pLine.line.length() - 1;
			    } else {
				/* Write any other mark than star to output */
				pLine.charPosition = pLine.line.length();

				writeToCommentArray( pLine );

				pLine.charPosition = pLine.line.length() - 1;

			    }
			}
		    }
		    /* If these were last characters in the pLine.line */
		    if ( pLine.charPosition == (pLine.line.length() - COMMENT_LEN) ) {
			pLine.startPosition = 0;
			pLine.charPosition = pLine.line.length() - 1;
		    }
		}
	    }

	} catch (RuntimeException e) {
	    throw e;
	} 

	return pLine;

    }


    /**
     * This method is for searching comment end mark 
     *  
     *  @param SourceParserLine
     *  		pLine is code line object that includes line and positions
     *  
     *  @return SourceParserLine
     * 		  pLine with new values
     */

    protected SourceParserLine scanCommentSection(SourceParserLine pLine) throws Exception{

	/* Sub string from line for comparing key words */
	String lastString = "";

	try{

	    if( ( pLine.line.indexOf(COMMENT_END, pLine.charPosition)  
		    != pLine.line.indexOf("*", pLine.charPosition) )
		    && (pLine.line.indexOf("*", pLine.charPosition) != -1) ){

		/* Line contains star that is not part of comment mark */

		lastString = pLine.line.substring(pLine.charPosition, 
			pLine.line.indexOf("*", pLine.charPosition) );
		lastString = lastString.trim();
		if(lastString.length() < 1){
		    /* Star was the first character in the line 
		     * move startPosition to the right to exclude star*/
		    pLine.startPosition = pLine.line.indexOf("*", pLine.charPosition) +1;
		}

	    }

	    if(pLine.line.indexOf(COMMENT_END, pLine.charPosition) >= pLine.charPosition){
		/* Line contains comment end mark */

		pLine.charPosition = pLine.line.indexOf(COMMENT_END, 
			pLine.charPosition);
		/* Write comment to the comment array */		
		writeToCommentArray( pLine );
		/* Reset section */
		pLine.commentSection = false;

		/* Set new character position */
		if(pLine.charPosition < (pLine.line.length() - COMMENT_LEN) ){

		    pLine.charPosition = pLine.charPosition + COMMENT_LEN;

		} else {

		    pLine.charPosition = pLine.line.length() -1;
		}

	    } else { /* No comment end, comment continue to the next line */
		pLine.charPosition = pLine.line.length();

		/* Write this line to array */
		writeToCommentArray( pLine );

		pLine.charPosition = pLine.line.length() -1;
	    }

	} catch (RuntimeException e) {
	    throw e;
	} 

	return pLine;

    }

    /**
     * This method is for writing string to arraylist 
     *  
     *  @param SourceParserLine
     *  		pLine is code line object that includes line and positions
     */

    protected void writeToCommentArray(SourceParserLine pLine) throws Exception {

	String parsedLine; 				// String for parsed pLine.line
	CommentLine comment;				// Comment line object

	try {
	    /* Check input, startPosition is starting column and charPosition is end column */
	    if (pLine.startPosition < pLine.charPosition) {

		parsedLine = pLine.line.substring(pLine.startPosition, pLine.charPosition);
		/* Write comments to the output if parsed substring is not empty */
		if (parsedLine.length() > 0) {
		    /* Create comment pLine.line object */
		    comment = new CommentLine(parsedLine, pLine.lineNumber,
			    pLine.startPosition);
		    /* Add object to array */
		    commentArray.add(comment);
		}

	    }
	} catch (RuntimeException e) {
	    throw e;
	} 

    }

    /**
     * This method is searching quotation and apostrophe marks that
     * usually indicates variable in the declaration.
     * If either mark is found, method set section started.
     * When quotation or apostrophe section is set true, comments or 
     * references are not parsed.
     *  
     *  @param SourceParserLine
     *  		pLine is code line object that includes line and positions
     */

    protected SourceParserLine findVariableSection(SourceParserLine pLine) {

	/* Check that character at charPosition is quotation or apostrophe
	 * mark.
	 * If it wasn't either mark, leave method and return pLine as it was.
	 *  */

	if ( ( ( ( (pLine.line.charAt(pLine.charPosition)) == '"') 
		&& !pLine.quotationSection) 
		&& !pLine.apostropheSection) 
		&& !pLine.commentSection) {

	    /* Set quotation section started */
	    pLine.quotationSection = true; 
	    pLine.charPosition++; // Increment char Position
	}


	if ( ( ( ( (pLine.line.charAt(pLine.charPosition)) == '\'') 
		&& !pLine.quotationSection) 
		&& !pLine.apostropheSection) 
		&& !pLine.commentSection) {

	    /* Set apostrophe section started */
	    pLine.apostropheSection = true; 
	    pLine.charPosition++; // Increment char Position
	    /*
	     * Special case - backslash inside apostrophes indicates
	     * that reserved character follows. If next character after
	     * apostrophe is backslash then bypass next character after
	     * backslash also.
	     */
	    if (pLine.line.charAt(pLine.charPosition) == '\\') {
		/* Jump over special marks */
		if(pLine.charPosition 
			< (pLine.line.length()-COMMENT_LEN) ){
		    pLine.charPosition = pLine.charPosition + 2;	
		}else {
		    pLine.charPosition = pLine.line.length()-1;
		}

	    }
	}

	return pLine;
    }

    /**
     * This method is searching quotation and apostrophe second mark
     * to reset apostrophe or quotation section.
     * If either mark is found, method reset section (false).
     *  
     *  @param SourceParserLine
     *  		pLine is code line object that includes line and positions
     */

    protected SourceParserLine endVariableSection(SourceParserLine pLine){


	/*
	 * Test if character position is inside the quotation section,
	 * and this is a second quotation mark
	 */
	if ( ( ( ( ( (pLine.line.charAt(pLine.charPosition)) == '"') 
		&& pLine.quotationSection) 
		&& !pLine.apostropheSection) 
		&& !pLine.commentSection)
		&& (pLine.charPosition < pLine.line.length()) ){
	    /*
	     * If this quotation mark is not a special character in the
	     * variable declaration, like "xxxx\"yy" or "xx\\\"" 
	     * quotation section ends.
	     */
	    if ( (pLine.line.charAt(pLine.charPosition - 1) == '\\') 
		    && (pLine.line.charAt(pLine.charPosition - 2) == '\\') ) {
		pLine.quotationSection = false;

	    } else if (!(pLine.line.charAt(pLine.charPosition - 1) == '\\')) {
		pLine.quotationSection = false;
	    } 
	}	
	/*
	 * Test if character position is inside the apostrophe section,
	 * and this is a second apostrophe mark
	 */
	if ( ( ( ( ( (pLine.line.charAt(pLine.charPosition)) == '\'') 
		&& !pLine.quotationSection) 
		&& pLine.apostropheSection) 
		&& !pLine.commentSection) 
		&& (pLine.charPosition < pLine.line.length()) ){
	    /*
	     * If this apostrophe mark is not a special character in the
	     * variable declaration, like '\'' apostrophe section
	     * ends.
	     */
	    if ( (pLine.line.charAt(pLine.charPosition - 1) == '\\') 
		    && (pLine.line.charAt(pLine.charPosition - 2) == '\\') ) {
		pLine.apostropheSection = false;

	    } else if (!(pLine.line.charAt(pLine.charPosition - 1) == '\\')) {
		pLine.apostropheSection = false;
	    } 


	}

	return pLine;
    }
}
