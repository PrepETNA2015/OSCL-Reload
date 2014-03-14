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
import java.util.Scanner;
import javax.swing.*;

import checker.localization.Locale;

/**
 * Translator.java
 * 
 * Lets the user choose the language where the user can start to translate the tool.
 * Creates also an template.
 * 
 * @author Mikko Kupsu
 */
public class Translator extends JPanel {
	private static Locale loc = new Locale();
	private JList list;
	private DefaultListModel listModel;
	private JButton apply;
	private JButton cancel;
	private JTextField output;
	private String[] fileName = {"language-", "newLanguage", ".txt" };
	private JFrame j;

	public Translator(JFrame jx) {
		super(new BorderLayout());
		listModel = new DefaultListModel();
		this.j = jx;
		
		// Reading the language files
		File file = new File("languages/");
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().indexOf(".svn") == -1)
				listModel.addElement(files[i].toString());
		}
		
		// Creation of the GUI
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

		// Text field and layout
		output = new JTextField(10);
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
		buttons.add(apply);
		buttons.add(Box.createHorizontalStrut(5));
		buttons.add(output);
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
		if (output.getText() != "")
			this.fileName[1] = output.getText();
				
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter("languages/" + this.fileName[0] + this.fileName[1] + this.fileName[2]));
			Scanner in = new Scanner(new File(this.list.getSelectedValue().toString()));

			String temp = "";
			while (in.hasNext()) {
				temp = in.nextLine();
				int point = temp.indexOf(":");
				if (point < 0 ) {
					out.write(temp + "\n");
				}
				else {
					String eka = temp.substring(0, point);
					String toka = temp.substring(point + 1, temp.length());
					out.write("#" + toka + "\n");
					out.write(eka + ":\n");
				}
			}
			out.close();
		}
		catch (Exception e3){
			//System.out.println(e3.toString());
		}
		j.dispose();
	}

	/**
	 * Operation for cancel button.
	 * 
	 * Closes the window.
	 * 
	 * @param action event
	 */
	private void cancelActionPerformed(java.awt.event.ActionEvent evt) {
		j.dispose();
	}

	/**
	 * Starts and shows the GUI.
	 * 
	 * @param Array of agruments. None needed.
	 */
	public static void main() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(loc.lc("Select language"));
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JComponent newContentPane = new Translator(frame);
				newContentPane.setOpaque(true);
				frame.setContentPane(newContentPane);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
