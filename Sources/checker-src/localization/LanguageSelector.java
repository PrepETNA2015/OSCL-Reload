/**
 * 
 *   Copyright (C) <2008> <Mikko Kupsu>
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

package checker.localization;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.swing.*;
import java.util.HashMap;
import checker.localization.Locale;
import checker.gui.LicenseMain;


/**
 * LanguageSelector.java
 * 
 * Lets user select language used by the system. Writes the chosen
 * language to a configuration file.
 * 
 * @author mikko
 */
public class LanguageSelector extends JPanel {
	private JList list;
    private DefaultListModel listModel;
    private JButton apply;
    private JButton cancel;
    private HashMap<String, File> store = new HashMap<String, File>();
    private JFrame parent;
    private LicenseMain lwin;
    public Locale loc = new Locale();

    public LanguageSelector(JFrame iniater, LicenseMain l) {
        super(new BorderLayout());
        lwin = l;
        parent = iniater;
        listModel = new DefaultListModel();
        
        // Reading the language files
        File file = new File("languages/");
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            String temp = files[i].toString();
            int help = temp.indexOf('-');
            if (help != -1) {
                temp = temp.substring(help + 1, temp.length() - 4);
                store.put(temp, files[i]);
                listModel.addElement(temp);
            }
        }
        
        //Creation of the list
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        JScrollPane listScrolling = new JScrollPane(list);

        // Creation of cancel button
        cancel = new JButton(loc.lc("Cancel"));
        cancel.setActionCommand(loc.lc("Cancel"));       
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        // Creation of apply button
        apply = new JButton(loc.lc("Apply"));
        apply.setActionCommand(loc.lc("Apply"));
        apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyActionPerformed(evt);
            }
        });

        //Creates the panel
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(apply);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(new JSeparator());
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(cancel);
        buttons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(listScrolling, BorderLayout.CENTER);
        add(buttons, BorderLayout.PAGE_END);
    }
    
	/**
	 * Operation for the apply button.
	 * 
	 * Reads the selected file and creates new translation template.
	 * 
	 * @param action event
	 */
    private void applyActionPerformed(java.awt.event.ActionEvent evt) {
    	try{
            // Creates a file 
            FileWriter fstream = new FileWriter("conf.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(store.get(list.getSelectedValue().toString()).getAbsolutePath());
            //Close the output stream
            out.close();
            }
        catch (Exception e3){
              //System.out.println(e3.toString());
        }
	lwin.dispose();
	new LicenseMain().setVisible(true);
        parent.dispose();
    }
    
	/**
	 * Operation for cancel button.
	 * 
	 * Closes the window.
	 * 
	 * @param action event
	 */
    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {
        parent.dispose();
    }
}
