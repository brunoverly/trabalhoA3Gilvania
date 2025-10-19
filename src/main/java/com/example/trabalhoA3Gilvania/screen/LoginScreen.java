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

public class LoginScreen extends Application {

    @Override
    public void start(Stage stage) {

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/login.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // Carregar fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS com teste de retorno
            URL cssUrl = getClass().getResource("/css/style.css");
            // Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Remover bordas e botões do Windows
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

// Configurar cena
            stage.initStyle(StageStyle.TRANSPARENT);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.show();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
