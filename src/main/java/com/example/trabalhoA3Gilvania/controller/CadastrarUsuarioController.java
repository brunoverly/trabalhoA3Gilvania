package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
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

    @FXML private ImageView cadastrarVoltarButtonImage;
    @FXML private AnchorPane register;
    @FXML private Button cadastrarCancelButton;
    @FXML private Button cadastrarButton;
    @FXML private Label registrationSuccessfull;
    @FXML private Label passwordError;
    @FXML private TextField cadastroNome;
    @FXML private TextField cadastroMatricula;
    @FXML private TextField cadastroSenha;
    @FXML private TextField cadastroConfirmarSenha;
    @FXML private ComboBox<String> cadastroComboBox;


    //Carregar imagens
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL cadastrarVoltarButtonImageURL = getClass().getResource("/imagens/voltar.png");
        Image cadastrarVoltarButtonImageImagem = new Image(cadastrarVoltarButtonImageURL.toExternalForm());
        cadastrarVoltarButtonImage.setImage(cadastrarVoltarButtonImageImagem);


        cadastroComboBox.getItems().addAll("Administrador", "Aprovisionador", "Mecânico");

        ImageView fecharImagem = (ImageView) cadastrarCancelButton.getGraphic();

        // Hover (mouse entrou)
        cadastrarCancelButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.1);
            fecharImagem.setScaleY(1.1);
            cadastrarCancelButton.setCursor(Cursor.HAND); // cursor muda para mão
        });

        // Hover (mouse saiu)
        cadastrarCancelButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            cadastrarCancelButton.setCursor(Cursor.DEFAULT);
        });

    }

    public void cadastrarCancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cadastrarCancelButton.getScene().getWindow();
        stage.close();
    }

    public void cadastrarButtonOnAction(ActionEvent event) {
        if (cadastroNome.getText().isBlank()
                || cadastroMatricula.getText().isBlank()
                || cadastroComboBox.getValue() == null
                || cadastroSenha.getText().isBlank()
                || cadastroConfirmarSenha.getText().isBlank()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Preencha todos os campos para prosseguir");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
            alert.showAndWait();
            return;
        }

        // Verificar se matrícula já existe
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
                        alert.setHeaderText(null);
                        alert.setContentText("Já existe um usuário cadastrado com essa matrícula");
                        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                        stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
                        alert.showAndWait();
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (!verificarPIN()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("PIN deve ser númerico e possuir 6 dígitos");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
            alert.showAndWait();
            return;
        }

        if (!cadastroSenha.getText().equals(cadastroConfirmarSenha.getText())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("PIN informados não correspondem");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
            alert.showAndWait();
            return;
        }

        // Tudo certo — prossegue com o cadastro
        registerUser();
    }

    public void registerUser() {
        DataBaseConection connectNow = new DataBaseConection();
        Connection connetDB = connectNow.getConection();

        String nome = cadastroNome.getText();
        int matricula = Integer.parseInt(cadastroMatricula.getText());
        String cargo = cadastroComboBox.getValue();
        int pin = Integer.parseInt(cadastroSenha.getText());

        String insertFields = "INSERT INTO users (nome, cargo, matricula, pin) VALUES ('";
        String insertValues = nome + "', '" + cargo + "', '" + matricula + "', '" + pin + "')";
        String insertToRegister = insertFields + insertValues;

        try {
            Statement statement = connetDB.createStatement();
            statement.executeUpdate(insertToRegister);
            FormsUtil.limparCampos(register);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Usuário cadastrado com sucesso");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
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
