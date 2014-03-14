package updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
/**
 *
 * @author floreag
 */
public class Updater {
    private final static String versionURL = "http://oslc.bravesites.com/version";
    private final static String historyURL = "http://oslc.bravesites.com/version";
    public static String getLatestVersion() throws Exception
    {
        String data = getData(versionURL);
        return data.substring(data.indexOf("[version]")+9,data.indexOf("[/version]"));
    }
    public static String getWhatsNew() throws Exception
    {
        String data = getData(historyURL);
        return data.substring(data.indexOf("[history]")+9,data.indexOf("[/history]"));
    }
    private static String getData(String address)throws Exception
    {
        URL url = new URL(address);
        
        InputStream html = null;

        html = url.openStream();
        
        int c = 0;
        StringBuffer buffer = new StringBuffer("");

        while(c != -1) {
            c = html.read();
            
        buffer.append((char)c);
        }
        html.close();
        return buffer.toString();
    }
}
