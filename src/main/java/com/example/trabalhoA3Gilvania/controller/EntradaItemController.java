package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
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

public class EntradaItemController {

    @FXML private Button entradaItemCancelar;
    @FXML private Button entradaItemConfirmar;
    @FXML private TextField entradaOrdemServico;
    @FXML private TextField entradadCodItem;
    @FXML private TextField entrdadaQtdPedido;
    @FXML private TextField entradaLocalArmazenado;
    @FXML private TextField entradaCodOperacao;
    @FXML private TextField entradaItemDescricao;
    @FXML private TextField entradaQtdRecebida;
    @FXML private ImageView entrada1;

    private int idItem;
    private String codItem;
    private String codOperacao;
    private String codOs;
    private String descricaoItem;
    private String qtdPedido;


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


    public void carregaDados(){
    entradaOrdemServico.setText(codOs);
    entradaCodOperacao.setText(codOperacao);
    entradadCodItem.setText(codItem);
    entradaItemDescricao.setText(descricaoItem);
    entrdadaQtdPedido.setText(qtdPedido);
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL entrada1ImageURL = getClass().getResource("/imagens/entrada1.png");
        Image entrada1Image = new Image(entrada1ImageURL.toExternalForm());
        entrada1.setImage(entrada1Image);
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
                        statement.setString(1, "recebido");
                        statement.setString(2, entradaLocalArmazenado.getText());
                        statement.setInt(3,Integer.parseInt(entradaQtdRecebida.getText()));
                        statement.setTimestamp(4, ts);
                        statement.setInt(5, idItem);

                        int linhasAfetadas = statement.executeUpdate();
                        if(linhasAfetadas > 0){
                            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                            alert2.setTitle("Aviso");
                            alert2.setHeaderText(null);
                            alert2.setContentText("Item atualizado com sucesso!");
                            alert2.showAndWait();
                        }

                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Stage stage = (Stage) entradaItemCancelar.getScene().getWindow();
                stage.close();
            }
        }



    }

