package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.screen.ConsultEditItemScreen;
import com.example.trabalhoA3Gilvania.screen.EditItemScreen;
import com.example.trabalhoA3Gilvania.screen.SolicitarScreen;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

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
                System.out.println("idItem = " + idItem);
                try (Connection connectDB = new DataBaseConection().getConection()) {
                    String querySqlItem = """
                             UPDATE item
                                SET status = ?, localizacao = ?, qtd_recebida = ?, ultima_atualizacao = ?
                                WHERE id = ?
                        """;

                    LocalDateTime agora = LocalDateTime.now();
                    Timestamp ts = Timestamp.valueOf(agora);

                    try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                        statement.setString(1, "armazenado");
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

