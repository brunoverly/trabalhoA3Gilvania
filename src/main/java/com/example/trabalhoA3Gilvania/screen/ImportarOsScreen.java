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
import javafx.scene.text.Font;
import javafx.stage.Stage;
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
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ImportarOsScreen extends Application {

    public void start(Stage stage) {

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/importarOs.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};

            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            // Criar cena
            Scene scene = new Scene(root);

            // Carregar CSS
            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());


            // 🔹 Adicionar o ícone (logo)
            URL logoUrl = getClass().getResource("/imagens/logo.png");
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));


            // Configurar stage
            stage.setTitle("Importar Ordem de Servico");
            stage.setScene(scene);
            stage.show();

            TextField tf = (TextField) root.lookup("#importOsPathField"); // seu TextField pelo id
            tf.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void Preview(String numeroOs, File fileSelected) throws IOException {
        DataFormatter formatter = new DataFormatter();
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        List<Operacao> operacoes = new ArrayList<>();


        //Acessando o arquivo
        @Cleanup FileInputStream file = new FileInputStream(fileSelected);
        Workbook workbook = new XSSFWorkbook(file);

        //Seleciona a primeira aba
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


            }
        }
    }
}