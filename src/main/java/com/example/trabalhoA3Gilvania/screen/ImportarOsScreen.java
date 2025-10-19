package com.example.trabalhoA3Gilvania.screen;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.excelHandling.Operacao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ImportarOsScreen extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/importarOs.fxml");
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
            stage.setTitle("Importar ordem de serviço");
            stage.setResizable(false);
            stage.show();

            // Focar no TextField
            TextField tf = (TextField) root.lookup("#importOsPathField");
            if (tf != null) tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void Preview(String numeroOs, File fileSelected) throws IOException {
        DataFormatter formatter = new DataFormatter();
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        List<Operacao> operacoes = new ArrayList<>();

        // Acessando o arquivo
        @Cleanup FileInputStream file = new FileInputStream(fileSelected);
        Workbook workbook = new XSSFWorkbook(file);

        // Seleciona a primeira aba
        Sheet sheet = workbook.getSheetAt(0);

        boolean osExistOnExcel = false;
        boolean osExistente = false;
        boolean osCadastrada = false;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String osString = formatter.formatCellValue(row.getCell(1));
            String operacaoString = formatter.formatCellValue(row.getCell(2));
            int idOperacao;

            if (osString.equals(numeroOs)) {
                // Lógica da operação aqui
            }
        }
    }
}
