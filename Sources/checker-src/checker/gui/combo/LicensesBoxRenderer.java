package checker.gui.combo;

import checker.license.License;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Renderer for e.g. a combo box for selecting license (this should also
 * work with lists)
 *
 * @author ekurkela
 */
public class LicensesBoxRenderer extends JLabel implements ListCellRenderer {
        public LicensesBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        // returns a JLabel containing the screen name (full name) of a license
        public Component getListCellRendererComponent(
                                           JList list,
                                           Object value,
                                           int index,
                                           boolean isSelected,
                                           boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            String val;
            try {
            	val = ((License)value).getScreenName();
            }
            catch(Exception e) {
            	val ="";
            }
            setText(val);

            return this;
        }
}
