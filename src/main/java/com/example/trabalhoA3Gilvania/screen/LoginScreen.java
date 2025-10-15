package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;

public class LoginScreen extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/login.fxml");
            if (fxmlUrl == null) {
                System.out.println("FXML NÃO encontrado!");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // Carregar fontes
            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                URL fontUrl = getClass().getClassLoader().getResource("fonts/" + fontFile);
                if (fontUrl == null) {
                    System.out.println("Fonte NÃO encontrada: " + fontFile);
                } else {
                    Font.loadFont(fontUrl.toExternalForm(), 14);
                    System.out.println("Fonte carregada: " + fontFile);
                }
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getClassLoader().getResource("css/style.css");
            if (cssUrl == null) {
                System.out.println("CSS NÃO encontrado!");
            } else {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS carregado com sucesso: " + cssUrl.toExternalForm());
            }

            // Configurar stage
            stage.setTitle("Login Screen");
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
