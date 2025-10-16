package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.excelHandling.GerenciadorOperacao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;

import java.util.ResourceBundle;

public class ImportarOsController {
    @FXML private Button importVoltar;
    @FXML private Button importFazerImport;
    @FXML private Button importSelecionarExcel;

    @FXML private TextField importNumeroOs;
    @FXML private TableView<String> importTableOs;
    @FXML private TableView<String> importTableOperacao;
    @FXML private TableView<String> importTableItem;


    @FXML private ImageView importar1;
    @FXML private ImageView importar3;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL importar1ImageURL = getClass().getResource("/imagens/importar1.png");
        Image importar1Image = new Image(importar1ImageURL.toExternalForm());
        importar1.setImage(importar1Image);

        URL importar3ImageURL = getClass().getResource("/imagens/importar3.png");
        Image importar3Image = new Image(importar3ImageURL.toExternalForm());
        importar3.setImage(importar3Image);

    }


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
