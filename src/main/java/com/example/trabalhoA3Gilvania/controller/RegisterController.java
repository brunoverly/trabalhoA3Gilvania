package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;



public class RegisterController implements Initializable {

    @FXML private ImageView userImageView;

    @FXML private AnchorPane register;

    @FXML private Button cadastrarCancelButton;
    @FXML private Button cadastrarButton;
    @FXML private Label registrationSuccessfull;
    @FXML private Label passwordError;

    @FXML private TextField cadastroNome;
    @FXML private TextField cadastroMatricula;
    @FXML private TextField cadastroEmail;
    @FXML private TextField cadastroCodCargo;
    @FXML private TextField cadastroSenha;
    @FXML private TextField cadastroConfirmarSenha;


    //Carregar imagens
    public void initialize(URL url, ResourceBundle resourceBundle) {
            URL userImageUrl = getClass().getResource("/imagens/Imagem3.png");
            Image userImage = new Image(userImageUrl.toExternalForm());
            userImageView.setImage(userImage);
    }



    //Metodos
    //Acao botao cancelar registro de usuario

    public void cadastrarCancelButtonOnAction(ActionEvent event){
        Stage stage = (Stage) cadastrarCancelButton.getScene().getWindow();
        stage.close();
    }

    public void cadastrarButtonOnAction(ActionEvent event){
        if(cadastroSenha.getText().equals(cadastroConfirmarSenha.getText())){
            registerUser();
        }else{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aviso");
                alert.setHeaderText(null); // opcional, sem cabeçalho
                alert.setContentText("Senhas informadas nao correspondem!");
                alert.showAndWait();

        }
    }

    public void registerUser(){
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        String nome = cadastroNome.getText();
        int matricula = Integer.parseInt(cadastroMatricula.getText());
        String email = cadastroEmail.getText();
        int codCargo = Integer.parseInt(cadastroCodCargo.getText());
        String senha = cadastroSenha.getText();

        String insertFields = "insert into users (nome, cod_cargo, matricula, email, senha) values ('";
        String insertValues = nome + "'," +  codCargo + ","+ matricula + ", '" + email + "', '" + senha + "')";
        String insertToRegister =  insertFields + insertValues;

        try{
            Statement statement = connetDB.createStatement();
            statement.executeUpdate(insertToRegister);
            FormsUtil.limparCampos(register);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Usuário cadastrado com sucesso!");
            // Exibe o pop-up e espera o usuário clicar
            Optional<ButtonType> resultado = alert.showAndWait();

        }
        catch (Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }













}

