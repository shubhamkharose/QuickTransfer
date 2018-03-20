package jfileshare;

import java.awt.Desktop;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class ReceiverController {


    @FXML
    private TextField folder;

    @FXML
    private TextField ip;

    @FXML
    private ProgressBar progress;

    @FXML
    private Label per;

    @FXML
    private Button connect;

    @FXML
    private Button browse;

    @FXML
    private Label status;
   
    int port_no;
    String ip_add;
    File f;
    Socket s;
    String filename[],filepath;
    boolean con =false ;
    boolean mark;
    long []filesize;
    DecimalFormat df;
    SecretKey key;
    boolean state = false;
    String rFiles="";
    String sos=System.getProperty("os.name");
    //private static byte[] iv; /*This array is inititalized with 8 values.....Removed for security of encryption technique*/
    
    
    public void initialize()
    {
        status.setText("Disconnected");
        status.setTextFill(Color.web("#ff0000"));
        df=new DecimalFormat("#0.##");
        ip.setTooltip(new Tooltip("Enter IP address of \"Sender\"!"));
        String home = System.getProperty("user.home");
        if(sos.contains("Window"))
            folder.setText(home+"\\Downloads");
        else if(sos.contains("Linux"))
            folder.setText(home+"/Downloads");
        connect.setTooltip(new Tooltip("For group sharing make sure that Sender is Waiting\nand Connect one by one"));
    }
    
    private static byte[] decrypt(byte[] inBytes,SecretKey key,String xform) throws Exception
    {
        Cipher cipher=Cipher.getInstance(xform);
        IvParameterSpec ips=new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE,key,ips);
        return cipher.doFinal(inBytes);
    }
	
    
    @FXML
    void actionPerformed(ActionEvent event){
        Button b = (Button)event.getSource();
        if(b == connect){
            state = false;
            
            filepath = folder.getText();
            f = new File(filepath);
            if(!f.exists()){
                try{
                   if(f.mkdir()==false){
                       throw new Exception();
                   }
                }
                catch(Exception e){
                    sdisplay("Illegal folder ","Caution!");
                    return ;
                }
            }
            ip_add = ip.getText();
            InetAddress []IPs=null;
            try
            {
                IPs=InetAddress.getAllByName(ip_add);
                /*String str="";
                for(int i=0;i<IPs.length;i++)
                {
                    str+="\n";
                    str+=""+IPs[i];
                }
                sdisplay(str);
                return;*/
            }
            catch(Exception e1){return;}
            /*try
            {
                InetAddress IP=InetAddress.getByName(ip_add);
                if(IP==null)
                    throw new Exception();
            }
            catch(Exception e1)
            {
                sdisplay("Invalid IP !","Caution!");
                return ;
            }*/
            
            Socket socket=null;
            int i;
            
                int j=0;
                
                    for(j=0;j<IPs.length;j++)
                    {
                        try
                        {
                            socket=new Socket(IPs[j], 5000);
                        }
                        catch(Exception e)
                        {
                            continue;
                        }
                        break;
                    }
                
                if(IPs.length==j)
                {
                    sdisplay("Unable to reach server \n \t try Again !!","Caution!");
                    return ;
                }
                    
            
            browse.setDisable(true);
            connect.setDisable(true);
            
            final Socket fsocket=socket;
            new Thread() {       
                public void run()
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("Connected");
                            status.setTextFill(Color.web("#00ff00"));
                        }
                    });

                    DataInputStream is = null;
                    DataOutputStream os=null;
                    try {
                        is = new DataInputStream(fsocket.getInputStream());
                        os=new DataOutputStream(fsocket.getOutputStream());
                    } catch (IOException ex) {

                    }
                    int noOfFiles;
                    try
                    {
                        noOfFiles=is.readInt();
                        mark=is.readBoolean();
                        filename=new String[noOfFiles];
                        filesize=new long[noOfFiles];
                        
                    }
                    catch(Exception e1)
                    {
                        sdisplay("Error receiving data","Caution!");
                        return;
                    }
                    
                    if(mark==false)
                    {
                        for(int i=0;i<noOfFiles;i++)
                        {
                            try {
                                filename[i] = is.readUTF();
                                rFiles+=filename[i]+"\n";
                                filesize[i] = is.readLong();
                                
                            } catch (IOException ex) {
                                
                            }

                            byte[] contents = new byte[1000000];

                            FileOutputStream fos = null;

                            try {
                                    if(sos.contains("Window"))
                                        fos = new FileOutputStream(new File(filepath+"\\"+filename[i]));
                                    else if(sos.contains("Linux"))
                                        fos = new FileOutputStream(new File(filepath+"/"+filename[i]));
                                
                            } catch (FileNotFoundException ex) {
                                
                            }
                            BufferedOutputStream bos = new BufferedOutputStream(fos);

                            int bytesRead = 0; 
                            int comp = 0;
                            long mb=1048576,nano=1000000000;
                            long start = System.nanoTime();
                            try {
                                while((bytesRead=is.read(contents))!=-1){
                                    bos.write(contents, 0, bytesRead);
                                    bos.flush();
                                    comp+=bytesRead;

                                    final long fcomp=comp,fstart=start,fsize=filesize[i];
                                    
                                    if(i==noOfFiles-1)
                                        state=true;
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.setProgress((float)((fcomp*1.0)/fsize));

                                            
                                            per.setText(((fcomp*100)/fsize)+" %   "+df.format((fcomp/(double)mb)/((System.nanoTime()-fstart)/(double)nano))+"MB/s");
                                            
                                        }
                                    });
                                    
                                    if(comp>=filesize[i])
                                        break;
                                }
                                bos.flush();
                                fos.close();
                                bos.close();
                                
                            } catch (IOException ex) {   
                            }
                            
                            try
                            {
                                
                                os.writeInt(1);
                                
                            }
                            catch(Exception e1){}
                            
                            JFileShare.dataCount+=filesize[i];
                        }
                        
                        JFileShare.fileCount+=noOfFiles;
                    }
                    else
                    {
                        
                        ObjectInputStream ois2;
                        String xform="DES/CBC/PKCS5Padding";
                        /*

							This section of code belongs to generation of key for decryption.
							This code section is removed for security of encryption technique.


						
						*/
                        int i;
                        for(i=0;i<noOfFiles;i++)
                        {
                            try {
                                filename[i] = is.readUTF();
                                rFiles+=filename[i]+"\n";
                                filesize[i] = is.readLong();
                            } catch (IOException ex) {
                            }
        
                            FileOutputStream fos = null;

                            try {
                                if(sos.contains("Window"))
                                    fos = new FileOutputStream(new File(filepath+"\\"+filename[i]));
                                else if(sos.contains("Linux"))
                                    fos = new FileOutputStream(new File(filepath+"/"+filename[i]));
                            } catch (FileNotFoundException ex) {
                     
                            }
                            BufferedOutputStream bos = new BufferedOutputStream(fos);

                            long current = 0;
                            long mb=1048576,nano=1000000000;
                            long start = System.nanoTime();
                            
                            byte[] contents=null;
                            byte[] decBytes=null;   
                            while(current<filesize[i])
                            { 
                  
                                int size = 1000000,size2=0;
                                if(filesize[i]-current>size)
                                {
                              
                                    current += size;   
                                    contents = new byte[size+8];

                                    try {
                                        
                                        is.readFully(contents, 0, size+8);
                                        
                                        decBytes = decrypt(contents,key,xform);
                                    } catch (Exception ex) {//System.out.println("In readFully error");
                                        
                                    }
                                    //System.out.println(""+size+" "+contents.length+" "+decBytes.length);
                                    try {
                                        bos.write(decBytes, 0, decBytes.length);
                                    } catch (IOException ex) {
                                    }
                                }
                                else
                                {
                                    size=(int)(filesize[i]-current);
                                    if(size%8!=0)
                                            size2=(size/8+1)*8;
                                    else
                                            size2=size;
                                    current+=size;
                                    contents = new byte[size2+8];
                                    try {
                                        is.readFully(contents, 0, size2+8);
                                        decBytes=decrypt(contents,key,xform);
                                    } catch (Exception ex) { }
                                    //System.out.println(""+size2+" "+contents.length+" "+decBytes.length);
                                    try {
                                        bos.write(decBytes, 0, decBytes.length);
                                    } catch (IOException ex) {
                                        
                                    }
                                }				

                                final long fcomp=current,fstart=start,fsize=filesize[i];
   
                                if(i==noOfFiles-1)
                                    state=true;
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setProgress((float)((fcomp*1.0)/fsize));

                                        ////System.out.println("Per  "+(float)((fcomp*1.0)/fsize));
                                        per.setText(((fcomp*100)/fsize)+" %   "+df.format((fcomp/(double)mb)/((System.nanoTime()-fstart)/(double)nano))+"MB/s");
                                        //System.out.printf("Receiving file ... %d %% complete! Speed : %.2f Mbps\n",(fcomp*100)/fsize,((fcomp/(double)mb)/((System.nanoTime()-fstart)/(double)nano)));
                                    }
                                });
                            }
                            
                            try
                            {
                                bos.flush();
                                fos.close();
                                bos.close();
                            }
                            catch(Exception e){}
                            try
                            {
                                os.writeInt(1);
                                ////System.out.println("1 written");
                            }
                            catch(Exception e1){}
                            
                            JFileShare.dataCount+=filesize[i];
                        }
                        
                        JFileShare.fileCount+=noOfFiles;
                    }
                    try { 
                        fsocket.close();
                        
                    } catch (IOException ex) {
                    }
                    //System.out.println("File(s) saved successfully!");
                    Platform.runLater(new Runnable() {
                    @Override

                    public void run() {
                        if(state){
                        sdisplay(rFiles+"\nfile(s) received succesfully!","Receiver");
                        
                        if(sos.contains("Window"))
                        {
                            //Open explorer
                            ProcessBuilder pb = null;      
                            Process p = null;
                            String cmd="";
                        
                            cmd="explorer "+filepath;
                        
                            try
                            {
                               Runtime rt = Runtime.getRuntime();
                               rt.exec(cmd);
                            }
                            catch(IOException e)
                            {
                            }
			}
                        Stage s = (Stage) browse.getScene().getWindow();
                        s.close();
                        FXMLDocumentController.main.show();
                        }
                        }
                    });
                }
            }.start();
        }
        else if(b == browse){
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Choose directory");
            try
            {
                filepath = (dc.showDialog(browse.getScene().getWindow())).getPath();
                if(filepath==null){
                    throw new Exception();
                }
            }
            catch(Exception e1){return;}
            folder.setText(filepath);
        }
    }
    void sdisplay(String s,String head){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(head);
        alert.setHeaderText(s);
        alert.setContentText(null);
        alert.showAndWait();     
    }
}
/*
TO 
do
connecting loader
*/