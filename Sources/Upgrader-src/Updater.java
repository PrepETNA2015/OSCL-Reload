package updater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
/**
 *
 * @author floreag
 */
public class Updater {
    private final static String new_verURL = "http://oslc.bravesites.com/version";
    
    public static String getLatestVersion() throws Exception
    {
        String data = getData("file://"+new File(".").getAbsolutePath()+"/version.html");
        
        return data.substring(data.indexOf("[version]")+9,data.indexOf("[/version]"));
    }
    public static String getWhatsNew() throws Exception
    {
        String data = getData(new_verURL);
        return data.substring(data.indexOf("[version]")+9,data.indexOf("[/version]"));
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
