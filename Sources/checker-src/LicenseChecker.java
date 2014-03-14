/**
 * 
 *   Copyright (C) 2006 Sakari K��ri�inen, Lauri Koponen
 *   Copyright (C) 2008 Jyrki Laine
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
 */
package checker;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.event.EventListenerList;

import checker.filepackage.FilePackage;
import checker.filepackage.FilePackageFactory;
import checker.filepackage.PackageFile;
import checker.license.ForbiddenPhrase;
import checker.license.License;
import checker.license.LicenseDatabase;
import checker.license.LicenseException;
import checker.license.Tag;
import checker.matching.LicenseMatch;
import checker.matching.ForbiddenPhraseMatch;
import checker.matching.LicenseMatcher;
import checker.matching.MatchPosition;
import checker.repository.RepositoryFactory;
import checker.sourceparser.SourceParser;
import checker.sourceparser.SourceParserFactory;
import checker.copyright.CopyrightFile;
import checker.copyright.CopyrightHolder;
import checker.event.*;

/**
 * The main program.
 */
public class LicenseChecker {

	/**
	 * Subdirectory of the main program that contains the license data files
	 */
	private static final String licenseDirectory = "licenses";

	/**
	 * Filename in the main program directory where the settings are saved.
	 */
	private static final String settingsFileName = ".oslc2_settings";

	private static final double MATCH_THRESHOLD = 0.10;

	private static final String LICENSE_FILENAME_PATTERNS = "LICENSE\\..*,LICENSE,LICENCE\\..*,LICENCE,LICENSING\\..*,LICENSING,COPYING\\..*,COPYING";

	private static Properties properties = null;

	/**
	 * List of all supported license file name patterns
	 */
	private static String[] licenseFileNames;

	/**
	 * Container for license data.
	 */
	private LicenseDatabase licenseDatabase;

	/**
	 * Package that is being processed.
	 */
	private FilePackage filePackage;

	/**
	 * Files read from the package.
	 */
	private ArrayList<FileID> files;

	/**
	 * List of source files.
	 */
	private ArrayList<FileID> sourceFiles;

	/**
	 * List of license files.
	 */
	private ArrayList<FileID> licenseFiles;

	/**
	 * List of unknown files
	 */
	private ArrayList<FileID> unknownFiles;

	/**
	 * List of excluded directories
	 */
	private ArrayList<String> excludePaths;

	/**
	 * References between files.
	 */
	//private ArrayList<Reference> references;
	private Map<FileID, ArrayList<Reference>> references;

	/**
	 * License matches found.
	 */
	private HashMap<FileID, ArrayList<LicenseMatch>> licenseMatches;

	/**
	 * Set of all found licenses.
	 */
	private Set<License> foundLicenses;

	/**
	 * Number of found files for each license
	 */
	private Map<License, Integer> licenseCounts;

	/**
	 * List of conflicting licenses in a file or imported to a file.
	 */
	private HashMap<Reference, ArrayList<Pair<License, License>>> licenseConflicts;

	/**
	 * Number of conflicting references
	 */
	private int numRefConflicts;

	/**
	 * List of conflicting licenses inside single files.
	 */
	private HashMap<FileID, ArrayList<Pair<License, License>>> internalLicenseConflicts;

	/**
	 * List of conflicting license pairs found in the files.
	 */
	private ArrayList<Pair<License, License>> globalLicenseConflicts;

	/**
	 * List of exceptions created when trying to process a given file.
	 */
	private HashMap<FileID, ArrayList<Exception>> fileExceptions;

	/**
	 * List of non-fatal exceptions that don't crash the whole program and that
	 * are not tied to a particular file
	 */
	private ArrayList<Exception> genericExceptions;

	/**
	 * Listeners listening to processing events
	 */
	private EventListenerList listenerList;

	/**
	 * Is processing canceled?
	 */
	private boolean canceled;
    
	static {
		loadLicenseFilenamePatterns();
		String licensePatterns = getSetting("license_filename_patterns");
		if (licensePatterns != null) {
			licenseFileNames = licensePatterns.split(",");
		}
	}

	private static ArrayList<CopyrightFile> copyrightFiles;

	private static ArrayList<CopyrightFile> nonCopyrightFiles;

	private static String reportFileName;

	/**
	 * The constructor.
	 * 
	 */
	public LicenseChecker() {
		listenerList = new EventListenerList();
		excludePaths = new ArrayList<String>();
		copyrightFiles = new ArrayList<CopyrightFile>();
		nonCopyrightFiles = new ArrayList<CopyrightFile>();
	}

	private void reset() {
		sourceFiles = new ArrayList<FileID>();
		licenseFiles = new ArrayList<FileID>();
		unknownFiles = new ArrayList<FileID>();
		references = null;
		licenseMatches = new HashMap<FileID, ArrayList<LicenseMatch>>();
		foundLicenses = new HashSet<License>();
		licenseCounts = new HashMap<License, Integer>(); 
		licenseConflicts = new HashMap<Reference, ArrayList<Pair<License, License>>>();
		internalLicenseConflicts = new HashMap<FileID, ArrayList<Pair<License, License>>>();
		globalLicenseConflicts = new ArrayList<Pair<License, License>>();
		fileExceptions = new HashMap<FileID, ArrayList<Exception>>();
		genericExceptions = new ArrayList<Exception>();
	}

	/**
	 * Read user's home directory path.
	 * 
	 * @return Path to user's home directory
	 */
	private static String getHomeDir() {
		Properties props = System.getProperties();
		String path = props.getProperty("user.home");

		return path + File.separator;
	}

	/**
	 * 
	 * @param l
	 */
	public void addProcessingListener(LicenseProcessListener l) {
		listenerList.add(LicenseProcessListener.class, l);
	}

	/**
	 * 
	 * @param l
	 */
	public void removeProcessingListener(LicenseProcessListener l) {
		listenerList.remove(LicenseProcessListener.class, l);
	}

	/**
	 * Cancel processPackage().
	 * 
	 * TBD: check thread-safety!
	 */
	public void cancel() {
		canceled = true;
	}

