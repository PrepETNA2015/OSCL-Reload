/*
 * CompatibleLicenses.java
 *
 * Created on 27.1.2009, 10:59:50
 */

package checker.gui.license;

import checker.gui.TableSorter;
import checker.gui.table.SelectLicensesTableModel;
import checker.gui.table.SimpleListTableModel;
import checker.license.License;
import checker.localization.Locale;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author ekurkela
 */
public class CompatibleLicenses extends javax.swing.JDialog implements TableModelListener {
    private Locale loc = new Locale();
    private SelectLicensesTableModel selectLicensesTableModel;
    private SimpleListTableModel compatibleLicensesTableModel;
    private Set<License> foundLicenses = new HashSet<License>();
    private Set<License> allLicenses = new HashSet<License>();

    /** Creates new CompatibleLicenses dialog */
    public CompatibleLicenses(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        selectLicensesTableModel = new SelectLicensesTableModel();
         //replaced with code using TableSorter clSelectTable.setModel(selectLicensesTableModel);
        TableSorter selectLicensesTableSorter = new TableSorter(selectLicensesTableModel);
		clSelectTable.setModel(selectLicensesTableSorter);
		selectLicensesTableSorter.setTableHeader(clSelectTable.getTableHeader());
        // hide column containing license objects
        clSelectTable.removeColumn(clSelectTable.getColumnModel().getColumn(0));
        clSelectTable.getModel().addTableModelListener(this);

        compatibleLicensesTableModel = new SimpleListTableModel(loc.lc("License"));
        //replaced with code using TableSorter clCompatibleTable.setModel(compatibleLicensesTableModel);
        TableSorter compatibleLicensesTableSorter = new TableSorter(compatibleLicensesTableModel);
		clCompatibleTable.setModel(compatibleLicensesTableModel);
		compatibleLicensesTableSorter.setTableHeader(clCompatibleTable.getTableHeader());
    }

    public void setFoundLicenses(Set<License> fL) {
        foundLicenses = fL;
    }

    public void setAllLicenses(Set<License> aL) {
        allLicenses = aL;
    }

    public void tableChanged(TableModelEvent e) {
        updateCompatibleLicensesTable();

        if(selectedLicensesAreCompatible()) {
            selectedCompatibleLabel.setText(" ");
        }
        else {
            selectedCompatibleLabel.setText(loc.lc("Selected licenses are incompatible"));
            selectedCompatibleLabel.setForeground(new Color(150,0,0));
        }
    }

    /* updates table of compatible licenses depending on the selected licenses */
    private void updateCompatibleLicensesTable() {
        /* find compatible licenses by calculating the intersection of the
         * licenses compatible with each selected license */
        Set<License> selectedLicenses = getSelectedLicenses();
        Set<License> compatibleLicenses = new HashSet<License>(allLicenses);
        for(License l : selectedLicenses) {
            compatibleLicenses.retainAll(getCompatibleLicenses(l));
        }

        /* convert compatible licenses to array */
        String[] compatibleArray = new String[compatibleLicenses.size()];
        int i = 0;
        for(License l : compatibleLicenses) {
            compatibleArray[i] = l.getScreenName();
            i++;
        }

        /* if incompatible licenses have been selected,
         * make list of compatible licenses empty for intuitivity */
        if(!selectedLicensesAreCompatible()) {
            compatibleArray = new String[0];
        }

        /* update table of compatible licenses */
        compatibleLicensesTableModel.update(loc.lc("License"), compatibleArray);
        compatibleLicensesTableModel.fireTableDataChanged();
    }

    /* returns true if selected licenses are compatible with each other */
    public boolean selectedLicensesAreCompatible() {
        boolean compatible = true;
        Set<License> selectedLicenses = getSelectedLicenses();
        /* compare each selected license with the other selected licenses
         * and see if each of those is either compatible or the same */
        for(License l1 : selectedLicenses) {
            for(License l2 : selectedLicenses) {
                if(!(l1.isCompatible(l2, null) || l1.getId().equals(l2.getId()))) {
                    compatible = false;
                    //System.out.println(l1.getId() + " is not compatible with " + l2.getId());
                }
                else {
                    //System.out.println(l1.getId() + " is compatible with " + l2.getId());
                }
            }
        }

        return compatible;
    }

