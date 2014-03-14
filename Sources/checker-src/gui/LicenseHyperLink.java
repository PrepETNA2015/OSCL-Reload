/**
 * 
 *   Copyright (C) <2006> <Veli-Jussi Raitila>
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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

/**
 * A JLabel that looks and behaves like a hyperlink
 *
 * @author Veli-Jussi Raitila
 * 
 */
public class LicenseHyperLink extends JLabel implements MouseListener {

    private final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    private final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private final String activeText =   "<html><a href=\"#\">%s</a></html>";
    private final String inactiveText = "<html><u><font color=\"gray\">%s</font></u></html>";
    private String text;
    private boolean active;
    
    /** Creates a new instance of LicenseHyperLink */
    public LicenseHyperLink() {
        super();
        text = null;
        active = false;
        addMouseListener(this);
    }

    /**
     * Creates a new instance of LicenseHyperLink
     * @param a
     */
    public LicenseHyperLink(boolean a) {
        super();
        text = null;
        active = a;
        addMouseListener(this);
    }

    public void mouseEntered(MouseEvent e) {
        if (active) setCursor(handCursor);
    }

    public void mouseExited(MouseEvent e) {
    	if (active) setCursor(defaultCursor);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void setActive(boolean a) {
    	active = a;
    	setText(text);
    }
    
    public boolean isActive() {
    	return active;
    }
    
    public void setText(String t) {
        text = t;

    	if (active) super.setText(String.format(activeText, text));
    	else super.setText(String.format(inactiveText, text));
    }
    
}
