package ru.spbau.egorov.net_arch.controller;

import javafx.scene.control.Alert;

abstract class Controller {
    protected void showInfoDialog(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Oops");
        alert.setHeaderText("Looks like there was an error!");
        alert.setContentText(content);
        alert.showAndWait();
    }
}
