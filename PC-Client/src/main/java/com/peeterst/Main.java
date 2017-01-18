package com.peeterst;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("fxml/main.fxml"));
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/com/peeterst/view/start.fxml"));
            primaryStage.setTitle("Checklist PC-Client");
            Scene mainScene = new Scene(root, 1024, 768);
            primaryStage.setScene(mainScene);
            mainScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            primaryStage.show();
        }catch(Exception e){
            log.error("Error at main {}",e);
            throw e;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
