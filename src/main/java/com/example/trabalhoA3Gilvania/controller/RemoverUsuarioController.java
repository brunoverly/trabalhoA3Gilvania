package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.*;
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
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class RemoverUsuarioController implements Initializable {

    @FXML private ImageView removerUserVoltarButtonImage;
    @FXML private ImageView remover2;
    @FXML private Button removeCancelarButton;
    @FXML private TextField removeMatricula;
    @FXML private TextField removeDadosNome;
    @FXML private TextField removeDadosCargo;
    @FXML private TextField removeDadosMatricula;

    FormsUtil alerta = new FormsUtil();

    // Carregar imagens
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL removerUserVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image removerUserVoltarButtonImageImagem = new Image(removerUserVoltarButtonImageURL.toExternalForm());
        removerUserVoltarButtonImage.setImage(removerUserVoltarButtonImageImagem);

        URL remover2ImageURL = getClass().getResource("/imagens/user5.png");
        Image remover2Image = new Image(remover2ImageURL.toExternalForm());
        remover2.setImage(remover2Image);

        ImageView fecharImagem = (ImageView) removeCancelarButton.getGraphic();

        // Hover (mouse entrou)
        removeCancelarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            removeCancelarButton.setCursor(Cursor.HAND); // cursor muda para mão
        });

        // Hover (mouse saiu)
        removeCancelarButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            removeCancelarButton.setCursor(Cursor.DEFAULT);
        });
    }

    public void removeCancelarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) removeCancelarButton.getScene().getWindow();
        stage.close();
    }

    public void removeBuscarMatriculaOnAction(ActionEvent event) {
        if (!matriculaValida()) {
            System.out.println("a");
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso" , "Infome uma matrícula válida para prosseguir")
                    .showAndWait();
        }
        else{
            System.out.println("b");
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

                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Matrícula informada não localizada")
                                    .showAndWait();
                        }
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                e.getCause();
            } catch (SQLException e) {
                e.printStackTrace();
                e.getCause();
            }
        }
    }

    private boolean matriculaValida() {
            if (removeMatricula.getText().isBlank()) {
                return false;
            } else {
                try {
                    int matricula = Integer.parseInt(removeMatricula.getText());
                    return true;
                } catch (Exception e) {
                    return false;
                }
        }
    }

    public void removeConfirmarButtonOnAction (ActionEvent event){
            boolean confirmar = alerta.criarAlertaConfirmacao("Confirmar", "Tem certeza que deseja remover este usuário?");
            if (confirmar) {
                DataBaseConection connectNow = new DataBaseConection();
                try (Connection connectDB = connectNow.getConection()) {

                    int matricula = Integer.parseInt(removeMatricula.getText());
                    String query = "DELETE FROM users WHERE matricula = ?";

                    try (PreparedStatement ps = connectDB.prepareStatement(query)) {
                        ps.setInt(1, matricula);
                        int linhasAfetadas = ps.executeUpdate();

                        if (linhasAfetadas > 0) {
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Usuário removido")
                                            .showAndWait();

                            removeMatricula.setText("");
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
