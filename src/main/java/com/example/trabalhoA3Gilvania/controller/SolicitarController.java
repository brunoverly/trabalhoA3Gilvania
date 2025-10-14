package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.screen.SolicitarScreen;
import com.example.trabalhoA3Gilvania.screen.StartPageScreen;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;







public class SolicitarController {

    @FXML private Button solicitarCancelarButton;
    @FXML private Button solicitarConfirmarButton;




    public void solicitarCancelarButtonOnAction(ActionEvent event){
        Stage stage = (Stage) solicitarCancelarButton.getScene().getWindow();
        stage.close();
    }

    public void solicitarConfirmarButtonOnAction(ActionEvent event){




    }

}
