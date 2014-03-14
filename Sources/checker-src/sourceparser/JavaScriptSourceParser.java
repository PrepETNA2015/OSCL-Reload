/**
 * Copyright (C) 2008 Mika Pajanen, Tuomo Jorri
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 * MA 02111-1307 USA
 *
 * Also add information on how to contact you by electronic and paper mail.
 */
package checker.sourceparser;

import java.util.ArrayList;
import checker.FileID;
import checker.CommentLine;
import checker.Reference;
import java.io.File;

/**
 * @classname   JavaScriptSourceParser
 * @version     $Id$
 * @date        2008-11-13
 * @author      Mika Pajanen, Tuomo Jorri
 */

/**
 * JavaScriptSourceParser parsers JavaScript source files for comments.
 *
 * Based very heavily on the JavaSourceParser class.
 */
public class JavaScriptSourceParser implements SourceParser {
    private final static String COMMENT_SINGLE_LINE = "//";
    private final static String COMMENT_MULTI_LINE_START = "/*";
    private final static int COMMENT_LENGTH = 2;

    private ParserComments pComments = null;

    /**
     * Constructor.
     */
    public JavaScriptSourceParser() {
    }

    /**
     * Checks if the given file is a JavaScript source file.
     */
    public boolean isSourceFile(FileID file) {
        return file.name.toLowerCase().endsWith(".js");
    }

    /**
     * Scans the file for comments.
     *
     * Must be executed before getComments.
     */
    public void scanFile(FileID file, ArrayList<String> fileContent) throws Exception {
        this.pComments = new ParserComments();
        SourceParserLine pLine = new SourceParserLine();

        try {
            for (int objectNumber = 0; objectNumber < fileContent.size(); objectNumber++) {
                pLine.line = fileContent.get(objectNumber);

                if (pLine.line.contains(COMMENT_MULTI_LINE_START)
                    || pLine.line.contains(COMMENT_SINGLE_LINE)
                    || pLine.commentSection) {
                    for (pLine.charPosition = 0; pLine.charPosition < pLine.line.length(); pLine.charPosition++) {
                        // check if we find ' or ". if we do, set the flags so we'll skip the comment search
                        pLine = this.pComments.findVariableSection(pLine);

                        if (pLine.commentSection
                            && !pLine.quotationSection
                            && !pLine.apostropheSection
                            && pLine.charPosition < pLine.line.length()) {
                            // ok, we're looking for comments and are not within ' or ", try looking for the end of the comment and set the flag
                            pLine = this.pComments.scanCommentSection(pLine);
                        }

                        if (!pLine.commentSection
                            && !pLine.quotationSection
                            && !pLine.apostropheSection
                            && (pLine.charPosition <= (pLine.line.length() - COMMENT_LENGTH))) {
                            // now look for the comments
                            pLine = this.pComments.scanLine(pLine);
                        }

                        pLine = this.pComments.endVariableSection(pLine);
                    }
                }

                // reset for next line
                pLine.lineNumber++;
                pLine.startPosition = 0;
                pLine.quotationSection = false;
                pLine.apostropheSection = false;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Always returns null as JavaScript files do not inherently have references present.
     */
    public ArrayList<Reference> getReferences() {
        /* JavaScript files do not inherently know anything about references */
        return new ArrayList<Reference>();
    }

    /**
     * Returns the comments that were found from the file.
     */
    public ArrayList<CommentLine> getComments() {
        return this.pComments.getComments();
    }
}
