/**
 * 
 *   Copyright (C) 2008 Lasse Parikka
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
package checker.gui.svn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
import org.tmatesoft.svn.core.internal.util.SVNSSLUtil;

import checker.localization.Locale;


/**
 * Asks user to accept the digital certificate with ssl connections.
 * Provides credentials for SSH connections.
 * @author lparikka
 */
public class AuthenticationProvider implements ISVNAuthenticationProvider {
	private Locale loc = new Locale();
    
    private static final int MAX_PROMPT_COUNT = 3;
    private Map myRequestsCount = new HashMap();
    private String username;
    private String password;
    private javax.swing.JDialog parent;
    
    public AuthenticationProvider(String username, String password, 
    		javax.swing.JDialog parentForCertificateWindow) {
    	this.username = username;
    	this.password = password;
    	parent = parentForCertificateWindow;
    	
    }
    
    public int acceptServerAuthentication(SVNURL url, String realm, 
    		Object certificate, boolean resultMayBeStored) {
        
    	if (!(certificate instanceof X509Certificate)) {
            return ISVNAuthenticationProvider.ACCEPTED_TEMPORARY;
        }
    	
        String hostName = url.getHost();
        X509Certificate cert = (X509Certificate) certificate;
        StringBuffer prompt = SVNSSLUtil.getServerCertificatePrompt(cert, realm, hostName);
        CertificateWindow prompter = new CertificateWindow(null, true);
       
        prompter.setLocationRelativeTo(parent);
        prompter.setCertificateString(prompt.toString());
        prompter.setVisible(true);
        
        char ch = prompter.getResult();
        
            if (ch == 'R' || ch == 'r') {
                return ISVNAuthenticationProvider.REJECTED;
            } else if (ch == 't' || ch == 'T') {
                return ISVNAuthenticationProvider.ACCEPTED_TEMPORARY;
            } else if (resultMayBeStored && (ch == 'p' || ch == 'P')) {
                return ISVNAuthenticationProvider.ACCEPTED;
            }
        return ISVNAuthenticationProvider.REJECTED;
    }

    public SVNAuthentication requestClientAuthentication(String kind, 
    		SVNURL url, String realm, SVNErrorMessage errorMessage, 
    		SVNAuthentication previousAuth, boolean authMayBeStored) {
       
    	Integer requestsCount = (Integer) myRequestsCount.get(kind + "$" + url + "$" + realm);
        
    	if (requestsCount == null) {
            myRequestsCount.put(kind + "$" + url + "$" + realm, new Integer(1));
        } else if (requestsCount.intValue() == MAX_PROMPT_COUNT) {
            // no more than three requests per realm
            return null;
        } else {
            myRequestsCount.put(kind + "$" + url + "$" + realm, new Integer(requestsCount.intValue() + 1));
        }
        
        if (ISVNAuthenticationManager.SSH.equals(kind)) {
        	return new SVNSSHAuthentication(username, password, 22, authMayBeStored);
            
        }
        return null;
    }

   

    private static String readLine() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
    *
    * @author  lparikka
    */
    public class CertificateWindow extends javax.swing.JDialog {

       
       private char result = ' ';
    	
       /** Creates new form CertificateWindow */
       public CertificateWindow(java.awt.Frame parent, boolean modal) {
           super(parent, modal);
           initComponents();
       }

       /** This method is called from within the constructor to
        * initialize the form.
        * WARNING: Do NOT modify this code. The content of this method is
        * always regenerated by the Form Editor.
        */
       @SuppressWarnings("unchecked")
       // <editor-fold defaultstate="collapsed" desc="Generated Code">
       private void initComponents() {

           jScrollPane1 = new javax.swing.JScrollPane();
           jTextArea1 = new javax.swing.JTextArea();
           jTextArea1.setEditable(false);
           acceptTempButton = new javax.swing.JButton();
           acceptPerButton = new javax.swing.JButton();
           rejectButton = new javax.swing.JButton();

           setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
           setTitle(loc.lc("Accept Digital certificate"));

           jTextArea1.setColumns(20);
           jTextArea1.setRows(5);
           jScrollPane1.setViewportView(jTextArea1);

           acceptTempButton.setText(loc.lc("Accept temporarily"));
           acceptTempButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                   acceptTempButtonActionPerformed(evt);
               }
           });

           acceptPerButton.setText(loc.lc("Accept permanently"));
           acceptPerButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                   acceptPerButtonActionPerformed(evt);
               }
           });

           rejectButton.setText(loc.lc("Reject"));
           rejectButton.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                   rejectButtonActionPerformed(evt);
               }
           });

           org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
           getContentPane().setLayout(layout);
           layout.setHorizontalGroup(
               layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                   .addContainerGap()
                   .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                       .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                       .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                           .add(rejectButton)
                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                           .add(acceptPerButton)
                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                           .add(acceptTempButton)))
                   .addContainerGap())
           );
           layout.setVerticalGroup(
               layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(layout.createSequentialGroup()
                   .addContainerGap()
                   .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 27, Short.MAX_VALUE)
                   .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                       .add(acceptTempButton)
                       .add(acceptPerButton)
                       .add(rejectButton))
                   .addContainerGap())
           );
           pack();
       }// </editor-fold>

    private void acceptTempButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	result = 't';
    	this.setVisible(false);
    }

    private void acceptPerButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	result = 'p';
    	this.setVisible(false);
    }

    private void rejectButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	result = 'R';
    	this.setVisible(false);
    }

    public char getResult() {
    	return result;
    }
    
    public void setCertificateString(String text) {
    	jTextArea1.setText(text);
    }

     
    // Variables declaration - do not modify
    private javax.swing.JButton acceptPerButton;
    private javax.swing.JButton acceptTempButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton rejectButton;
    // End of variables declaration

    }
}
//Reviewed by mkupsu 29.11.8