    /* returns licenses compatible with the given license
     *
     * TBD: rewrite & place in License.java */
    public Set<License> getCompatibleLicenses(License l) {
        Set<License> compatibleLicenses = new HashSet<License>();
        for (License l2 : allLicenses) {
            if (l.compareTo(l2) == 0) {
                for (License l3 : allLicenses) {
                    if (l2.isCompatible(l3, null)) {
                        compatibleLicenses.add(l3);
                    }
                }
            }
        }
        return compatibleLicenses;
    }

    /* returns selected licenses */
    private Set<License> getSelectedLicenses() {
        Set<License> selectedLicenses = new HashSet<License>();
        for (int i =0; i<selectLicensesTableModel.getRowCount(); i++) {
            if (selectLicensesTableModel.getValueAt(i, 2).equals(true)) {
                selectedLicenses.add(selectLicensesTableModel.getLicenseAt(i));
            }
        }

        return selectedLicenses;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        clSelectTable = new javax.swing.JTable();
        clUseCurrentCheckBox1 = new javax.swing.JCheckBox();
        selectedCompatibleLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        clCompatibleTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        clCancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(loc.lc("List compatible licenses"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(loc.lc("Select licenses")));

        clSelectTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("License"), ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        clSelectTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(clSelectTable);
        clSelectTable.getColumnModel().getColumn(1).setHeaderValue("");

        clUseCurrentCheckBox1.setText(loc.lc("Use licenses of current package"));
        clUseCurrentCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clUseCurrentCheckBox1ActionPerformed(evt);
            }
        });

        selectedCompatibleLabel.setForeground(new java.awt.Color(0, 153, 51));
        selectedCompatibleLabel.setText(" ");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(clUseCurrentCheckBox1)
                    .add(selectedCompatibleLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clUseCurrentCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectedCompatibleLabel)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(loc.lc("Compatible licenses")));

        clCompatibleTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                loc.lc("License")
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        clCompatibleTable.setShowVerticalLines(false);
        jScrollPane2.setViewportView(clCompatibleTable);

        jLabel1.setText(loc.lc("<html>Please note that the listed licenses are compatible <br>\nindividually - not necessarily if combined.</html>"));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .add(18, 18, 18))
        );

        clCancelButton.setText(loc.lc("Cancel"));
        clCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clCancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(516, Short.MAX_VALUE)
                .add(clCancelButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(clCancelButton)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void clCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clCancelButtonActionPerformed
        dispose();
}//GEN-LAST:event_clCancelButtonActionPerformed

    private void clUseCurrentCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clUseCurrentCheckBox1ActionPerformed
        /* If the checkbox is ticked, disable selecting of licenses and
         * select the licenses found in the current package */
        if(clUseCurrentCheckBox1.isSelected()) {
            clSelectTable.setEnabled(false);
            clSelectTable.setBackground(new Color(230,230,230));

            /* Compare each license with the ones that have been found in the
             * package */
            for(int i=0; i<selectLicensesTableModel.getRowCount(); i++) {
                selectLicensesTableModel.setValueAt(false, i, 2);
                
                for (Iterator<License> iterator = foundLicenses.iterator(); iterator.hasNext();) {
                    License license = iterator.next();
                    if(license.compareTo(selectLicensesTableModel.getLicenseAt(i)) == 0) {
                        selectLicensesTableModel.setValueAt(true, i, 2);
                        //System.out.println(license.getId() + " " + selectLicensesTableModel.getLicenseAt(i).getId() + " " + license.compareTo(selectLicensesTableModel.getLicenseAt(i)));
                    }
                }
            }
        }
        /* Else make license selection table enabled */
        else {
            clSelectTable.setEnabled(true);
            clSelectTable.setBackground(new Color(255,255,255));
        }
    }//GEN-LAST:event_clUseCurrentCheckBox1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CompatibleLicenses dialog = new CompatibleLicenses(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton clCancelButton;
    private javax.swing.JTable clCompatibleTable;
    private javax.swing.JTable clSelectTable;
    private javax.swing.JCheckBox clUseCurrentCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel selectedCompatibleLabel;
    // End of variables declaration//GEN-END:variables

}
