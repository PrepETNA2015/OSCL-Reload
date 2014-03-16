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

/**
 * @Classname 	SourceParserLine.java	
 * @Version 	1.7
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */

/**
 * SourceParserLine is a one source code line including required positions 
 * for source code parsing.
 * 
 * Create object of this class for every line in the file content
 *  
 */

public class SourceParserLine {

    SourceParserLine (){

	/* Set default values */
	line = "";

	lineNumber = 0;

	charPosition = 0;

	startPosition = 0;

	commentSection = false;

	apostropheSection = false;

	quotationSection = false;

    }

    /* Line for source parsing */
    protected String line;

    /* Number of the line, starting from 0 */
    protected int lineNumber;

    /* Character position in the line, starting from 0 */
    protected int charPosition;

    /* Starting position in the line which is used for starting point for writing */
    protected int startPosition;

    /* Indicates comment section in the line */
    protected boolean commentSection;

    /* Indicates apostrophe section in the line */
    protected boolean apostropheSection;

    /* Indicates quotation section in the line */
    protected boolean quotationSection;

}
