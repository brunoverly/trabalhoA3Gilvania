package com.example.trabalhoA3Gilvania;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.animation.RotateTransition;
import javafx.util.Duration;

import java.util.Optional;

public class FormsUtil {

    // Armazena o Stage principal da aplicação
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Cria um alerta centralizado sobre a janela principal (primaryStage)
     */
    public static Alert criarAlerta(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        try {
            // Define o ícone do alerta
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(FormsUtil.class.getResource("/imagens/logo.png").toExternalForm()));

            // Faz o alerta abrir centralizado sobre a janela principal
            if (primaryStage != null) {
                alert.initOwner(primaryStage);
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar o ícone do alerta: " + e.getMessage());
        }

        return alert;
    }

    /**
     * Cria um alerta de confirmação centralizado
     */
    public static boolean criarAlertaConfirmacao(String title, String message) {
        Alert alert = criarAlerta(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /**
     * Cria um GIF de loading dentro de um StackPane
     */
    public static StackPane createGifLoading() {
        // Carrega o GIF
        Image image = new Image(FormsUtil.class.getResourceAsStream("/imagens/loading.gif"));
        ImageView gifView = new ImageView(image);
        gifView.setFitWidth(70);
        gifView.setPreserveRatio(true);

        // Cria o StackPane de overlay
        StackPane overlay = new StackPane(gifView);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.03); -fx-background-radius: 17.5px;");
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); // ocupa toda a área do container

        return overlay;
    }

    /**
     * Cria uma animação de loading circular (opcional)
     */
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
}
