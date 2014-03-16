/**
 * 
 *   Copyright (C) <2006> <Veli-Jussi Raitila>
 *   Edited by <2008> <Mikko Kupsu>
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

package checker.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.JComponent;

import checker.localization.*;

import org.jdesktop.swingworker.SwingWorker;

import checker.ErrorManager;
import checker.FileID;
import checker.LicenseChecker;
import checker.Log;
import checker.LogEntry;
import checker.Pair;
import checker.Reference;
import checker.event.LicenseEvent;
import checker.event.LicenseProcessEvent;
import checker.event.LicenseProcessListener;
import checker.gui.combo.CriteriaBoxModel;
import checker.gui.cvs.CVSCheckoutWizard;
import checker.gui.filter.Criteria;
import checker.gui.filter.LicenseCriterion;
import checker.gui.filter.MatchCriterion;
import checker.gui.license.AddLicenseWizard;
import checker.gui.license.CompatibleLicenses;
import checker.gui.license.DownloadDatabase;
import checker.gui.license.EditLicenseWizard;
import checker.gui.svn.SVNCheckoutWizard;
import checker.gui.table.ConflictAltTableModel;
import checker.gui.table.ConflictAltTableRow;
import checker.gui.table.ConflictTableModel;
import checker.gui.table.ConflictTableRow;
import checker.gui.table.CountTableModel;
import checker.gui.table.CopyrightHolderTableModel;
import checker.gui.table.CopyrightedFileTableModel;
import checker.gui.table.DecimalCellRenderer;
import checker.gui.tree.Directory;
import checker.gui.tree.FileAbstract;
import checker.gui.tree.FileLicense;
import checker.gui.tree.FileReference;
import checker.gui.tree.FileSource;
import checker.gui.tree.FileUnknown;
import checker.gui.tree.LicenseTreeModel;
import checker.gui.tree.LicenseTreeNode;
import java.util.HashSet;
import checker.license.License;
import checker.license.LicenseDatabase;
import checker.matching.LicenseMatch;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;

import checker.gui.TableSorter;
import checker.gui.license.DeleteLicense;

/**
 * Main GUI frame for LicenseChecker
 *  
 * @author Veli-Jussi Raitila
 * 
 */
public class LicenseMain extends javax.swing.JFrame implements LicenseProcessListener {

    // Sorters for the tables
    private TableSorter sorter1 = null;
    private TableSorter sorter2 = null;
    private TableSorter sorter3 = null;
    private TableSorter sorter4 = null;
    private TableSorter sorter5 = null;
    // The file separator used on this platform
    public static String fileSeparator;
    // A LicenseChecker instance
    private LicenseChecker lc;
    // Processing task (SwingWorker)
    private CheckerTask checkerTask;
    // Opening task (SwingWorker)
    private OpenTask openTask;
    // Package being analyzed, chosen by the user
    private File activePackage;
    // Open tabs, keep track of these
    private HashMap<FileAbstract, LicenseTab> openTabs;
    // Current file the user is browsing
    private ListIterator<FileAbstract> currentFile;
    // File counts
    private int 
    	srcCount, licCount, unkCount, allCount, 
    	disLicCount, confRefCount, confGblCount,
    	copyrFilesCount, copyrHoldersCount;
    // Localization file
    public Locale loc;
    ConflictTableModel confmodel;
    
    /**
     * Creates new form LicenseMain
     */
    public LicenseMain() {
    	this.loc = new Locale();
        initComponents();
        initHelp();
        
        /* Fix file separator for regexps in Windows */
        if ("\\".equals(File.separator)) 
            LicenseMain.fileSeparator = "\\\\";
        else
        	LicenseMain.fileSeparator = File.separator;
        
        openTabs = new HashMap<FileAbstract, LicenseTab>();
        currentFile = new LinkedList<FileAbstract>().listIterator();
        

        /* Set icon (Java 5+) */
        this.setIconImage(new ImageIcon("resources/icon_32.png").getImage());
        //overviewDialog.setIconImage(new ImageIcon("resources/icon_32.png").getImage());
        
        /* Would work with Java 6+. Advantage: capability of rendering multiple
         * icons depending on the context instead of scaling a single image for
         * every occasion.
        List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon("resources/icon_16.png").getImage());
        icons.add(new ImageIcon("resources/icon_24.png").getImage());
        icons.add(new ImageIcon("resources/icon_32.png").getImage());
        icons.add(new ImageIcon("resources/icon_48.png").getImage());
        icons.add(new ImageIcon("resources/icon_64.png").getImage());
        icons.add(new ImageIcon("resources/icon_128.png").getImage());
        this.setIconImages(icons);
        */
    }
    
