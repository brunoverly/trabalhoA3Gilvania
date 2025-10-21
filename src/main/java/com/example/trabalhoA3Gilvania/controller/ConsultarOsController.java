package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ConsultarOsController implements Initializable {
    @FXML private Button consultVoltarButton;
    @FXML private Button consultBuscarOs;

    @FXML private Label consultLabelOsBuscada;
    @FXML private ImageView consultarBackImage;
    @FXML private TextField consultNumeroOs;

    @FXML private TableView<Item> consultTableItem;
    @FXML private TableView<Operacao> consultTableOperacao;
    @FXML private TableColumn<Operacao, String> constulTabelCodOperacao;
    @FXML private TableColumn<Operacao, String> consultTableOperacaoStatus;
    @FXML private TableColumn<Item, String> consultTableCodItem;
    @FXML private TableColumn<Item, String> consultTableDescricaoItem;
    @FXML private TableColumn<Item, String> consultTablePedidoItem;
    @FXML private TableColumn<Item, String> consultTableRecebidoItem;
    @FXML private TableColumn<Item, String> consultTableItemStatus;
    @FXML private AnchorPane consultarOsAnchorPane;
    @FXML private SplitPane consultarOsSplitPane;
    @FXML private AnchorPane consultarOsTableViewOperacao;
    @FXML private AnchorPane consultarOsTableViewItens;


    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private FilteredList<Item> itensFiltrados;

    FormsUtil alerta = new FormsUtil();

    public void initialize(URL url, ResourceBundle resourceBundle) {

        URL consultarBackImageURL = getClass().getResource("/imagens/close.png");
        Image consultarBack = new Image(consultarBackImageURL.toExternalForm());
        consultarBackImage.setImage(consultarBack);


        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableOperacaoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));
        consultTableOperacao.setItems(todasOperacoes); // tabela de operações


        itensFiltrados = new FilteredList<>(todosItens, item -> false);
        consultTableItem.setItems(itensFiltrados); // tabela de itens
        consultTableOperacao.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                consultarOsTableViewItens.setVisible(true);
                String codOperacaoSelecionada = newSelection.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));

            } else {
                itensFiltrados.setPredicate(item -> false);
            }
        });
        ImageView fecharImagem = (ImageView) consultVoltarButton.getGraphic();

        // Hover (mouse entrou)
        consultVoltarButton.setOnMouseEntered(e -> {
            fecharImagem.setScaleX(1.2);
            fecharImagem.setScaleY(1.2);
            consultVoltarButton.setCursor(Cursor.HAND); // cursor muda para mão
        });

        // Hover (mouse saiu)
        consultVoltarButton.setOnMouseExited(e -> {
            fecharImagem.setScaleX(1.0);
            fecharImagem.setScaleY(1.0);
            consultVoltarButton.setCursor(Cursor.DEFAULT);
        });



    }


    @FXML
    public void consultBuscarOsOnAction(ActionEvent event) {
        if (verificarNumeroOS()) {
            BuscarDB();
        }
    }

    @FXML
    public void constulVoltarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();
    }

    public void BuscarDB() {
        consultarOsTableViewItens.setVisible(false);

        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        String numeroOs = consultNumeroOs.getText();

        try (Connection connectDB = new DataBaseConection().getConection();
             CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.consultar_os(?) }")) {

            cs.setString(1, numeroOs);
            boolean hasResults = cs.execute();

            // 1️⃣ Primeiro ResultSet: COUNT(*)
            if (hasResults) {
                try (ResultSet rsCount = cs.getResultSet()) {
                    if (rsCount.next()) {
                        int total = rsCount.getInt("total");
                        if (total == 0) {
                            alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "OS não encontrada").showAndWait();
                            consultarOsSplitPane.setVisible(false);
                            consultarOsTableViewOperacao.setVisible(false);
                            consultarOsTableViewItens.setVisible(false);
                            return; // Sai da função, nada para mostrar
                        }
                    }
                }
            }

            // 2️⃣ Segundo ResultSet: itens da OS
            if (cs.getMoreResults()) {
                try (ResultSet rsItens = cs.getResultSet()) {
                    while (rsItens.next()) {
                        Item item = new Item(
                                rsItens.getInt("id"),
                                rsItens.getString("cod_item"),
                                rsItens.getInt("id_operacao"),
                                rsItens.getString("cod_operacao"),
                                rsItens.getString("descricao"),
                                rsItens.getInt("qtd_pedido"),
                                rsItens.getInt("qtd_recebida"),
                                rsItens.getString("status")
                        );
                        listaItens.add(item);
                    }
                }
                todosItens.clear();
                todosItens.addAll(listaItens);
            }

            // 3️⃣ Terceiro ResultSet: operações agregadas
            if (cs.getMoreResults()) {
                try (ResultSet rsOperacoes = cs.getResultSet()) {
                    while (rsOperacoes.next()) {
                        Operacao operacao = new Operacao(
                                rsOperacoes.getInt("id"),
                                rsOperacoes.getString("cod_operacao"),
                                rsOperacoes.getString("status")
                        );
                        listaOperacao.add(operacao);
                    }
                }
                consultTableOperacao.setItems(listaOperacao);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao buscar OS").showAndWait();
        }
        consultarOsSplitPane.setVisible(true);
        consultarOsTableViewOperacao.setVisible(true);
    }



    public boolean verificarNumeroOS() {
        boolean retorno = true;
        if(consultNumeroOs == null || consultNumeroOs.getText().isBlank()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe o número da ordem de serviço")
                    .showAndWait();
            retorno = false;
        }
        return retorno;
    }


    public static class Item {
        private SimpleIntegerProperty idItem;
        private SimpleStringProperty codItem;
        private SimpleIntegerProperty idOperacao;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleIntegerProperty qtdPedido;
        private SimpleIntegerProperty qtdRecebida;
        private SimpleStringProperty status;


        public Item(String codItem, String operacaoString, String descricaoItem, int qtdItem) {
        }

        public Item(int idItem, String codItem, int idOperacao, String codOperacao, String descricao, int qtdPedido, int qtdRecebida, String status) {
            this.idItem = new SimpleIntegerProperty(idItem);
            this.codItem = new SimpleStringProperty(codItem);
            this.idOperacao = new SimpleIntegerProperty(idOperacao);
            this.codOperacao = new SimpleStringProperty(codOperacao);
            this.descricao = new SimpleStringProperty(descricao);
            this.qtdPedido = new SimpleIntegerProperty(qtdPedido);
            this.qtdRecebida = new SimpleIntegerProperty(qtdRecebida);
            this.status = new SimpleStringProperty(status);
        }

        // Getters
        public int getIdItem() {
            return idItem.get();
        }
        public String getCodItem() {
            return codItem.get();
        }
        public int getIdOperacao() {
            return idOperacao.get();
        }
        public String getCodOperacao() {
            return codOperacao.get();
        }
        public String getDescricao() {
            return descricao.get();
        }

        public int getQtdPedido() {
            return qtdPedido.get();
        }

        public int getQtdRecebida() {
            return qtdRecebida.get();
        }

        public String getStatus() {
            return status.get();
        }

        // Setters
        public void setIdItem(int idItem) {
            this.idItem.set(idItem);
        }
        public void setCodItem(String codItem) {
            this.codItem.set(codItem);
        }
        public void setIdOperacao(int idOperacao) {
            this.idOperacao.set(idOperacao);
        }
        public void setCodOperacao(String codOperacao) {
            this.codOperacao.set(codOperacao);
        }
        public void setDescricao(String descricao) {
            this.descricao.set(descricao);
        }

        public void setQtdPedido(int qtdPedido) {
            this.qtdPedido.set(qtdPedido);
        }

        public void setQtdRecebida(int qtdRecebida) {
            this.qtdRecebida.set(qtdRecebida);
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        // Métodos property() para TableView bindings
        public SimpleStringProperty codItemProperty() {
            return codItem;
        }

        public SimpleStringProperty descricaoProperty() {
            return descricao;
        }

        public SimpleIntegerProperty qtdPedidoProperty() {
            return qtdPedido;
        }

        public SimpleIntegerProperty qtdRecebidaProperty() {
            return qtdRecebida;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }
    }

        public static class Operacao {
            private SimpleIntegerProperty id;
            private SimpleStringProperty codOperacao;
            private SimpleStringProperty status;


            public Operacao(String operacaoString) {
            }

            public Operacao(int id,String codOperacao, String status) {
                this.id = new SimpleIntegerProperty(id);
                this.codOperacao = new SimpleStringProperty(codOperacao);
                this.status = new SimpleStringProperty(status);
            }

            // Getters
            public int getId() {
                return id.get();
            }
            public String getCodOperacao() {
                return codOperacao.get();
            }

            public String getStatus() {
                return status.get();
            }

            // Setters
            public void setId(int id) {
                this.id.set(id);
            }
            public void setCodOperacao(String codOperacao) {
                this.codOperacao.set(codOperacao);
            }

            public void setStatus(String status) {
                this.status.set(status);
            }

            // Métodos property() para bindings (TableView, etc.)
            public SimpleStringProperty codOperacaoProperty() {
                return codOperacao;
            }

            public SimpleStringProperty statusProperty() {
                return status;
            }
        }

    }
