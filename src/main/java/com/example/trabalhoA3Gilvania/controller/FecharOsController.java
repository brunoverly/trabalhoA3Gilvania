package com.example.trabalhoA3Gilvania.controller;
import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.FormsUtil;
import com.example.trabalhoA3Gilvania.OnFecharJanela;
import com.example.trabalhoA3Gilvania.Sessao;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class FecharOsController implements Initializable {
    @FXML private Button consultVoltarButton;
    @FXML private Button consultBuscarOs;
    @FXML private Button confirmCloseOsButton;

    @FXML private Label consultLabelOsBuscada;
    @FXML private ImageView fecharOsVoltar;


    @FXML private TextField consultNumeroOsBuscado;
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
    @FXML private AnchorPane fecharAnchorPane;

    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();

    private OnFecharJanela listener;

    FormsUtil alerta = new FormsUtil();

    public void setOnFecharJanela(OnFecharJanela listener) {
        this.listener = listener;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL fecharOsVoltarURL = getClass().getResource("/imagens/close.png");
        Image fecharOsVoltarImage = new Image(fecharOsVoltarURL.toExternalForm());
        fecharOsVoltar.setImage(fecharOsVoltarImage);

       // URL fechar2ImageURL = getClass().getResource("/imagens/close.png");
        //Image fechar2Image = new Image(fechar2ImageURL.toExternalForm());
        //fechar2.setImage(fechar2Image);


        constulTabelCodOperacao.setCellValueFactory(new PropertyValueFactory<>("codOperacao"));
        consultTableOperacaoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        consultTableCodItem.setCellValueFactory(new PropertyValueFactory<>("codItem"));
        consultTableDescricaoItem.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        consultTablePedidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdPedido"));
        consultTableRecebidoItem.setCellValueFactory(new PropertyValueFactory<>("qtdRecebida"));
        consultTableItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));


        consultTableOperacao.setItems(todasOperacoes);
        consultTableItem.setItems(todosItens);

        consultTableOperacao.setSelectionModel(null);
        consultTableItem.setSelectionModel(null);
        consultTableOperacao.setPlaceholder(new Label(""));
        consultTableItem.setPlaceholder(new Label(""));

        Platform.runLater(() -> {
            Stage stage = (Stage) fecharAnchorPane.getScene().getWindow();

            // Quando a janela for fechada (X ou voltar)
            stage.setOnHidden(event -> {
                if (listener != null) {
                    listener.aoFecharJanela(); // 🔔 chama o método da interface
                }
            });
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


    @FXML
    public void confirmCloseOsButton(ActionEvent event) {
        // Verifica se o campo está vazio
        if (consultNumeroOs == null || consultNumeroOs.getText().isBlank()) {
            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Informe o número da ordem de serviço")
                    .showAndWait();
            return;
        }

        String numeroOs = consultNumeroOs.getText();

        try (Connection connectDB = new DataBaseConection().getConection()) {

            // Chama procedure encerrar_os
            int resultadoEncerramento = 0;
            try (CallableStatement csEncerrar = connectDB.prepareCall("{ CALL projeto_java_a3.encerrar_os(?, ?, ?, ?) }")) {
                csEncerrar.setString(1, numeroOs);
                csEncerrar.setString(2, "Ordem de serviço"); // tipo
                csEncerrar.setString(3, "Ordem de serviço encerrada"); // descrição
                csEncerrar.setInt(4, Sessao.getMatricula());

                boolean hasRS = csEncerrar.execute();
                if (hasRS) {
                    try (ResultSet rs = csEncerrar.getResultSet()) {
                        if (rs.next()) {
                            resultadoEncerramento = rs.getInt("resultado");
                        }
                    }
                }
            }

            // Mostra alerta baseado no resultado
            switch (resultadoEncerramento) {
                case 0 -> alerta.criarAlerta(Alert.AlertType.WARNING, "Aviso", "OS não encontrada").showAndWait();
                case 1 -> alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "OS já se encontra encerrada").showAndWait();
                case 2 -> alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem de serviço encerrada com sucesso").showAndWait();
                default -> alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Erro desconhecido ao encerrar OS").showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao conectar com banco de dados").showAndWait();
        }

        consultNumeroOs.setText("");
        consultLabelOsBuscada.setVisible(false);
        fecharAnchorPane.setVisible(false);
    }




    public void BuscarDB() {
        String numeroOs = consultNumeroOs.getText();

        try (Connection connectDB = new DataBaseConection().getConection()) {
            CallableStatement cs = connectDB.prepareCall("{ CALL projeto_java_a3.encerrar_os_dados(?) }");
            cs.setString(1, numeroOs);

            boolean hasResults = cs.execute();

            // Primeiro ResultSet: verifica se a OS existe
            if (hasResults) {
                try (ResultSet rsResultado = cs.getResultSet()) {
                    if (rsResultado.next()) {
                        int resultado = rsResultado.getInt("resultado");
                        if (resultado == 0) {
                            alerta.criarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Ordem de serviço não encontrada")
                                    .showAndWait();
                            return; // para execução do método
                        }
                    }
                }
            }

            // Agora lemos os outros ResultSets: itens e operações
            ObservableList<Item> listaItens = FXCollections.observableArrayList();
            ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

            // Avança para o próximo ResultSet (itens)
            if (cs.getMoreResults()) {
                try (ResultSet rsItens = cs.getResultSet()) {
                    while (rsItens.next()) {
                        Item item = new Item(
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
                    todosItens.clear();
                    todosItens.addAll(listaItens);
                }
            }

            // Avança para o próximo ResultSet (operações)
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
                    todasOperacoes.clear();
                    todasOperacoes.addAll(listaOperacao);
                }
            }

            // Atualiza as TableViews
            consultTableOperacao.setItems(todasOperacoes);
            consultTableItem.setItems(todosItens);

        } catch (SQLException ex) {
            ex.printStackTrace();
            alerta.criarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao conectar com banco de dados").showAndWait();
        }
        consultLabelOsBuscada.setVisible(true);
        fecharAnchorPane.setVisible(true);
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
        private SimpleStringProperty codItem;
        private SimpleIntegerProperty idOperacao;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleIntegerProperty qtdPedido;
        private SimpleIntegerProperty qtdRecebida;
        private SimpleStringProperty status;


        public Item() {
        }

        public Item(String codItem, int idOperacao, String codOperacao, String descricao, int qtdPedido, int qtdRecebida, String status) {
            this.codItem = new SimpleStringProperty(codItem);
            this.idOperacao = new SimpleIntegerProperty(idOperacao);
            this.codOperacao = new SimpleStringProperty(codOperacao);
            this.descricao = new SimpleStringProperty(descricao);
            this.qtdPedido = new SimpleIntegerProperty(qtdPedido);
            this.qtdRecebida = new SimpleIntegerProperty(qtdRecebida);
            this.status = new SimpleStringProperty(status);
        }

        // Getters
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


        public Operacao() {
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

