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
import java.sql.*;
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
            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Informe uma matrícula válida para prosseguir")
                    .showAndWait();
            return;
        }

        int matricula = Integer.parseInt(removeMatricula.getText());
        String procedureCall = "{ CALL projeto_java_a3.remover_usuario_dados(?) }";

        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall(procedureCall)) {

            cs.setInt(1, matricula);

            boolean hasResults = cs.execute();

            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        int resultado = rs.getInt("resultado");

                        if (resultado == 1) {
                            removeDadosNome.setText(rs.getString("nome"));
                            removeDadosCargo.setText(rs.getString("cargo"));
                            removeDadosMatricula.setText(String.valueOf(matricula));
                        } else {
                            // Usuário não encontrado, limpa campos
                            removeDadosNome.setText("");
                            removeDadosCargo.setText("");
                            removeDadosMatricula.setText("");

                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Matrícula informada não localizada")
                                    .showAndWait();
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
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
            int matricula = Integer.parseInt(removeMatricula.getText());

            String procedureCall = "{ CALL projeto_java_a3.remover_usuario_deletar(?) }";

            try (Connection connectDB = new DataBaseConection().getConection();
                 CallableStatement cs = connectDB.prepareCall(procedureCall)) {

                cs.setInt(1, matricula);

                boolean hasResult = cs.execute();

                if (hasResult) {
                    try (ResultSet rs = cs.getResultSet()) {
                        if (rs.next()) {
                            int resultado = rs.getInt("resultado");

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Aviso");
                            alert.setHeaderText(null);

                            if (resultado == 1) {
                                alert.setContentText("Usuário removido com sucesso");
                                // Limpa os campos
                                removeMatricula.setText("");
                                removeDadosNome.setText("");
                                removeDadosCargo.setText("");
                                removeDadosMatricula.setText("");
                            } else {
                                alert.setContentText("Usuário não encontrado");
                            }
                            alert.showAndWait();
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao remover usuário")
                    .showAndWait();
            }
        }
        }
    }
