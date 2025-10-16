package com.example.trabalhoA3Gilvania.controller;



import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.screen.InicioScreen;
import com.example.trabalhoA3Gilvania.Sessao;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;

import javafx.scene.image.Image;

import java.sql.*;
import java.util.ResourceBundle;


public class LoginController {
    @FXML
    private Button loginButton;
    @FXML
    private Label loginErrorMessage;
    @FXML
    ImageView brand;
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
        Image test = new Image(getClass().getResourceAsStream("/imagens/brand.png"));

        URL imagemPrincipalURL = getClass().getResource("/imagens/login1.png");
        Image imagemPrincipal = new Image(imagemPrincipalURL.toExternalForm());
        login1.setImage(imagemPrincipal);


        URL imagemUsuarioURL = getClass().getResource("/imagens/login2.png");
        Image imagemUsuario = new Image(imagemUsuarioURL.toExternalForm());
        login2.setImage(imagemUsuario);

        URL imagemPinURL = getClass().getResource("/imagens/login3.png");
        Image imagemPin = new Image(imagemPinURL.toExternalForm());
        login3.setImage(imagemPin);

        URL imagemBrandURL = getClass().getResource("/imagens/brand.png");
        Image imagemBrand = new Image(imagemBrandURL.toExternalForm());
        brand.setImage(imagemBrand);

    }


    //BOTOES
    //Acao do botao cancelar login
    public void LoginCancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }

    //Acao do botao fazer login
    public void LoginButtonOnAction(ActionEvent event) {
        if ((enterUserNameField.getText().isBlank() == false) && (enterPasswordField.getText().isBlank() == false)) {
            validateLogin();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Informe usuario e senha para prosseguir");
            alert.showAndWait();
        }


    }

    //METODOS
    //Validar o login conectando ao banco de dados
    public void validateLogin() {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connectDB = connectNow.getConection();
/// ////////////////////////////////////////////////////////
        try {
            String querySqlUser = """
                        SELECT nome, matricula, cargo
                        FROM users
                        WHERE matricula = ? AND pin = ?
                    """;
            try (PreparedStatement buscaUsuario = connectDB.prepareStatement(querySqlUser)) {
                buscaUsuario.setInt(1, Integer.parseInt(enterUserNameField.getText()));
                buscaUsuario.setInt(2, Integer.parseInt(enterPasswordField.getText()));
                ResultSet rs = buscaUsuario.executeQuery();
                if (rs.next()) {
                    Sessao.setUsuario(
                            rs.getInt("matricula"),
                            rs.getString("nome"),
                            rs.getString("cargo")
                    );
                    TelaInicial();
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.close();

                }
                else{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Aviso");
                    alert.setHeaderText(null); // opcional, sem cabeçalho
                    alert.setContentText("Usuario ou senha invalidos!");
                    alert.showAndWait();
                }
            }
            catch (NumberFormatException e){
                e.printStackTrace();
                e.getCause();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    public static void TelaInicial() {
        InicioScreen telaInicial = new InicioScreen();
        Stage stage = new Stage();
        telaInicial.start(stage);
    }
}

