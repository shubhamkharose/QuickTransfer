package jfileshare;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.crypto.SecretKey;
import static jfileshare.SenderController.clients;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class SenderController {

    HttpServer server;
    static public CheckBox [] clients;
    
    @FXML
    private Button Browse;

    @FXML
    private TextField file;
    
    @FXML
    private TextField no_of_user;

    @FXML
    private Label ip;

    @FXML
    private ProgressBar progress;

    @FXML
    private Button Enable;

    @FXML
    private Label per;

    @FXML
    private Button send,share;

    @FXML
    private Button connect,ConDev;

    @FXML
    private Label status;

    @FXML
    private Button showIp;

    @FXML
    private CheckBox checkencry;
            
    boolean hotspot=false;
    boolean mark;
    int nor;
    List<File> f;
    ServerSocket ssock;
    Socket[] socket;
    DecimalFormat df;
    int noOfFiles;
    boolean state = false;
    String hostname;
    //private static byte[] iv; /*This array is inititalized with 8 values.....Removed for security of encryption technique*/
    String sos=System.getProperty("os.name");
    
    public void initialize() {
       
        this.hotspot = false;
        ip.setVisible(false);
        status.setText("Disconnected");
        status.setTextFill(Color.web("#ff0000"));
        checkencry.setTooltip(new Tooltip("\"Encryption\" may slow down transfer speed !"));
        Enable.setTooltip(new Tooltip("Use \"Ethernet\" for better speed !"));
        share.setTooltip(new Tooltip("Share this application to another PC !"));
        df=new DecimalFormat("#0.##");
        nor=1;
        no_of_user.setText(""+nor);
        try
        {
            hostname=(InetAddress.getLocalHost()).getHostName();
        }
        catch(Exception e){}
        try {
            ip.setText("Host : "+(InetAddress.getLocalHost()).getHostName());
        } catch (UnknownHostException ex) {}
        ip.setVisible(true);
    }
    
    private static byte[] encrypt(byte[] inBytes,SecretKey key,String xform) throws Exception
    {
        Cipher cipher=Cipher.getInstance(xform);
        IvParameterSpec ips=new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,key,ips);
        return cipher.doFinal(inBytes);
    }

    @FXML
    void actionPerformed(ActionEvent event) 
    {
        Button b = (Button)event.getSource();
        
        if(b == Enable){
            if(sos.contains("Window"))
            {
                if(hotspot==false)
                {   
                    sdisplay("Make sure that your WiFi is on.\n\nIf hotspot is not enabled try running the app in administrative mode","Sender");
                    ProcessBuilder pb = null;      
                    Process p = null;
                    pb = new ProcessBuilder("Enable.bat");                
                    pb.redirectErrorStream(true);
                    try {
                        p = pb.start();
                    } catch (IOException ex) {
                        return;
                    }

                    Enable.setText("Disable Hotspot");
                    hotspot=true;
                    try {
                        ip.setText("Key : QT123456\nHost : "+(InetAddress.getLocalHost()).getHostName());
                    } catch (UnknownHostException ex) {}
                    ip.setVisible(true);
                }
                else
                {   
                    ProcessBuilder pb = new ProcessBuilder("Disable.bat");                
                    pb.redirectErrorStream(true);
                    try {
                        Process p = pb.start();
                    } catch (IOException ex) {
                        return;
                    }
                    Enable.setText("Enable Hotspot");
                    hotspot=false;
                    ip.setVisible(false);
                }
            }
            else if(sos.contains("Linux"))
            {
                Stage child = new Stage();
                try
                {
                    Parent root = FXMLLoader.load(getClass().getResource("HotspotHelp.fxml"));
                    Scene scene = new Scene(root);
                    child.setTitle("Linux Hotspot Support");
                    child.setScene(scene);
                    child.show();
                }
                catch(Exception e){return;}
            }
        }
        else if(b == connect){
            
            try{
                nor = Integer.parseInt(no_of_user.getText());
                if(nor <=0 || nor > 7){
                    throw new Exception();
                }
            }
            catch(Exception e){
                sdisplay("Invalid number of receivers","Caution!");
                return ;
            }
            clients = new CheckBox[nor];
            
            try {
                    ssock=new ServerSocket(5000);
                } catch (IOException ex) {
                    
                        
                        try {
                            ssock.close();
                        } catch (IOException ex1) {
                            return;
                        }
                    
                    return;
                }
                ssock.setPerformancePreferences(0,1,2);
            connect.setDisable(true);
            
            
            
            Stage s = (Stage) send.getScene().getWindow();
            s.setOnCloseRequest(new EventHandler <WindowEvent>(){
                @Override
                public void handle(WindowEvent event) {
                    
                    
                        if(ssock.isBound()){
                            try {
                                ssock.close();
                            }
                            catch (IOException ex) {
                            }
                        }
                    
                    if(server!=null)
                        server.stop(0);
                    if(sos.contains("Window"))
                    {
                        ProcessBuilder pb = new ProcessBuilder("Disable.bat");                
                        pb.redirectErrorStream(true);
                        try {
                            Process p = pb.start();
                        } catch (IOException ex) {
                        }
                    }
                    FXMLDocumentController.main.show();
                  
                }
            });
            new Thread()
            {
                @Override
                public void run(){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("Waiting...");
                            status.setTextFill(Color.web("#000000"));
                        }
                    });
                    try
                    {
                        socket=new Socket[nor];
                        for(int i=0;i<nor;i++)
                        {
                            socket[i]=ssock.accept();
                            
                            clients[i] = new CheckBox(socket[i].getInetAddress().getHostName());
                            clients[i].setSelected(true);
                            
                        }
                        ssock.close();
                        ConDev.setDisable(false);    
                    }
                    catch(Exception e){}
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            connect.setDisable(true);
                            send.setDisable(false);
                            status.setText("Connected");
                            status.setTextFill(Color.web("#00ff00"));
                        }
                    });
                }
            }.start();
        }
        else if(b == Browse){
            FileChooser fc = new FileChooser();
            f = fc.showOpenMultipleDialog(null);
            if(f == null){
                sdisplay("File not choosen !","Caution!");
                return ;
            }
            noOfFiles=f.size();
            if(noOfFiles==1)
                file.setText(f.get(0).getPath());
            else
                file.setText("Multiple files selected...");
        }
        else if(b == ConDev){
            Stage stage = new Stage();
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("ConnectedDevices.fxml"));
            } catch (IOException ex) {
                return;
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        else if(b == send){
            if(f==null)
            {
                sdisplay("File not choosen!","Caution!");
                return;
            }
            mark=checkencry.isSelected();
            checkencry.setDisable(true);
            send.setDisable(true);
            checkencry.setDisable(true);
            Browse.setDisable(true);
            ConDev.setDisable(true);
            
            if(mark==false)
            {
                new Thread() 
                {    
                    @Override
                    public void run() {
                        int dev=0;
                        for(int j=0;j<nor;j++)
                            if(clients[j].isSelected()==true)
                                dev++;
            
                        FileInputStream fis=null;
                        DataOutputStream os[] = new DataOutputStream[nor];
                        DataInputStream is[]=new DataInputStream[nor];
                        
                        for(int j=0;j<nor;j++)
                            try {
                                os[j]=new DataOutputStream(socket[j].getOutputStream());
                                is[j]=new DataInputStream(socket[j].getInputStream());
                            }
                            catch (Exception e1) {
                            }
                        
                        noOfFiles=f.size();
                        
                        for(int p=0;p<nor;p++)
                        {
                            try
                            {
                                os[p].writeInt(noOfFiles);
                                os[p].writeBoolean(mark);
                            }
                            catch(Exception e1){}
                        }
                        
                        for(int i=0;i<noOfFiles;i++)
                        {
                            
                            try {
                                fis = new FileInputStream(f.get(i));
                            } catch (Exception e1) {
                            }
                            BufferedInputStream bis = new BufferedInputStream(fis);

                            for(int j=0;j<nor;j++){
                                if(clients[j].isSelected()==false)
                                    continue;
                                try {
                                    os[j].writeUTF(f.get(i).getName());
                                    os[j].writeLong(f.get(i).length());
                                } catch (Exception e1) {
                                }
                            }
                        
                            byte[] contents;
                            final long fileLength = f.get(i).length(); 
                            long current = 0;
                            long mb=1048576,nano=1000000000;
                            long start = System.nanoTime();

                            while(current!=fileLength){ 
                                int size = 1000000;
                                if(fileLength - current >= size)
                                    current += size;    
                                else{ 
                                    size = (int)(fileLength - current); 
                                    current = fileLength;
                                } 
                                contents = new byte[size]; 
                                try {
                                    bis.read(contents, 0, size);
                                   
                                } catch (Exception e1) {
                                }

                                for(int j=0;j<nor;j++)
                                {
                                    if(clients[j].isSelected()==false)
                                        continue;
                                    try {
                                        os[j].write(contents);
                                        os[j].flush();
                                    } catch (Exception e1) {
                                        //return;
                                    }
                                }
                                final long curr=current,fileL=fileLength,fstart=start;
                                final int fdev=dev;

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setProgress((float)((curr*1.0)/fileL));

                                        per.setText(((curr*100)/fileL)+" %   "+df.format((curr*fdev/(double)mb)/((System.nanoTime()-fstart)/(double)nano))+"MB/s");                           
                                    }
                                });
                                
                            }   
                            int flg;
                            try
                            {
                                for(int j=0;j<nor;j++){
                                    if(clients[j].isSelected()==false)
                                        continue;
                                    
                                    flg=is[j].readInt();
                                    
                                }
                            }
                            catch(Exception e1){}
                            
                            JFileShare.dataCount+=fileLength*dev;
                        }
                        
                        JFileShare.fileCount+=noOfFiles;
                        
                        for(int i=0;i<nor;i++)
                        {
                            try {
                                os[i].flush();
                                socket[i].close();
                                ssock.close();
                                state = true;
                            } catch (Exception e1) {
                            }
                        }	
                        Platform.runLater(new Runnable() {
                        @Override

                        public void run() {
                            if(state){
                            sdisplay("File(s) sent succesfully!","Sender");
                            Stage s = (Stage) ConDev.getScene().getWindow();
                            s.close();
                            FXMLDocumentController.main.show();
                            }
                            }
                        });
                    }
                }.start();
            }
            else
            {
                new Thread() 
                {
                    @Override
                    public void run() {
                        int dev=0;
                        for(int j=0;j<nor;j++)
                            if(clients[j].isSelected()==true)
                                dev++;
                        FileInputStream fis=null;
                        DataOutputStream os[] = new DataOutputStream[nor];
                        DataInputStream is[]=new DataInputStream[nor];
                        
                        
                        String xform="DES/CBC/PKCS5Padding";
                        
						/*

							This section of code belongs to generation of key for encryption.
							This code section is removed for security of encryption technique.


						
						*/
                        
                        for(int j=0;j<nor;j++)
                            try {
                                os[j]=new DataOutputStream(socket[j].getOutputStream());
                                is[j]=new DataInputStream(socket[j].getInputStream());
                            }
                            catch (Exception e1) {
                            }
                        
                        noOfFiles=f.size();
                        for(int p=0;p<nor;p++)
                        {
                            try
                            {
                                os[p].writeInt(noOfFiles);
                                os[p].writeBoolean(mark);
                            }
                            catch(Exception e1){}
                        }
                        
                        for(int i=0;i<noOfFiles;i++)
                        {
                            try {
                                fis = new FileInputStream(f.get(i));
                            } catch (Exception e1) {
                            }
                            BufferedInputStream bis = new BufferedInputStream(fis);

                            for(int j=0;j<nor;j++){
                                if(clients[j].isSelected()==false)
                                    continue;
                                try {
                                    os[j].writeUTF(f.get(i).getName());
                                    os[j].writeLong(f.get(i).length());
                                } catch (Exception e1) {
                                }
                            }
                        
                            byte[] contents;
                            byte[] contents2;
                            byte[] encBytes=null;
        
                            final long fileLength = f.get(i).length(); 
                            long current = 0;
                            long mb=1048576,nano=1000000000;
                            long start = System.nanoTime();

                            while(current!=fileLength){ 
                                int size = 1000000,size2=0;
                                if(fileLength-current>size)
                                        current += size;    
                                else
                                {
                                    size=(int)(fileLength-current);
                                    if(size%8!=0)
                                            size2=(size/8+1)*8;
                                    else
                                            size2=size;
                                    current+=size;

                                }
                                contents = new byte[size]; 
                                try {	
                                    bis.read(contents, 0, size);
                                } catch (IOException ex) 
                                {}

                                if(size2!=0)
                                {
                                    contents2 = new byte[size2]; 
                                    int j=0;
                                    for(;j<size;j++)
                                            contents2[j]=contents[j];
                                    for(;j<size2;j++)
                                            contents2[j]=1;
                                try {
                                    encBytes=encrypt(contents2,key,xform);
                                } catch (Exception ex) {
                                }
                                }   
                                else	
                                {
                                try {
                                    encBytes=encrypt(contents,key,xform);
                                } catch (Exception ex) {
                                    
                                }
                                }
                                for(int j=0;j<nor;j++)
                                {
                                    if(clients[j].isSelected()==false)
                                        continue;
                                    try {
                                        os[j].write(encBytes,0,encBytes.length);
                                    } catch (Exception e1) {
                                    }
                                }
                                final long curr=current,fileL=fileLength,fstart=start;
                                final int fdev=dev;
                                
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setProgress((float)((curr*1.0)/fileL));

                                        per.setText(((curr*100)/fileL)+" %   "+df.format((curr*fdev/(double)mb)/((System.nanoTime()-fstart)/(double)nano))+"MB/s");                           
                                    }
                                });
                            } 
                             
                            int flg;
                            try
                            {
                                for(int j=0;j<nor;j++){
                                    if(clients[j].isSelected()==false)
                                            continue;
                                    flg=is[j].readInt();
                                }
                            }
                            catch(Exception e1){}
                            
                            JFileShare.dataCount+=fileLength*dev;
                        }
                        
                        JFileShare.fileCount+=noOfFiles;
                        
                        for(int i=0;i<nor;i++)
                        {
                            try {
                                os[i].flush();
                                socket[i].close();
                                ssock.close();
                                state = true;
                            } catch (Exception e1) {
                            }
                        }
                         Platform.runLater(new Runnable() {
                            @Override

                            public void run() {
                                if(state){
                                sdisplay("File(s) sent succesfully!","Sender");
                                Stage s = (Stage) ConDev.getScene().getWindow();
                                s.close();
                                FXMLDocumentController.main.show();
                                }
                                }
                            });
                    }
                }.start();   
            }   
        }
        else if(((Button)event.getSource())==share)
        {
            File qtSetup=new File("QT_setup.zip");
            if(qtSetup.exists()==false)
            {
                qtSetup = new FileChooser().showOpenDialog(null);
            
                if(qtSetup == null){
                    sdisplay("Invalid File","Caution!");
                    return ;
                }
                try
                {
                    DataInputStream qtdis = new DataInputStream(new FileInputStream(qtSetup));
                    DataOutputStream qtdos = new DataOutputStream(new FileOutputStream(new File("QT_setup.zip")));
                    byte data[] = new byte[100000];

                    while(true){
                       if(qtdis.read(data)<0)
                           break;
                       qtdos.write(data);
                    }
                    qtdis.close();
                    qtdos.close();
                }
                catch(Exception e1){}
            }
            
            if(server!=null){
               sdisplay("Server already created!","Caution!");     
               return;
            }
            try {
                server = HttpServer.create(new InetSocketAddress(6789), 0);
            } catch (IOException ex) {
                sdisplay("Error in creating server!","Caution!");     
                return;
            }
            server.createContext("/",  new IndexHandler());
            server.setExecutor(null);
            server.start();
            sdisplay("Open \"sender_ip:6789\" in receiver's browser.","Sender");
            Stage s = (Stage) send.getScene().getWindow();
            s.setOnCloseRequest(new EventHandler <WindowEvent>(){
                @Override
                public void handle(WindowEvent event) {
                    for(int i=0;i<nor;i++)
                    {
                        if(ssock!=null && ssock.isBound()){
                            try {
                                ssock.close();
                            }
                            catch (IOException ex) {
                            }
                        }
                    }
                    if(server!=null)
                        server.stop(0);
                    if(sos.contains("Window"))
                    {
                        ProcessBuilder pb = new ProcessBuilder("Disable.bat");                
                        pb.redirectErrorStream(true);
                        try {
                            Process p = pb.start();
                        } catch (IOException ex) {
                        }
                    }
                    FXMLDocumentController.main.show();
                  
                }
            });
        }
     }
    public static void sdisplay(String s,String head){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(head);
        alert.setHeaderText(s);
        alert.setContentText(null);
        alert.showAndWait();     
    }
    
}


class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String field = t.getRequestURI().getPath();
        if(field.equals("/"))
            field+="index.html";
        field = field.substring(1);

        if(field.equals("exe"))
        {
            Headers h = t.getResponseHeaders();
            h.add("Content-Disposition","filename=\"QT_setup.zip\"");
            field="QT_setup.zip";
        }

        File file = new File(field);
        if(file.exists()==false){
            SenderController.sdisplay("Resource not found !!","Caution!");
            return;
        }
        byte [] bytearray  = new byte [(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(bytearray, 0, bytearray.length);

        t.sendResponseHeaders(200, file.length());
        OutputStream os = t.getResponseBody();
        os.write(bytearray,0,bytearray.length);
        os.close();
    }
}