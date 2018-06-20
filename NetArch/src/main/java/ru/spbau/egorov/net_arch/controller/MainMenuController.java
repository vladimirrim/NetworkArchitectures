package ru.spbau.egorov.net_arch.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {
    public Button setupClientButton;
    public Button setupServerButton;

    public void setupClient() {
        String fxmlFile = "/fxml/ClientMenu.fxml";
        loadStage(fxmlFile);
    }

    public void setupServer() {
        String fxmlFile = "/fxml/ServerMenu.fxml";
        loadStage(fxmlFile);
    }

    private void loadStage(String fxmlFile) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("Client Setup");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
        Stage mainStage = (Stage) setupClientButton.getScene().getWindow();
        mainStage.close();
    }
}
