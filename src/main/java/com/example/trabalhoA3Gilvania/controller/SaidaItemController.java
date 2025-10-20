package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class SaidaItemController implements Initializable {
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
        @FXML private ImageView retiradaVoltarButtonImage;

        private int idItem;
        private String codItem;
        private String codOperacao;
        private String codOs;
        private String descricaoItem;
        private String qtdPedido;
        private String localizacao;
        private String status;
        private int qtdRecebida;
        private int idOperacao;


        FormsUtil alerta = new FormsUtil();
        private OnFecharJanela listener;

        public void setOnFecharJanela(OnFecharJanela listener) {
            this.listener = listener;
        }

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
        public void setIdOperacao(int idOperacao){this.idOperacao = idOperacao;}

        public void initialize(URL url, ResourceBundle resourceBundle) {
        URL retiradaVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image retiradaVoltarButtonImageImagem = new Image(retiradaVoltarButtonImageURL.toExternalForm());
            retiradaVoltarButtonImage.setImage(retiradaVoltarButtonImageImagem);

        Platform.runLater(() -> {
            Stage stage = (Stage) retiraraCodOs.getScene().getWindow();

            // Quando a janela for fechada (X ou voltar)
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela(); // 🔔 chama o método da interface
                }
            });
        });

            ImageView fecharImagem = (ImageView) retirarCancelButton.getGraphic();

            // Hover (mouse entrou)
            retirarCancelButton.setOnMouseEntered(e -> {
                fecharImagem.setScaleX(1.2);
                fecharImagem.setScaleY(1.2);
                retirarCancelButton.setCursor(Cursor.HAND); // cursor muda para mão
            });

            // Hover (mouse saiu)
            retirarCancelButton.setOnMouseExited(e -> {
                fecharImagem.setScaleX(1.0);
                fecharImagem.setScaleY(1.0);
                retirarCancelButton.setCursor(Cursor.DEFAULT);
            });





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
                alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso","Informe a matrícula a quem foi entregue")
                        .showAndWait();
            }

            try{
                int converNumero = Integer.parseInt(retirarMatriculaMecanico.getText().trim());
            }
            catch (Exception e){
                alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "Informe a matrícula uma matrícula válida")
                        .showAndWait();
                return;
            }
            try (Connection conn = new DataBaseConection().getConection()) {
                String sql = "CALL projeto_java_a3.atualizar_saidaitem_e_log(?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idItem);
                    stmt.setString(2, codOperacao);
                    stmt.setString(3, "Item");
                    stmt.setString(4, codOs);
                    stmt.setInt(5, Integer.parseInt(retirarMatriculaMecanico.getText()));
                    stmt.setInt(6, Sessao.getMatricula());
                    stmt.setString(7, "Item entregue na oficina");
                    stmt.setInt(8, Sessao.getMatricula());

                    stmt.execute();

                    alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Registro atualizado com sucesso").showAndWait();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Stage stage = (Stage) retirarCancelButton.getScene().getWindow();

            // 🔔 chama o callback antes de fechar
            if (listener != null) {
                listener.aoFecharJanela();
            }

            // fecha a janela
            stage.close();
        }
}





