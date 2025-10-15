package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.screen.RegisterScreen;
import com.example.trabalhoA3Gilvania.screen.StartPageScreen;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import java.io.File;
import javafx.scene.image.Image;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;


public class LoginController {
    @FXML
    private Button loginCancelButton;
    @FXML
    private Label  loginErrorMessage;
    @FXML
    private ImageView login1;
    @FXML
    private ImageView login2;
    @FXML
    private ImageView login3;
    @FXML
    private TextField enterUserNameField;
    @FXML
    private TextField enterPasswordField;




    //Conexao com banco de dados
    public void initialize(URL url, ResourceBundle resourceBundle) {
       URL imagemPrincipalURL = getClass().getResource("/imagens/login1.png");
       Image imagemPrincipal= new Image(imagemPrincipalURL.toExternalForm());
        login1.setImage(imagemPrincipal);


       URL imagemUsuarioURL = getClass().getResource("/imagens/login2.png");
       Image imagemUsuario = new Image(imagemUsuarioURL.toExternalForm());
        login2.setImage(imagemUsuario);

        URL imagemPinURL = getClass().getResource("/imagens/login3.png");
        Image imagemPin = new Image(imagemPinURL.toExternalForm());
        login3.setImage(imagemPin);

    }


    //BOTOES
    //Acao do botao cancelar login
    public void LoginCancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) loginCancelButton.getScene().getWindow();
        stage.close();
    }

    //Acao do botao fazer login
    public void LoginButtonOnAction(ActionEvent event){
        if((enterUserNameField.getText().isBlank() == false) && (enterPasswordField.getText().isBlank() == false )){
                validateLogin();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Informe usuario e senha para prosseguir");
            alert.showAndWait();
        }


    }









    //METODOS
    //Validar o login conectando ao banco de dados
    public void validateLogin(){
        DataBaseConection connectNow = new DataBaseConection();
        Connection connectDB = connectNow.getConection();

        String verifyLogin = "SELECT COUNT(1) FROM USERS WHERE NOME = '" + enterUserNameField.getText() + "' AND SENHA = '"+ enterPasswordField.getText() + "';";

        try{
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while(queryResult.next()){
                if(queryResult.getInt(1) == 1){
                    Stage stage = (Stage) loginCancelButton.getScene().getWindow();
                    stage.close();
                    TelaInicial();
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Aviso");
                    alert.setHeaderText(null); // opcional, sem cabeçalho
                    alert.setContentText("Usuario ou senha invalidos!");
                    alert.showAndWait();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }



    //ALTERNAR ENTRE AS TELAS
    //Tela de cadstro de usuarios
    public static void TelaInicial() throws Exception {
        StartPageScreen telaInicial = new StartPageScreen();
        Stage stage = new Stage();
        telaInicial.start(stage);
    }
}
