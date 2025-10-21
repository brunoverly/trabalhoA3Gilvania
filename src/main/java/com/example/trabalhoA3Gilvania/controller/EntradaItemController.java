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
import java.util.ResourceBundle;

public class EntradaItemController implements Initializable {

    @FXML private Button entradaItemCancelar;
    @FXML private Button entradaItemConfirmar;
    @FXML private TextField entradaOrdemServico;
    @FXML private TextField entradadCodItem;
    @FXML private TextField entrdadaQtdPedido;
    @FXML private TextField entradaLocalArmazenado;
    @FXML private TextField entradaCodOperacao;
    @FXML private TextField entradaItemDescricao;
    @FXML private TextField entradaQtdRecebida;
    @FXML private ImageView entradaItemVoltarButtonImage;

    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private String qtdPedido;
    private int idOperacao;
    private OnFecharJanela listener;

    FormsUtil alerta = new FormsUtil();

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
    public void setIdOperacao(int idOperacao){
        this.idOperacao = idOperacao;
    }


    public void carregaDados(){
    entradaOrdemServico.setText(codOs);
    entradaCodOperacao.setText(codOperacao);
    entradadCodItem.setText(codItem);
    entradaItemDescricao.setText(descricaoItem);
    entrdadaQtdPedido.setText(qtdPedido);

    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL entradaItemVoltarButtonImageURL = getClass().getResource("/imagens/close.png");
        Image entradaItemVoltarButtonImageImage = new Image(entradaItemVoltarButtonImageURL.toExternalForm());
        entradaItemVoltarButtonImage.setImage(entradaItemVoltarButtonImageImage);

        Platform.runLater(() -> {
            Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();

            // Quando a janela for fechada (X ou voltar)
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela(); // 🔔 chama o método da interface
                }
            });
        });

        ImageView fecharImagem = (ImageView) entradaItemCancelar.getGraphic();

        // Hover (mouse entrou)
        entradaItemCancelar.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            entradaItemCancelar.setCursor(Cursor.HAND); // cursor muda para mão
        });

        // Hover (mouse saiu)
        entradaItemCancelar.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            entradaItemCancelar.setCursor(Cursor.DEFAULT);
        });

    }


    public void entradaItemCancelarOnAction(ActionEvent event){
        Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
        stage.close();
    }

    public void entradaItemConfirmarOnAction() {
        if ((entradaLocalArmazenado.getText().isBlank()) || (entradaQtdRecebida.getText().isBlank())) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe a quantidade recebida e local armazenado")
                    .showAndWait();

        } else if (!verificarValorDigitado()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Valor informado é inválida ou maior que a quantidade informada na ordem de serviço")
                    .showAndWait();
        } else {
            int qtdRecebida = Integer.parseInt(entradaQtdRecebida.getText());
            String localizacao = entradaLocalArmazenado.getText();

            String procedureCall = "{ CALL projeto_java_a3.atualizar_item_entrada(?, ?, ?, ?, ?, ?, ?, ?) }";

            try (Connection connectDB = new DataBaseConection().getConection();
                 CallableStatement cs = connectDB.prepareCall(procedureCall)) {

                cs.setInt(1, idItem);                // p_id
                cs.setString(2, "Recebido");         // p_status
                cs.setString(3, localizacao);        // p_localizacao
                cs.setInt(4, qtdRecebida);           // p_qtd_recebida
                cs.setString(5, "Item");             // p_tipo
                cs.setString(6, codOs);              // p_cod_os
                cs.setString(7, "Item recebido na base"); // p_descricao
                cs.setInt(8, Sessao.getMatricula()); // p_matricula

                cs.execute();

                alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Item atualizado com sucesso")
                        .showAndWait();

                Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                e.printStackTrace();
                alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar item")
                        .showAndWait();
            }
        }
    }

        public boolean verificarValorDigitado(){
            try{
                Integer.parseInt(entradaQtdRecebida.getText().trim());
            }
            catch(Exception e){
                return false;
            }
            if(Integer.parseInt(entrdadaQtdPedido.getText()) < Integer.parseInt(entradaQtdRecebida.getText().trim())){
                return false;
            }
        return true;
        }

    }

