
/*
 * AddLicenseDialog.java
 *
 * Created on 26.1.2009, 15:03:07
 */

package checker.gui.license;

import checker.Pair;
import checker.Reference;
import checker.gui.TableSorter;
import checker.gui.combo.LicensesBoxModel;
import checker.gui.combo.LicensesBoxRenderer;
import java.awt.CardLayout;
import checker.gui.table.SelectLicensesTableModel;
import checker.license.License;
import checker.license.LicenseEditor;
import checker.license.LicenseWriter;
import checker.localization.Locale;
import java.lang.String;
import javax.swing.JOptionPane;

/**
 * A dialog for editing existing licenses.
 *
 * @author ekurkela
 */
public class EditLicenseWizard extends javax.swing.JDialog {

    private static Locale loc = new Locale();
    private SelectLicensesTableModel selectLicenseTableModel;
    private int currentCard = 0;
    private static String[] cardTitle = {loc.lc("Select license"), loc.lc("Enter license name and text"), loc.lc("Select compatible licenses")};

    /** Creates new form AddLicenseDialog */
    public EditLicenseWizard(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        selectLicenseTableModel = new SelectLicensesTableModel();
        // replaced with code using TableSorter for compatibility with Java 5 licenseTable.setModel(selectLicenseTableModel);
        TableSorter selectLicensesTableSorter = new TableSorter(selectLicenseTableModel);
		licenseTable.setModel(selectLicensesTableSorter);
		selectLicensesTableSorter.setTableHeader(licenseTable.getTableHeader());
        // hide column containing license objects
        licenseTable.removeColumn(licenseTable.getColumnModel().getColumn(0));

        // disable options that aren't currently supported
        licenseNameTextField.setEnabled(false);
        licenseTypeComboBox.setEnabled(false);

        // set model and renderer for license selection combo box
        selectLicenseComboBox.setModel(new LicensesBoxModel(true));
        selectLicenseComboBox.setRenderer(new LicensesBoxRenderer());

        // show first card
        currentCard = 0;
        showCard(currentCard);
    }

    /* validates input */
    public boolean validateCard(int n) {
        boolean valid = true;

        if(n == 1) {
            String licenseNameString = licenseNameTextField.getText();
            String licenseTextString = licenseTextTextArea.getText();
            LicenseWriter licenseWriter = new LicenseWriter(licenseNameString, licenseTextString);
            //fix for license name already existing check -jpkheikk 20090213
            //String licenseIDName = licenseWriter.getLicenseIDName(licenseNameString, licenseTypeComboBox.getSelectedIndex(), 0); //jpkheikk 20090213
            String licenseIDName = ((License)selectLicenseComboBox.getSelectedItem()).getId();
            /**
             * Check if a license with the same name already exists or license text is empty.
             */
            if(licenseWriter.isLicenseExisting(licenseIDName)) {
                // inform the user about the existence of a license with same name
                /*JOptionPane.showMessageDialog(this,
                        loc.lc("License with that name already exists!"),
                        loc.lc("Error"), JOptionPane.ERROR_MESSAGE);
                valid = false;*/
            } else if (licenseNameString.length() < 1) {
                // license name is null -> report invalid input
                JOptionPane.showMessageDialog(this,
                        loc.lc("License name is empty"),
                        loc.lc("Error"), JOptionPane.ERROR_MESSAGE);
                //System.out.println(loc.lc("ERROR: license name is null"));
                valid = false;
            } else if (licenseTextString.length() < 1) {
                // license text is null -> report invalid input
                JOptionPane.showMessageDialog(this,
                        loc.lc("License text is empty"),
                        loc.lc("Error"), JOptionPane.ERROR_MESSAGE);
                //System.out.println(loc.lc("ERROR: license text is null"));
                valid = false;
            }
        }

        return valid;
    }

