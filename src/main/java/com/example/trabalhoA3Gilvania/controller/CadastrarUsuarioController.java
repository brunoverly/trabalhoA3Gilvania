package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt;
import java.net.URL;
import java.sql.*;
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

    FormsUtil alerta = new FormsUtil();

    // Inicialização
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL cadastrarVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image cadastrarVoltarButtonImageImagem = new Image(cadastrarVoltarButtonImageURL.toExternalForm());
        cadastrarVoltarButtonImage.setImage(cadastrarVoltarButtonImageImagem);

        cadastroComboBox.getItems().addAll("Administrador", "Aprovisionador", "Mecânico");

        ImageView fecharImagem = (ImageView) cadastrarCancelButton.getGraphic();

        cadastrarCancelButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            cadastrarCancelButton.setCursor(Cursor.HAND);
        });

        cadastrarCancelButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            cadastrarCancelButton.setCursor(Cursor.DEFAULT);
        });
    }

    // Fecha a janela
    public void cadastrarCancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cadastrarCancelButton.getScene().getWindow();
        stage.close();
    }

    // Botão de cadastro
    public void cadastrarButtonOnAction(ActionEvent event) {
        // Valida campos
        if (cadastroNome.getText().isBlank()
                || cadastroMatricula.getText().isBlank()
                || cadastroComboBox.getValue() == null
                || cadastroSenha.getText().isBlank()
                || cadastroConfirmarSenha.getText().isBlank()) {

            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Preencha todos os campos para prosseguir")
                    .showAndWait();
            return;
        }

        // Valida PIN
        if (!verificarPIN()) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","PIN deve ser númerico e possuir 6 dígitos")
                    .showAndWait();
            return;
        }

        // Confirma senha
        if (!cadastroSenha.getText().equals(cadastroConfirmarSenha.getText())) {
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","PIN informados não correspondem")
                    .showAndWait();
            return;
        }

        // Chama método de cadastro
        registerUser();
    }

    // Cadastro via procedure
    public void registerUser() {
        String nome = cadastroNome.getText();
        int matricula = Integer.parseInt(cadastroMatricula.getText());
        String cargo = cadastroComboBox.getValue();
        int pin = Integer.parseInt(cadastroSenha.getText());
        String hash = BCrypt.hashpw(String.valueOf(pin), BCrypt.gensalt(12));

        String procedureCall = "{ CALL projeto_java_a3.cadastrar_usuario(?, ?, ?, ?) }";

        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall(procedureCall)) {

            cs.setInt(1, matricula);
            cs.setString(2, nome);
            cs.setString(3, cargo);
            cs.setString(4, hash);

            boolean hasResult = cs.execute();

            if (hasResult) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        int resultado = rs.getInt("resultado");

                        if (resultado == 1) {
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso","Usuário cadastrado com sucesso")
                                    .showAndWait();
                        } else {
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","Usuário já cadastrado")
                                    .showAndWait();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro","Falha ao cadastrar usuário")
                    .showAndWait();
        }

        // Limpa todos os campos ao invés de fechar a janela
        limparCampos();
    }

    // Novo método para limpar os campos
    private void limparCampos() {
        cadastroNome.clear();
        cadastroMatricula.clear();
        cadastroSenha.clear();
        cadastroConfirmarSenha.clear();
        cadastroComboBox.getSelectionModel().clearSelection();
    }

    // Verifica se o PIN é válido
    public boolean verificarPIN() {
        boolean tipoValido = true;
        boolean tamanhoValido = true;

        try {
            Integer.parseInt(cadastroSenha.getText());
        } catch (Exception e) {
            tipoValido = false;
        }

        if (cadastroSenha.getText().length() != 6) {
            tamanhoValido = false;
        }

        return tipoValido && tamanhoValido;
    }
}
