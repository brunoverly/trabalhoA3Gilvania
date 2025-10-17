package com.example.trabalhoA3Gilvania.controller;

import com.example.trabalhoA3Gilvania.DataBaseConection;
import com.example.trabalhoA3Gilvania.Sessao;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConsultarItemController implements Initializable{
    @FXML private Button consultVoltarButton;
    @FXML private Button consultBuscarOs;

    @FXML private Label consultItemLabel;
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
    @FXML private ImageView consultarItem1;
    @FXML private AnchorPane consultarItemAnchorPane;


    private Stage janelaEntradaItem;
    private Stage janelaSaidaItem;

    private ObservableList<Operacao> todasOperacoes = FXCollections.observableArrayList();
    private ObservableList<Item> todosItens = FXCollections.observableArrayList();
    private FilteredList<Item> itensFiltrados;

    private String modo;
    private String codOperacao;
    private String codOs;
    private String codItem;
    private String descricaoItem;
    private String qtdPedido;
    private int idItem;
    private String localizacao;
    private String status;
    private int qtdRecebida;

    public void setModo(String modo) {
        this.modo = modo;
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
    public void setIdItem(int idItem) {this.idItem = idItem;}
    public void setLocalizacao(String localizacao){this.localizacao = localizacao;}
    public void setStatus(String status){this.status = status;}
    public void setQtdRecebida(int qtdRecebida){this.qtdRecebida = qtdRecebida;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        URL consultarItem1URL = getClass().getResource("/imagens/remover1.png");
        Image consultar1Image = new Image(consultarItem1URL.toExternalForm());
        consultarItem1.setImage(consultar1Image);

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
                String codOperacaoSelecionada = newSelection.getCodOperacao().trim();
                itensFiltrados.setPredicate(item -> item.getCodOperacao() != null &&
                        item.getCodOperacao().trim().equalsIgnoreCase(codOperacaoSelecionada));
            } else {
                itensFiltrados.setPredicate(item -> false);
            }
        });

        // Configuração do TableRow com ContextMenu
        consultTableItem.setRowFactory(table -> {
            TableRow<Item> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // atualiza o menu sempre que o item da linha mudar
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                configurarContextMenu(row, contextMenu);
            });

            // só mostrar o menu em linhas não vazias
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            // Clique duplo para entrada/saída
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    Item selecionado = row.getItem();
                    String codItemSelecionado = selecionado.getCodItem();
                    String codOperacaoItemSelecionado = selecionado.getCodOperacao();
                    String codOsItemSelecionado = consultNumeroOs.getText();
                    String descricaoItemSelecionado = selecionado.getDescricao();
                    int qtdPedidoItemSelecionado = selecionado.getQtdPedido();
                    int idItemselecionado = selecionado.getIdItem();

                    try {
                        if (modo == null) return;
                        if (modo.equals("Solicitar") && selecionado.getStatus().equals("Aguardando entrega")) {
                            Alert alert2 = new Alert(Alert.AlertType.WARNING);
                            alert2.setTitle("Aviso");
                            alert2.setHeaderText(null);
                            alert2.setContentText("O item selecionado ainda consta como 'Aguardando entrega', a solicitação só pode ser realizada quando o item estiver com status 'Recebido'");
                            alert2.showAndWait();
                            return;
                        }

                        if(!Sessao.getCargo().equals("Aprovisionador") && modo.equals("Solicitar") && selecionado.getStatus().equals("Recebido")) {
                                if (selecionado != null) {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Confirmação");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Deseja solicitar a entrega do item: '"+ selecionado.getDescricao() +"' na oficina?");
                                    Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                                    stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));

                                    Optional<ButtonType> resultado = alert.showAndWait();
                                    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                                        Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                                        alert2.setTitle("Aviso");
                                        alert2.setHeaderText(null);
                                        stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
                                        alert2.setContentText("Requisitado a entrega do item: '" + selecionado.getDescricao() + "'");
                                        alert2.showAndWait();



                                        // Atualiza status do item
                                        try (Connection connectDB = new DataBaseConection().getConection()) {
                                            String querySqlItem = "UPDATE item SET status = 'Solicitado na oficina' WHERE id = ?";
                                            String querySqlOperacao = "UPDATE operacao SET status = 'Item(s) solicitados' WHERE id = ?";
                                            try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                                                statement.setInt(1, selecionado.getIdItem());
                                                statement.executeUpdate();
                                            }
                                            try (PreparedStatement statement = connectDB.prepareStatement(querySqlOperacao)) {
                                                statement.setInt(1, selecionado.getIdOperacao());
                                                statement.executeUpdate();
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                        DataBaseConection registarAtualizacao = new DataBaseConection();
                                        registarAtualizacao.AtualizarBanco(
                                                "Operação",
                                                codOsItemSelecionado,
                                                "Item solicitado na oficina",
                                                Sessao.getMatricula()
                                        );

                                        // Insere registro na tabela de controle de solicitações
                                        try (Connection connectDB = new DataBaseConection().getConection()) {
                                            String querySqlSolicitacao = "INSERT INTO controle_solicitacao_item (solicitador_por, id_item) VALUES (?, ?)";
                                            try (PreparedStatement statement = connectDB.prepareStatement(querySqlSolicitacao)) {
                                                statement.setInt(1, Sessao.getMatricula());
                                                statement.setInt(2, selecionado.getIdItem());
                                                statement.executeUpdate();
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }

                        }
                        /// ///////////////////////////////////////////////////


                        if ((Sessao.getCargo().equals("Administrador") || Sessao.getCargo().equals("Aprovisionador")) &&
                                (selecionado.getStatus().equals("Aguardando entrega") || selecionado.getStatus().equals("Recebido") || selecionado.getStatus().equals("Solicitado na oficina"))) {

                            switch (modo) {
                                case "Entrada":
                                    if (selecionado.getStatus().equals("Aguardando entrega")) {
                                        LancarEntradaItem(codItemSelecionado, codOperacaoItemSelecionado,
                                                codOsItemSelecionado, descricaoItemSelecionado,
                                                qtdPedidoItemSelecionado, idItemselecionado);
                                    }
                                    break;

                                case "Retirar":
                                    if (selecionado.getStatus().equals("Solicitado na oficina") || selecionado.getStatus().equals("Recebido")) {
                                        try (Connection connectDB = new DataBaseConection().getConection()) {
                                            String querySqlItem = """
                        SELECT localizacao, status, qtd_recebida
                        FROM item
                        WHERE id = ?
                    """;
                                            try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                                                statement.setInt(1, idItemselecionado);
                                                ResultSet rs = statement.executeQuery();
                                                if (rs.next()) {
                                                    localizacao = rs.getString("localizacao");
                                                    status = rs.getString("status");
                                                    qtdRecebida = rs.getInt("qtd_recebida");
                                                }
                                            }
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }

                                        LancaSaidaItem(codItemSelecionado, codOperacaoItemSelecionado, codOsItemSelecionado,
                                                descricaoItemSelecionado, qtdPedidoItemSelecionado, idItemselecionado,
                                                localizacao, status, qtdRecebida);
                                    }
                                    break;
                            }
                        }






                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return row;
        });
    }




    @FXML
    public void consultBuscarOsOnAction(ActionEvent event) {
        if (verificarNumeroOS()) {
            BuscarDB(consultNumeroOs.getText());
            consultarItemAnchorPane.setVisible(true);
        }
    }

    @FXML
    public void constulVoltarButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) consultVoltarButton.getScene().getWindow();
        stage.close();
    }

    public void BuscarDB(String numeroOs) {

        ObservableList<Item> listaItens = FXCollections.observableArrayList();
        ObservableList<Operacao> listaOperacao = FXCollections.observableArrayList();

        try (Connection connectDB = new DataBaseConection().getConection()) {
            String querySqlItem = """
                        SELECT item.id,
                               item.cod_item,
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
                            rs.getInt("id"),
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
            alert.setContentText("Informe o número da ordem de servico");
            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
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
                            alert.setContentText("O número da ordem de serviço informada não foi localizado");
                            Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                            stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
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
        private SimpleIntegerProperty idItem;
        private SimpleStringProperty codItem;
        private SimpleIntegerProperty idOperacao;
        private SimpleStringProperty codOperacao;
        private SimpleStringProperty descricao;
        private SimpleIntegerProperty qtdPedido;
        private SimpleIntegerProperty qtdRecebida;
        private SimpleStringProperty status;


        public Item() {
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


    public void LancarEntradaItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem) throws Exception {
        // Se já existir, fecha a janela antes de abrir nova
        if (janelaEntradaItem != null) {
            janelaEntradaItem.close();
            janelaEntradaItem = null;
        }

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/entradaItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaEntradaItem = new Stage();
            janelaEntradaItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            // Obtém o controller e passa o parâmetro
            EntradaItemController controller = fxmlLoader.getController();
            controller.setCodItem(codItem);
            controller.setCodOperacao(codOperacao);
            controller.setCodOs(codOs);
            controller.setDescricaoItem(descricaoItem);
            controller.setQtdPedido(qtdPedido);
            controller.setIdItem(idItem);
            controller.carregaDados();

            // Configurar stage
            janelaEntradaItem.setTitle("Entrada de item");
            janelaEntradaItem.setResizable(false);
            janelaEntradaItem.setScene(scene);
            janelaEntradaItem.show();

            TextField tf = (TextField) root.lookup("#entradaQtdRecebida");
            tf.requestFocus();

            janelaEntradaItem.setOnHidden(event -> janelaEntradaItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LancaSaidaItem(String codItem, String codOperacao, String codOs, String descricaoItem, int qtdPedido, int idItem, String localizacao, String status, int qtdRecebida) throws Exception {
        // Se já existir, fecha a janela antes de abrir nova
        if (janelaSaidaItem != null) {
            janelaSaidaItem.close();
            janelaSaidaItem = null;
        }

        try {
            // Carregar FXML
            URL fxmlUrl = getClass().getResource("/com/example/trabalhoA3Gilvania/saidaItem.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            String[] fonts = {"Poppins-Regular.ttf", "Poppins-Bold.ttf"};
            for (String fontFile : fonts) {
                Font.loadFont(getClass().getResource("/fonts/" + fontFile).toExternalForm(), 14);
            }

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/css/style.css");
            scene.getStylesheets().add(cssUrl.toExternalForm());

            URL logoUrl = getClass().getResource("/imagens/logo.png");
            janelaSaidaItem = new Stage();
            janelaSaidaItem.getIcons().add(new Image(logoUrl.toExternalForm()));

            SaidaItemController controller = fxmlLoader.getController();
            controller.setCodItem(codItem);
            controller.setCodOperacao(codOperacao);
            controller.setCodOs(codOs);
            controller.setDescricaoItem(descricaoItem);
            controller.setQtdPedido(qtdPedido);
            controller.setIdItem(idItem);
            controller.setLocalizacao(localizacao);
            controller.setStatus(status);
            controller.setQtdRecebida(qtdRecebida);
            controller.carregaDados();

            // Configurar stage
            janelaSaidaItem.setTitle("Lançar entrega de item");
            janelaSaidaItem.setResizable(false);
            janelaSaidaItem.setScene(scene);
            janelaSaidaItem.show();

            TextField tf = (TextField) root.lookup("#retirarMatriculaMecanico");
            tf.requestFocus();

            janelaSaidaItem.setOnHidden(event -> janelaSaidaItem = null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AtualizarTituloPorModo() {
        if (Sessao.getCargo().equals("Mecânico")) {
            consultItemLabel.setText("Solicitar item");
        } else if (modo != null) {

            switch (modo) {
                case "Entrada":
                    consultItemLabel.setText("Lançar entrada de item");
                    break;
                case "Retirar":
                    consultItemLabel.setText("Lançar retirada de item");
                    break;
                default:
                    consultItemLabel.setText("Solicitar item");
            }
        }
    }
    public void configurarContextMenu(TableRow<Item> row, ContextMenu contextMenu) {
        // Limpa os itens anteriores
        contextMenu.getItems().clear();

        if (modo == null) return;

        Item selecionado = row.getItem();
        if (selecionado == null) return;

        // 🔹 Caso o usuário nao seja aprovisionador e o modo seja "solicitar"
        if (modo.equals("Solicitar") && selecionado.getStatus().equals("Recebido") && !Sessao.getCargo().equals("Aprovisionador")) {
            MenuItem solicitarItem = new MenuItem("Requisitar item");
            contextMenu.getItems().add(solicitarItem);

            solicitarItem.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmação");
                alert.setHeaderText(null);
                alert.setContentText("Deseja solicitar a entrega do item: '" + selecionado.getDescricao() + "' na oficina?");
                Stage stageAlert = (Stage) alert.getDialogPane().getScene().getWindow();
                stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));

                Optional<ButtonType> resultado = alert.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

                    Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                    alert2.setTitle("Aviso");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Requisitado a entrega do item: '" + selecionado.getDescricao() + "'.");
                    Stage stageAlert2 = (Stage) alert.getDialogPane().getScene().getWindow();
                    stageAlert.getIcons().add(new Image(getClass().getResource("/imagens/logo.png").toExternalForm()));
                    alert2.showAndWait();

                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        String querySqlItem = "UPDATE item SET status = 'Solicitado na oficina' WHERE id = ?";
                        String querySqlOperacao = "UPDATE operacao SET status = 'Item(s) solicitados' WHERE id = ?";
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                            statement.setInt(1, selecionado.getIdItem());
                            statement.executeUpdate();
                        }
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlOperacao)) {
                            statement.setInt(1, selecionado.getIdOperacao());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Registrar atualização no banco
                    DataBaseConection registarAtualizacao = new DataBaseConection();
                    registarAtualizacao.AtualizarBanco(
                            "Operação",
                            selecionado.getCodOperacao(),
                            "Item solicitado na oficina",
                            Sessao.getMatricula()
                    );

                    // Inserir registro de solicitação
                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        String querySqlSolicitacao = "INSERT INTO controle_solicitacao_item (solicitador_por, id_item) VALUES (?, ?)";
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlSolicitacao)) {
                            statement.setInt(1, Sessao.getMatricula());
                            statement.setInt(2, selecionado.getIdItem());
                            statement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // 🔹 Caso o usuário seja ADMINISTRADOR ou APROVISIONADOR
        else if (!Sessao.getCargo().equals("Mecânico")) {
            if (modo.equals("Entrada") && selecionado.getStatus().equals("Aguardando entrega")) {
                MenuItem lancarEntrada = new MenuItem("Lançar entrada");
                contextMenu.getItems().add(lancarEntrada);

                lancarEntrada.setOnAction(event -> {
                    try {
                        LancarEntradaItem(
                                selecionado.getCodItem(),
                                selecionado.getCodOperacao(),
                                consultNumeroOs.getText(),
                                selecionado.getDescricao(),
                                selecionado.getQtdPedido(),
                                selecionado.getIdItem()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // 👉 Se o modo for "retirar"
            else if (modo.equals("Retirar") && selecionado.getStatus().equals("Recebido")) {
                MenuItem lancarSaida = new MenuItem("Lançar retirada");
                contextMenu.getItems().add(lancarSaida);

                lancarSaida.setOnAction(event -> {
                    try (Connection connectDB = new DataBaseConection().getConection()) {
                        String querySqlItem = "SELECT localizacao, status, qtd_recebida FROM item WHERE id = ?";
                        try (PreparedStatement statement = connectDB.prepareStatement(querySqlItem)) {
                            statement.setInt(1, selecionado.getIdItem());
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                localizacao = rs.getString("localizacao");
                                status = rs.getString("status");
                                qtdRecebida = rs.getInt("qtd_recebida");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        LancaSaidaItem(
                                selecionado.getCodItem(),
                                selecionado.getCodOperacao(),
                                consultNumeroOs.getText(),
                                selecionado.getDescricao(),
                                selecionado.getQtdPedido(),
                                selecionado.getIdItem(),
                                localizacao,
                                status,
                                qtdRecebida
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

}


