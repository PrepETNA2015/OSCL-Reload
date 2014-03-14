/**
 * 
 *   Copyright (C) 2009 Tuomo Jorri
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

import checker.Log;
import checker.LogEntry;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 
 */

public class PythonSourceParser implements SourceParser {
    /* Localization */
    private Locale loc = new Locale();

    /* Array list for extracted references, one variable per object */
    private static ArrayList<Reference> referenceArray; 
    private static ArrayList<CommentLine> commentArray;

    /* ParserComments object for comment operations */		
    private ParserComments pComments = null;

    /* ReferenecProcessing object for reference path processing */
    private ReferenceProcessing refProc;


    public PythonSourceParser() {
        this.referenceArray = new ArrayList<Reference>();
        this.commentArray = new ArrayList<CommentLine>();
    	this.refProc = new ReferenceProcessing();
    }

    /**
     * getComments returns the source file's comments.
     */
    public ArrayList<CommentLine> getComments() {
	    return this.commentArray;
    }

    /**
     * getReferences return the detected references.
     */
    public ArrayList<Reference> getReferences() {
        return this.referenceArray;
    }

    /**
     * scanFile extracts comments and references from given source file.
     * 
     * See SourceParser interface
     * 
     */
    public void scanFile(FileID sourceFile, ArrayList<String> fileContent) throws Exception {
        this.parseComments(sourceFile, fileContent);

        this.parseReferences(sourceFile, fileContent);
    }

    private void parseReferences(FileID sourceFile, ArrayList<String> fileContent) {
    }

// Cases:
//
// 0) if in blockquote mode and no ending blockquote, record everything
// 1) "... # <record until EOL>"
//
// 2) " ... ''' <record> ''' ..."
// 3) " ... """ <record> """ ..."
//
// 4) " ... """ <record> <EOL>" set mode to block quote
// 5) " ... ''' <record> <EOL>" set mode to block quote
//
// http://www.fileformat.info/tool/regex.htm
//
// regexps:
//
// Capture single line comments:
// "^[^#^'''^\"\"\"]*#+(.*)$"
//
// Single line block quotes
// "^.*'''(.*)'''$"
// "^.*\"\"\"(.*)\"\"\"$
//
// Beginning of multi-line block quote
// 
// ^.*'''(.*)[^''']*$
// ^.*\"\"\"(.*)[^\"\"\"]*$
//
// End of multi-line block quote

    /**
     * Populates the comments array with comments from the source file.
     *
     * @param sourceFile
     * @param fileContent
     */
    private void parseComments(FileID sourceFile, ArrayList<String> fileContent) {
        String lineContent;

        // try to find one-line comments
        Pattern pHash = Pattern.compile("^[^#]*#(.*)$");
        Pattern pApos = Pattern.compile("'''([^''']*)'''");
        Pattern pQuot = Pattern.compile("\"\"\"([^\"\"\"]*)\"\"\"$");
        Matcher m;
        CommentLine comment;

        for (int line = 0; line < fileContent.size(); line++) {
            lineContent = fileContent.get(line);
            
            m = pHash.matcher(lineContent);

            if (m.matches()) {
                comment = new CommentLine(lineContent, line, m.start(1));
                this.commentArray.add(comment);
                Log.log(LogEntry.VERBOSE, "Found comment on line "+line+": "+lineContent);
            }

            m = pApos.matcher(lineContent);

            //while (m.find()) {
            if (m.matches()) {
                while (m.find()) {
                    comment = new CommentLine(lineContent, line, m.start());
                    this.commentArray.add(comment);
                    Log.log(LogEntry.VERBOSE, "Found comment on line "+line+": "+lineContent);
                }
            }

            m = pQuot.matcher(lineContent);

            //while (m.find()) {
            if (m.matches()) {
                while (m.find()) {
                    comment = new CommentLine(lineContent, line, m.start());
                    this.commentArray.add(comment);
                    Log.log(LogEntry.VERBOSE, "Found comment on line "+line+": "+lineContent);
                }
            }
        }

        // multiliners
        Pattern pStart;
        Pattern pEnd;
        boolean insideBlock;

        // try to find multiliners with '''
        pStart = Pattern.compile("^[^''']*'''([^''']*)$");
        pEnd = Pattern.compile("^([^''']*)'''.*$");
        insideBlock = false;
        
        for (int line = 0; line < fileContent.size(); line++) {
            lineContent = fileContent.get(line);

            if (insideBlock == true) {
                m = pEnd.matcher(lineContent);

                if (m.matches()) {
                    // comment is ending
                    insideBlock = false;
                    
                    comment = new CommentLine(lineContent, line, 0);
                    this.commentArray.add(comment);
                } else {
                    // comment is just continuing
                    comment = new CommentLine(lineContent, line, 0);
                    this.commentArray.add(comment);
                }
            } else {
                m = pStart.matcher(lineContent);

                if (m.matches()) {
                    // comment is starting
                    insideBlock = true;
                    
                    comment = new CommentLine(lineContent, line, m.start(1));
                    this.commentArray.add(comment);
                }
            }
        }

        // try to find multiliners with """
        pStart = Pattern.compile("^[^\"\"\"]*\"\"\"([^\"\"\"]*)$");
        pEnd = Pattern.compile("^([^\"\"\"]*)\"\"\".*$");
        insideBlock = false;
        
        for (int line = 0; line < fileContent.size(); line++) {
            lineContent = fileContent.get(line);

            if (insideBlock == true) {
                m = pEnd.matcher(lineContent);

                if (m.matches()) {
                    // comment is ending
                    insideBlock = false;
                    
                    comment = new CommentLine(lineContent, line, 0);
                    this.commentArray.add(comment);
                    Log.log(LogEntry.VERBOSE, "Ending multiline comment on line "+line+": "+lineContent);
                } else {
                    // comment is just continuing
                    comment = new CommentLine(lineContent, line, 0);
                    this.commentArray.add(comment);
                    Log.log(LogEntry.VERBOSE, "Continuing multiline comment on line "+line+": "+lineContent);
                }
            } else {
                m = pStart.matcher(lineContent);

                if (m.matches()) {
                    // comment is starting
                    insideBlock = true;
                    
                    comment = new CommentLine(lineContent, line, m.start(1));
                    this.commentArray.add(comment);
                    Log.log(LogEntry.VERBOSE, "Starting multline comment on line "+line+": "+lineContent);
                }
            }
        }
    }

    /**
     * See SourceParser interface 
     * 
     */
    public boolean isSourceFile(FileID file) {
        return file.name.toLowerCase().endsWith(".py");
    }
}
