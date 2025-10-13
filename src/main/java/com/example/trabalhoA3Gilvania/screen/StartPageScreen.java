package com.example.trabalhoA3Gilvania.screen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.desktop.AppForegroundListener;
import java.io.IOException;

public class StartPageScreen extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartPageScreen.class.getResource("/com/example/trabalhoA3Gilvania/startPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900 , 600);
        stage.setScene(scene);
        stage.show();
        Platform.runLater(() -> scene.getRoot().requestFocus());
        stage.setResizable(false);

    }
}
