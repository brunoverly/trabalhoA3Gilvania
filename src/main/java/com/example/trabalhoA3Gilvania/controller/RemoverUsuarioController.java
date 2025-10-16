package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class RemoverUsuarioController implements Initializable {

    @FXML
    private ImageView remover1;
    @FXML
    private ImageView remover2;
    @FXML
    private ImageView remover3;

    @FXML
    private Button removeBuscarMatricula;
    @FXML
    private Button removeCancelarButton;
    @FXML
    private Button removeConfirmarButton;

    @FXML
    private AnchorPane removeUser;

    @FXML
    private TextField removeMatricula;
    @FXML
    private TextField removeDadosNome;
    @FXML
    private TextField removeDadosCargo;
    @FXML
    private TextField removeDadosMatricula;

    @FXML
    private Label removeMotrarDadosUser;
    @FXML
    private Label removeNomeBuscado;
    @FXML
    private Label removeCodCargoBuscado;
    @FXML
    private Label removeMatriculaBuscado;
    @FXML
    private Label removeEmailBuscado;

    // Carregar imagens
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL remover1ImageURL = getClass().getResource("/imagens/remover1.png");
        Image remover1Image = new Image(remover1ImageURL.toExternalForm());
        remover1.setImage(remover1Image);

        URL remover2ImageURL = getClass().getResource("/imagens/remover2.png");
        Image remover2Image = new Image(remover2ImageURL.toExternalForm());
        remover2.setImage(remover2Image);

        URL remover3ImageUrl = getClass().getResource("/imagens/remover3.png");
        Image remover3Image = new Image(remover3ImageUrl.toExternalForm());
        remover3.setImage(remover3Image);
    }

    public void removeCancelarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) removeCancelarButton.getScene().getWindow();
        stage.close();
    }

    public void removeBuscarMatriculaOnAction(ActionEvent event) {
        try (Connection connectDB = new DataBaseConection().getConection()) {

            String verifcarCadastroBanco = "SELECT nome, cargo FROM users WHERE matricula = ?";
            try (PreparedStatement statement = connectDB.prepareStatement(verifcarCadastroBanco)) {
                int matricula = Integer.parseInt(removeMatricula.getText());
                statement.setInt(1, matricula);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        // Usuário encontrado, preenche os campos
                        removeDadosNome.setText(rs.getString("nome"));
                        removeDadosCargo.setText(rs.getString("cargo"));
                        removeDadosMatricula.setText(String.valueOf(matricula));
                    } else {
                        // Usuário não encontrado
                        removeDadosNome.setText("");
                        removeDadosCargo.setText("");
                        removeDadosMatricula.setText("");

                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Aviso");
                        alert.setHeaderText(null);
                        alert.setContentText("Não foi localizado nenhum usuário cadastrado com a matrícula informada");
                        Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                        stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
                        alert.showAndWait();
                    }
                }

            }

        } catch (NumberFormatException e) {
            removeMotrarDadosUser.setText("Matrícula inválida!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeConfirmarButtonOnAction (ActionEvent event){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação");
            alert.setHeaderText(null);
            alert.setContentText("Tem certeza que deseja remover este usuário?");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));

            Optional<ButtonType> resultado = alert.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                DataBaseConection connectNow = new DataBaseConection();
                try (Connection connectDB = connectNow.getConection()) {

                    int matricula = Integer.parseInt(removeMatricula.getText());
                    String query = "DELETE FROM users WHERE matricula = ?";

                    try (PreparedStatement ps = connectDB.prepareStatement(query)) {
                        ps.setInt(1, matricula);
                        int linhasAfetadas = ps.executeUpdate();

                        if (linhasAfetadas > 0) {
                            Alert usuarioRemovido = new Alert(Alert.AlertType.INFORMATION);
                            usuarioRemovido.setTitle("Alerta");
                            usuarioRemovido.setHeaderText(null);
                            usuarioRemovido.setContentText("Usuário removido!");
                            stageAlert = (Stage) usuarioRemovido.getDialogPane().getScene().getWindow();
                            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
                            usuarioRemovido.show();

                            removeDadosNome.setText("");
                            removeDadosMatricula.setText("");
                            removeDadosCargo.setText("");

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
