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
import java.io.IOException;
import java.util.ArrayList;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.checkout.ModuleListInformation;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.event.FileUpdatedEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.netbeans.lib.cvsclient.event.TerminationEvent;

/**
 * the CVSRepository class provides methods for working with
 * a CVS repository. 
 */
public class CVSRepository {
	
	private GlobalOptions globalOptions;
	
	private String connectionType;
	
	private String host;
	
	private String userName;
	
	private String encryptedPassword;
	
	private String repository;
	
	private PServerConnection pserver = null;
	
	private ArrayList<String> modules;
	
	private ArrayList<String> aliases;
	
	
	/**
	 * Set the connection parameters. 
	 * @param root cvsroot including host, username and type of connection.
	 * @param password a User password.
	 */
	public void setParameters(String root, String password)
			throws Exception{
		
		 if (!root.startsWith(":"))
             throw new IllegalArgumentException();
         
         int oldColonPosition = 0;
         int colonPosition = root.indexOf(':', 1);
         
         if (colonPosition == -1)
             throw new IllegalArgumentException();
         connectionType = root.substring(oldColonPosition + 1, colonPosition);
         oldColonPosition = colonPosition;
         colonPosition = root.indexOf('@', colonPosition+1);
         
         if (colonPosition==-1)
             throw new IllegalArgumentException();
         userName = root.substring(oldColonPosition+1, colonPosition);
         oldColonPosition = colonPosition;
         colonPosition = root.indexOf(':', colonPosition+1);
         
         if (colonPosition==-1)
             throw new IllegalArgumentException();
         host = root.substring(oldColonPosition+1, colonPosition);
         if (connectionType==null || userName==null || host==null)
             throw new IllegalArgumentException();
         
         globalOptions = new GlobalOptions();
         globalOptions.setCVSRoot(root);
         encryptedPassword = password;
         
         if (encryptedPassword != null)
        	 encryptedPassword = StandardScrambler.getInstance().scramble(password);
         
		
	}
	
	/**
	 * Opens the connection to the server.
	 * @return
	 * @throws Exception
	 */
	public boolean openConnection() throws Exception {
		
		if (connectionType.equals("pserver")) {
			
			pserver = new PServerConnection(CVSRoot.parse(globalOptions.getCVSRoot()));
			if (encryptedPassword != null) {
				pserver.setEncodedPassword(encryptedPassword);
			}
			pserver.open();
			return true;

		}
		return false;
	}
	
	/**
	 * Close the connection to the server.
	 * @throws IOException 
	 */
	public void close() throws IOException {
		if (connectionType.equals("pserver") && pserver != null) {
			pserver.close();
		}
	}
	
	/**
	 * Checkout a module from the repository.
	 */
	public void doCheckout(String module, String localPath, CVSListener listener) 
			throws Exception{
		
		
		Client client = null;
		CheckoutCommand command = null;
		
		try {
			this.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.openConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (connectionType.equals("pserver")) {
			client = new Client(pserver, new StandardAdminHandler());
			client.setLocalPath(localPath);
			client.getEventManager().addCVSListener(listener);
			command = new CheckoutCommand();
			
			
		}
		command.setBuilder(null);
		command.setModule(module);
		client.executeCommand(command, globalOptions);
	}
	
	/**
	 * Lists subfolders in given repository folder.
	 * @param repositoryPath
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> listModules(String repositoryPath) throws Exception {
		
		modules = new ArrayList<String>();
		Client client = null;
		UpdateCommand command = new UpdateCommand();

		
		try {
			this.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.openConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (connectionType.equals("pserver")) {
			client = new Client(pserver, new StandardAdminHandler());
		}
		command.setBuildDirectories(true);
		AdminHandler adminHandler = new VirtualAdminHandler(CVSRoot.parse(globalOptions.getCVSRoot()), 
					repositoryPath);
		String tmpDir = System.getProperty("java.io.tmpdir");
		File tmp = new File(tmpDir);
	    client.setAdminHandler(adminHandler);
		client.setLocalPath(tmp.getAbsolutePath());
		EventManager mgr = client.getEventManager();
		globalOptions.setDoNoChanges(true);
		mgr.addCVSListener(new CVSListener()  {
			public void messageSent(MessageEvent e) {
				if (e.isError()) {
					String message = e.getMessage();
					if (message.endsWith("' -- ignored")) {  // NOI18N
						int start = message.indexOf(": New directory `");
						if (start != -1) {
							int pathStart = start + (": New directory `").length();
							int pathEnd = message.length() - "' -- ignored".length();
							String path = message.substring(pathStart, pathEnd);
							modules.add(path);
							

						}
					}
				}
			}
			public void messageSent(BinaryMessageEvent e) {
			}
			public void fileAdded(FileAddedEvent e) {
			}
			public void fileToRemove(FileToRemoveEvent e) {
			}
			public void fileRemoved(FileRemovedEvent e) {
			}
			public void fileUpdated(FileUpdatedEvent e) {
			}
			public void fileInfoGenerated(FileInfoEvent e) {
			}

			public void moduleExpanded(ModuleExpansionEvent e) {
			}
			public void commandTerminated(TerminationEvent e) {
				// TODO Auto-generated method stub

			}
		


		});
		
		try {
			
			client.executeCommand(command, globalOptions);
			
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		globalOptions.setDoNoChanges(false);
		return modules;
		
	}
	
	public String getCVSRootPath() {
		if (connectionType.equals("pserver")) {
			return pserver.getRepository();
		}
		return null;
	}
	
	/**
	 * Lists aliases in given repository.
	 */
	public ArrayList<String> listAliases() 
		throws Exception{
		
		aliases = new ArrayList<String>(); 
		CheckoutCommand checkout = new CheckoutCommand();
	    checkout.setShowModules(true);
	    Client client = null;
	    
		try {
			this.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.openConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (connectionType.equals("pserver")) {
			client = new Client(pserver, new StandardAdminHandler());
		}
		if (client != null) {
    		
			EventManager mgr = client.getEventManager();
            mgr.addCVSListener(new CVSListener() {
                public void messageSent(MessageEvent e) {
                }
                public void messageSent(BinaryMessageEvent e) {
                }
                public void fileAdded(FileAddedEvent e) {
                }
                public void fileToRemove(FileToRemoveEvent e) {
                }
                public void fileRemoved(FileRemovedEvent e) {
                }
                public void fileUpdated(FileUpdatedEvent e) {
                }
                public void fileInfoGenerated(FileInfoEvent e) {
                    ModuleListInformation moduleList = (ModuleListInformation) e.getInfoContainer();
                    aliases.add(moduleList.getModuleName());
                  
                }
                public void commandTerminated(TerminationEvent e) {
                }
                public void moduleExpanded(ModuleExpansionEvent e) {
                }
            });
            try {
				client.executeCommand(checkout, globalOptions);
			} catch (Exception e1) {
				throw new Exception(e1.getMessage());
			}
			
		}
		
		return aliases;
	}
	
	
	
	
}
