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
            if (Integer.parseInt(Updater.getLatestVersion()) > 0) {
                new UpdateInfo(Updater.getWhatsNew());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
