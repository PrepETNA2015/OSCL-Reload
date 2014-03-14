/**
 * 
 *   Copyright (C) <2006> <Veli-Jussi Raitila>
 *   Edited by <2008> <Mikko Kupsu>
 *   Edited by <2009> <Eero Kurkela>
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import checker.localization.Locale;

import checker.ErrorManager;
import checker.Pair;
import checker.gui.table.MatchColorRenderer;
import checker.gui.table.MatchTableModel;
import checker.gui.table.MatchTableRow;
import checker.gui.table.TagTableModel;
import checker.gui.tree.FileAbstract;
import checker.gui.tree.FileReference;
import checker.gui.tree.FileSource;
import checker.gui.tree.LicenseTreeNode;
import checker.license.License;
import checker.license.Tag;
import checker.matching.LicenseMatch;
import checker.matching.MatchPosition;

import com.eit.easyprint.EasyPrint;

/**
 * A panel tab that is inserted into the view
 * for displaying details about a particular file.
 *
 * @author Veli-Jussi Raitila
 * 
 */
public class LicenseTab extends javax.swing.JPanel {
    
	// Reserve room for how many conflicts before scrolling
	private final int MAX_CONFLICTS = 4;
	// The file this tab contains
	private FileAbstract file;
	// Localization file
	private Locale loc;
	
	/** Creates new form LicenseTab */
    public LicenseTab() {
    	this.loc = new Locale();
        initComponents();
    	conflictsPanel.setVisible(false);
        conflictsPane.setVisible(false);
    	licensesTable.setDefaultRenderer(
    			Color.class, 
    			new MatchColorRenderer(true));
    }
    

    /**
     * Return the FileAbstract this tab contains
     * 
     * @return
     */
    public FileAbstract getFile() {
    	return file;
    }
    
    /**
     * Adds lines of text to the pane on this tab,
     * associate this tab to the file represented
     * and finally populate matches and conflicts.
     * 
     * @param lines
     */
    public void setFile(ArrayList<String> l, FileAbstract f) {

    	file = f;
    	
    	StringBuffer buffer = new StringBuffer();
    	for (String line : l) {
    		buffer.append(line);
    		buffer.append('\n');
    	}
    	fileTextArea.setText(buffer.toString());
    	fileTextArea.setCaretPosition(0);
    	
    	/* Populate matches table */
   		setMatches();
   		/* Populate conflicts table */
       	setConflicts();
    }
    
    /**
     * Populate matches, free-form fields and do highlighting
     */
    private void setMatches() {
    	ArrayList<LicenseMatch> m = file.getMatches();

    	if (m != null) {
    		switch (m.size()) {
    		case 0:
        		/* File has no license */
    			setLicense(loc.lc("This file is missing a license"));
    			break;
    		case 1:
        		/* File has one license */
    			setLicense(loc.lc("This file corresponds to"), m.get(0).getLicense().getScreenName());
    			break;
    		default:
        		/* File has several licenses */
    			setLicense(loc.lc("This file corresponds to"), loc.lc("several licenses"));
    			break;
    		}

			/* Add details about the license(s) */
			MatchTableModel matchmodel = new MatchTableModel();
			TagTableModel tagmodel = new TagTableModel();
		
			for (LicenseMatch match : m) {
		    	
				/* Handle free-form fields */
				HashMap<Tag, String> tags = match.getTags();
				if (tags != null) {
					tagmodel.putAll(tags);
				}
				
				/* Handle matches */
				String licenseid = match.getLicense().getId();
                String licenseScreenName = match.getLicense().getScreenName();
				float matchp = match.getMatchPr();
				int licensehash = licenseid.hashCode();
				Color matchcolor; 
				int alpha = 28 + Math.round(100.0f * matchp); 
					
				// TODO Better coloring algorithm, maybe?
		   		matchcolor = new Color(
		   				((licensehash & 0x000000ff)), 
		   				((licensehash & 0x00000ff0) >> 4), 
		   				((licensehash & 0x0000ff00) >> 8),
		   				alpha);
		   		
		    	matchmodel.addRow(new MatchTableRow(
		    			matchcolor,
		    			licenseScreenName,
		    			matchp 
		    			));
		
		    	/* Highlight match positions */
		    	for (MatchPosition pos : match.getMatchPositions()) {
		    		try {
			    		int startpos = fileTextArea.getLineStartOffset(
			    				pos.getStartLine()) + 
			    				pos.getStartCol();
			    		int endpos = fileTextArea.getLineStartOffset(
			    				pos.getEndLine()) + 
			    				pos.getEndCol();
			    		
			    		Highlighter hilight = fileTextArea.getHighlighter();
			    		hilight.addHighlight(startpos, endpos + 1, 
			    				new DefaultHighlightPainter(matchcolor));
		    		}
		    		catch (Exception e) {
			        	ErrorManager.error(loc.lc("Error highlighting"), e);
		    		}
		    	}
		    }
			licensesTable.setModel(matchmodel);
			tagsTable.setModel(tagmodel);

		/* A license cannot be applied to this file */
    	} else setLicense(loc.lc("This file is not a source file")); 
    }
    
