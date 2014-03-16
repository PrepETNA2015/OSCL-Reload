/**
 * 
 *   Copyright (C) 2008 Jyrki Laine
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
 */

package checker;

import java.util.*;
import java.io.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.text.SimpleDateFormat;

public class ReportWriter {
	
	public ReportWriter() {
		
	}
	
	public void writeReport(ArrayList<String> text, String fileName) throws IOException, DocumentException {
		int dot = fileName.lastIndexOf('.');
		String extension = fileName.substring(dot + 1);
		if (extension.equals("pdf")) {
			writePdfReport(text, fileName);
		} else if (extension.equals("rtf")) {
			writeRtfReport(text, fileName);
		} else {
			//TODO: Exception handling
		}
	}
	
	private String getTitle() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	    String time = sdf.format( cal.getTime());
	    return "OSLC License Report - " + time;
	}
	
	private void writeRtfReport(ArrayList<String> text, String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(getTitle());
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		for (String line : text) {
			bufferedWriter.write(line);
			bufferedWriter.newLine();
		}
		bufferedWriter.flush();
	}
	
	private void writePdfReport(ArrayList<String> text, String fileName) throws IOException, DocumentException {		
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream(fileName));
		document.open();
		document.addTitle(getTitle());
		
		Paragraph paragraph = new Paragraph();
		for (String line : text) {
			if (line.equals("")) {
				paragraph.add(Chunk.NEWLINE);
			} 
			else {
				paragraph.add(line);
				paragraph.add(Chunk.NEWLINE);
			}
		}
		document.add(paragraph);
		/*List list = new List(false);
		for (String line : text) {
			if (line.equals("")) {
				document.add(list);
				list = new List(false);
			} 
			else {
				list.add(new ListItem(line));
			}
		}*/
		//document.add(list);
		document.close();
	}
	
	
}
