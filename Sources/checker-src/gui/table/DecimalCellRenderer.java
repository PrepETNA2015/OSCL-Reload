package checker.gui.table;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author ekurkela
 *
 * a cell renderer that shows decimals, e.g. "3.0"
 */
public class DecimalCellRenderer extends DefaultTableCellRenderer {
    private final NumberFormat formatter;

    public DecimalCellRenderer(String format) {
        if (format == null) {
            throw new IllegalArgumentException("format == null");
        }
        this.formatter = new DecimalFormat(format);
        setHorizontalAlignment(JLabel.RIGHT);
    }

    protected void setValue(Object value) {
        if (value instanceof Number) {
            value = formatter.format(value);
        }
        super.setValue(value);
    }
}
