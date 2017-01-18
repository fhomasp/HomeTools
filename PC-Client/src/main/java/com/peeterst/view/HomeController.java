package com.peeterst.view;

import com.peeterst.android.server.Server;
import com.peeterst.android.server.ServerConfigService;
import com.peeterst.listener.DockletListener;
import com.peeterst.service.ChecklistService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class HomeController extends BaseDockView {
    //Static listener instance of DockletListener
    private static DockletListener listener;

    @FXML
    public TextField serverHost;

    @FXML
    public TextField serverPort;

    @FXML
    public TextField checklistPath;

    ServerConfigService serverConfigService;
    ChecklistService checklistService;

    public HomeController(){
        super.title = "Home";
        super.id = "home";
    }


    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {


    }

    public void addListener(DockletListener listener){
        HomeController.listener = listener;
    }


    public void populateServerData() {
        try {
            serverConfigService = ServerConfigService.getInstance();
            checklistService = ChecklistService.getInstance();

            Properties envProps = serverConfigService.getEnvProperties();

            String port = envProps.getProperty("port");
            String hostname = Server.getHostAddress();
            String path = checklistService.getAppDirPath();

            serverHost.setText(hostname);
            serverPort.setText(port);
            checklistPath.setText(path);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}