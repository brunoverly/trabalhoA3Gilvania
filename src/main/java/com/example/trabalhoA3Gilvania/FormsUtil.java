package com.example.trabalhoA3Gilvania;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.animation.RotateTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

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

    public static StackPane createLoadingAnimation() {
        Circle circle = new Circle(40);
        circle.setStroke(Color.DODGERBLUE);
        circle.setStrokeWidth(8);
        circle.setFill(Color.TRANSPARENT);

        RotateTransition rotate = new RotateTransition();
        rotate.setNode(circle);
        rotate.setDuration(Duration.seconds(1));
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();

        StackPane loadingPane = new StackPane(circle);
        loadingPane.setStyle("-fx-background-color: white;");

        return loadingPane;
    }
    public static StackPane createGifLoading() {
        // Carrega o GIF
        Image image = new Image(FormsUtil.class.getResourceAsStream("/imagens/loading.gif"));
        ImageView gifView = new ImageView(image);
        gifView.setFitWidth(70);
        gifView.setPreserveRatio(true);

        // Cria o StackPane de overlay
        StackPane overlay = new StackPane(gifView);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.03); " +
                "-fx-background-radius: 17.5px;");
        // 5% opacidade
        // fundo semi-transparente
        overlay.setAlignment(Pos.CENTER); // centraliza o GIF
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); // ocupa toda a área do container
        return overlay;
    }


}
