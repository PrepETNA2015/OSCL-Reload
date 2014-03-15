/*
 * 
 * Copyright (C) 2014 FLOREA Gheorghe,LE MOIGNE Adrien,WIESER Frank
 * 
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package updater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author floreag
 */
public class Main_Gui extends JFrame{

    private Thread worker;
    private final String path; 
    private JTextArea outText;
    private JButton cancle;
    private JButton launch;
    private JScrollPane sp;
    private JPanel pan1;
    private JPanel pan2;
    
    

     public Main_Gui() throws IOException {
        initComponents();
        outText.setText("Contacting Download Server...");
        path = new File("..").getCanonicalPath();
        download();
    }
    private void initComponents() {
        System.out.println("init components");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

       

        pan1 = new JPanel();
        pan1.setLayout(new BorderLayout());

        pan2 = new JPanel();
        pan2.setLayout(new FlowLayout());

        outText = new JTextArea();
        sp = new JScrollPane();
        sp.setViewportView(outText);
        
        launch = new JButton("Launch App");
        launch.setEnabled(false);
        launch.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                launch();
            }
        });
        pan2.add(launch);

        cancle = new JButton("Cancel Update");
        cancle.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        pan2.add(cancle);
        pan1.add(sp,BorderLayout.CENTER);
        pan1.add(pan2,BorderLayout.SOUTH);

        add(pan1);
        pack();
        this.setSize(500, 400);
    }

    private void download()
    {
        worker = new Thread(
        new Runnable(){
            public void run()
            {
                try {                    
                    downloadFile(getDownloadLinkFromHost());
                    unzip();
                    copyFiles(new File("/tmp/OSCL-Reload-master"),path);
                    launch.setEnabled(true);
                    outText.setText(outText.getText()+"\nUpdate Finished!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "An error occured while preforming update!");
                }
            }
        });
        worker.start();
    }
    private void launch()
    {
        String[] run = {"java","-jar","OSLC.jar"};
        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
        
    }
    
    private void copyFiles(File f,String dir) throws IOException
    {
        File[]files = f.listFiles();
        outText.setText(outText.getText()+"\nCopy files");
        for(File ff:files)
        {
            if(ff.isDirectory()){
                new File(dir+"/"+ff.getName()).mkdir();
                copyFiles(ff,dir+"/"+ff.getName());
            }
            else
            {
                copy(ff.getAbsolutePath(),dir+"/"+ff.getName());
            }

        }
    }
    public void copy(String srFile, String dtFile) throws FileNotFoundException, IOException{

          File f1 = new File(srFile);
          File f2 = new File(dtFile);

          InputStream in = new FileInputStream(f1);

          OutputStream out = new FileOutputStream(f2);

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          in.close();
          out.close();
      }
    private void unzip() throws IOException
    {
        String pathOfZip = "/tmp/update_OSCL.zip";
        String pathToExtract = "/tmp/";
        int BUFFER_SIZE = 1024;
        int size;
        byte[] buffer = new byte[BUFFER_SIZE];

        outText.setText(outText.getText()+"\nUnzip");
        try {
            File f = new File(pathToExtract);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(pathOfZip), BUFFER_SIZE));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = pathToExtract  +"/"+ ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    }
                    else {
                        FileOutputStream out = new FileOutputStream(path, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                        try {
                            while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) {
                                fout.write(buffer, 0, size);
                            }

                            zin.closeEntry();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            fout.flush();
                            fout.close();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                zin.close();
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        outText.setText(outText.getText()+"\nUnzip finished");
    }
    private void downloadFile(String link) throws MalformedURLException, IOException
    {
        
        String saveTo = "/tmp/";
    try {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        FileOutputStream out = new FileOutputStream(saveTo + "update_OSCL.zip");
        byte[] b = new byte[1024];
        int count;
        while ((count = in.read(b)) >= 0) {
            out.write(b, 0, count);
        }
        out.flush(); out.close(); in.close();                   

    } catch (IOException e) {
        e.printStackTrace();
    }
        outText.setText(outText.getText()+"\nDownload Complete!");             
    }
    private String getDownloadLinkFromHost() throws MalformedURLException, IOException
    {
        String path = "http://oslc.bravesites.com/version";
        URL url = new URL(path);

        InputStream html = null;
        html = url.openStream();
	            
        int c = 0;
        StringBuilder buffer = new StringBuilder("");

        while(c != -1) {
            c = html.read();
       
        buffer.append((char)c);
        }  
        
        return buffer.substring(buffer.indexOf("[url]")+5,buffer.indexOf("[/url]"));
    }
    
    public static void start() throws IOException {
       new Main_Gui().setVisible(true);     
    }

}
