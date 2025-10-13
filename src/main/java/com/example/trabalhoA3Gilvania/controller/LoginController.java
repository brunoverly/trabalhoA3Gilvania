package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
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
    private ImageView brandingImageView;
    @FXML
    private ImageView lockImageView;
    @FXML
    private TextField enterUserNameField;
    @FXML
    private TextField enterPasswordField;




    //Conexao com banco de dados
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL brandingImageUrl = getClass().getResource("/imagens/Imagem1.jpg");
        Image brandingImage = new Image(brandingImageUrl.toExternalForm());
        brandingImageView.setImage(brandingImage);


        URL lockImageUrl = getClass().getResource("/imagens/Imagem2.jpg");
        Image lockImage = new Image(lockImageUrl.toExternalForm());
        lockImageView.setImage(lockImage);
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