    /* process input given via card */
    private void processCard(int n) {
        // license selected -> load properties to gui
        if(n == 0) {
            License selected = (License)selectLicenseComboBox.getSelectedItem();

            // set basic information in fields
            // name
            licenseNameTextField.setText(selected.getScreenName());
            String licenseText = "";
            for(int i=0; i<selected.getLicenseText().size(); i++) {
                if(i != 0) {
                    licenseText += "\n";
                }
                licenseText += selected.getLicenseText().get(i);
            }
            // text
            licenseTextTextArea.setText(licenseText);
            // version/type
            if(selected.isForbiddenPhrase()) {
                licenseTypeComboBox.setSelectedIndex(3);
            }
            else if(selected.isLongVersion()) {
                licenseTypeComboBox.setSelectedIndex(2);
            }
            else if(selected.isShortVersion()) {
                licenseTypeComboBox.setSelectedIndex(1);
            }
            else {
                licenseTypeComboBox.setSelectedIndex(0);
            }

            // set compatible licenses
            if(selected.getCompatibleReference() != null) {
                for(Pair<Reference.ReferenceType,License> reference : selected.getCompatibleReference()) {
                    License license = reference.e2;
                    for(int i=0;i<licenseTable.getRowCount();i++) {
                        if(selectLicenseTableModel.getLicenseAt(i).compareTo(license) == 0) {
                            selectLicenseTableModel.setValueAt(true, i, 2);
                        }
                    }
                }
            }
        }
    }

    /* switches between steps of wizard */
    public void showCard(int n) {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());

        if(n == 0) {
           backButton.setEnabled(false);
           // disable "next" button if list of licenses is empty 
           if(selectLicenseComboBox.getModel().getSize() < 1) {
        	   nextButton.setEnabled(false);
           }
           else {
        	   nextButton.setEnabled(true);
           }
           finishButton.setEnabled(false);
        }
        else if(n == 1) {
           backButton.setEnabled(true);
           nextButton.setEnabled(true);
           finishButton.setEnabled(false);
        }
        else if(n == 2) {
           backButton.setEnabled(true);
           nextButton.setEnabled(false);
           finishButton.setEnabled(true);
        }

        //show card
        cl.show(mainPanel, Integer.toString(n));

        //update card title
        titleLabel.setText(this.cardTitle[n]);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        finishButton = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        selectLicensePanel = new javax.swing.JPanel();
        selectLicenseComboBox = new javax.swing.JComboBox();
        basicInfoPanel = new javax.swing.JPanel();
        licenseNameLabel = new javax.swing.JLabel();
        licenseNameTextField = new javax.swing.JTextField();
        licenseTextLabel = new javax.swing.JLabel();
        licenseTextScrollPane = new javax.swing.JScrollPane();
        licenseTextTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        licenseTypeComboBox = new javax.swing.JComboBox();
        compatibilityPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        licenseTable = new javax.swing.JTable();
        selectAllButton = new javax.swing.JButton();
        selectNoneButton = new javax.swing.JButton();
        invertSelectionButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        titleLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(loc.lc("Edit license"));

        backButton.setText(loc.lc("Back"));
        backButton.setEnabled(false);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        nextButton.setText(loc.lc("Next"));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(loc.lc("Cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        finishButton.setText(loc.lc("Finish"));
        finishButton.setEnabled(false);
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });

        mainPanel.setLayout(new java.awt.CardLayout());

