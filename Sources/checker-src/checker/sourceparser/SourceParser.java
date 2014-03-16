/**
 * 
 *   Copyright (C) <2006> <Sakari Kääriäinen>
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
import checker.FileID;
import checker.Reference;

/**
 * Interface for a source file language type.
 * 
 */
public interface SourceParser {

    /**
     * Is this file a source file of this type? (for example, in java only
     * *.java files qualify, but in C *.c and MakeFile are both accepted)
     * <li>Is this enough, or should the contents be analyzed?
     * 
     * @return
     */
    public boolean isSourceFile(FileID file);

    /**
     * Scans through single file. Extracts the comments and detects any
     * information needed for getReferences().
     * 
     * @param fileContent
     */
    public void scanFile(FileID file, ArrayList<String> fileContent) throws Exception;

    /**
     * Returns the source file's comments. Must be called after scanFile().
     */
    public ArrayList<CommentLine> getComments();

    /**
     * Gets the detected references. Must be called only after all source files
     * in the package have been processed by scanFile().
     * 
     * 
     * @return List of found references.
     * 
     */
    public ArrayList<Reference> getReferences();
}
