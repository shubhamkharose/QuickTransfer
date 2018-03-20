package jfileshare;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConnectedDevicesController {

    @FXML
    private VBox list;
    
    public void initialize(){
        CheckBox [] ref =SenderController.clients;
         for(int i=0;i<ref.length;i++){
            list.getChildren().add(ref[i]);
        }
    }
    @FXML
    void actionPerformed(ActionEvent e){
        ((Stage)(list.getScene().getWindow())).close();
    }
}
