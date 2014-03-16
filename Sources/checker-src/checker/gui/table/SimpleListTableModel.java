
package checker.gui.table;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ekurkela
 */
public class SimpleListTableModel extends AbstractTableModel {
    private String[] columnNames = {""};
    private ArrayList<SimpleListTableRow> data;

    public SimpleListTableModel(String columnName) {
        columnNames[0] = columnName;
        data = new ArrayList<SimpleListTableRow>();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data.get(row).getData();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void addRow(SimpleListTableRow row) {
        data.add(row);
    }

    public void addRow(String row) {
        SimpleListTableRow newRow = new SimpleListTableRow(row);
        data.add(newRow);
     }

    public void clear() {
        data = new ArrayList<SimpleListTableRow>();
    }


    public void update(String columnName, String[] rows) {
        columnNames[0] = columnName;
        data = new ArrayList<SimpleListTableRow>();

        for(String row : rows) {
            addRow(row);
        }
    }
}


