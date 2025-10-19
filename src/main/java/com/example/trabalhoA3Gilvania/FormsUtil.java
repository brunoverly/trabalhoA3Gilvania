package com.example.trabalhoA3Gilvania;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Optional;

public class FormsUtil {

    public static Alert criarAlerta(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(FormsUtil.class.getResource("/imagens/logo.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("Erro ao carregar o icone");
        }

        return alert;
    }

    public static boolean criarAlertaConfirmacao(String title, String message) {
        Alert alert = criarAlerta(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

}
