package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ConsultEditItemScreen extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ConsultOsScreen.class.getResource("/com/example/trabalhoA3Gilvania/consultEditItem.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> scene.getRoot().requestFocus());

        stage.setResizable(false);
    }
}
