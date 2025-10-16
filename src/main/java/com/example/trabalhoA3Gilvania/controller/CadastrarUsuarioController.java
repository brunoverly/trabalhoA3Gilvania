package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class CadastrarUsuarioController implements Initializable {

    @FXML private ImageView registrar1;
    @FXML private AnchorPane register;
    @FXML private Button cadastrarCancelButton;
    @FXML private Button cadastrarButton;
    @FXML private Label registrationSuccessfull;
    @FXML private Label passwordError;
    @FXML private TextField cadastroNome;
    @FXML private TextField cadastroMatricula;
    @FXML private TextField cadastroEmail;
    @FXML private TextField cadastroSenha;
    @FXML private TextField cadastroConfirmarSenha;
    @FXML private ComboBox<String> cadastroComboBox;


    //Carregar imagens
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL registrarImagem1URL = getClass().getResource("/imagens/cadastro1.png");
        Image registarImagem1 = new Image(registrarImagem1URL.toExternalForm());
        registrar1.setImage(registarImagem1);
        cadastroComboBox.getItems().addAll("Administrador", "Aprovisionador", "Mecanico");

    }

    public void cadastrarCancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cadastrarCancelButton.getScene().getWindow();
        stage.close();
    }

    public void cadastrarButtonOnAction(ActionEvent event) {
        if (cadastroNome.getText().isBlank()
                || cadastroMatricula.getText().isBlank()
                || cadastroEmail.getText().isBlank()
                || cadastroComboBox.getValue() == null
                || cadastroSenha.getText().isBlank()
                || cadastroConfirmarSenha.getText().isBlank()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Preencha todos os campos para prosseguir");
            alert.showAndWait();
        }else if(!cadastroMatricula.getText().isBlank()){
            try (Connection connectDB = new DataBaseConection().getConection()) {
                String verifcarCadastroBanco = "SELECT COUNT(*) FROM users WHERE matricula = ?";
                try (PreparedStatement statement1 = connectDB.prepareStatement(verifcarCadastroBanco)) {
                    statement1.setInt(1, Integer.parseInt(cadastroMatricula.getText()));
                    ResultSet resultadoBuscaOs = statement1.executeQuery();
                    if (resultadoBuscaOs.next()) {
                        int count = resultadoBuscaOs.getInt(1);
                        if (count > 0) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Aviso");
                            alert.setHeaderText(null); // opcional, sem cabeçalho
                            alert.setContentText("Ja existe um usuario cadastrado com essa matricula");
                            alert.showAndWait();
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else if(!verificarPIN()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("PIN deve ser numero e possuir 6 digitos!");
            alert.showAndWait();
        }
        else if(!cadastroSenha.getText().equals(cadastroConfirmarSenha.getText())){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("PIN informados nao correspondem!");
            alert.showAndWait();
        }
        else {
            registerUser();
        }
    }

    public void registerUser() {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        String nome = cadastroNome.getText();
        int matricula = Integer.parseInt(cadastroMatricula.getText());
        String email = cadastroEmail.getText();
        String cargo = cadastroComboBox.getValue();
        int pin = Integer.parseInt(cadastroSenha.getText());

        String insertFields = "INSERT INTO users (nome, cargo, matricula, email, pin) VALUES ('";
        String insertValues = nome + "', '" + cargo + "', '" + matricula + "', '" + email + "', '" + pin + "')";
        String insertToRegister = insertFields + insertValues;

        try {
            Statement statement = connetDB.createStatement();
            statement.executeUpdate(insertToRegister);
            FormsUtil.limparCampos(register);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Usuário cadastrado com sucesso!");
            // Exibe o pop-up e espera o usuário clicar
            Optional<ButtonType> resultado = alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    public boolean verificarPIN() {
        boolean tipoValido = true;
        boolean tamanhoValido = true;

        try {
            int pin = Integer.parseInt(cadastroSenha.getText());
        } catch (Exception e) {
            tipoValido = false;
        }
        if (cadastroSenha.getText().length() != 6) {
            tamanhoValido = false;
        }
        return tipoValido && tamanhoValido;
    }
}