        org.jdesktop.layout.GroupLayout selectLicensePanelLayout = new org.jdesktop.layout.GroupLayout(selectLicensePanel);
        selectLicensePanel.setLayout(selectLicensePanelLayout);
        selectLicensePanelLayout.setHorizontalGroup(
            selectLicensePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectLicensePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectLicenseComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 268, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(328, Short.MAX_VALUE))
        );
        selectLicensePanelLayout.setVerticalGroup(
            selectLicensePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectLicensePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectLicenseComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(306, Short.MAX_VALUE))
        );

        mainPanel.add(selectLicensePanel, "0");

        licenseNameLabel.setText(loc.lc("License name"));

        licenseTextLabel.setText(loc.lc("License text"));

        licenseTextTextArea.setColumns(20);
        licenseTextTextArea.setRows(5);
        licenseTextScrollPane.setViewportView(licenseTextTextArea);

        jLabel1.setText(loc.lc("License type"));

        licenseTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { loc.lc("Normal"), "Short version", "Long version", "Forbidden phrase" }));
        licenseTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseTypeComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout basicInfoPanelLayout = new org.jdesktop.layout.GroupLayout(basicInfoPanel);
        basicInfoPanel.setLayout(basicInfoPanelLayout);
        basicInfoPanelLayout.setHorizontalGroup(
            basicInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(basicInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(basicInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(basicInfoPanelLayout.createSequentialGroup()
                        .add(licenseNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(licenseNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 208, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(basicInfoPanelLayout.createSequentialGroup()
                        .add(licenseTextLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(licenseTextScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE))
                    .add(basicInfoPanelLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(licenseTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 197, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        basicInfoPanelLayout.linkSize(new java.awt.Component[] {jLabel1, licenseNameLabel, licenseTextLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        basicInfoPanelLayout.linkSize(new java.awt.Component[] {licenseNameTextField, licenseTextScrollPane}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        basicInfoPanelLayout.setVerticalGroup(
            basicInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(basicInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(basicInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(licenseNameLabel)
                    .add(licenseNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(basicInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(licenseTextLabel)
                    .add(licenseTextScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(basicInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(licenseTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        mainPanel.add(basicInfoPanel, "1");

        licenseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("License"), "Compatible"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        licenseTable.setShowVerticalLines(false);
        jScrollPane2.setViewportView(licenseTable);

        selectAllButton.setText(loc.lc("Select all"));
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        selectNoneButton.setText(loc.lc("Select none"));
        selectNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoneButtonActionPerformed(evt);
            }
        });

        invertSelectionButton.setText(loc.lc("Invert selection"));
        invertSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertSelectionButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout compatibilityPanelLayout = new org.jdesktop.layout.GroupLayout(compatibilityPanel);
        compatibilityPanel.setLayout(compatibilityPanelLayout);
        compatibilityPanelLayout.setHorizontalGroup(
            compatibilityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(compatibilityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(compatibilityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                    .add(compatibilityPanelLayout.createSequentialGroup()
                        .add(selectAllButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(selectNoneButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(invertSelectionButton)))
                .addContainerGap())
        );

        compatibilityPanelLayout.linkSize(new java.awt.Component[] {invertSelectionButton, selectAllButton, selectNoneButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        compatibilityPanelLayout.setVerticalGroup(
            compatibilityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, compatibilityPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 287, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(compatibilityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectAllButton)
                    .add(selectNoneButton)
                    .add(invertSelectionButton))
                .add(43, 43, 43))
        );

        mainPanel.add(compatibilityPanel, "2");

        titleLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        titleLabel.setText(loc.lc("Select license"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(344, Short.MAX_VALUE)
                .add(backButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nextButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(finishButton)
                .addContainerGap())
            .add(jSeparator7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(titleLabel)
                .addContainerGap(519, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(titleLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(finishButton)
                    .add(cancelButton)
                    .add(nextButton)
                    .add(backButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        int newCard = currentCard - 1;
        if(newCard >= 0) {
            //update current card
            currentCard--;
            
            // show card
            this.showCard(currentCard);
        }
}//GEN-LAST:event_backButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
}//GEN-LAST:event_cancelButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if(validateCard(currentCard)) {
            int newCard = currentCard + 1;
            if(newCard <= cardTitle.length) {
                //handle input
                processCard(currentCard);

                //update current card
                currentCard++;

                // show card
                this.showCard(currentCard);
            }
        }
}//GEN-LAST:event_nextButtonActionPerformed

    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishButtonActionPerformed
        String licenseNameString = licenseNameTextField.getText();
        String licenseTextString = licenseTextTextArea.getText();
        //fix for license name already existing check -jpkheikk 20090213
        //String licenseIDName = licenseWriter.getLicenseIDName(licenseNameString, licenseTypeComboBox.getSelectedIndex(), 0); //jpkheikk 20090213
        String licenseId = ((License)selectLicenseComboBox.getSelectedItem()).getId();
        int selectedCount = 0;
        /* jpkheikk, modified/ekurkela */
        // counting the amount of selections
        for (int i = 0; i < selectLicenseTableModel.getRowCount(); i++) {
            if (selectLicenseTableModel.getValueAt(i, 2).equals(true)) {
                selectedCount++;
            }
        }

        // adding the selections to a String table of correct size
        String[] compatibleLicenses = new String[selectedCount];

        //System.out.println("SelectedCount: " + selectedCount);
        int j = 0;
        for (int i = 0; i < selectLicenseTableModel.getRowCount(); i++) {
            if (selectLicenseTableModel.getValueAt(i, 2).equals(true)) {
                compatibleLicenses[j] = selectLicenseTableModel.getLicenseAt(i).getId();
                j++;
            }
        }

        // Creating a JOptionPane to allow user to select whether to save the license or not.
        int answer = JOptionPane.showConfirmDialog(this,
                String.format(loc.lc("Save license %s?"), licenseNameString),
                loc.lc("Save license?"), JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            // if user answers yes: the .txt and .meta files are updated
            int tag = licenseTypeComboBox.getSelectedIndex();

            LicenseEditor le = new LicenseEditor();
            //if(!le.updateLicense(licenseId, licenseNameString, licenseTextString, tag, compatibleLicenses)) {
            int res = le.updateLicense(licenseId, licenseTextString, compatibleLicenses, compatibleLicenses.length, tag);
            if(res == 0) {
                JOptionPane.showMessageDialog(this,
                        "License updated",
                        "OSLC", JOptionPane.INFORMATION_MESSAGE);
                //System.out.println(loc.lc("License updated"));
            }
            else {
                String msg = "Error: ";
                if(res == 1) {
                    msg += "file(s) do not exist";
                }
                else if(res == 2) {
                    msg += "file(s) is(are) write protected";
                }
                else if(res == 3) {
                    msg += "meta file not deleted (fatal error)";
                }
                else if(res == 4) {
                    msg += "txt file not deleted (fatal error)";
                }
                JOptionPane.showMessageDialog(this,
                        msg,
                        loc.lc("Error"), JOptionPane.ERROR_MESSAGE);
                //System.out.println(loc.lc(msg));
            }

            //dispose
            dispose();
        }
    /* end */
        
}//GEN-LAST:event_finishButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        for (int i = 0; i < selectLicenseTableModel.getRowCount(); i++) {
            selectLicenseTableModel.setValueAt(true, i, 2);
        }
}//GEN-LAST:event_selectAllButtonActionPerformed

    private void selectNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoneButtonActionPerformed
        for (int i = 0; i < selectLicenseTableModel.getRowCount(); i++) {
            selectLicenseTableModel.setValueAt(false, i, 2);
        }
}//GEN-LAST:event_selectNoneButtonActionPerformed

    private void invertSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertSelectionButtonActionPerformed
        for (int i = 0; i < selectLicenseTableModel.getRowCount(); i++) {
            selectLicenseTableModel.setValueAt(!selectLicenseTableModel.getValueAt(i, 2).equals(true), i, 2);
        }
}//GEN-LAST:event_invertSelectionButtonActionPerformed

    private void licenseTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseTypeComboBoxActionPerformed

}//GEN-LAST:event_licenseTypeComboBoxActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EditLicenseWizard dialog = new EditLicenseWizard(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel basicInfoPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel compatibilityPanel;
    private javax.swing.JButton finishButton;
    private javax.swing.JButton invertSelectionButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JLabel licenseNameLabel;
    private javax.swing.JTextField licenseNameTextField;
    private javax.swing.JTable licenseTable;
    private javax.swing.JLabel licenseTextLabel;
    private javax.swing.JScrollPane licenseTextScrollPane;
    private javax.swing.JTextArea licenseTextTextArea;
    private javax.swing.JComboBox licenseTypeComboBox;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JComboBox selectLicenseComboBox;
    private javax.swing.JPanel selectLicensePanel;
    private javax.swing.JButton selectNoneButton;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}
