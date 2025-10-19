package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;
import com.example.trabalhoA3Gilvania.controller.SaidaItemController;
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
        URL entradaItemVoltarButtonImageURL = getClass().getResource("/imagens/voltar.png");
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
            fecharImagem.setScaleX(1.1);
            fecharImagem.setScaleY(1.1);
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

    public void entradaItemConfirmarOnAction(){
        if((entradaLocalArmazenado.getText().isBlank()) || (entradaQtdRecebida.getText().isBlank())){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Informe a quantidade recebida e local armazenado");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
            alert.showAndWait();
        }
        else if(!verificarValorDigitado()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null); // opcional, sem cabeçalho
            alert.setContentText("Valor informado é inválida ou maior que a quantidade informada na ordem de serviço");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
            alert.showAndWait();
        }
        else{
                try (Connection connectDB = new DataBaseConection().getConection()) {
                    String querySqlItem = """
                             UPDATE item
                                SET status = ?, localizacao = ?, qtd_recebida = ?, ultima_atualizacao = ?
                                WHERE id = ?
                        """;

                    LocalDateTime agora = LocalDateTime.now();
                    Timestamp ts = Timestamp.valueOf(agora);

                    try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                        statement.setString(1, "Recebido");
                        statement.setString(2, entradaLocalArmazenado.getText());
                        statement.setInt(3,Integer.parseInt(entradaQtdRecebida.getText()));
                        statement.setTimestamp(4, ts);
                        statement.setInt(5, idItem);

                        int linhasAfetadas = statement.executeUpdate();
                        if(linhasAfetadas > 0){
                            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                            alert2.setTitle("Aviso");
                            alert2.setHeaderText(null);
                            alert2.setContentText("Item atualizado com sucesso");
                            Stage stageAlert = (Stage) alert2.getDialogPane().getScene().getWindow();
                            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
                            alert2.showAndWait();

                            System.out.println(" Id do item" + idItem);
                            System.out.println(descricaoItem);
                            System.out.println(" Id da operacao" + idOperacao);

                        }

                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                DataBaseConection registarAtualizacao = new DataBaseConection();
                registarAtualizacao.AtualizarStatusPorSolicitacao(idOperacao);
                registarAtualizacao.AtualizarBanco(
                        "Item",
                         codOs,
                        "Item recebido na base",
                        Sessao.getMatricula()
                );

                Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
                stage.close();
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

