package com.example.trabalhoA3Gilvania;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class FormsUtil {

    //Metodo para limpar os campos de texto dentro das telas
    public static void limparCampos(Pane root) {
        for (Node node : root.getChildren()) {
            if (node instanceof TextField) {
                ((TextField) node).clear();
            } else if (node instanceof PasswordField) {
                ((PasswordField) node).clear();
            } else if (node instanceof ComboBox<?>) {
                ((ComboBox<?>) node).getSelectionModel().clearSelection();
            } else if (node instanceof Pane) {
                limparCampos((Pane) node);
            }
        }
    }
}
