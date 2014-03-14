/**
 * 
 *   Copyright (C) <2006> <Sakari Kääriäinen, Mika Rajanen>
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

import checker.FileID;

/**
 * Class for creating SourceParser instances.
 * 
 * @Classname 	SourceParserFactory.java	
 * @Version 	1.18
 * @Date 	15.2.2007
 * @author 	Group 14, Mika Rajanen
 */
public class SourceParserFactory {

    /**
     * Creates a new SourceParser object that represents the given source type.
     * Detection of the source file is done based on the filename (extension or
     * the full name).
     * <li>It's assumed that there are no conflicts in filenames of different
     * source types.
     * <li>Only one SourceParser object is created per source type, so it's
     * permitted to use this object to store information required by reference
     * detection.
     * 
     * @param FileID
     * 				file is the source file candidate
     * @return SourceParser
     * 				parser for parsing supported source code
     */
    public static SourceParser createSourceParser(FileID file) throws Exception  {
        /* Creates New SourceParser Objects */
        SourceParser parser = null;

        try {
            if (isSourceFile(file)) {
                /* attempt to determine what programming language is in question */

                /* Java? */
                parser = new JavaSourceParser();

                if (parser.isSourceFile(file)) {
                    return parser;
                }

                /* C++? */
                parser = new CppSourceParser();
            
                if (parser.isSourceFile(file)) {
                    return parser;
                }

                /* PHP? */
                parser = new PHPSourceParser();

                if (parser.isSourceFile(file)) {
                    return parser;
                }

                /* maybe JavaScript? */
                parser = new JavaScriptSourceParser();

                if (parser.isSourceFile(file)) {
                    return parser;
                }

                /* ADD NEW PARSERS HERE */

                parser = new PythonSourceParser();

                if (parser.isSourceFile(file)) {
                    return parser;
                }

                /* no idea, let's default to null */
                parser = null;
            }
        } catch (Exception e) {
            throw e;
        }

        return parser;
    }

    /**
     * Is the given file a source file? (any type)
     * 
     * @param file
     *            Source file candidate.
     * @return true if the file is a source file.
     */
    public static boolean isSourceFile(FileID file) {

	boolean foundSourceFile = false;

	/* Make sure that all letters are lower case */
	String fName = file.name.toLowerCase();

	if( fName.endsWith(".java")
		|| fName.endsWith(".cpp") 
		|| fName.endsWith(".c")
		|| fName.endsWith(".cc")
		|| fName.endsWith(".h")
		|| fName.endsWith(".hpp")
		|| fName.endsWith(".cxx")
		|| fName.endsWith(".hxx")
		|| fName.endsWith(".hc")

		/* Add new file name extension here for new source code parser */

		|| fName.endsWith(".php")
        || fName.endsWith(".js")
        || fName.endsWith(".py")){ 
	    /* Found supported source file */
	    foundSourceFile=true;
	}

	return foundSourceFile;
    }

}	