    /**
     * Populate conflicts 
     */
    private void setConflicts() {
    	
    	/* Don't model conflicts if displaying a file without references */
		if (!(file instanceof FileSource)) return;

		DefaultListModel model = new DefaultListModel();

		for (LicenseTreeNode node : file.getChildren()) {
    		if (node instanceof FileReference) {
    			FileReference fileref = (FileReference)node;
    	    	ArrayList<Pair<License, License>> p = fileref.getConflicts();

    	    	if (p != null) 
    		    for (Pair<License, License> pair : p) {
    		    	String c = String.format(
    		    			loc.lc("License %s conflicts with %s in %s"), 
    		    			pair.e1.getScreenName(),
    		    			pair.e2.getScreenName(),
    		    			fileref.toString());
    		    	model.addElement(c);
    		    }
    		}
    	}
    	
		/* If conflicts were found, reflect that in the tab */
    	int count = model.getSize();
    	if (count != 0) {
    		count = count <= MAX_CONFLICTS ? count : MAX_CONFLICTS;
    		
    	    conflictsList.setModel(model);
    	    conflictsList.setVisibleRowCount(count);
        	conflictsPanel.setVisible(true);
    	}
    }
    
    /**
     * Set license information at the bottom of the tab.
     * This is the case where a file does not have a license
     * 
     * @param info
     */
    private void setLicense(String info) {
    	infoLabel.setText(info);
    	licenseLabel.setVisible(false);
    	detailsHyperLink.setVisible(false);
    }

    /**
     * Set license information at the bottom of the tab.
     * File has one license
     * 
     * @param info
     * @param license
     */
    private void setLicense(String info, String license) {
    	infoLabel.setText(info);
    	licenseLabel.setText(license);
    	detailsHyperLink.setActive(true);
    }

    public void setWordWrap(boolean wrap) {
    	fileTextArea.setLineWrap(wrap);
    }
    
    public void printLicense() {
    	/*
    	LicensePrinter printer = new LicensePrinter();
    	printer.print(jTextArea1.getText());
    	*/
    	
    	EasyPrint.printPlain(fileTextArea.getText(), file.toString());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        detailsDialog = new javax.swing.JDialog();
        detailPanel = new javax.swing.JPanel();
        detailPane = new javax.swing.JTabbedPane();
        licensesPane = new javax.swing.JScrollPane();
        licensesTable = new javax.swing.JTable();
        tagsPane = new javax.swing.JScrollPane();
        tagsTable = new javax.swing.JTable();
        infoPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        licenseLabel = new javax.swing.JLabel();
        detailsHyperLink = new checker.gui.LicenseHyperLink();
        fileTextPane = new javax.swing.JScrollPane();
        fileTextArea = new javax.swing.JTextArea();
        conflictsPanel = new javax.swing.JPanel();
        conflictsLabel = new javax.swing.JLabel();
        conflictsPane = new javax.swing.JScrollPane();
        conflictsList = new javax.swing.JList();

        detailsDialog.setTitle(loc.lc("Details"));
        detailPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(loc.lc("File Overview")));
        licensesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("Legend"), loc.lc("License"), loc.lc("Match%")
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        licensesPane.setViewportView(licensesTable);

