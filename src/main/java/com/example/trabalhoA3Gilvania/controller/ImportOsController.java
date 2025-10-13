package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.excelHandling.GerenciadorOperacao;
import com.example.trabalhoA3Gilvania.screen.ImportScreen;
import com.example.trabalhoA3Gilvania.screen.RegisterScreen;
import com.example.trabalhoA3Gilvania.screen.StartPageScreen;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class ImportOsController {
    @FXML private Button importVoltar;
    @FXML private Button importFazerImport;
    @FXML private Button importSelecionarExcel;

    @FXML private TextField importNumeroOs;

    GerenciadorOperacao cadastrarOs = new GerenciadorOperacao();
    File filePath;

    public void importSelecionarExcelOnAction(ActionEvent event){
       filePath = cadastrarOs.selecionarArquivo();
    }

    public void importVoltarOnAction(ActionEvent event){
        Stage stage = (Stage) importVoltar.getScene().getWindow();
        stage.close();
    }

    public void importFazerImportOnAction(ActionEvent event) throws IOException {
        if((importNumeroOs.getText().isBlank())){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Informe o numero da OS");
            alert.showAndWait();
        }
        else if(filePath == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Selecione o arquivo");
            alert.showAndWait();
        }
        else {
            String numeroOsDigitado = importNumeroOs.getText();
            cadastrarOs.criar(numeroOsDigitado, filePath);
            importNumeroOs.setText("");
        }
    }


}
