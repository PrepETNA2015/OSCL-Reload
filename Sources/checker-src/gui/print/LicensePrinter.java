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

package checker.gui.print;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * A class for printing file contents
 * EXPERIMENTAL
 * Has not been thoroughly tested,
 * PDF printing did not work 
 *
 * @author Veli-Jussi Raitila
 * @deprecated
 */
public class LicensePrinter {

	/**
	 * Print the given text.
	 *
	 * @param text The text to be printed.
	 */
	public void print(String text) {

		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		DocAttributeSet das = new HashDocAttributeSet();
		
		Doc myDoc = new SimpleDoc(text.getBytes(), flavor, das);
		
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

		PrintService ps[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		PrintService service = ServiceUI.printDialog(null, 200, 200, ps, defaultService, flavor, pras);
		
		if (service != null) {
			/*
			for (DocFlavor f : service.getSupportedDocFlavors()) {
				System.out.println(String.format("Flavor: %s", f.toString()));
			}
			
			for (Attribute a : service.getAttributes().toArray()) {
				System.out.println(String.format("Attr: %s", a.getName()));
			}
			*/
			
			DocPrintJob job = service.createPrintJob();
			try {
				job.print(myDoc, pras);
			} catch (PrintException pe) {
				pe.printStackTrace();
			}
		}
	}
}