	public void fileOpenBegun(LicenseEvent e) {
		/* FIXME Prevent the user from generating events */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	statusLabel.setText(loc.lc("Opening file"));
                statusBar.setValue(0);
                statusBar.setIndeterminate(true);
            }
        });
	}

	public void fileOpenEnded(LicenseEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	statusLabel.setText(loc.lc("File opened"));
                statusBar.setIndeterminate(false);
            }
        });
	}

	public void processBegun(LicenseEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                processBar.setIndeterminate(false);
        		processLabel.setText(loc.lc("Begin processing files."));
            }
        });
	}

	public void fileProcessed(LicenseProcessEvent e) {
		final int i = e.getFileIndex();
		final int c = e.getFileCount();
		final FileID f = e.getFile();

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				processBar.setValue(i * 100 / c);
				processLabel.setText("[" + i + "/" + c + "] " + f.name);
			}
		});
	}

	public void processEnded(LicenseEvent e) {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        		processLabel.setText(loc.lc("Processing finished."));
        		processDialog.setVisible(false);
            }
        });
	}

	public void processCancelled(LicenseEvent e) {
		checkerTask.cancel(false);
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        		processLabel.setText(loc.lc("Processing cancelled."));
        		processDialog.setVisible(false);
            }
        });
	}

    /**
     * Check a particular file/dir/package for licenses
     * Display a dialog and start a worker thread.
     */
    private void runChecker() {

    	if (lc != null) {
	    	int value = JOptionPane.showConfirmDialog(
	    			this, loc.lc("Do you want to open a new package? The current one will close."),
	    			loc.lc("Confirm package open"),
	    			JOptionPane.YES_NO_OPTION
	    			);
	    	
	    	if (value == JOptionPane.NO_OPTION)
	    		return;
    	}

    	/* Create a new tree */
    	licenseTree.reset();
    	
    	/* Create a new lc instance */
        lc = new LicenseChecker();
        lc.addProcessingListener(this);

        /* Close all open files */
        closeAllTabs();
        
        /* Start the processing task */
        checkerTask = new CheckerTask();
        checkerTask.execute();

        processBar.setValue(0);
        processBar.setIndeterminate(true);
		processLabel.setText(loc.lc("Unpacking files."));

        processDialog.pack();
        processDialog.setLocationRelativeTo(this);
        processDialog.setVisible(true);
    }

    /**
     * A worker thread that opens a file
     */
    class OpenTask extends SwingWorker<ArrayList<String>, Void> {

    	private FileAbstract fileabs;
    	private FileID file;
    	
    	public OpenTask(FileAbstract f) {
    		fileabs = f;
    		file = f.getFileID();
    	}
    	
    	@Override
        protected ArrayList<String> doInBackground() {
    		ArrayList<String> lines = null;
    		
	        try {
	        	lines = lc.readFile(file);
	        } catch (Exception e) {
	        	ErrorManager.error(loc.lc("File contents cannot be shown"), e);
	        	/*		
	        	statusLabel.setText(loc.lc("File contents cannot be shown"));

	        	JOptionPane.showMessageDialog(LicenseMain.this,
	        		    loc.lc("File contents cannot be shown"),
	        		    loc.lc("File not found"),
	        		    JOptionPane.ERROR_MESSAGE);
				 */
			}

			return lines;
		}

		@Override
		protected void done() {
			if (!isCancelled()) {
				try {
					LicenseTab tab = new LicenseTab();

					tab.setWordWrap(wrapBox.getState());
					tab.setFile(get(), fileabs);

					tabbedPane.addTab(file.name, tab);
					tabbedPane.setSelectedComponent(tab);
					openTabs.put(fileabs, tab);

					while (currentFile.hasNext()) {
						currentFile.next();
						currentFile.remove();
					}
					currentFile.add(fileabs);

					statusLabel.setText(loc.lc("Done"));
				}
				catch (InterruptedException ignore) {}
				catch (java.util.concurrent.ExecutionException e) {
					ErrorManager.error(loc.lc("Error inserting file to a tab"), e);
				}
			}

			updateToolBar();
			// statusBar.setIndeterminate(false);
		}

	}

	/**
	 * A worker thread that opens a package, processes it
	 * and inserts the results in a hierarchical structure
	 */
	class CheckerTask extends SwingWorker<LicenseTreeNode, Void> {

		@Override
		public LicenseTreeNode doInBackground() {
			LicenseTreeNode node = null;

			/* Try opening a package */
			try {
				lc.openPackage(activePackage);
			} catch (Exception e) {
				ErrorManager.error(loc.lc("Error opening package from the GUI"), e);
			}

			/* Continue processing. Exceptions occurring here
			 * are caught in 'done'
			 */
			lc.processPackage();

			ArrayList<FileID> sourceFiles = lc.getSourceFiles();
			ArrayList<FileID> licenseFiles = lc.getLicenseFiles();
			ArrayList<FileID> unknownFiles = lc.getUnknownFiles();
			File rootFile = lc.getPackage().getRootFile();

			// Retrieve some information to be displayed in the overview
			srcCount = sourceFiles.size();
			licCount = licenseFiles.size();
			unkCount = unknownFiles.size();
			disLicCount = lc.getLicenseCounts().size();
			confRefCount = lc.getNumReferenceConflicts(); 
			confGblCount = lc.getGlobalLicenseConflicts().size() / 2;
            copyrFilesCount = new HashSet(lc.getCopyrightFiles()).size();
            copyrHoldersCount = lc.getCopyrightHolders().size();

			allCount = srcCount + licCount + unkCount;

			/* Read several files.
			 * Insert them in a hierarchical data structure
			 * separating file types, package/dir name as root
			 */
			Directory root = new Directory(rootFile.getName());

			Directory curdir = root; 

			/* Parse through source files and add them to a 
			 * hierarchical structure. Parsing is done
			 * by tokenizing the file path with a file separator  
			 */
			for (FileID file : sourceFiles) {
				if (file.path != null) {
					String[] dirs = file.path.split(LicenseMain.fileSeparator);
					for (String dir : dirs) {
						if(!curdir.hasFile(dir)) 
							curdir.addFile(new Directory(dir)); 
						curdir = (Directory)curdir.getFile(dir);
					}
				}

				/*
    			if(lc.getLicenseMatches(file) == null) {
    				for (Exception e : lc.getFileExceptions(file)) {
    					System.err.println(file);
    					e.printStackTrace();
    				}
    			}
				 */

				FileSource source = new FileSource(file, lc.getLicenseMatches(file));

				/* Add references and associate conflicts with them */
				ArrayList<Reference> refs = lc.getReferences(file);
				if (refs != null)
					for (Reference ref : refs) {
						ArrayList<Pair<License, License>> conflicts = lc.getLicenseConflicts(ref);
						source.addReference(new FileReference(ref, conflicts));
					}

				/* Add a possible self-reference ie. file conflicts with itself. */
				ArrayList<Pair<License, License>> selfconflicts = lc.getInternalLicenseConflicts(file);
				if (selfconflicts != null) {  
					Reference self = new Reference(file, file);
					self.referenceType = Reference.ReferenceType.IMPORT;
					self.declaration = loc.lc("(self)");;
					source.addReference(new FileReference(self, selfconflicts)); 
				}

				curdir.addFile(source);
				curdir = root;
			}

			/* Parse through license files.
			 * As above
			 */
			for (FileID file : licenseFiles) {
				if (file.path != null) {
					String[] dirs = file.path.split(LicenseMain.fileSeparator);
					for (String dir : dirs) {
						if(!curdir.hasFile(dir)) 
							curdir.addFile(new Directory(dir)); 
						curdir = (Directory)curdir.getFile(dir);
					}
				}
				curdir.addFile(new FileLicense(file, lc.getLicenseMatches(file)));
				curdir = root;
			}

			/* Parse through unknown files.
			 * As above
			 */
			for (FileID file : unknownFiles) {
				if (file.path != null) {
					String[] dirs = file.path.split(LicenseMain.fileSeparator);
					for (String dir : dirs) {
						if(!curdir.hasFile(dir)) 
							curdir.addFile(new Directory(dir)); 
						curdir = (Directory)curdir.getFile(dir);
					}
				}
				curdir.addFile(new FileUnknown(file));
				curdir = root;
			}

			node = root;

			return node;
		}

		@Override
		protected void done() {
			Toolkit.getDefaultToolkit().beep();

			/* If processing is complete and not cancelled,
			 * Set the generated model to the tree and
			 * update other views to display the results.
			 */

			licenseTree.setRootVisible(true);

			if (!isCancelled()) { 	 
                                 try { 	 
                                         licenseTree.setModel(new LicenseTreeModel(get())); 	 
                                         licenseTree.showReferences(referencesBox.isSelected()); 	 
                                 } 	 
                                 catch (InterruptedException ignore) {} 	 
                                 catch (java.util.concurrent.ExecutionException e) { 	 
                                         ErrorManager.error(loc.lc("Could not generate a tree from the chosen file/dir"), e); 	 
                                 } 	 
                         } else lc = null; // Destroy license checker if cancelled

            /* Update the overview panel */
            updateOverview();
            /* Update the tool bar */
            updateToolBar();
            /* Update filtering criteria */
            updateCriteria();
            /* Update the tree (apply filtering) */
            updateFiltering();
        }
    }

    /**
     * Update the combo box that displays filtering criteria
     */
    private void updateCriteria() {
		CriteriaBoxModel critmodel = new CriteriaBoxModel();
		criteriaDialog.setTitle(loc.lc("Choose Criteria"));
		
		switch (filterCombo.getSelectedIndex()) {
    	case 0:
    	case 1:
    	case 2:
    		criteriaButton.setEnabled(false);
    		break;
    	case 3:
        	if (lc != null) {
		    	for (License license : lc.getLicenseCounts().keySet()) {
		    		critmodel.addCriteria(new Criteria(new LicenseCriterion(license)));
		    	}
        	}
    		criteriaDialog.setTitle(loc.lc("Choose License"));
        	criteriaButton.setEnabled(true);
    		break;
    	case 4:
	    	for (float i = 0.2f; i <= 0.5f; i += 0.1f) {
	    		critmodel.addCriteria(new Criteria(new MatchCriterion(i)));
	    	}
			criteriaDialog.setTitle(loc.lc("Choose Match%"));
        	criteriaButton.setEnabled(true);
    		break;
    	default:
    		// Undefined filter, do nothing
    		break;
		}

		criteriaCombo.setModel(critmodel);
	}

	/**
	 * Apply tree filtering
	 */
	private void updateFiltering() {
		if (allCount == 1) licenseTree.setRootVisible(false);

		Object selected = criteriaCombo.getSelectedItem(); 
		Criteria c = Criteria.ALL; 

		switch (filterCombo.getSelectedIndex()) {
		case 0:
			licenseTree.applyFilter(LicenseTreeModel.FilterType.ALL, Criteria.ALL);
			break;
		case 1:
			licenseTree.applyFilter(LicenseTreeModel.FilterType.CONFLICTS, Criteria.ALL);
			break;
		case 2:
			licenseTree.applyFilter(LicenseTreeModel.FilterType.MISSING, Criteria.ALL);
			break;
		case 3:
			if (selected instanceof Criteria) c = (Criteria)selected;
			licenseTree.applyFilter(LicenseTreeModel.FilterType.LICENSED, c);
			break;
		case 4:
			if (selected instanceof Criteria) c = (Criteria)selected;
			licenseTree.applyFilter(LicenseTreeModel.FilterType.UNCERTAIN, c);
			break;
		default:
			// Undefined filter, do nothing
			break;
		}
	}

	/**
	 * Updates the overview panel
	 */
	private void updateOverview() {
		String na = "<html><font color=\"gray\">0</font></html>";
		String none = "<html><font color=\"green\">0</font></html>";
		String count = "<html>%d</html>";
		String count_red = "<html><font color=\"red\">%d</font></html>";

		if (lc == null) {
			overviewHyperLink.setActive(false);
			overviewDialog.setVisible(false);
			srcCountLabel.setText(na);
			allCountLabel.setText(na);
			disLicCountLabel.setText(na);
			confRefCountLabel.setText(na);
			confGblCountLabel.setText(na);
                        copyrFilesCountLabel.setText(na);
                        copyrHoldersCountLabel.setText(na);
			return;
		}

		overviewHyperLink.setActive(true);

		/* Set global counts in the overview panel */
		if (srcCount == 0) srcCountLabel.setText(none);
		else srcCountLabel.setText(String.format(count, srcCount));

		if (allCount == 0) allCountLabel.setText(none);
		else allCountLabel.setText(String.format(count, allCount));

		if (disLicCount == 0) disLicCountLabel.setText(none);
		else disLicCountLabel.setText(String.format(count, disLicCount));

		if (confRefCount == 0) confRefCountLabel.setText(none);
		else confRefCountLabel.setText(String.format(count_red, confRefCount));

		if (confGblCount == 0) confGblCountLabel.setText(none);
		else confGblCountLabel.setText(String.format(count_red, confGblCount));
                
		if (copyrFilesCount == 0) copyrFilesCountLabel.setText(none);
		else copyrFilesCountLabel.setText(String.format(count, copyrFilesCount));
                
		if (copyrHoldersCount == 0) copyrHoldersCountLabel.setText(none);
		else copyrHoldersCountLabel.setText(String.format(count, copyrHoldersCount));
                
		CountTableModel licmodel = new CountTableModel(lc.getLicenseCounts(), lc.getMaxMatchPrs());
		this.sorter4 = new TableSorter(licmodel);
		matchesTable.setModel(this.sorter4);
		this.sorter4.setTableHeader(matchesTable.getTableHeader());
        /* ekurkela */
        matchesTable.getColumnModel().getColumn(2).setCellRenderer(new DecimalCellRenderer("0.0"));
        /* end */

		/** 
		 * Populate conflict table model with global conflicts,
		 * filtering duplicates.
		 */
		confmodel = new ConflictTableModel();
		for (Pair<License, License> pair : lc.getGlobalLicenseConflicts()) {
			if (pair.e1.compareTo(pair.e2) < 0)
				confmodel.addRow(new ConflictTableRow(
						pair.e1.getScreenName(),
						pair.e2.getScreenName()));
		}
		this.sorter1 = new TableSorter(confmodel);
		conflictsTable.setModel(sorter1);
		this.sorter1.setTableHeader(conflictsTable.getTableHeader());

        /* ekurkela */

        /**
         * Populate copyrighted files table
         */
        CopyrightedFileTableModel cfmodel = new CopyrightedFileTableModel(lc.getCopyrightFiles());
      	this.sorter2 = new TableSorter(cfmodel);
        copyrightedFilesTable.setModel(this.sorter2);
	this.sorter2.setTableHeader(copyrightedFilesTable.getTableHeader());

        /**
         * Populate non-copyrighted files table
         */
        CopyrightedFileTableModel ncfmodel = new CopyrightedFileTableModel(lc.getNonCopyrightFiles(), true);
	this.sorter5 = new TableSorter(ncfmodel);
	nonCopyrightedFilesTable.setModel(this.sorter5);
	this.sorter5.setTableHeader(nonCopyrightedFilesTable.getTableHeader());

        /**
         * Populate copyright holders table
         */
        CopyrightHolderTableModel chmodel = new CopyrightHolderTableModel(lc.getCopyrightHolders());
		this.sorter3 = new TableSorter(chmodel);
                copyrightHoldersTable.setModel(this.sorter3);
		this.sorter3.setTableHeader(copyrightHoldersTable.getTableHeader());
                
        /* end */
	}

	private void updateToolBar() {
		if (lc == null) currentFile = new LinkedList<FileAbstract>().listIterator();

		firstButton.setEnabled(currentFile.previousIndex() > 0);
		prevButton.setEnabled(currentFile.previousIndex() > 0);
		nextButton.setEnabled(currentFile.hasNext());
		lastButton.setEnabled(currentFile.hasNext());
	}

	/**
	 * Jump to a given reference in a tree
	 * if applicable.
	 * 
	 * @param ref
	 */
	private void jumptoReference(FileReference ref) {
		try {
			LicenseTreeNode node = licenseTree.findNode(ref);
			if (node instanceof FileAbstract) showFile((FileAbstract)node);
		} catch (Exception e) {
			String reason = ref.getReference().information;
			if (reason == null) reason = loc.lc("File not found");
			JOptionPane.showMessageDialog(this,
					reason,
					loc.lc("Referred file cannot be shown"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Jump to a given file in a tree
	 * if applicable.
	 * 
	 * @param ref
	 */
	private void jumptoFile(FileAbstract ref) {
		try {
			LicenseTreeNode node = licenseTree.findNode(ref);
			if (node instanceof FileAbstract) {
				FileAbstract file = (FileAbstract) node;
				if(openTabs.containsKey(file)) switchFile(file); 
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
        		    loc.lc("File not found"),
        		    loc.lc("File cannot be shown"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void switchFile(FileAbstract file) {
		tabbedPane.setSelectedComponent(openTabs.get(file));
	}

	/**
	 * Start an opening task and upon completion 
	 * show file contents in a tab
	 * 
	 * @param fileabs
	 */
	private void showFile(FileAbstract fileabs) {

		/* If the file is viewable, populate a tab and show it */
		if(!fileabs.isViewable()) {
			JOptionPane.showMessageDialog(this, loc.lc("File is not viewable"));
		} else {
			openTask = new OpenTask(fileabs);
			openTask.execute();
		}
	}

	/* NOTE 
	 * Converted into a task
	 * 
    private void showFile_DEL(FileAbstract fileabs) {

        FileID file = fileabs.getFileID();

        // Check if tab is already open 
        if(openTabs.containsKey(file)) {
            tabbedPane.setSelectedComponent(openTabs.get(file));
        	return;
        }

        // If the file is viewable, populate a tab and show it 
        if(!fileabs.isViewable()) {
    		JOptionPane.showMessageDialog(this, loc.lc("File is not viewable"));
    	} else {

	        LicenseTab tab = new LicenseTab();
	        try {

	        	// Insert file contents onto the tab
	        	// and associate the FileAbstract it
	        	// represents 
	        	tab.setWordWrap(wrapBox.getState());
	        	tab.setFile(
	        			lc.readFile(file),
	        			fileabs
	        			);

	        	tabbedPane.addTab(file.name, tab);
	            tabbedPane.setSelectedComponent(tab);
	            openTabs.put(file, tab);

	        } catch (FileNotFoundException e) {
	        	JOptionPane.showMessageDialog(this,
	        		    loc.lc("File contents cannot be shown"),
	        		    loc.lc("File not found"),
	        		    JOptionPane.ERROR_MESSAGE);
	        } catch (Exception e) {
	        	ErrorManager.error(loc.lc("Error inserting file to a tab"), e);
	        }
        }

    }
	 */

	private void closeTab(LicenseTab tab) {
		FileAbstract fileabs = tab.getFile();

		tabbedPane.remove(tab);
		openTabs.remove(fileabs);
	}

	private void closeAllTabs() {
		tabbedPane.removeAll();
		openTabs.clear();
	}

	/**
	 * Initialize application help
	 */
	private void initHelp() {
		try {
			ClassLoader cl = LicenseMain.class.getClassLoader();
			URL url = HelpSet.findHelpSet(cl, "documentation/OSLC.hs");
			JHelp helpViewer = new JHelp(new HelpSet(cl, url));
			helpFrame.getContentPane().add(helpViewer);
		} catch (Exception e) {
			ErrorManager.error(loc.lc("HelpSet not found"), e);
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        exportChooser = new javax.swing.JFileChooser();
        tabPopup = new javax.swing.JPopupMenu();
        closeTabItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        printTabItem = new javax.swing.JMenuItem();
        prefsDialog = new javax.swing.JDialog(this);
        prefsPanel = new javax.swing.JPanel();
        prefsCancel = new javax.swing.JButton();
        prefsOk = new javax.swing.JButton();
        prefsLabel = new javax.swing.JLabel();
        processDialog = new javax.swing.JDialog(this);
        processLabel = new javax.swing.JLabel();
        processBar = new javax.swing.JProgressBar();
        processCancel = new javax.swing.JButton();
        overviewDialog = new javax.swing.JDialog();
        overviewDlgPanel = new javax.swing.JPanel();
        overviewDlgPane = new javax.swing.JTabbedPane();
        matchesPane = new javax.swing.JScrollPane();
        matchesTable = new javax.swing.JTable();
        conflictsPanel = new javax.swing.JPanel();
        conflictsPane = new javax.swing.JScrollPane();
        conflictsTable = new javax.swing.JTable();
        copyrightedFilesPane = new javax.swing.JScrollPane();
        copyrightedFilesTable = new javax.swing.JTable();
        nonCopyrightedFilesPane = new javax.swing.JScrollPane();
        nonCopyrightedFilesTable = new javax.swing.JTable();
        copyrightHoldersPane = new javax.swing.JScrollPane();
        copyrightHoldersTable = new javax.swing.JTable();
        aboutDialog = new javax.swing.JDialog();
        aboutLabel1 = new javax.swing.JLabel();
        aboutLabel2 = new javax.swing.JLabel();
        aboutScrollPane = new javax.swing.JScrollPane();
        aboutTextPane = new javax.swing.JTextPane();
        criteriaDialog = new javax.swing.JDialog();
        criteriaCombo = new javax.swing.JComboBox();
        helpFrame = new javax.swing.JFrame();
        mainPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        overviewPanel = new javax.swing.JPanel();
        overviewLabel1 = new javax.swing.JLabel();
        srcCountLabel = new javax.swing.JLabel();
        overviewLabel2 = new javax.swing.JLabel();
        allCountLabel = new javax.swing.JLabel();
        overviewLabel3 = new javax.swing.JLabel();
        disLicCountLabel = new javax.swing.JLabel();
        overviewLabel4 = new javax.swing.JLabel();
        confRefCountLabel = new javax.swing.JLabel();
        overviewLabel5 = new javax.swing.JLabel();
        confGblCountLabel = new javax.swing.JLabel();
        overviewLabel7 = new javax.swing.JLabel();
        copyrFilesCountLabel = new javax.swing.JLabel();
        overviewLabel6 = new javax.swing.JLabel();
        copyrHoldersCountLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        overviewHyperLink = new checker.gui.LicenseHyperLink();
        treePanel = new javax.swing.JPanel();
        filterCombo = new javax.swing.JComboBox();
        referencesBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        licenseTree = new checker.gui.tree.LicenseTree();
        criteriaButton = new javax.swing.JButton();
        rightPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        toolBar = new javax.swing.JToolBar();
        openButton = new javax.swing.JButton();
        firstButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        lastButton = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        statusBar = new javax.swing.JProgressBar();
        statusLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openItem = new javax.swing.JMenuItem();
        closeItem = new javax.swing.JMenuItem();
        closeAllItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        printItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        exportItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        svnCheckoutItem = new javax.swing.JMenuItem();
        cvsCheckoutItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        quitItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        overviewBox = new javax.swing.JCheckBoxMenuItem();
        wrapBox = new javax.swing.JCheckBoxMenuItem();
        optionsMenu = new javax.swing.JMenu();
        prefsItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        addLicenseItem = new javax.swing.JMenuItem();
        editLicenseItem = new javax.swing.JMenuItem();
        delLicenseItem = new javax.swing.JMenuItem();
        compatibleLicenseItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        downloadDatabaseItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        langItem = new javax.swing.JMenuItem();
        newLangItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        aboutItem = new javax.swing.JMenuItem();

        fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        exportChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        exportChooser.setAcceptAllFileFilterUsed(false);
        exportChooser.addChoosableFileFilter(new FileFilter(){
            public boolean accept(File path){
                return (path.isDirectory() ||
                    path.getAbsolutePath().endsWith(".pdf"));
            }

            public String getDescription(){
                return ".pdf";
            }
        });
        exportChooser.addChoosableFileFilter(new FileFilter(){
            public boolean accept(File path){
                return (path.isDirectory() ||
                    path.getAbsolutePath().endsWith(".rtf"));
            }

            public String getDescription(){
                return ".rtf";
            }
        });

        closeTabItem.setText(loc.lc("Close File"));
        closeTabItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTabItemActionPerformed(evt);
            }
        });
        tabPopup.add(closeTabItem);
        tabPopup.add(jSeparator3);

        printTabItem.setText(loc.lc("Print File"));
        printTabItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printTabItemActionPerformed(evt);
            }
        });
        tabPopup.add(printTabItem);

        prefsDialog.setTitle(loc.lc("Preferences"));
        prefsDialog.setModal(true);

        prefsCancel.setText(loc.lc("Cancel"));
        prefsCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefsCancelActionPerformed(evt);
            }
        });

        prefsOk.setText(loc.lc("OK"));
        prefsOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefsOkActionPerformed(evt);
            }
        });

        prefsLabel.setText(loc.lc("Nothing here yet..."));

        org.jdesktop.layout.GroupLayout prefsPanelLayout = new org.jdesktop.layout.GroupLayout(prefsPanel);
        prefsPanel.setLayout(prefsPanelLayout);
        prefsPanelLayout.setHorizontalGroup(
            prefsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, prefsPanelLayout.createSequentialGroup()
                .addContainerGap(254, Short.MAX_VALUE)
                .add(prefsCancel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(prefsOk)
                .addContainerGap())
            .add(prefsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(prefsLabel)
                .addContainerGap(279, Short.MAX_VALUE))
        );
        prefsPanelLayout.setVerticalGroup(
            prefsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, prefsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(prefsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 236, Short.MAX_VALUE)
                .add(prefsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(prefsOk)
                    .add(prefsCancel))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout prefsDialogLayout = new org.jdesktop.layout.GroupLayout(prefsDialog.getContentPane());
        prefsDialog.getContentPane().setLayout(prefsDialogLayout);
        prefsDialogLayout.setHorizontalGroup(
            prefsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(prefsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        prefsDialogLayout.setVerticalGroup(
            prefsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(prefsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        processDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        processDialog.setTitle(loc.lc("Processing files"));
        processDialog.setModal(true);
        processDialog.setResizable(false);
        processDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                processDialogWindowClosing(evt);
            }
        });

        processLabel.setText(loc.lc("Please wait."));

        processCancel.setText(loc.lc("Cancel"));
        processCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout processDialogLayout = new org.jdesktop.layout.GroupLayout(processDialog.getContentPane());
        processDialog.getContentPane().setLayout(processDialogLayout);
        processDialogLayout.setHorizontalGroup(
            processDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(processDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(processDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(processLabel)
                    .add(processDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(processCancel)
                        .add(processBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        processDialogLayout.setVerticalGroup(
            processDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(processDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(processLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(processBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(processCancel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        overviewDialog.setTitle(loc.lc("Details"));

        overviewDlgPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(loc.lc("Package Overview")));

        matchesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("License"), "Count", "Max %"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        matchesPane.setViewportView(matchesTable);

        overviewDlgPane.addTab(loc.lc("Matches"), matchesPane);

        conflictsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1"
            }
        ));
        conflictsPane.setViewportView(conflictsTable);

        org.jdesktop.layout.GroupLayout conflictsPanelLayout = new org.jdesktop.layout.GroupLayout(conflictsPanel);
        conflictsPanel.setLayout(conflictsPanelLayout);
        conflictsPanelLayout.setHorizontalGroup(
            conflictsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(conflictsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
        );
        conflictsPanelLayout.setVerticalGroup(
            conflictsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(conflictsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
        );

        overviewDlgPane.addTab(loc.lc("Conflicts (global)"), conflictsPanel);

        copyrightedFilesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("File name"), "File path", "Copyright holder"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        copyrightedFilesPane.setViewportView(copyrightedFilesTable);

        overviewDlgPane.addTab(loc.lc("Copyrighted files"), copyrightedFilesPane);

        nonCopyrightedFilesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1"
            }
        ));
        nonCopyrightedFilesPane.setViewportView(nonCopyrightedFilesTable);

        overviewDlgPane.addTab(loc.lc("Non-copyrighted files"), nonCopyrightedFilesPane);

        copyrightHoldersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("Copyright holder"), "Number of files"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        copyrightHoldersPane.setViewportView(copyrightHoldersTable);

        overviewDlgPane.addTab(loc.lc("Copyright holders"), copyrightHoldersPane);

        org.jdesktop.layout.GroupLayout overviewDlgPanelLayout = new org.jdesktop.layout.GroupLayout(overviewDlgPanel);
        overviewDlgPanel.setLayout(overviewDlgPanelLayout);
        overviewDlgPanelLayout.setHorizontalGroup(
            overviewDlgPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(overviewDlgPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
        );
        overviewDlgPanelLayout.setVerticalGroup(
            overviewDlgPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(overviewDlgPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout overviewDialogLayout = new org.jdesktop.layout.GroupLayout(overviewDialog.getContentPane());
        overviewDialog.getContentPane().setLayout(overviewDialogLayout);
        overviewDialogLayout.setHorizontalGroup(
            overviewDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(overviewDlgPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        overviewDialogLayout.setVerticalGroup(
            overviewDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(overviewDlgPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        aboutDialog.setTitle(loc.lc("About OSLC"));

        aboutLabel1.setText(loc.lc("Open Source License Checker"));

        aboutLabel2.setText("version 3.0, 2009");

        aboutTextPane.setContentType("text/html");
        aboutTextPane.setEditable(false);
        aboutTextPane.setText("<html>\n<p>This software is released under GPL 2.0 license.</p>\nJing Jing-Helles<br>\nSakari K‰‰ri‰inen<br>\nYuan Yuan<br>\nLauri Koponen<br>\nVeli-Jussi Raitila<br>\nMika Rajanen<br>\nXie Xiaolei<br>\nJussi Sirpoma<br>\nMikko Kupsu<br>\nEetu Jalonen<br>\nJyrki Laine<br>\nJohannes Heikkinen<br>\nEero Kurkela<br>\nTuomo Jorri<br>\nLasse Parikka<br>\n</p>\n<p>\nFor more info about the application:<br>\n<a href=\"http://forge.objectweb.org/projects/oslcv3/\">http://forge.objectweb.org/projects/oslcv3/</a><br>\n</p>\n<p>\nThis project is created for the T-76.4115 course in<br>\nSoftware Engineering and Business lab, <br>\nin Computer Science and Engineering department <br>\nof Helsinki University of Technology<br>\n</p>\n<p>\nSpecial thanks to Prof. Juha Laine, Ville Oksanen, Seppo Sahi, Jari Vanhanen and Ossi Syd<br>\nfor guiding us throughout the project.<br>\n</p>\n</html>\n");
        aboutScrollPane.setViewportView(aboutTextPane);

        org.jdesktop.layout.GroupLayout aboutDialogLayout = new org.jdesktop.layout.GroupLayout(aboutDialog.getContentPane());
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);
        aboutDialogLayout.setHorizontalGroup(
            aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(aboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(aboutScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(aboutLabel1)
                    .add(aboutLabel2))
                .addContainerGap())
        );
        aboutDialogLayout.setVerticalGroup(
            aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(aboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(aboutLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(aboutLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(aboutScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addContainerGap())
        );

        criteriaDialog.setTitle(loc.lc("Choose Criteria"));

        criteriaCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { loc.lc("All") }));
        criteriaCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                criteriaComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout criteriaDialogLayout = new org.jdesktop.layout.GroupLayout(criteriaDialog.getContentPane());
        criteriaDialog.getContentPane().setLayout(criteriaDialogLayout);
        criteriaDialogLayout.setHorizontalGroup(
            criteriaDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(criteriaCombo, 0, 148, Short.MAX_VALUE)
                .addContainerGap())
        );
        criteriaDialogLayout.setVerticalGroup(
            criteriaDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(criteriaCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        helpFrame.setTitle(loc.lc("OSLC Help"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(loc.lc("OSLC v3.0"));

        mainPanel.setLayout(new java.awt.BorderLayout());

        splitPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.25);

        overviewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(loc.lc("Overview")));
        overviewPanel.setLayout(new java.awt.GridLayout(8, 2, -100, 10));

        overviewLabel1.setText(loc.lc("Source Files"));
        overviewPanel.add(overviewLabel1);

        srcCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        srcCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(srcCountLabel);

        overviewLabel2.setText(loc.lc("All Files"));
        overviewPanel.add(overviewLabel2);

        allCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        allCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(allCountLabel);

        overviewLabel3.setText(loc.lc("Distinct Licenses"));
        overviewPanel.add(overviewLabel3);

        disLicCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        disLicCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(disLicCountLabel);

        overviewLabel4.setText(loc.lc("Conflicts (reference)"));
        overviewPanel.add(overviewLabel4);

        confRefCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        confRefCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(confRefCountLabel);

        overviewLabel5.setText(loc.lc("Conflicts (global)"));
        overviewPanel.add(overviewLabel5);

        confGblCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        confGblCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(confGblCountLabel);

        overviewLabel7.setText(loc.lc("Copyrighted Files"));
        overviewPanel.add(overviewLabel7);

        copyrFilesCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        copyrFilesCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(copyrFilesCountLabel);

        overviewLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        overviewLabel6.setText(loc.lc("Copyright Holders"));
        overviewPanel.add(overviewLabel6);

        copyrHoldersCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        copyrHoldersCountLabel.setText("<html><font color=\"gray\">0</font></html>");
        overviewPanel.add(copyrHoldersCountLabel);
        overviewPanel.add(jLabel1);

        overviewHyperLink.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        overviewHyperLink.setText(loc.lc("Details..."));
        overviewHyperLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                overviewHyperLinkMouseClicked(evt);
            }
        });
        overviewPanel.add(overviewHyperLink);

        treePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(loc.lc("Filter")));

        filterCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { loc.lc("All Files"), loc.lc("Conflicting Files"), loc.lc("Missing Licenses"), loc.lc("Licensed Files"), loc.lc("Uncertain Licenses") }));
        filterCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboActionPerformed(evt);
            }
        });

        referencesBox.setSelected(true);
        referencesBox.setText(loc.lc("Show References"));
        referencesBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        referencesBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        referencesBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                referencesBoxItemStateChanged(evt);
            }
        });

        licenseTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                licenseTreeKeyPressed(evt);
            }
        });
        licenseTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                licenseTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(licenseTree);

        criteriaButton.setText(loc.lc("Choose..."));
        criteriaButton.setEnabled(false);
        criteriaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                criteriaButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout treePanelLayout = new org.jdesktop.layout.GroupLayout(treePanel);
        treePanel.setLayout(treePanelLayout);
        treePanelLayout.setHorizontalGroup(
            treePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(treePanelLayout.createSequentialGroup()
                .add(referencesBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 168, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, treePanelLayout.createSequentialGroup()
                .add(filterCombo, 0, 95, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(criteriaButton))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
        );
        treePanelLayout.setVerticalGroup(
            treePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, treePanelLayout.createSequentialGroup()
                .add(treePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(criteriaButton)
                    .add(filterCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(referencesBox))
        );

        org.jdesktop.layout.GroupLayout leftPanelLayout = new org.jdesktop.layout.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(overviewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .add(treePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(leftPanelLayout.createSequentialGroup()
                .add(overviewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(treePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        splitPane.setLeftComponent(leftPanel);

        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        tabbedPane.setComponentPopupMenu(tabPopup);

        org.jdesktop.layout.GroupLayout rightPanelLayout = new org.jdesktop.layout.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
        );

        splitPane.setRightComponent(rightPanel);

        mainPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        toolBar.setFloatable(false);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/folder.png"))); // NOI18N
        openButton.setBorderPainted(false);
        openButton.setFocusPainted(false);
        openButton.setFocusable(false);
        openButton.setOpaque(false);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        toolBar.add(openButton);

        toolBar.addSeparator();
        firstButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/resultset_first.png"))); // NOI18N
        firstButton.setBorderPainted(false);
        firstButton.setEnabled(false);
        firstButton.setFocusable(false);
        firstButton.setOpaque(false);
        firstButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstButtonActionPerformed(evt);
            }
        });
        toolBar.add(firstButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/resultset_previous.png"))); // NOI18N
        prevButton.setBorderPainted(false);
        prevButton.setEnabled(false);
        prevButton.setFocusable(false);
        prevButton.setOpaque(false);
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        toolBar.add(prevButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/resultset_next.png"))); // NOI18N
        nextButton.setBorderPainted(false);
        nextButton.setEnabled(false);
        nextButton.setFocusable(false);
        nextButton.setOpaque(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        toolBar.add(nextButton);

        lastButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/resultset_last.png"))); // NOI18N
        lastButton.setBorderPainted(false);
        lastButton.setEnabled(false);
        lastButton.setFocusable(false);
        lastButton.setOpaque(false);
        lastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastButtonActionPerformed(evt);
            }
        });
        toolBar.add(lastButton);

        mainPanel.add(toolBar, java.awt.BorderLayout.NORTH);

        statusPanel.setLayout(new java.awt.BorderLayout());
        statusPanel.add(statusBar, java.awt.BorderLayout.EAST);

        statusLabel.setText(loc.lc("Ready"));
        statusPanel.add(statusLabel, java.awt.BorderLayout.WEST);

        mainPanel.add(statusPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setMnemonic('F');
        fileMenu.setText(loc.lc("File"));

        openItem.setMnemonic('O');
        openItem.setText(loc.lc("Open..."));
        openItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openItemActionPerformed(evt);
            }
        });
        fileMenu.add(openItem);

        closeItem.setMnemonic('C');
        closeItem.setText(loc.lc("Close"));
        closeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeItem);

        closeAllItem.setMnemonic('A');
        closeAllItem.setText(loc.lc("Close All"));
        closeAllItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeAllItem);
        fileMenu.add(jSeparator1);

        printItem.setMnemonic('P');
        printItem.setText(loc.lc("Print..."));
        printItem.setEnabled(false);
        printItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printItemActionPerformed(evt);
            }
        });
        fileMenu.add(printItem);
        fileMenu.add(jSeparator7);

        exportItem.setText(loc.lc("Export report..."));
        exportItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportItem);
        fileMenu.add(jSeparator2);

        svnCheckoutItem.setText(loc.lc("Checkout From SVN..."));
        svnCheckoutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svnCheckoutItemActionPerformed(evt);
            }
        });
        fileMenu.add(svnCheckoutItem);

        cvsCheckoutItem.setText(loc.lc("Checkout From CVS..."));
        cvsCheckoutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cvsCheckoutItemActionPerformed(evt);
            }
        });
        fileMenu.add(cvsCheckoutItem);
        fileMenu.add(jSeparator4);

        quitItem.setMnemonic('u');
        quitItem.setText(loc.lc("Quit"));
        quitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitItem);

        menuBar.add(fileMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText(loc.lc("View"));

        overviewBox.setMnemonic('v');
        overviewBox.setSelected(true);
        overviewBox.setText(loc.lc("Show Overview"));
        overviewBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overviewBoxActionPerformed(evt);
            }
        });
        viewMenu.add(overviewBox);

        wrapBox.setMnemonic('W');
        wrapBox.setText(loc.lc("Word Wrap"));
        wrapBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapBoxActionPerformed(evt);
            }
        });
        viewMenu.add(wrapBox);

        menuBar.add(viewMenu);

        optionsMenu.setText(loc.lc("Options"));
        optionsMenu.setEnabled(false);

        prefsItem.setText(loc.lc("Preferences"));
        prefsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefsItemActionPerformed(evt);
            }
        });
        optionsMenu.add(prefsItem);

        menuBar.add(optionsMenu);

        toolsMenu.setMnemonic('T');
        toolsMenu.setText(loc.lc("Tools"));

        addLicenseItem.setText(loc.lc("Add new license..."));
        addLicenseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLicenseItemActionPerformed(evt);
            }
        });
        toolsMenu.add(addLicenseItem);

        editLicenseItem.setText(loc.lc("Edit license..."));
        editLicenseItem.setEnabled(false);
        editLicenseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLicenseItemActionPerformed(evt);
            }
        });
        toolsMenu.add(editLicenseItem);

        delLicenseItem.setText(loc.lc("Delete license..."));
        delLicenseItem.setEnabled(false);
        delLicenseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delLicenseItemActionPerformed(evt);
            }
        });
        toolsMenu.add(delLicenseItem);

        compatibleLicenseItem.setText(loc.lc("List compatible licenses..."));
        compatibleLicenseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compatibleLicenseItemActionPerformed(evt);
            }
        });
        toolsMenu.add(compatibleLicenseItem);
        toolsMenu.add(jSeparator6);

        downloadDatabaseItem.setText(loc.lc("Download license database..."));
        downloadDatabaseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadDatabaseItemActionPerformed(evt);
            }
        });
        toolsMenu.add(downloadDatabaseItem);
        toolsMenu.add(jSeparator8);

        langItem.setText(loc.lc("Select language"));
        langItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                langItemActionPerformed(evt);
            }
        });
        toolsMenu.add(langItem);

        newLangItem.setText(loc.lc("Create new language file"));
        newLangItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLangItemActionPerformed(evt);
            }
        });
        toolsMenu.add(newLangItem);

        menuBar.add(toolsMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText(loc.lc("Help"));

        helpItem.setText(loc.lc("Help Contents"));
        helpItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpItem);
        helpMenu.add(jSeparator5);

        aboutItem.setText(loc.lc("About OSLC"));
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void lastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastButtonActionPerformed
		FileAbstract file = null;
		while (currentFile.hasNext()) file = currentFile.next();

		jumptoFile(file);
		updateToolBar();
	}//GEN-LAST:event_lastButtonActionPerformed

	private void firstButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstButtonActionPerformed
		while (currentFile.hasPrevious()) currentFile.previous();

		jumptoFile(currentFile.next());
		updateToolBar();
	}//GEN-LAST:event_firstButtonActionPerformed

	private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
		//System.err.println("before next: " + currentFile.previousIndex() + ":" + currentFile.nextIndex());

		jumptoFile(currentFile.next());
		updateToolBar();
	}//GEN-LAST:event_nextButtonActionPerformed

	private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
		//System.err.println("before prev: " + currentFile.previousIndex() + ":" + currentFile.nextIndex());

		currentFile.previous();
		if (currentFile.hasPrevious()) currentFile.previous();

		jumptoFile(currentFile.next());
		updateToolBar();
	}//GEN-LAST:event_prevButtonActionPerformed

	private void helpItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpItemActionPerformed
		helpFrame.pack();
		helpFrame.setLocationRelativeTo(this);
		helpFrame.setVisible(true);
	}//GEN-LAST:event_helpItemActionPerformed

	private void criteriaComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_criteriaComboActionPerformed
		updateFiltering();
	}//GEN-LAST:event_criteriaComboActionPerformed

	private void criteriaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_criteriaButtonActionPerformed
		criteriaDialog.pack();
		criteriaDialog.setLocationRelativeTo(this);
		criteriaDialog.setVisible(true);
	}//GEN-LAST:event_criteriaButtonActionPerformed

	private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
		aboutDialog.pack();
		aboutDialog.setLocationRelativeTo(this);
		aboutDialog.setVisible(true);
	}//GEN-LAST:event_aboutItemActionPerformed

	private void licenseTreeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_licenseTreeKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			Object node = licenseTree.getLastSelectedPathComponent();

			if (node instanceof FileAbstract) {
				FileAbstract file = (FileAbstract)node;

				/* Check if tab is already open */
				if(openTabs.containsKey(file)) switchFile(file);
				else showFile(file);

			} else if (node instanceof FileReference) {
				FileReference ref = (FileReference)node;

				jumptoReference(ref);
			}
		}
	}//GEN-LAST:event_licenseTreeKeyPressed

	private void printItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printItemActionPerformed
		Component tab = tabbedPane.getSelectedComponent(); 
		if (tab instanceof LicenseTab) ((LicenseTab)tab).printLicense();
	}//GEN-LAST:event_printItemActionPerformed

	private void printTabItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printTabItemActionPerformed
		Component tab = tabbedPane.getSelectedComponent(); 
		if (tab instanceof LicenseTab) ((LicenseTab)tab).printLicense();
	}//GEN-LAST:event_printTabItemActionPerformed

	private void overviewHyperLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_overviewHyperLinkMouseClicked
		if(overviewHyperLink.isActive()) {
			overviewDialog.pack();
			overviewDialog.setLocationRelativeTo(this);
			overviewDialog.setVisible(true);
		}
	}//GEN-LAST:event_overviewHyperLinkMouseClicked

	private void closeTabItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeTabItemActionPerformed
		Component tab = tabbedPane.getSelectedComponent(); 
		if (tab instanceof LicenseTab) closeTab((LicenseTab)tab);
	}//GEN-LAST:event_closeTabItemActionPerformed

	private void closeItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeItemActionPerformed
		Component tab = tabbedPane.getSelectedComponent(); 
		if (tab instanceof LicenseTab) closeTab((LicenseTab)tab);
	}//GEN-LAST:event_closeItemActionPerformed

	private void filterComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterComboActionPerformed
		updateCriteria();
		updateFiltering();
	}//GEN-LAST:event_filterComboActionPerformed

	private void referencesBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_referencesBoxItemStateChanged
		if (evt.getStateChange() == ItemEvent.DESELECTED) {
			licenseTree.showReferences(false);
		} else {
			licenseTree.showReferences(true);
		}
	}//GEN-LAST:event_referencesBoxItemStateChanged

	private void svnCheckoutItemActionPerformed(java.awt.event.ActionEvent evt) {                                         
		SVNCheckoutWizard wizard = new SVNCheckoutWizard(this, true);
		wizard.setLocationRelativeTo(this);
		wizard.setVisible(true);
		if (wizard.getCheckedFolder() != null) {
        		activePackage = new File(wizard.getCheckedFolder());
        		if (activePackage.exists()) {
        			runChecker();
        		}
		}
	}   
	
	private void cvsCheckoutItemActionPerformed(java.awt.event.ActionEvent evt) {                                         
		CVSCheckoutWizard wizard = new CVSCheckoutWizard(this, true);
		wizard.setLocationRelativeTo(this);
		wizard.setVisible(true);
		if (wizard.getCheckedFolderPath() != null) {
        		activePackage = new File(wizard.getCheckedFolderPath());
        		if (activePackage.exists()) {
        			runChecker();
        		}
		}
	}      


	private void processDialogWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_processDialogWindowClosing
		lc.cancel();
	}//GEN-LAST:event_processDialogWindowClosing

	private void processCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processCancelActionPerformed
		lc.cancel();
	}//GEN-LAST:event_processCancelActionPerformed

	private void wrapBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrapBoxActionPerformed
		int tabCount = tabbedPane.getTabCount();

		for (int i = 0; i < tabCount; i++) {
			LicenseTab tab = (LicenseTab) tabbedPane.getComponentAt(i);
			tab.setWordWrap(wrapBox.getState());
		}
	}//GEN-LAST:event_wrapBoxActionPerformed

	private void licenseTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_licenseTreeMouseClicked
		Object selnode = licenseTree.getLastSelectedPathComponent();

		if (evt.getClickCount() == 2) {
			if (selnode instanceof FileAbstract) { 
				FileAbstract file = (FileAbstract)selnode;
				showFile(file);
			} else if (selnode instanceof FileReference) {
				FileReference ref = (FileReference)selnode;
				jumptoReference(ref);
			}
		}
	}//GEN-LAST:event_licenseTreeMouseClicked

	private void overviewBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overviewBoxActionPerformed
		overviewPanel.setVisible(overviewBox.getState());
	}//GEN-LAST:event_overviewBoxActionPerformed

	private void prefsCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefsCancelActionPerformed
		prefsDialog.setVisible(false);
	}//GEN-LAST:event_prefsCancelActionPerformed

	private void prefsOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefsOkActionPerformed
		prefsDialog.setVisible(false);
	}//GEN-LAST:event_prefsOkActionPerformed

	private void prefsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefsItemActionPerformed
		prefsDialog.pack();
		prefsDialog.setLocationRelativeTo(this);
		prefsDialog.setVisible(true);
	}//GEN-LAST:event_prefsItemActionPerformed

    /**
     * 
     * @author jpkheikk
     * 
     * Changes made by Johannes Heikkinen for oslcv3.
     *
     * modified/ekurkela
     * 
     * @param evt
     */

    private void licenseOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseOKButtonActionPerformed
    }//GEN-LAST:event_licenseOKButtonActionPerformed
    private void licenseCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                
    }
    private void compatibleOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compatibleOKButtonActionPerformed
    }//GEN-LAST:event_compatibleOKButtonActionPerformed
    private void compatibleCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    }
    private void compatibleCancelButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                                   
    }
    private void listCompatibleLicensesButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                             
    }
    private void addLicenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLicenseItemActionPerformed
		AddLicenseWizard wizard = new AddLicenseWizard(this, true);
		wizard.setLocationRelativeTo(this);
		wizard.setVisible(true);
}//GEN-LAST:event_addLicenseItemActionPerformed
	private void compatibleLicenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compatibleLicenseItemActionPerformed
		CompatibleLicenses cl = new CompatibleLicenses(this, true);
        try {
            cl.setFoundLicenses(lc.getFoundLicenses());
        }
        catch(Exception e) {
            //System.out.println("setFoundLicenses: " + e.getMessage());
        }

        try {
            /* TBD: fix path "licenses" /ekurkela */
            LicenseDatabase ldb = new LicenseDatabase("licenses");
            ldb.buildLicenseDatabase();
            ArrayList<License> allLicenses = new ArrayList<License>(ldb.getLicenses());
            allLicenses.addAll(ldb.getForbiddenPhrases());
            cl.setAllLicenses(new HashSet(allLicenses));
        }
        catch(Exception e) {
            //System.out.println("setAllLicenses: " + e.getMessage());
        }
        
		cl.setLocationRelativeTo(this);
		cl.setVisible(true);
	}//GEN-LAST:event_compatibleLicenseItemActionPerformed
	
	/* END */ 
	
	private void langItemActionPerformed(java.awt.event.ActionEvent evt) {
	    
                JFrame frame = new JFrame(loc.lc("Select language"));
                frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                JComponent newContentPane = new LanguageSelector(frame, this);
                newContentPane.setOpaque(true);
                frame.setContentPane(newContentPane);
                frame.pack();
                frame.setVisible(true);
		//System.out.println("True");
	}
	
	private void newLangItemActionPerformed(java.awt.event.ActionEvent evt) {	
	   Translator tr = new Translator(this);
	   tr.main();
	}

	private void closeAllItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAllItemActionPerformed
		closeAllTabs();
	}//GEN-LAST:event_closeAllItemActionPerformed

	private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitItemActionPerformed
		System.exit(0);
	}//GEN-LAST:event_quitItemActionPerformed

	private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
		int retval = fileChooser.showOpenDialog(this);

		if (retval == JFileChooser.APPROVE_OPTION) {
			// System.out.println("You chose to open this file: " +
			activePackage = fileChooser.getSelectedFile(); 
			if (activePackage.exists()) {
				runChecker();
			} else {
				JOptionPane.showMessageDialog(this,
						loc.lc("File you selected does not exist"),
						loc.lc("File not found"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}//GEN-LAST:event_openButtonActionPerformed

	private void openItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openItemActionPerformed
		int retval = fileChooser.showOpenDialog(this);

		if (retval == JFileChooser.APPROVE_OPTION) {
			// System.out.println("You chose to open this file: " +
			activePackage = fileChooser.getSelectedFile(); 
			if (activePackage.exists()) {
				runChecker();
			} else {
				JOptionPane.showMessageDialog(this,
						loc.lc("File you selected does not exist"),
						loc.lc("File not found"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}//GEN-LAST:event_openItemActionPerformed

    /* Export report
     *
     * ekurkela */
    private void exportItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportItemActionPerformed
        if(lc == null) {
            JOptionPane.showMessageDialog(this, loc.lc("No package available"));
        }
        else {
            File saveFile = null;

            while (true) {
                int returnVal = exportChooser.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String path = exportChooser.getSelectedFile().getPath();

                    /* append extension to path if necessary */
                    if (!(path.endsWith(".rtf") || path.endsWith(".pdf"))) {
                        String desc = exportChooser.getFileFilter().getDescription().toString().toLowerCase();
                        path = path + desc;
                    }

                    /* manage filename collision */
                    File chosen = new File(path);
                    if (!chosen.exists()) {
                        saveFile = chosen;
                        break;
                    } else {
                        int confirm = JOptionPane.showConfirmDialog(this, "Overwrite file? " + chosen.getName());
                        if (confirm == JOptionPane.OK_OPTION) {
                            saveFile = chosen;
                            break;
                        } else if (confirm == JOptionPane.NO_OPTION) {
                            continue;
                        }
                        break;
                    }
                } else {
                    break;
                }
            }


            /* write report */
            if(saveFile != null) {
                //System.out.println("Exporting report: " + saveFile.getPath());
                lc.writeReportGui(saveFile.getPath());
             }
        }
    }//GEN-LAST:event_exportItemActionPerformed

    private void downloadDatabaseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadDatabaseItemActionPerformed
        DownloadDatabase dd = new DownloadDatabase(this, true);
		dd.setLocationRelativeTo(this);
		dd.setVisible(true);
    }//GEN-LAST:event_downloadDatabaseItemActionPerformed

    private void editLicenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLicenseItemActionPerformed
        EditLicenseWizard w = new EditLicenseWizard(this, true);
		w.setLocationRelativeTo(this);
		w.setVisible(true);
    }//GEN-LAST:event_editLicenseItemActionPerformed

    private void delLicenseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delLicenseItemActionPerformed
        DeleteLicense w = new DeleteLicense(this, true);
		w.setLocationRelativeTo(this);
		w.setVisible(true);
    }//GEN-LAST:event_delLicenseItemActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		Log.setVerbosity(LogEntry.ERROR);
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName()); 
			/*
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); 
			 */
		} catch(Exception e) {
			ErrorManager.error("Could not set L&F", e);
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new LicenseMain().setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JLabel aboutLabel1;
    private javax.swing.JLabel aboutLabel2;
    private javax.swing.JScrollPane aboutScrollPane;
    private javax.swing.JTextPane aboutTextPane;
    private javax.swing.JMenuItem addLicenseItem;
    private javax.swing.JLabel allCountLabel;
    private javax.swing.JMenuItem closeAllItem;
    private javax.swing.JMenuItem closeItem;
    private javax.swing.JMenuItem closeTabItem;
    private javax.swing.JMenuItem compatibleLicenseItem;
    private javax.swing.JLabel confGblCountLabel;
    private javax.swing.JLabel confRefCountLabel;
    private javax.swing.JScrollPane conflictsPane;
    private javax.swing.JPanel conflictsPanel;
    private javax.swing.JTable conflictsTable;
    private javax.swing.JLabel copyrFilesCountLabel;
    private javax.swing.JLabel copyrHoldersCountLabel;
    private javax.swing.JScrollPane copyrightHoldersPane;
    private javax.swing.JTable copyrightHoldersTable;
    private javax.swing.JScrollPane copyrightedFilesPane;
    private javax.swing.JTable copyrightedFilesTable;
    private javax.swing.JButton criteriaButton;
    private javax.swing.JComboBox criteriaCombo;
    private javax.swing.JDialog criteriaDialog;
    private javax.swing.JMenuItem cvsCheckoutItem;
    private javax.swing.JMenuItem delLicenseItem;
    private javax.swing.JLabel disLicCountLabel;
    private javax.swing.JMenuItem downloadDatabaseItem;
    private javax.swing.JMenuItem editLicenseItem;
    private javax.swing.JFileChooser exportChooser;
    private javax.swing.JMenuItem exportItem;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JComboBox filterCombo;
    private javax.swing.JButton firstButton;
    private javax.swing.JFrame helpFrame;
    private javax.swing.JMenuItem helpItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenuItem langItem;
    private javax.swing.JButton lastButton;
    private javax.swing.JPanel leftPanel;
    private checker.gui.tree.LicenseTree licenseTree;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane matchesPane;
    private javax.swing.JTable matchesTable;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newLangItem;
    private javax.swing.JButton nextButton;
    private javax.swing.JScrollPane nonCopyrightedFilesPane;
    private javax.swing.JTable nonCopyrightedFilesTable;
    private javax.swing.JButton openButton;
    private javax.swing.JMenuItem openItem;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JCheckBoxMenuItem overviewBox;
    private javax.swing.JDialog overviewDialog;
    private javax.swing.JTabbedPane overviewDlgPane;
    private javax.swing.JPanel overviewDlgPanel;
    private checker.gui.LicenseHyperLink overviewHyperLink;
    private javax.swing.JLabel overviewLabel1;
    private javax.swing.JLabel overviewLabel2;
    private javax.swing.JLabel overviewLabel3;
    private javax.swing.JLabel overviewLabel4;
    private javax.swing.JLabel overviewLabel5;
    private javax.swing.JLabel overviewLabel6;
    private javax.swing.JLabel overviewLabel7;
    private javax.swing.JPanel overviewPanel;
    private javax.swing.JButton prefsCancel;
    private javax.swing.JDialog prefsDialog;
    private javax.swing.JMenuItem prefsItem;
    private javax.swing.JLabel prefsLabel;
    private javax.swing.JButton prefsOk;
    private javax.swing.JPanel prefsPanel;
    private javax.swing.JButton prevButton;
    private javax.swing.JMenuItem printItem;
    private javax.swing.JMenuItem printTabItem;
    private javax.swing.JProgressBar processBar;
    private javax.swing.JButton processCancel;
    private javax.swing.JDialog processDialog;
    private javax.swing.JLabel processLabel;
    private javax.swing.JMenuItem quitItem;
    private javax.swing.JCheckBox referencesBox;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JLabel srcCountLabel;
    private javax.swing.JProgressBar statusBar;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem svnCheckoutItem;
    private javax.swing.JPopupMenu tabPopup;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JPanel treePanel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JCheckBoxMenuItem wrapBox;
    // End of variables declaration//GEN-END:variables

}