        detailPane.addTab(loc.lc("Matches"), licensesPane);

        tagsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("License"), loc.lc("Tag"), loc.lc("Value")
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
        tagsPane.setViewportView(tagsTable);

        detailPane.addTab(loc.lc("Free Fields"), tagsPane);

        org.jdesktop.layout.GroupLayout detailPanelLayout = new org.jdesktop.layout.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout detailsDialogLayout = new org.jdesktop.layout.GroupLayout(detailsDialog.getContentPane());
        detailsDialog.getContentPane().setLayout(detailsDialogLayout);
        detailsDialogLayout.setHorizontalGroup(
            detailsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        detailsDialogLayout.setVerticalGroup(
            detailsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(detailPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setLayout(new java.awt.BorderLayout());

        infoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        infoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        infoLabel.setText(loc.lc("Matches"));
        infoPanel.add(infoLabel);

        licenseLabel.setText(loc.lc("license"));
        infoPanel.add(licenseLabel);

        detailsHyperLink.setText(loc.lc("Details..."));
        detailsHyperLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                detailsHyperLinkMouseClicked(evt);
            }
        });

        infoPanel.add(detailsHyperLink);

        add(infoPanel, java.awt.BorderLayout.NORTH);

        fileTextPane.setBorder(null);
        fileTextArea.setColumns(20);
        fileTextArea.setEditable(false);
        fileTextArea.setFont(new java.awt.Font("Courier New", 0, 12));
        fileTextArea.setRows(5);
        fileTextArea.setAutoscrolls(false);
        fileTextPane.setViewportView(fileTextArea);

        add(fileTextPane, java.awt.BorderLayout.CENTER);

        conflictsPanel.setLayout(new java.awt.BorderLayout());

        conflictsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        conflictsLabel.setForeground(java.awt.Color.red);
        conflictsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        conflictsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/delete.png")));
        conflictsLabel.setText(loc.lc("This file has license conflicts (click to show)"));
        conflictsLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        conflictsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                conflictsLabelMouseClicked(evt);
            }
        });

        conflictsPanel.add(conflictsLabel, java.awt.BorderLayout.NORTH);

        conflictsList.setVisibleRowCount(1);
        conflictsPane.setViewportView(conflictsList);

        conflictsPanel.add(conflictsPane, java.awt.BorderLayout.CENTER);

        add(conflictsPanel, java.awt.BorderLayout.SOUTH);

    }// </editor-fold>//GEN-END:initComponents

    private void conflictsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_conflictsLabelMouseClicked
        if(!conflictsPane.isVisible()) conflictsPane.setVisible(true);
        else conflictsPane.setVisible(false);
        conflictsPanel.revalidate();
    }//GEN-LAST:event_conflictsLabelMouseClicked

    private void detailsHyperLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_detailsHyperLinkMouseClicked
    	if(detailsHyperLink.isActive()) {
        	detailsDialog.pack();
            detailsDialog.setLocationRelativeTo(this);
            detailsDialog.setVisible(true);
    	}
    }//GEN-LAST:event_detailsHyperLinkMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel conflictsLabel;
    private javax.swing.JList conflictsList;
    private javax.swing.JScrollPane conflictsPane;
    private javax.swing.JPanel conflictsPanel;
    private javax.swing.JTabbedPane detailPane;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JDialog detailsDialog;
    private checker.gui.LicenseHyperLink detailsHyperLink;
    private javax.swing.JTextArea fileTextArea;
    private javax.swing.JScrollPane fileTextPane;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel licenseLabel;
    private javax.swing.JScrollPane licensesPane;
    private javax.swing.JTable licensesTable;
    private javax.swing.JScrollPane tagsPane;
    private javax.swing.JTable tagsTable;
    // End of variables declaration//GEN-END:variables
    
}
