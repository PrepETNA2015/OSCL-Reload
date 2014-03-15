/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package updater;
/**
 *
 * @author floreag
 */
public class Main_updater {
    public static void main(String[] args) {
        
       try {
            if (Float.parseFloat(Updater.getLatestVersion()) > 0) {
                if (Float.parseFloat(Updater.getLatestVersion()) != Float.parseFloat(Updater.getWhatsNew()) )
                new UpdateInfo(Updater.getWhatsNew(), Updater.getLatestVersion());
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
