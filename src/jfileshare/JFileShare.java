/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jfileshare;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author onk_r
 */
public class JFileShare extends Application {
    
    public static boolean cancelFlg;
    public static long fileCount,dataCount;
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));     
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image(JFileShare.class.getClassLoader().getResource("QT.png").toExternalForm()));
        stage.setTitle("Quick_Transfer");
        stage.setScene(scene);
        
        File configFile=null;
        File check=null;
        check= new File("QT_setup.zip");
        configFile=new File("QT.config");
        
        if(configFile.exists()==false)
        {
            configFile.createNewFile();
            DataOutputStream fdos=new DataOutputStream(new FileOutputStream(configFile));
            fdos.writeBoolean(false);
            fdos.writeLong(0);  //fileCount
            fdos.writeLong(0);  //dataCOunt
            cancelFlg=false;
            fileCount=0;
            dataCount=0;
            fdos.close();
        }
        if(!check.exists() && cancelFlg==false){
            Stage child = new Stage();
            Parent root1 = null;
            try{
                root1 = FXMLLoader.load(getClass().getResource("Init.fxml"));
            }
            catch(Exception e){}
            Scene scene1 = new Scene(root1);
            child.setScene(scene1);
            child.setTitle("One time setup");
            child.show();
            DataOutputStream fdos=new DataOutputStream(new FileOutputStream(new File("QT.config")));
            fdos.writeBoolean(true);
            fdos.writeLong(0);
            fdos.writeLong(0);
            fdos.close();
        }
        else 
            stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