	protected void fireProcessBegunEvent() {
		LicenseEvent event = null;

		//Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == LicenseProcessListener.class) {
				// Lazily create the event:
				if (event == null)
					event = new LicenseEvent();
				((LicenseProcessListener)listeners[i+1]).processBegun(event);
			}
		}
	}

	protected void fireProcessCanceledEvent() {
		LicenseEvent event = null;

		//Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == LicenseProcessListener.class) {
				// Lazily create the event:
				if (event == null)
					event = new LicenseEvent();
				((LicenseProcessListener)listeners[i+1]).processCancelled(event);
			}
		}
	}

	protected void fireProcessEndedEvent() {
		LicenseEvent event = null;

		//Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == LicenseProcessListener.class) {
				// Lazily create the event:
				if (event == null)
					event = new LicenseEvent();
				((LicenseProcessListener)listeners[i+1]).processEnded(event);
			}
		}
	}

	protected void fireProcessEvent(FileID file, int index, int count) {
		LicenseProcessEvent event = null;

		//Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == LicenseProcessListener.class) {
				// Lazily create the event:
				if (event == null)
					event = new LicenseProcessEvent(file, index, count);
				((LicenseProcessListener)listeners[i+1]).fileProcessed(event);
			}
		}
	}

	protected void fireFileOpenBegunEvent() {
		LicenseEvent event = null;

		//Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == LicenseProcessListener.class) {
				// Lazily create the event:
				if (event == null)
					event = new LicenseEvent();
				((LicenseProcessListener)listeners[i+1]).fileOpenBegun(event);
			}
		}
	}

	protected void fireFileOpenEndedEvent() {
		LicenseEvent event = null;

		//Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == LicenseProcessListener.class) {
				// Lazily create the event:
				if (event == null)
					event = new LicenseEvent();
				((LicenseProcessListener)listeners[i+1]).fileOpenEnded(event);
			}
		}
	}

	private double matchThreshold() {
		String s;
		try {
			/* this might throw */
			s = (String) getSetting("matchThreshold");
			return Double.valueOf(s);

		} catch (Exception e) {
		}

		/* reset invalid values to default */
		setSetting("matchThreshold", new Double(MATCH_THRESHOLD).toString());
		return MATCH_THRESHOLD;
	}
    
	private static void loadLicenseFilenamePatterns() {
		if (getSetting("license_filename_patterns") == null) {
			setSetting("license_filename_patterns", LICENSE_FILENAME_PATTERNS);
		}
	}

	/**
	 * Gets a list of all supported package type id:s.
	 * 
	 * @return list of supported package types
	 */
	public ArrayList<String> getFilePackageTypes() {
		return FilePackageFactory.getSupportedPackageTypeIDs();
	}

	/**
	 * Gets a list of all supported repository type id:s.
	 * 
	 * @return list of supported repository types
	 */
	public ArrayList<String> getRepositoryTypes() {
		return RepositoryFactory.getSupportedRepositoryTypeIDs();
	}

	/**
	 * Gets the list of parameter descriptions for a given repository.
	 * Descriptions will be returned in the same order as the parameter values
	 * needs to be given to openRepository().
	 * 
	 * @param repositoryTypeID TBD 
	 * @return TBD
	 */
	public ArrayList<String> getRepositoryParameters(String repositoryTypeID)
	throws Exception {
		return RepositoryFactory.getParameterDescriptions(repositoryTypeID);
	}


	/**
	 * Load settings from the settings file.
	 * 
	 * If file loading fails, start with no saved settings.
	 */
	private static void loadSettings()  {
		if(properties != null) return;

		/* load settings */

		try {
			FileInputStream fis = new FileInputStream(getHomeDir() + settingsFileName);

			// TODO: how to get rid of this warning?
			properties = new Properties();
			properties.load(fis);

			fis.close();

		} catch (FileNotFoundException e) {
			/* settings file not found, start from scratch */
			properties = new Properties();
		} catch (IOException e) {
			/* loading settings file failed, start from scratch */
			properties = new Properties();
		}
	}

	/**
	 * Gets the given setting. (reads the setting from a setting file if
	 * necessary)
	 * 
	 * @param settingName Name of the setting to read
	 * @return Value of the setting
	 */
	public static String getSetting(String settingName) {
		if(properties == null) loadSettings();

		return properties.getProperty(settingName);
	}

	/**
	 * Sets the value of a given setting.
	 * 
	 * @param settingName Name of the setting to set
	 * @param value New value of the setting
	 */
	public static void setSetting(String settingName, String value) {
		if(properties == null) loadSettings();
		properties.setProperty(settingName, value);
		saveSettings();
	}

	/**
	 * Saves the settings to a setting file.
	 * 
	 */
	public static void saveSettings() {
		if(properties == null) return;

		try {
			FileOutputStream fos = new FileOutputStream(getHomeDir() + settingsFileName);

			properties.store(fos, "OSLC3.0 settings");

			fos.close();
		} catch(FileNotFoundException e) {
			// TODO: settings file not accessible
		} catch(IOException e) {
			// TODO: saving settings failed
		}
	}

	/**
	 * Opens the given package.
	 * 
	 * @param file Package to be opened
	 */
	public void openPackage(File file) throws Exception {
		Log.log(LogEntry.DEBUG, "Opening package: " + file);
		filePackage = FilePackageFactory.createFilePackage(file);
	}

	/**
	 * Opens the given repository.
	 * 
	 * @param repositoryTypeID TBD
	 * @param parameters TBD
	 */
	public void openRepository(String repositoryTypeID, String... parameters)
	throws Exception {
		filePackage = RepositoryFactory.createRepository(repositoryTypeID,
				parameters);
	}

	/**
	 * Get the current package.
	 * 
	 * @return Currently open package
	 */
	public FilePackage getPackage() {
		return filePackage;
	}

	/**
	 * Gets a list of source files.
	 * 
	 * @return List of source files
	 */
	public ArrayList<FileID> getSourceFiles() {
		return sourceFiles;
	}

	/**
	 * Gets a list of license files. (not license data files)
	 * 
	 * @return List of license files
	 */
	public ArrayList<FileID> getLicenseFiles() {
		return licenseFiles;
	}

	/**
	 * Gets a list of files of unknown type.
	 * 
	 * @return List of unknown files
	 */
	public ArrayList<FileID> getUnknownFiles() {
		return unknownFiles;
	}

	/**
	 * Gets a list of license matches for a given file.
	 * 
	 * @param file Get license matches for this file
	 * @return Found matches for given file, null if file has not been processed
	 */
	public ArrayList<LicenseMatch> getLicenseMatches(FileID file) {
		return licenseMatches.get(file);
	}

	/**
	 * Returns the maximum match-% found for a given license.
	 * @param license
	 * @return
	 */
	/*
	public float getMaxMatchPr(License license) {
		float maxPr = 0;
		for (ArrayList<LicenseMatch> matches : licenseMatches.values()) {
			for (LicenseMatch match : matches) {
				if (license.getId().equals(match.getLicense().getId())) {
					if (match.getMatchPr() > maxPr)
						maxPr = match.getMatchPr();
					break;
				}
			}
		}
		return maxPr;
	}
	 */

	/**
	 * Returns maximum match-%:s found for each license.
	 */
	public HashMap<License,Float> getMaxMatchPrs() {

		// init % counts
		HashMap<License,Float> result = new HashMap<License,Float>();
		for (License license : licenseDatabase.getLicenses())
			result.put(license,new Float(0));
		for (License license : licenseDatabase.getForbiddenPhrases())
			result.put(license,new Float(0));
		for (License license : licenseDatabase.getLicenseExceptions())
			result.put(license,new Float(0));

		// resolve max counts
		for (ArrayList<LicenseMatch> matches : licenseMatches.values()) {
			for (LicenseMatch match : matches) {
				License license = match.getLicense();
				float matchPr = match.getMatchPr();
				if (result.containsKey(license) && result.get(license) < matchPr)
					result.put(license, matchPr);
			}
		}		

		return result;
	}


	public Map<License, Integer> getLicenseCounts() {
		return licenseCounts;
	}

	/**
	 * Gets references of given file.
	 * 
	 * @param file Get references for this file
	 * @return Found references for given file, null if file has 
	 * 			no references
	 */
	public ArrayList<Reference> getReferences(FileID file) {

		if(references == null) return null;

		return references.get(file);
	}

	/**
	 * Get a set of all found licenses.
	 */
	public Set<License> getFoundLicenses() {
		return foundLicenses;
	}


	/**
	 * Get the list of license conflicts for given reference. A license
	 * pair is returned for each conflicting license, with Pair.e2 marking
	 * the license in the referenced file.
	 * 
	 * @param ref Return conflicts found for this reference 
	 * @return Found license conflicts for given reference, null if
	 * 			reference has no conflicts
	 */
	public ArrayList<Pair<License, License>> getLicenseConflicts(Reference ref) {
		return licenseConflicts.get(ref);
	}

	/**
	 * Get the number of license conflicts found in file references.
	 * 
	 * @return Number of license conflicts found in file references
	 */
	public int getNumReferenceConflicts() {
		return numRefConflicts;
	}


	/**
	 * Get the list of license conflicts inside a file. A license pair is returned
	 * for each conflicting pair of licenses. If licenses a and b conflict
	 * with each other, both pairs (a, b) and (b, a) are returned.
	 *  
	 * @param file Return conflicts found for this file
	 * @return List of license conflicts found inside the given file 
	 */
	public ArrayList<Pair<License, License>> getInternalLicenseConflicts(FileID file) {
		return internalLicenseConflicts.get(file); 
	}

	/**
	 * Return found global license conflicts. A license pair is returned
	 * for each conflicting pair of licenses. If licenses a and b conflict
	 * with each other, both pairs (a, b) and (b, a) are returned.  
	 * 
	 * @return List of conflicts between any/all found licenses 
	 */
	public ArrayList<Pair<License, License>> getGlobalLicenseConflicts() {
		return globalLicenseConflicts;
	}

    /**
     * Return all files found in the package
     *
     * @return All files found in the package
     */
    public ArrayList<FileID> getFiles() {
        return files;
    }


	/**
	 * Gets the list of Exception:s generated while processing a given file.
	 * 
	 * @param file Get exceptions for this file
	 * @return List of exceptions that happened during processing.
	 */
	public ArrayList<Exception> getFileExceptions(FileID file) {
		return fileExceptions.get(file);
	}

	/**
	 * Gets a list of supported licenses.
	 * 
	 * @return Set of licenses found in the license database.
	 */
	public HashSet<License> getLicenses() {
		return licenseDatabase.getLicenses();
	}

	/**
	 * Reads the file contents.
	 * 
	 * @param file Return the contents of this file
	 * @return Contents of the given file, one String for each text line.
	 */
	public ArrayList<String> readFile(FileID file) throws Exception {

		fireFileOpenBegunEvent();
		ArrayList<String> contents = filePackage.readFile(file);
		fireFileOpenEndedEvent();

		return contents;
	}

	/**
	 * Reads the source file's comments.
	 * 
	 * @param file Return the comments from this file
	 * @return Contents of the comments in the given file, one CommentLine
	 * for each text line.
	 */
	public ArrayList<CommentLine> readComments(FileID file) throws Exception {

		SourceParser parser;

		parser = SourceParserFactory.createSourceParser(file);

		/* get file contents */
		ArrayList<String> fileContent = filePackage.readFile(file);

		parser.scanFile(file, fileContent);
		return parser.getComments();
	}

	/**
	 * Add a path to be excluded from processing.
	 * 
	 * @param path Path inside FilePackage, case sensitive
	 */
	public void addExcludePath(String path) {
		excludePaths.add(path);
	}

	/**
	 * Check if a file is excluded.
	 * 
	 * @param path Path to file inside open package
	 * @return true if file is excluded, false if ok
	 */
	private boolean isExcluded(FileID path) {

		if (excludePaths.isEmpty())
			return false;

		String s = path.toString();

		for (int i = 0; i < excludePaths.size(); i++) {
			if (s.startsWith(excludePaths.get(i))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Build a HashMap from a list of Reference objects
	 * 
	 * @param refList List of file references
	 * @return Map with references connected to FileIDs of the referenced files
	 */
	private Map<FileID, ArrayList<Reference>> referencesToMap(ArrayList<Reference> refList) {
		Map<FileID, ArrayList<Reference>> map = new HashMap<FileID, ArrayList<Reference>>();

		for(Reference r : refList) {

			/* get the reference list for this FileID */
			ArrayList<Reference> list = map.get(r.sourceFile);

			if(list == null) {
				/* this FileID does not exist in map, create a new list for it */
				list = new ArrayList<Reference>();
				map.put(r.sourceFile, list);
			}

			/* add reference to list */
			list.add(r);
		}

		return map;
	}

	/**
	 * Get the number of files in a FilePackage
	 * 
	 * @param filePackage Count the number of files in this FilePackage
	 * @return Number of files in the FilePackage
	 */

	private int numFiles(FilePackage filePackage) {
		int count = 0;
		Iterator<PackageFile> i = filePackage.iterator();

		Log.log(LogEntry.VERBOSE, "Scanning files...");

		while (i.hasNext()) {
			if(canceled)
				return count;
			i.next();
			count++;
		}

		return count;
	}

	/**
	 * Combined list of licenses and forbidden phrases
	 */
	private ArrayList<License> allLicenses;

	private void processSourceFile(PackageFile pf) {
		FileID file = pf.getFileID();

		try {

			Log.log(LogEntry.VERBOSE, "Processing file: " + file);

			SourceParser parser = SourceParserFactory.createSourceParser(file);

			// Scan the file
			ArrayList<String> fileContent = pf.getContents();
			//Log.log(LogEntry.DEBUG, "file:" + fileContent);
			parser.scanFile(file, fileContent); 

			// Match the licenses
			ArrayList<LicenseMatch> matches = new ArrayList<LicenseMatch>();

			/*
			 * for (LicenseMatcher.MatchAlgorithm algorithm :
			 * LicenseMatcher.MatchAlgorithm .values()) for (License
			 * license : licenseDatabase.getLicenses())
			 * matches.add(LicenseMatcher.match(parser .getComments(),
			 * license, algorithm));
			 */

			ArrayList<CommentLine> comments = parser.getComments();

			matches.addAll(LicenseMatcher.match(comments,
					allLicenses,
					LicenseMatcher.MatchAlgorithm.EXACT,
					matchThreshold()));

			/*for (License license : licenseDatabase.getLicenses()) {
				LicenseMatch match = LicenseMatcher.match(parser.getComments(),
						license, LicenseMatcher.MatchAlgorithm.EXACT);
				if(match != null) {
					matches.add(match);
				}
			}*/

			matches.addAll(processModuleLicense(fileContent));

			licenseMatches.put(file, matches);

			//Check wihch files include copyright information
			CopyrightFile newCopyrightFile = new CopyrightFile(file.path, 
					file.name, comments);
			if (newCopyrightFile.hasCopyrightHolders()) {
				copyrightFiles.add(newCopyrightFile);
			} else {
				nonCopyrightFiles.add(newCopyrightFile);
			}

			// Get references from scanned source files
			if(references == null) {
				references = referencesToMap(parser.getReferences());
			} else {
				references.putAll(referencesToMap(parser.getReferences()));
			}

		} catch (Exception e) {
			/* if this is the first exception for this file,
			 * create a new exception list */
			if (!fileExceptions.containsKey(file))
				fileExceptions.put(file, new ArrayList<Exception>());

			fileExceptions.get(file).add(e);
		}
	}

	private final static String MODULE_LICENSE_START = "MODULE_LICENSE(\"";
	private final static String MODULE_LICENSE_END = "\")";

	private ArrayList<LicenseMatch> processModuleLicense(ArrayList<String> fileContent)
	{
		ArrayList<LicenseMatch> matches = new ArrayList<LicenseMatch>();

		/* This code supports only one MODULE_LICENSE texts per line */
		for (int l = 0; l < fileContent.size(); l++) {
			String line = fileContent.get(l);
			int modStart = line.indexOf(MODULE_LICENSE_START);
			if(modStart >= 0) {
				int modEnd = line.indexOf(MODULE_LICENSE_END, modStart+MODULE_LICENSE_START.length());
				if(modEnd >= 0) {
					String modText = line.subSequence(modStart+MODULE_LICENSE_START.length(), modEnd).toString();
					Log.log(LogEntry.DEBUG, "Found MODULE_LICENSE: " + modText);                    
					License license = licenseDatabase.getModuleLicense(modText);
					if (license != null) {
						MatchPosition mp = new MatchPosition(l, modStart,
								l, modEnd+MODULE_LICENSE_END.length(),
								0, 0,
								0, 0,
								0, 0
						);

						ArrayList<MatchPosition> positions  = new ArrayList<MatchPosition>();
						positions.add(mp);

						LicenseMatch m = null;

						if (license instanceof ForbiddenPhrase) {
							m = new ForbiddenPhraseMatch(license, mp);
						}
						else {
							m = new LicenseMatch(license, positions);
						}

						m.setMatchPr(1.0f);
						matches.add(m);

						Log.log(LogEntry.DEBUG, "Added match: " + m);                    
					}
				}
			}
		}
		return matches;
	}


	private void processLicenseFile(PackageFile pf) {
		FileID file = pf.getFileID();

		try {

			// Get file contents and turn them into CommentLine:s
			ArrayList<String> fileContent = pf.getContents();
			ArrayList<CommentLine> fileContentAsComments = new ArrayList<CommentLine>();
			int lineNumber = 0;
			for (String line : fileContent)
				fileContentAsComments.add(new CommentLine(line,
						lineNumber++, 0));

			// Match the licenses
			ArrayList<LicenseMatch> matches = new ArrayList<LicenseMatch>();
			// for (AlgorithmDescription algorithm : algorithms)
			/*
			for (LicenseMatcher.MatchAlgorithm algorithm : LicenseMatcher.MatchAlgorithm
					.values())
				for (License license : licenseDatabase.getLicenses())
					matches.add(LicenseMatcher.match(
                        fileContentAsComments, license, algorithm));
			licenseMatches.put(file, matches);
			 */

			matches.addAll(LicenseMatcher.match(fileContentAsComments,
					allLicenses,
					LicenseMatcher.MatchAlgorithm.EXACT,
					matchThreshold()));
			/*					for (License license : licenseDatabase.getLicenses()) {
				LicenseMatch match = LicenseMatcher.match(fileContentAsComments,
						license, LicenseMatcher.MatchAlgorithm.EXACT);
				if(match != null) {
					matches.add(match);
				}
			}*/

			licenseMatches.put(file, matches);

		} catch (Exception e) {
			/* if this is the first exception for this file,
			 * create a new exception list */
			if (!fileExceptions.containsKey(file))
				fileExceptions.put(file, new ArrayList<Exception>());

			fileExceptions.get(file).add(e); 
		}
	}

	/**
	 * Runs all processing steps for the package.
	 * <li>Sort files to source files,license files and unknown files
	 * <li>Scan source files
	 * <li>Scan license files
	 * <li>Determine references
	 * <li>Determine license conflicts
	 * 
	 */
	public void processPackage() {

		int index = 0, count = 0;

		// reset cancel state
		canceled = false;

		// hack
		if(licenseDatabase == null)
			buildLicenseDatabase();

		// hack2?
		reset();

		try {
			files = new ArrayList<FileID>();

			/* numFiles() will take a while so hack-optimize it away */
			if(listenerList.getListenerCount() > 0) {
				count = numFiles(filePackage);
				if(canceled) {
					fireProcessCanceledEvent();
					return;
				}
				index = 0;
				Log.log(LogEntry.VERBOSE, "Found " + count + " files.");
			}

			fireProcessBegunEvent();

			Iterator<PackageFile> i = filePackage.iterator();

			while(i.hasNext()) {

				if(canceled) {
					// TODO: need to clear some state before canceling?
					fireProcessCanceledEvent();
					return;
				}

				PackageFile pf = i.next();
				FileID file = pf.getFileID();

				/* ignore files in excluded directories */
				if(isExcluded(file)) {
					Log.log(LogEntry.DEBUG, file + ": excluded");
					continue;
				}

				files.add(file);

				// Is the file a source file?
				if (SourceParserFactory.isSourceFile(file)) {
					Log.log(LogEntry.DEBUG, file + ": source file");

					sourceFiles.add(file);

					fireProcessEvent(file, index, count);
					processSourceFile(pf);
					index++;

					continue;
				}

				// Is it license file?
				boolean known = false;
				for (String licenseFileName : licenseFileNames) {
					// Use HashSet if licenseFileNames is longer
					// if (file.name.equalsIgnoreCase(licenseFileName)) {
					Pattern p = Pattern.compile("^" + licenseFileName + "$", Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(file.name);
					if (m.matches()) {
						Log.log(LogEntry.DEBUG, file + ": license file");
						licenseFiles.add(file);
						fireProcessEvent(file, index, count);
						processLicenseFile(pf);
						index++;
						known = true;
						break;
					}
				}

				if(!known) {

					// It must be unknown file
					Log.log(LogEntry.DEBUG, file + ": unknown file"); 

					unknownFiles.add(file);
					index++;
				}

			}

			if(files.size() == 0) {
				Log.log(LogEntry.ERROR, "No source files found");
			}

			// Clean found license info
			processFoundLicenses();

			// Clean found references
			processFoundReferences();

			// All files have been scanned, determine license conflicts
			findLicenseConflicts();

			// Collect statistics
			countLicenses();

		} catch (Exception e) {
			ErrorManager.error(e); // TODO This kills the program
			// for all exceptions, more
			// fine-grained handling should
			// be implemented.
		}

		fireProcessEndedEvent();
	}

	/**
	 * Determine best found licenses for all files.
	 */
	private void processFoundLicenses() {

		/* Most of the cleaning is done in LicenseMatcher */

		/* Remove licenses with found sister licenses,
		 * remove forbidden phrases if all other licenses allow it,
		 * remove license exceptions if not connected with another license,
		 * remove duplicate licenses */

		for (FileID f : files) {
			ArrayList<LicenseMatch> matches = getLicenseMatches(f);
			if(matches == null) continue;

			/* set of all found licenses */
			Set<License> s = new HashSet<License>();
			for(LicenseMatch m : matches) {
				s.add(m.getLicense());
			}

			boolean done; /* done with checking? */
			do {
				done = true;
				for (LicenseMatch m : matches) {
					License matchLicense = m.getLicense();


					/* remove duplicates */
					for (LicenseMatch m2 : matches) {
						License l2 = m2.getLicense();
						if (m == m2) {
							/* don't compare with self */
							continue;
						}

						if (matchLicense.equals(l2)) {
							/* remove duplicate */
							/* must restart loop after remove() */

							/* remove match with smaller match% */
							/* don't remove license from set */
							if (m.getMatchPr() < m2.getMatchPr()) {
								matches.remove(m);
							} else {
								matches.remove(m2);
							}

							done = false;
							break;
						}
					}
					if(done == false) break; /* a duplicate was removed */

					if (matchLicense instanceof ForbiddenPhrase) {

						/* check if forbidden phrase is compatible with all
						 * found licenses */
						boolean compatibleWithAll = true;
						boolean foundOthers = false;
						for (LicenseMatch m2 : matches) {
							License l2 = m2.getLicense();
							if(matchLicense.equals(l2)) {
								/* don't compare with self */
								continue;
							}
							foundOthers = true;
							if (!matchLicense.isCompatible(m2.getLicense(),
									Reference.ReferenceType.IMPORT)) {
								//Log.log(LogEntry.DEBUG, "Forbidden phrase " + matchLicense + " not compatible with " + m2.getLicense());
								compatibleWithAll = false;
								break;
							}
						}

						if(foundOthers && compatibleWithAll) {
							/* forbidden phrase is compatible with all
							   found licenses, ignore the match */

							//Log.log(LogEntry.DEBUG, "Forbidden phrase " + matchLicense + " compatible with all found licenses");

							/* must restart loop after remove() */
							ForbiddenPhrase fp = (ForbiddenPhrase) matchLicense;
							if( ! fp.isKernelLicense())
							{
								s.remove(matchLicense);
								matches.remove(m);
								done = false;
							}

							break;
						}
					}

					if (matchLicense instanceof LicenseException) {
						/* check if a parent license was found for this 
						 * exception */
						LicenseException exception = (LicenseException) matchLicense;

						boolean parentFound = false;
						for (LicenseMatch m2 : matches) {
							if (exception.getParentLicenses().contains(m2.getLicense())) {
								parentFound = true;
								break;
							}
						}

						if(!parentFound) {
							/* parent not found, remove exception from matches */

							/* must restart loop after remove() */
							s.remove(matchLicense);
							matches.remove(m);
							done = false;
							break;
						}
					}

					/* check sister license */
					License sisterLicense = matchLicense.getSisterLicense(); 
					if (s.contains(sisterLicense)) {
						/* must restart loop after remove() */
						s.remove(matchLicense);
						matches.remove(m);
						done = false;
						break;
					}
				}
			} while(!done);
		}
	}

	/**
	 * Expand "*.java" references to all files matching the pattern
	 */
	private void processFoundReferences() {

		if (references == null) return;

		for (ArrayList<Reference> refList : references.values()) {

			ArrayList<Reference> addList = null;
			ArrayList<Reference> delList = null;

			for (Reference r : refList) {
				if ((r.referenceType == Reference.ReferenceType.IMPORT
						|| r.referenceType == Reference.ReferenceType.STATIC_INCLUDE)
						&& (r.targetFile != null)
						&& (r.targetFile.name != null)
						&& r.targetFile.name.startsWith("*")) {

					/* need to glob */

					String suffix = r.targetFile.name.substring(1);

					boolean foundTarget = false;

					for (FileID f : sourceFiles) {
						/* the complex if says:
						 * (if both paths are null
						 *    or path equals target path)
						 * and suffix matches, then... */
						if (((f.path == null) && (r.targetFile.path == null))
								|| ((f.path != null)
										&& f.path.equals(r.targetFile.path))
										&& f.name.endsWith(suffix)) {

							/* file matches pattern, create new reference */
							Reference newRef = new Reference(r.sourceFile, f);

							/* replace * in declaration with first part of filename */
							newRef.declaration = r.declaration.replaceAll("\\*",
									f.name.substring(0, f.name.indexOf('.')));
							newRef.information = r.information;
							newRef.referenceType = r.referenceType;

							if (addList == null)
								addList = new ArrayList<Reference>();
							addList.add(newRef);

							foundTarget = true;
						}
					}

					if(foundTarget) {
						/* found a target, forget the original reference */
						if (delList == null)
							delList = new ArrayList<Reference>();
						delList.add(r);
					}
				}
			}

			if(delList != null)
				refList.removeAll(delList);

			if(addList != null)
				refList.addAll(addList);
		}
	}

	/**
	 * Scans the licenseMatches and references, finds any conflicts.
	 * 
	 * Licenses found in source files are compared with other licenses found in the same and referenced source files.
	 * Licenses found in license files are compared only with other licenses found in the same file
	 * 
	 * Also all found licenses are checked for conflicts with any other found licenses.
	 */
	private void findLicenseConflicts() {

		/* check all files against references */
		numRefConflicts = 0;

		for(FileID f : files) {
			ArrayList<LicenseMatch> matches = getLicenseMatches(f);

			if(matches == null) continue;

			for(LicenseMatch m : matches) {

				License l = m.getLicense();

				/* check file against itself */
				for(LicenseMatch m2 : matches) {
					License l2 = m2.getLicense();

					if(l == l2) {
						/* license is always compatible with itself */
						continue;
					}

					if(!l2.isCompatible(l,
							Reference.ReferenceType.IMPORT)) {

						ArrayList<Pair<License, License>> conflicts
						= internalLicenseConflicts.get(f);

						if(conflicts == null) {
							conflicts = new 
							ArrayList<Pair<License, License>>();

							internalLicenseConflicts.put(f, conflicts);
						}

						conflicts.add(new Pair<License, License>
						(l, l2));
					}
				}

				/* check references for license conflicts */
				ArrayList<Reference> refList = getReferences(f);
				if(refList == null) continue;

				for(Reference ref : refList) {

					if((ref.referenceType != Reference.ReferenceType.IMPORT
							&& ref.referenceType != Reference.ReferenceType.STATIC_INCLUDE)) {
						continue;
					}

					if(ref.targetFile == null) continue;

					ArrayList<LicenseMatch> refMatchList
					= getLicenseMatches(ref.targetFile);

					if(refMatchList == null) continue;

					for(LicenseMatch refmatch : refMatchList) {

						if(refmatch.getLicense() == l) {
							/* license is always compatible with itself */
							continue;
						}

						if(!refmatch.getLicense().isCompatible(l,
								Reference.ReferenceType.IMPORT)) {

							ArrayList<Pair<License, License>> conflicts
							= licenseConflicts.get(ref);

							if(conflicts == null) {
								conflicts = new 
								ArrayList<Pair<License, License>>();

								licenseConflicts.put(ref, conflicts);
							}

							conflicts.add(new Pair<License, License>
							(l, refmatch.getLicense()));
							numRefConflicts++;
						}
					}
				}
			}
		}

		/* Find global conflicts */

		/* collect licenses found in all files */
		for(FileID f : files) {

			ArrayList<LicenseMatch>	matches = getLicenseMatches(f);
			if(matches == null) continue;

			for(LicenseMatch m : matches) {
				foundLicenses.add(m.getLicense());
			}
		}

		for(License l1 : foundLicenses) {
			for(License l2 : foundLicenses) {
				if(l1 == l2) continue;

				if(!l1.isCompatible(l2, Reference.ReferenceType.IMPORT)) {
					globalLicenseConflicts.add(
							new Pair<License, License>(l1, l2));
				}
			}
		}

		//globalLicenseConflicts;


	}

	/**
	 * Collect license statistics
	 */
	private void countLicenses() {
		licenseCounts.clear();

		ArrayList<FileID> files = (ArrayList<FileID>) sourceFiles.clone();
		files.addAll(licenseFiles);
		files.addAll(unknownFiles);

		for(FileID file : files) {
			ArrayList<LicenseMatch>	matches =
				(ArrayList<LicenseMatch>) getLicenseMatches(file);

			if(matches != null) {
				for(int j = 0; j < matches.size(); j++) {

					LicenseMatch match = matches.get(j);
					License foundLicense = match.getLicense();

					/* collect license statistics */
					Integer i = licenseCounts.get(foundLicense);
					if(i == null) {
						i = 1; 
						licenseCounts.put(foundLicense, i);
					} else {
						i++;
						licenseCounts.put(foundLicense, i);
					}
				}
			}
		}
	}

	/**
	 * Reads the license directory and creates a database of licenses.
	 *
     * switched to public /ekurkela
	 */
	public void buildLicenseDatabase() {
		// TODO find out the current directory, append licenseDirectory to it
		// and give it to LicenseDatabase

		// Does this work if user launches the program from command line in some
		// other directory?

		// String userDir = System.getProperty("user.dir");
		// licenseDatabase = new LicenseDatabase(userDir+"\\"+licenseDirectory);
		// // TODO this is system specific

		licenseDatabase = new LicenseDatabase(licenseDirectory);
		licenseDatabase.buildLicenseDatabase();

		allLicenses = new ArrayList<License>(licenseDatabase.getLicenses());
		allLicenses.addAll(licenseDatabase.getForbiddenPhrases());
	}



	/* CLI starts here */

	/* file name given on command line */
	private static String packagename = null;

	/* output references? */
	private static boolean printReferences = false;
	
	/* output license details? */
	private static boolean printDetails = false;

	/* output license summary? */
	private static boolean printSummary = false;

	/* output copyrights? */
	private static boolean printCopyrights = false;

	/* output report*/
	private static boolean printReport = false;

	/* output tags? */
	private static boolean printTags = false;

	/* paths to be excluded */
	private static ArrayList<String> excludePathList = new ArrayList<String>();

	private static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar oslc2.jar [options] [file]");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("  -r        Show file references");
		System.out.println("  -s        Show a summary of found licenses");
		System.out.println("  -t        Show found tags");
		System.out.println("  -v        Verbose (twice for more output)");
		System.out.println("  -h        Help (show this screen)");
		System.out.println("  -x PATH   Ignore PATH inside package");
		System.out.println("");
		System.out.println("When run without arguments, the GUI is started.");
		System.out.println("");
		System.out.println("Examples:");
		System.out.println("  Open GUI:");
		System.out.println("    java -jar oslc2.jar");
		System.out.println("");
		System.out.println("  Scan directory 'test_sources', show file references");
		System.out.println("    java -jar oslc2.jar -r test_sources/");
		System.out.println("");
		System.out.println("  Scan current directory, excluding subdirectory 'test_sources'");
		System.out.println("  java -jar oslc2.jar -x test_sources .");
		System.out.println("");
	}

	private static void excludePath(String path) {
		excludePathList.add(path);
	}

	private static void doExcludePaths(LicenseChecker lc) {
		if (!excludePathList.isEmpty()) {
			Log.log(LogEntry.DEBUG, "Excluding paths:");
			for (int i = 0; i < excludePathList.size(); i++) {
				Log.log(LogEntry.DEBUG, "  " + excludePathList.get(i));
				lc.addExcludePath(excludePathList.get(i));
			}
		}
	}

	/**
	 * Parse command line arguments.
	 * 
	 * @param args Command line arguments
	 */
	private static void parseArguments(String[] args) {

		int verbosity = 1;

		if(args.length < 1) {
			usage();
			System.exit(1);
		}

		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("--")) {
				/* just ignore this argument */
			} else if(args[i].equals("-v")) {
				verbosity++;
			} else if(args[i].equals("-r")) {
				printReferences = true;
			} else if(args[i].equals("-t")) {
				printTags = true;
			} else if(args[i].equals("-h")) {
				usage();
				System.exit(1);
			} else if (args[i].equals("-d")) {
				printDetails = true;
			} else if(args[i].equals("-s")) {
				printSummary = true;
			} else if(args[i].equals("-x")) {
				if(args.length <= i + 1) {
					usage();
					System.exit(1);
				}
				excludePath(args[i + 1]);
				i++;
			} else if(args[i].equals("-c")) {
				printCopyrights = true;
			} else if (args[i].equals("-p")) {
				printReport = true;
			} else if(args[i].charAt(0) == '-') {
				/* unknown option */
				System.out.println("unknown option " + args[i]);
				usage();
				System.exit(1);
			} else {
				/* not an option, must be package name */
				if(packagename != null) {
					/* two package names?! */
					System.out.println("must specify exactly one file");
					usage();
					System.exit(1);
				}
				packagename = args[i];
			}
		}

		/* verbosity matches priority in LogEntry */
		Log.setVerbosity(verbosity);

		/* check that we now have a package name */
		if(packagename == null) {
			System.out.println("must specify exactly one file");
			usage();
			System.exit(1);
		}

		Log.log(LogEntry.DEBUG, "Verbosity level: " + verbosity);
		Log.log(LogEntry.DEBUG, "Package name: " + packagename);

	}

	private void printMatchLocation(LicenseMatch match, FileID file) {
		try {
			ArrayList<MatchPosition> positions = match.getMatchPositions();
			ArrayList<String> contents = filePackage.readFile(file); 

			for (MatchPosition pos : positions) {
				System.out.print("  "); /* some indent */

				System.out.print(pos);
				System.out.print(" \"");

				if(pos.getStartLine() == pos.getEndLine()) {
					/* one line */
					String s = contents.get(pos.getStartLine())
					.substring(pos.getStartCol(), pos.getEndCol() + 1);

					/* if it's too long, cut and insert "..." */
					if(s.length() > 40) {
						s = s.substring(0, 20) + "..." + s.substring(s.length() - 20);
					}
					System.out.print(s);
				} else {

					String s = contents.get(pos.getStartLine()).substring(pos.getStartCol());
					if(s.length() > 20) {
						s = s.substring(0, 20);
					}
					System.out.print(s);
					System.out.print("...");

					s = contents.get(pos.getEndLine()).substring(0, pos.getEndCol() + 1);
					if(s.length() > 20) {
						s = s.substring(s.length() - 20, s.length());
					}
					System.out.print(s);
				}
				System.out.print("\"");
				System.out.println();
			}
		} catch (Exception e) {
			System.out.println(e);
			ErrorManager.error(e);
		}
	}

	/**
	 * Print a summary
	 * @param lc License checker object to get data from
	 * @param counts Calculated license hits
	 */
	private static ArrayList<String> printSummaryTable(LicenseChecker lc,
			Map<License, Integer> counts, ArrayList<String> printedText)
	{
		class LicenseComparator implements Comparator<License> {
			public int compare(License o1, License o2) {
				return o1.getId().compareTo(o2.getId());
			}
		}
		
		/* print first some statistics */
		int srcCount = lc.getSourceFiles().size();
		int licCount = lc.getLicenseFiles().size();
		int unkCount = lc.getUnknownFiles().size();
		int disLicCount = lc.getLicenseCounts().size();
		int confRefCount = lc.getNumReferenceConflicts(); 
		int confGblCount = lc.getGlobalLicenseConflicts().size() / 2;

		int allCount = srcCount + licCount + unkCount;

		printedText.add("");
		printedText.add("");
		printedText.add("Source files:       " + srcCount);
		printedText.add("License files:      " + licCount);
		printedText.add("All files:          " + allCount);
		printedText.add("Distinct licenses:  " + disLicCount);
		printedText.add("Conflicts (ref):    " + confRefCount);
		printedText.add("Conflicts (global): " + confGblCount);

		/* Sort the licenses for output */
		SortedSet<License> licenses =
			new TreeSet<License>(new LicenseComparator());

		licenses.addAll(lc.getFoundLicenses());

		/* get found license conflicts */
		ArrayList<Pair<License, License>> conflicts =
			lc.getGlobalLicenseConflicts();

		/* print header, note the correct number of spaces */
		printedText.add("");
		printedText.add("License        Count   Incompatible with");

		Iterator<License> i = licenses.iterator();
		while(i.hasNext()) {
			License l = i.next();
			Integer count = counts.get(l);
			String confstr = null;

			/* print conflicts also in order */
			SortedSet<License> conflictingLicenses =
				new TreeSet<License>(new LicenseComparator());

			for(Pair<License, License> c : conflicts) {
				if(c.e1 == l) {
					conflictingLicenses.add(c.e2);
				}
			}

			/* collect sorted conflicting licenses to a string */
			for(License c : conflictingLicenses) {
				if(confstr == null) {
					/* first item */
					confstr = c.getId();
				} else {
					confstr = confstr + " " + c.getId();
				}
			}

			if (confstr == null) confstr = "";

			String s = String.format("%-14s %-7d %s",
					l.getId(), count, confstr);
			printedText.add(s);
		}
		return printedText;
	}

	/**
	 * Gets all the files that contain copyright information.
	 * 
	 * @return A list of all the files that contain copyright information
	 */
	public ArrayList<CopyrightFile> getCopyrightFiles() {
		return copyrightFiles;
	}

	/**
	 * Gets all the files that do not contain copyright information.
	 *
	 * @return A list of all the files that do not contain copyright information
	 */
    public ArrayList<CopyrightFile> getNonCopyrightFiles() {
        return nonCopyrightFiles;
    }

	/**
	 * Gets all the copyright holders that are found from the processed files.
	 * The copyright holder objects contain information of all the files in which 
	 * they have a copyright.
	 * 
	 * @return A list of all the copyright holders that are found from the 
	 * processed files.
	 */
	public ArrayList<CopyrightHolder> getCopyrightHolders() {

		/*
		 * Get copyright holders from all the copyright files 
		 * and update the file lists of the copyright holders
		 */
		HashMap<String,CopyrightHolder> allCopyrightHolders = 
			new HashMap<String,CopyrightHolder>();
		for (CopyrightFile copyrightFile : copyrightFiles) {
			ArrayList<CopyrightHolder> newCopyrightHolders = 
				copyrightFile.getCopyrightHolders();
			for (CopyrightHolder newCopyrightHolder : newCopyrightHolders) {

				/*If the copyright holder is new*/
				if (!allCopyrightHolders.containsKey(newCopyrightHolder.name)) {

					/*Put new copyright holder into the list*/
					allCopyrightHolders.put(newCopyrightHolder.name, 
							newCopyrightHolder);
				} else {

					/*
					 * Add new copyright file for the existing 
					 * copyright holder
					 */
					CopyrightHolder equalCopyrightHolder =
						allCopyrightHolders.get(newCopyrightHolder.name);
					equalCopyrightHolder.addCopyrightFile(newCopyrightHolder.getCopyrightFiles().get(0));
				}
			}
		}
		Collection<CopyrightHolder> copyrightHolderCollection = 
			allCopyrightHolders.values();
		Iterator<CopyrightHolder> i = 
			copyrightHolderCollection.iterator();
		ArrayList<CopyrightHolder> returnedCopyrightHolders = 
			new ArrayList<CopyrightHolder>();
		while (i.hasNext()) {
			returnedCopyrightHolders.add(i.next());
		}
		return returnedCopyrightHolders;
	}


	/*
	 * Prints a command line representation of the copyright information
	 */
	private static ArrayList<String> printCopyrights(LicenseChecker lc, ArrayList<String> printedText) {
		printedText.add("");
		printedText.add("");
		ArrayList<CopyrightHolder> copyrightHolders = lc.getCopyrightHolders();
		for (CopyrightHolder thisCopyrightHolder : copyrightHolders) {
			printedText.add("");
			printedText.add(thisCopyrightHolder.name);
			if (thisCopyrightHolder.email != null) {
				printedText.add(thisCopyrightHolder.email);
			}
			if (thisCopyrightHolder.website != null) {
				printedText.add(thisCopyrightHolder.website);
			}
			printedText.add("");
			ArrayList<CopyrightFile> filesOfCopyrightHolder = 
				thisCopyrightHolder.getCopyrightFiles();
            /* modified / ekurkela: remove duplicates */
            HashSet h = new HashSet(filesOfCopyrightHolder);
            filesOfCopyrightHolder.clear();
            filesOfCopyrightHolder.addAll(h);
            /* end */
			for (CopyrightFile thisCopyrightFile : filesOfCopyrightHolder) {
				printedText.add(thisCopyrightFile.toString());
			}
		}
		if (!nonCopyrightFiles.isEmpty()) {
			printedText.add("");
			printedText.add("No copyright found:");
			printedText.add("");
			for (CopyrightFile thisCopyrightFile : nonCopyrightFiles) {
				printedText.add(thisCopyrightFile.toString());
			}
		}
		return printedText;
	}


	private static ArrayList<String> printLicenseMatches(LicenseChecker lc, ArrayList<String> printedText) {
		printedText.add("");
		printedText.add("");
		String addedLine = "";

		/* combine source and license file list */
		ArrayList<FileID> files = (ArrayList<FileID>) lc.getSourceFiles().clone();
		files.addAll(lc.getLicenseFiles());
		files.addAll(lc.getUnknownFiles());

		for(FileID file : files) {

			ArrayList<LicenseMatch>	matches =
				(ArrayList<LicenseMatch>) lc.getLicenseMatches(file);

			if(matches != null) {
				/* if matches is not null, sort it */
				java.util.Collections.sort(matches);
			}

			/* output "foo/bar/file.java:" */
			addedLine += file.toString() + ":";

			if(Log.getVerbosity() >= LogEntry.DEBUG) {
				/* debug header */
				printedText.add(addedLine);
				addedLine = "";
			}

			/* scan references for incompatible licenses */
			HashSet<Pair<License, License>> fileConflicts =
				new HashSet<Pair<License, License>>();

			/* collect conflicts */
			ArrayList<Pair<License, License>> c = lc.getInternalLicenseConflicts(file);
			if(c != null) fileConflicts.addAll(c);

			/* collect conflicts from references */
			ArrayList<Reference> refs = lc.getReferences(file);
			if(refs != null) {
				for(Reference r : refs) {
					c = lc.getLicenseConflicts(r);
					if(c != null) fileConflicts.addAll(c);
				}
			}

			boolean foundmatches = false;

			if(matches != null) {
				for(int j = 0; j < matches.size(); j++) {

					/* output ", " between licenses */ 
					if(j != 0) {
						if(!printTags) {
							addedLine += ", ";
						} else {
							/* unless tags are printed */
							printedText.add(addedLine);
							addedLine = "";
						}
					} else {
						addedLine += " ";
					}

					LicenseMatch match = matches.get(j);
					License foundLicense = match.getLicense();

					/* output match "lic (20%)" */
					addedLine += match.getLicense().getId();
					if (match.getMatchPr() != 1.0) {
						addedLine += String.format(" (%1.0f%%)",
								Math.floor(match.getMatchPr() * 100.0));
					}
					foundmatches = true;

					/* get a set of license conflicts */
					HashSet<License> conf =
						new HashSet<License>();
					for(Pair<License, License> p : fileConflicts) {
						if(p.e1 == foundLicense) {
							conf.add(p.e2);
						}
					}

					/* output " incompatible with (x, y, z)" */
					boolean firstConflict = true;
					for(License l : conf) {
						if(firstConflict) {
							addedLine += " incompatible with (";
							firstConflict = false;
						} else {
							addedLine += ", ";
						}
						addedLine += l.getId();
					}
					if(!firstConflict) {
						addedLine += ")";
					}

					/* output ", found at: and match locations if DEBUG */
					if(Log.getVerbosity() >= LogEntry.DEBUG) {
						addedLine += ", found at:\n";
						lc.printMatchLocation(match, file);
					}

					if(printTags) {
						HashMap<Tag, String> tags = match.getTags();

						if(tags != null) {
							if(Log.getVerbosity() < LogEntry.DEBUG) {
								/* no linefeed if match location was printed */
								printedText.add(addedLine);
								addedLine = "";
							}

							for(Tag t : tags.keySet()) {
								addedLine += "  " + t.getId() + ": " + tags.get(t);
							}
						}
					}
				}
			}

			/* get list of exceptions for this file */

			ArrayList<Exception> exceptions = lc.getFileExceptions(file);
			if(exceptions != null) {
				printedText.add(addedLine);
				addedLine = "";
				for(Exception e : exceptions) {
					addedLine += "Error while parsing: " + e;
					printedText.add(addedLine);
					addedLine = "";
					for (StackTraceElement st : e.getStackTrace()) {
						addedLine += st;
						printedText.add(addedLine);
						addedLine = "";
					}
				}
			}

			/* if no exceptions and no matches, */
			if(!foundmatches && (exceptions == null)) {
				addedLine += " No matches";
			}

			printedText.add(addedLine);
			addedLine = "";

			if(printReferences) {
				/* output list of references */
				if(refs != null) {
					for(Reference r : refs) {

						/* skip standard library references */
						if(r.referenceType == Reference.ReferenceType.IMPORT_STANDARD_LIBRARY) {
							continue;
						}

						addedLine += (" -> " + r.targetFile);

						/* check if the reference file is available */
						if(!files.contains(r.targetFile)) {
							addedLine += " (not found)";
							printedText.add(addedLine);
							addedLine = "";
							continue;
						}

						/* output " incompatible licenses: (x, y, z)" */
						boolean firstConflict = true;
						ArrayList<Pair<License, License>> conflicts =
							lc.getLicenseConflicts(r);
						if(conflicts != null) {
							HashSet<License> conf =
								new HashSet<License>();
							for(Pair<License, License> p : conflicts) {
								conf.add(p.e2);
							}
							for(License l : conf) {
								if(firstConflict) {
									addedLine += " (incompatible licenses: ";
									firstConflict = false;
								} else {
									addedLine += ", ";
								}
								addedLine += l.getId();
							}
							if(!firstConflict) {
								addedLine += ")";
							}
						}

						printedText.add(addedLine);
						addedLine = "";
					}
				}
			}
		}
		return printedText;
	}

	/*Print the report to a file*/
	private static void writeReport(ArrayList<String> printedText, String fileName) {
		ReportWriter reportWriter = new ReportWriter();
		try {
			reportWriter.writeReport(printedText, fileName);
		} catch (Exception e) {
			//TODO: Exception handling
		}
	}
	
	/**
	 * Writes a pdf/rtf report of the licenses and copyrights.
	 * Pdf report if the file name ends .pdf and 
	 * rtf reprot if the file name ends .rtf
	 * 
	 * @param fileName Name of the file
	 */
	public void writeReportGui(String fileName) {
		ArrayList<String> printedText = new ArrayList<String>();
		//printedText = printSummaryTable(this, this.getLicenseCounts(), printedText);
		printedText = printLicenseMatches(this, printedText);
		printedText = printCopyrights(this, printedText);
		writeReport(printedText, fileName);
	}

	/**
	 * Starts the program.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		//TODO: change start command from oslc2cli to oslccli
		Properties properties = System.getProperties();
		/* run gui if no arguments are specified */
		if(args.length == 0) {
			checker.gui.LicenseMain.main(args);
			return;
		}

		/* this should be the default for command line programs */
		Log.setVerbosity(LogEntry.ERROR);

		parseArguments(args);

		if (printReport) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Insert the name of the report file. (*.rtf or *.pdf)");
			try {
				reportFileName = reader.readLine();
			} catch (IOException e) {
				//TODO: Exception handling
			}
		}

		//Log.setVerbosity(LogEntry.DEBUG);

		Log.log(LogEntry.VERBOSE, "This is license checker 2.0");

		Log.log(LogEntry.VERBOSE, "Loading license database");
		LicenseChecker lc = new LicenseChecker();

		Log.log(LogEntry.VERBOSE, "Loading file " + packagename);
		File infile = new File(packagename);
		if(!infile.exists()) {
			System.out.println("Failed to load file \"" + packagename + "\", file not found");
			return;
		}

		try {
			lc.openPackage(infile);

		} catch (Exception e) {
			ErrorManager.error("Failed to load file \"" + packagename + "\"", e);
			return;
		}

		doExcludePaths(lc);

		Log.log(LogEntry.DEBUG, "Processing...");
		lc.processPackage();

		ArrayList<String> printedText = new ArrayList<String>();
		
		if (printDetails && printSummary) {
			printedText = printSummaryTable(lc, lc.getLicenseCounts(), printedText);
			printedText = printLicenseMatches(lc, printedText);
		}
		else if (printSummary) {
			printedText = printSummaryTable(lc, lc.getLicenseCounts(), printedText);

		} 
		else if (printDetails){
			printedText = printLicenseMatches(lc, printedText);
		} 
		else {
			System.out.println("You must choose -d to print details and -s to print summary");
		}
		if (printCopyrights) {
			printedText = printCopyrights(lc, printedText);
		}

		/*Print the report to the command line*/
		for (String line : printedText) {
			System.out.println(line);
		}
		
		if (printReport) {
			writeReport(printedText, reportFileName);
		}
		
		/* if conflicts were found, exit status is 1 */ 
		if (!lc.getGlobalLicenseConflicts().isEmpty())
			System.exit(1);

		return;
	}
}
