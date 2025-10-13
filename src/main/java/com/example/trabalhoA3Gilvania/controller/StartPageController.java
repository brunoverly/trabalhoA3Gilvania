package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.screen.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;

public class StartPageController {
    @FXML private MenuItem menuAddUser;
    @FXML private MenuItem menuRemoveUser;
    @FXML private MenuItem menuSair;
    @FXML private MenuItem menuImportarOs;

    public void menuRemoveUserOnActino(ActionEvent event){
        try{
            RemoverUsuario();
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }







    public void menuAddUserOnAction(ActionEvent event){
        try{
            CadastroUsuario();
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }



    public void menuSairOnAction (ActionEvent event){
        try{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação");
            alert.setHeaderText(null);
            alert.setContentText("Tem certeza que deseja fazer log off?");

            Optional<ButtonType> resultado = alert.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK){
                Stage stageAtual = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
                stageAtual.close();
                Login();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    public void menuImportarOsOnAction(ActionEvent event) throws Exception {
        ImportarOs();
    }

    public static void CadastroUsuario() throws Exception {
        RegisterScreen cadastroUsuario = new RegisterScreen();
        Stage stage = new Stage();
        cadastroUsuario.start(stage);
    }

    public static void RemoverUsuario() throws Exception {
        RemoveUserScreen removerUsuario = new RemoveUserScreen();
        Stage stage = new Stage();
        removerUsuario.start(stage);
    }

    public static void Login() throws Exception {
        LoginScreen login = new LoginScreen();
        Stage stage = new Stage();
        login.start(stage);
    }
    public static void ImportarOs() throws Exception {
        ImportScreen importarOs = new ImportScreen();
        Stage stage = new Stage();
        importarOs.start(stage);
    }
}
