package ru.spbau.egorov.net_arch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String fxmlFile = "/fxml/MainMenu.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        primaryStage.setTitle("Network Architectures");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(350);
        primaryStage.setMinWidth(600);
        primaryStage.show();
    }
}
