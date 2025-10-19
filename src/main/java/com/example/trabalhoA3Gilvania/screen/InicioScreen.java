package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class InicioScreen extends Application {

    // Variáveis para arrastar a janela
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/inicio.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // Carregar fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena transparente
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            // Adicionar ícone
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Configurar Stage sem borda
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.show();

            // Permitir arrastar a janela
            root.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
