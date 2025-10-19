package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;

public class SaidaItemScreen extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/saidaItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // Carregar fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Aplicar estilo de borda arredondada com fundo branco
            root.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-background-color: white;");

            // Criar cena transparente
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            // Configurar Stage sem borda do Windows
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);

            // Adicionar ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Permitir mover a janela clicando e arrastando
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // Configurar stage
            stage.setTitle("Lançar retirada de item");
            stage.setResizable(false);
            stage.show();

            // Focar no TextField
            TextField tf = (TextField) root.lookup("#retirarMatriculaMecanico");
            if (tf != null) tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
