package jfileshare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InitController {

    @FXML
    private Button cancel;
    
    @FXML
    private Label initLabel;
    
    File f =null;
    @FXML
    private Button choose;

    @FXML
    private TextField Floc;

    @FXML
    private Button accept;
    
    public void initialize()
    {
        initLabel.setText("Select \"QT_setup.zip\" file for sharing purpose.\n(It is the setup file you used to install this app.)");
    }
    @FXML
    public void actionPerformed(ActionEvent e) throws IOException{
        Button b = (Button)e.getSource();
        if(b == cancel){
            ((Stage)(accept.getScene().getWindow())).close();
        }
        else if(b== choose){
            f = new FileChooser().showOpenDialog(null);
            
            if(f == null){
                sdisplay("Invalid File","Caution!");
                return ;
            }
            Floc.setText(f.getName());
            return ;
        }
        else{
            if(f == null){
                sdisplay("Invalid File","Caution!");
                return ;
                
            }
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File("QT_setup.zip")));
            byte data[] = new byte[100000];
           
            while(true){
               if(dis.read(data)<0)
                   break;
               dos.write(data);
            }
            dis.close();
            dos.close();
            ((Stage)(accept.getScene().getWindow())).close();
        } 
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));     
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.getIcons().add(new Image(JFileShare.class.getClassLoader().getResource("QT.png").toExternalForm()));
        stage.setTitle("Quick_Transfer");
        stage.setScene(scene);
        stage.show();
    }
    void sdisplay(String s,String head){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(head);
        alert.setHeaderText(s);
        alert.setContentText(null);
        alert.showAndWait();     
    }
}
