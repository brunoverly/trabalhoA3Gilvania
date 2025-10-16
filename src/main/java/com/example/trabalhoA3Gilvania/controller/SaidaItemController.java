package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.Sessao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SaidaItemController {
        @FXML private Button retirarConfirmarButton;
        @FXML private Button retirarCancelButton;

        @FXML private Button entradaItemCancelar;
        @FXML private Button entradaItemConfirmar;
        @FXML private TextField retiraraCodOs;
        @FXML private TextField retirarCodOperacao;
        @FXML private TextField retirarCodItem;
        @FXML private TextField retirarDescricaoItem;
        @FXML private TextField retirarQtdItemOs;
        @FXML private TextField retirarQtdItemRecebida;
        @FXML private TextField retirarStatusItem;
        @FXML private TextField retirarLocalItem;
        @FXML private TextField retirarMatriculaMecanico;
        @FXML private ImageView retirar1;

        private int idItem;
        private String codItem;
        private String codOperacao;
        private String codOs;
        private String descricaoItem;
        private String qtdPedido;
        private String localizacao;
        private String status;
        private int qtdRecebida;


        public void setCodItem(String codItem) {
            this.codItem = codItem;
        }
        public void setCodOperacao(String codOperacao) {
            this.codOperacao = codOperacao;
        }
        public void setCodOs(String codOs) {
            this.codOs = codOs;
        }
        public void setDescricaoItem(String descricaoItem) {
            this.descricaoItem = descricaoItem;
        }
        public void setQtdPedido(int qtdPedido) {
            this.qtdPedido = String.valueOf(qtdPedido);
        }
        public void setIdItem(int idItem) {
            this.idItem = idItem;
        }
        public void setLocalizacao(String localizacao){this.localizacao = localizacao;}
        public void setStatus(String status){this.status = status;}
        public void setQtdRecebida(int qtdRecebida){this.qtdRecebida = qtdRecebida;}

        public void initialize(URL url, ResourceBundle resourceBundle) {
        URL retitar1ImageURL = getClass().getResource("/imagens/retirar1.png");
        Image retirar1Image = new Image(retitar1ImageURL.toExternalForm());
        retirar1.setImage(retirar1Image);
    }

        public void carregaDados(){
            retiraraCodOs.setText(codOs);
            retirarCodOperacao.setText(codOperacao);
            retirarCodItem.setText(codItem);
            retirarDescricaoItem.setText(descricaoItem);
            retirarQtdItemOs.setText(qtdPedido);
            retirarQtdItemRecebida.setText(String.valueOf(qtdRecebida));
            retirarStatusItem.setText(status);
            retirarLocalItem.setText(localizacao);
        }

        public void retirarCancelButtonOnAction(ActionEvent event){
            Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
            stage.close();
        }

        public void retirarConfirmarButtonOnAction(){
            if((retirarMatriculaMecanico.getText().isBlank())){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Aviso");
                alert.setHeaderText(null); // opcional, sem cabeçalho
                alert.setContentText("Informe a matricula do responsvael a quem foi entregue!");
                alert.showAndWait();
            }
            else{
                try (Connection connectDB = new DataBaseConection().getConection()) {
                    String querySqlRetirada = """
                                INSERT INTO controle_retirada_itens
                                    (id_item, entregue_para, data_retirada, cod_os, cod_operacao, entregue_por)
                                VALUES (?, ?, ?, ?, ?, ?)
                            """;
                    LocalDateTime agora = LocalDateTime.now();
                    Timestamp ts = Timestamp.valueOf(agora);

                    try (PreparedStatement statement = connectDB.prepareStatement(querySqlRetirada)) {
                        statement.setInt(1, idItem);
                        statement.setString(2, retirarMatriculaMecanico.getText());
                        statement.setTimestamp(3, ts);
                        statement.setString(4, codOs);
                        statement.setString(5, codOperacao);
                        statement.setInt(6, Sessao.getMatricula());

                        int linhasAfetadas = statement.executeUpdate();
                        if(linhasAfetadas > 0){
                            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                            alert2.setTitle("Aviso");
                            alert2.setHeaderText(null);
                            alert2.setContentText("Registro cadastrado com sucesso!");
                            alert2.showAndWait();
                        }
                        statement.close();
                        connectDB.close();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                /// //////////////////////////////////////
                try (Connection connectDB = new DataBaseConection().getConection()) {
                    String querySqlItem = """
                                    UPDATE item
                                    SET status = 'entregue na oficina'
                                    WHERE id = ?
                                """;
                    try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                        statement.setInt(1, idItem);
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

/// ///////////////////////////////////////////////
                Stage stage = (Stage) retirarCancelButton.getScene().getWindow();
                stage.close();
            }
        }

    }


