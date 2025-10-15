package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RetirarScreen extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RetirarScreen.class.getResource("/com/example/trabalhoA3Gilvania/retirar.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 519, 800);
        stage.setScene(scene);
        stage.show();
        Platform.runLater(() -> scene.getRoot().requestFocus());
        stage.setResizable(false);

    }
}
