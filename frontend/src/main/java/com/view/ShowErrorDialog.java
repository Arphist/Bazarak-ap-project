package com.view;

import javafx.scene.control.Alert;

public class ShowErrorDialog {
    public static void showErrorDialog(String title, String header, String message, String alertType) {
        Alert alert;
        switch (alertType) {
            case "ERROR": {
                alert = new Alert(Alert.AlertType.ERROR);
                break;
            }
            case "INFORMATION": {
                alert = new Alert(Alert.AlertType.INFORMATION);
                break;
            }
            case "CONFIRMATION": {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                break;
            }
            case "NONE": {
                alert = new Alert(Alert.AlertType.NONE);
                break;
            }
            case "WARNING": {
                alert = new Alert(Alert.AlertType.WARNING);
                break;
            }
            default:
                alert = new Alert(Alert.AlertType.NONE);
        }
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
