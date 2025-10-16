package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class FecharOsController {
    @FXML private Button consultVoltarButton;
    @FXML private Button consultBuscarOs;
    @FXML private Button confirmCloseOsButton;

    @FXML private Label consultLabelOsBuscada;
    @FXML private ImageView fechar1;
    @FXML private ImageView fechar2;


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

    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();

    public void initialize() {

        URL fechar1ImageURL = getClass().getResource("/imagens/remover1.png");
        Image fechar1Image = new Image(fechar1ImageURL.toExternalForm());
        fechar2.setImage(fechar1Image);

        URL fechar2ImageURL = getClass().getResource("/imagens/fechar1.png");
        Image fechar2Image = new Image(fechar2ImageURL.toExternalForm());
        fechar2.setImage(fechar2Image);


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


    }


    @FXML
    public void consultBuscarOsOnAction(ActionEvent event) {
        if (verificarNumeroOS()) {
            BuscarDB();
            consultLabelOsBuscada.setVisible(true);
            consultNumeroOsBuscado.setText(consultNumeroOs.getText());
            consultNumeroOsBuscado.setVisible(true);
        }
    }

    @FXML
    public void constulVoltarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void confirmCloseOsButton(ActionEvent event) {
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySqlConsultaStatus = """
                SELECT status
                FROM ordem_servico
                WHERE cod_os = ?
            """;

            try (PreparedStatement buscar = connectDB.prepareStatement(querySqlConsultaStatus)) {
                buscar.setString(1, consultNumeroOs.getText());
                ResultSet rs = buscar.executeQuery();

                if (!rs.next()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmação");
                    alert.setHeaderText(null);
                    alert.setContentText("Tem certeza que deseja encerrar a OS?");

                    Optional<ButtonType> resultado = alert.showAndWait();

                    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                        try{
                            String querySqlItem = """
                                         UPDATE ordem_servico
                                                        SET status = ?, datahora_encerramento = ?
                                                        WHERE cod_os = ?
                                    """;

                            LocalDateTime agora = LocalDateTime.now();
                            Timestamp ts = Timestamp.valueOf(agora);

                            try (PreparedStatement atualizar = connectDB.prepareStatement(querySqlItem)) {
                                atualizar.setString(1, "encerrada");
                                atualizar.setTimestamp(2, ts);
                                atualizar.setString(3, consultNumeroOs.getText());

                                int linhasAfetadas = atualizar.executeUpdate();

                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                        alert2.setTitle("Aviso");
                        alert2.setHeaderText(null);
                        alert2.setContentText("Ordem de servico encerrada!");
                        alert2.showAndWait();
                    }
                }
                else{
                    Alert alert2 = new Alert(Alert.AlertType.WARNING);
                    alert2.setTitle("Aviso");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Ordem de ja esta encerrada!");
                    alert2.showAndWait();
                }
            }

            } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void BuscarDB() {
        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        String numeroOs = consultNumeroOs.getText();

        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySqlItem = """
                        SELECT item.cod_item,
                               item.id_operacao,
                               operacao.cod_operacao,
                               item.descricao,
                               item.qtd_pedido,
                               item.qtd_recebida,
                               item.status
                        FROM item
                        JOIN operacao ON operacao.id = item.id_operacao
                        WHERE operacao.cod_os = ?
                    """;

            try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                statement.setString(1, numeroOs);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    Item item = new Item(
                            rs.getString("cod_item"),
                            rs.getInt("id_operacao"),
                            rs.getString("cod_operacao"),
                            rs.getString("descricao"),
                            rs.getInt("qtd_pedido"),
                            rs.getInt("qtd_recebida"),
                            rs.getString("status")
                    );
                    listaItens.add(item);
                }
            }
            todosItens.clear();
            todosItens.addAll(listaItens);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySqlOperacao = """
                SELECT cod_operacao, MAX(id) AS id, MAX(status) AS status
                FROM operacao
                WHERE cod_os = ?
                GROUP BY cod_operacao
            """;

            try (PreparedStatement statement = connectDB.prepareStatement(querySqlOperacao)) {
                statement.setString(1, numeroOs);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    Operacao operacao = new Operacao(
                            rs.getInt("id"),
                            rs.getString("cod_operacao"),
                            rs.getString("status")
                    );
                    listaOperacao.add(operacao);
                }
            }

            consultTableOperacao.setItems(listaOperacao);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean verificarNumeroOS() {
        boolean retorno = true;
        if(consultNumeroOs == null || consultNumeroOs.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Informe o numero da ordem de servico!");
            alert.showAndWait();
            retorno = false;
        }
        else{
            try (Connection connectDB = new DataBaseConection().getConection()) {

                String verifcarCadastroBanco = "SELECT COUNT(*) FROM ordem_servico WHERE cod_os = ?";
                try (PreparedStatement statement1 = connectDB.prepareStatement(verifcarCadastroBanco)) {
                    statement1.setString(1, consultNumeroOs.getText());
                    ResultSet resultadoBuscaOs = statement1.executeQuery();

                    if (resultadoBuscaOs.next()) {
                        int count = resultadoBuscaOs.getInt(1);
                        if (count == 0) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Aviso");
                            alert.setHeaderText(null);
                            alert.setContentText("O número da ordem de serviço informada não foi localizada");
                            alert.showAndWait();
                            retorno =  false;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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

