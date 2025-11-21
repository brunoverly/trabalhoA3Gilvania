package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class LoginScreen extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {
        // Desativa escalonamento em monitores HiDPI
        System.setProperty("prism.allowhidpi", "false");

        try {
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Arrastar janela
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            // Fontes
            Font.loadFont(getClass().getResource("/fonts/Poppins-Regular.ttf").toExternalForm(), 14);
            Font.loadFont(getClass().getResource("/fonts/Poppins-Bold.ttf").toExternalForm(), 14);

            // Criar cena exatamente no tamanho do FXML
            Scene scene = new Scene(root);

            // Remover bordas
            stage.initStyle(StageStyle.UNDECORATED);

            // Aplica a cena
            stage.setScene(scene);

            // Ajusta o Stage para o tamanho EXATO do FXML
            stage.sizeToScene();

            // Bloqueia o tamanho capturado (pixel perfeito)
            stage.setMinWidth(stage.getWidth());
            stage.setMaxWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
            stage.setMaxHeight(stage.getHeight());

            // Ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            if (logoUrl != null) {
                stage.getIcons().add(new Image(logoUrl.toExternalForm()));
            }

            // Exibir
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
