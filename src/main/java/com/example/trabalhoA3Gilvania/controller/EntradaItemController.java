package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.screen.ConsultEditItemScreen;
import com.example.trabalhoA3Gilvania.screen.EditItemScreen;
import com.example.trabalhoA3Gilvania.screen.SolicitarScreen;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;
import java.util.Optional;

public class EntradaItemController {

    @FXML private Button entradaItemCancelar;
    @FXML private Button entradaItemConfirmar;


    public void entradaItemCancelarOnAction(ActionEvent event){
        Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
        stage.close();
    }

    public void entradaItemConfirmarOnAction(ActionEvent event){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(null);
        alert.setContentText("Confirmar a entrada do item?");

        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Alert usuarioRemovido = new Alert(Alert.AlertType.INFORMATION);
            usuarioRemovido.setTitle("Alerta");
            usuarioRemovido.setHeaderText(null);
            usuarioRemovido.setContentText("Item atualizado!");
            usuarioRemovido.show();

            // mostrar o que foi atualizado do item
        }
    }











}
