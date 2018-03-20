package jfileshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXMLDocumentController {

    @FXML
    private Button receive,i;

    @FXML
    private Button send;
    
    @FXML
    private ImageView imageview;
    
    static Stage main;
    
    public void initialize()
    {
        try
        {
            DataInputStream fdis=new DataInputStream(new FileInputStream(new File("QT.config")));
            JFileShare.cancelFlg=fdis.readBoolean();
            JFileShare.fileCount=fdis.readLong();
            JFileShare.dataCount=fdis.readLong();
        }
        catch(Exception e){}
    }
    @FXML
    void actionPerformed(ActionEvent event) throws IOException {
        
            main = (Stage) send.getScene().getWindow();
        
            main.setOnCloseRequest(new EventHandler <WindowEvent>(){
                @Override
                public void handle(WindowEvent event) 
                {
                    try
                    {
                        DataOutputStream fdos=new DataOutputStream(new FileOutputStream(new File("QT.config")));
                        fdos.writeBoolean(true);
                        fdos.writeLong(JFileShare.fileCount);
                        fdos.writeLong(JFileShare.dataCount);
                        fdos.close();
                    }
                    catch(Exception e){}
                }
            });
            
        if(((Button)event.getSource())==send){
            Stage child = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("Sender.fxml"));
            Scene scene = new Scene(root);
            try{
                child.getIcons().add(new Image(JFileShare.class.getClassLoader().getResource("Sender.png").toExternalForm()));
            }
            catch(Exception e){
            }
            child.setTitle("Sender");
            child.setScene(scene);
            child.show();
            if(child!=null){
                main.hide();
            }
            child.setOnCloseRequest(new EventHandler <WindowEvent > (){
                @Override
                public void handle(WindowEvent event) {
                    main.setScene(send.getScene());
                    main.show();
                    String sos=System.getProperty("os.name");
                    if(sos.contains("Window"))
                    {
                        ProcessBuilder pb = new ProcessBuilder("Disable.bat");                
                        pb.redirectErrorStream(true);
                        try {
                            Process p = pb.start();
                        } catch (IOException ex) {
                        }
                    }
                }
            });
            
        }
        else if(((Button)event.getSource())==receive){
            Stage child = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("Receiver.fxml"));
            Scene scene = new Scene(root);
            child.setScene(scene);
            try{
            child.getIcons().add(new Image(JFileShare.class.getClassLoader().getResource("Receiver.png").toExternalForm()));
            }
            catch(Exception e){}
            child.setTitle("Receiver");
            child.show();
            
            if(child!=null){
                main.hide();
            }
            child.setOnCloseRequest(new EventHandler <WindowEvent > (){
                @Override
                public void handle(WindowEvent event) {
                    main.setScene(send.getScene());
                    main.show();
                }
            });
        }
        else {
            double dc=JFileShare.dataCount;
            String str="Bytes";
            DecimalFormat df=new DecimalFormat("#0.##");
            if(dc>1024*1024*1024)
            {
                str="GB";
                dc=dc/(1024*1024*1024);
            }
            else if(dc>1024*1024)
            {
                str="MB";
                dc=dc/(1024*1024);
            }
            else if(dc>1024)
            {
                str="KB";
                dc=dc/1024;
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About Us...");
            alert.setHeaderText("QuickTransfer v2.0\n"+df.format(dc)+" "+str+" data transfered so far...");
            alert.setContentText("Developers :\n\nOnkar Sathe\nShubham Kharose\nRohit Rathi\n\nContact us :\ndarkeyedev@gmail.com");
            alert.showAndWait();
        }
    }
}
