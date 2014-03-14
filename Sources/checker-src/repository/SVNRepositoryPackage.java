/**
 * 
 *   Copyright (C) 2008 Lasse Parikka
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
package checker.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;





/**
 * The SVNRepositoryPackage class provides
 * methods for working with a subversion repository.
 */
public class SVNRepositoryPackage {

	//repository url
	private String url;
	//username and password used in authentication.
	private String username;
	private String password;
	
	private String svnPath;
	private String svnRootPath;

	private SVNRepository repository = null;
	//destination path for checkout operation.
	private File destpath;

	ISVNAuthenticationManager authManager;
	private ArrayList<String> connectionTypes;


	/**
	 * Opens svn repository. 
	 * Supported connection methods: http, https, svn, svn+ssh(needs an authentication provider). 
	 * @param ur a repository location URL 
	 * @param user a user's name
	 * @param pass a user's password
	 * @param path Path relative to repository root.
	 * @param authProvider 
	 * @throws SVNException if a failure occured while connecting to a repository or 
	 * the user's authentication failed
	 */
	public void openRepository(String url, String username, String password, 
			String path, ISVNAuthenticationProvider authProvider) throws Exception{

		this.url = url;
		this.username = username;
		this.password = password;
		setupLibrary();
		repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		//if username and password is null then anonymous access is used.
		authManager = SVNWCUtil.createDefaultAuthenticationManager(SVNWCUtil.getDefaultConfigurationDirectory(), username, password, true);

		repository.setAuthenticationManager(authManager);
		//authentication provider needed only in svn+ssh access
		if (authProvider != null)
			authManager.setAuthenticationProvider(authProvider);

		repository.testConnection();
		SVNURL patfh = repository.getRepositoryRoot(false);
		svnRootPath = patfh.toString();
		svnPath = url.substring(url.lastIndexOf(patfh.getPath()) +
				patfh.getPath().length());
		
		repository.setLocation(patfh, false);
		

	}

	/**
	 * Fetches the contents and properties of a directory located at 
	 * the specified path. Information of each directory entry is represented 
	 * by a single SVNRepositoryEntry object.
	 *  
	 * @param path a directory path 
	 */
	public ArrayList<SVNRepositoryEntry> listEntries(String path) throws SVNException {

		ArrayList<SVNRepositoryEntry> svnEntries = new 
		ArrayList<SVNRepositoryEntry>();
		Collection<?> entries = repository.getDir(path, -1, null,
				(Collection<?>) null);
		Iterator<?> iterator = entries.iterator();
		while (iterator.hasNext()) {

			SVNDirEntry entry = (SVNDirEntry) iterator.next();
			SVNRepositoryEntry newEntry = null;
			String newPath = (path.equals("") ? "/" : path + "/")
			+ entry.getName();
			boolean isfolder = false;
			
			if (entry.getKind() == SVNNodeKind.DIR) 
				isfolder = true;

			newEntry = new SVNRepositoryEntry(entry.getName(), isfolder, 
					newPath, entry.getAuthor(),entry.getDate(), 
					entry.getRevision());
			svnEntries.add(newEntry);

		}
		return svnEntries;

	}

	/**
	 * Inizializes the svnkit library to work with a repository
	 * via different protocols.
	 */
	private void setupLibrary() throws SVNException {


		//For using over http:// and https://
		DAVRepositoryFactory.setup();

		//for using over svn:// and svn+xxx://
		SVNRepositoryFactoryImpl.setup();

		//for using over file://
		FSRepositoryFactory.setup();

	}

	/**
	 * Check out a working copy from the repository into 
	 * local folder.
	 * @param destPath the local path where the Working Copy will be placed
	 * @return value of the revision actually checked out from the repository
	 * @throws SVNException if failure occured during checkout.
	 *  
	 */
	public long checkoutFromRepository(String destPath, String url,
			ISVNEventHandler handler) throws SVNException {
		
		
		SVNClientManager clientManager;
		this.setupLibrary();
		SVNURL repositoryURL = null;
		repositoryURL = SVNURL.parseURIEncoded(url);
		
		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		/*
		 * Creates an instance of SVNClientManager providing authentication
		 * information (name, password) and an options driver
		 */
		clientManager = SVNClientManager.newInstance(options);
		clientManager.getUpdateClient().setEventHandler(handler);


		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);

		
		destpath = new File(destPath);
		//returns the number of the revision at which the working copy is 
		return  updateClient.doCheckout(repositoryURL, destpath, SVNRevision.HEAD,
				SVNRevision.HEAD,SVNDepth.INFINITY,  true);
		


	}

	/**
	 * Cleans up a working copy. 
	 * @param destination Path to working copy.
	 */
	public void cleanupWorkingCopy(File destination) {
		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		SVNWCClient client = new SVNWCClient(authManager, options );
		try {
			client.doCleanup(destination);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.getMessage();
		}
	}
	
	/**
	 * Gets a path relative to the repository root.
	 */
	public String getSvnPath() {
		return svnPath;
	}
	
	/**
	 * Gets repository root.
	 */
	public String getRootPath() {
		return svnRootPath;
	}
	
	
}


